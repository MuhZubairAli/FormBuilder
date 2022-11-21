package pk.gov.pbs.formbuilder.core;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import pk.gov.pbs.database.DatabaseUtils;
import pk.gov.pbs.database.ModelBasedRepository;
import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.database.FormBuilderRepository;
import pk.gov.pbs.formbuilder.database.dao.HouseholdMemberDao;
import pk.gov.pbs.formbuilder.inputs.singular.SpecifiableSelectable;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.models.SectionContext;
import pk.gov.pbs.formbuilder.models.IterativeMemberSection;
import pk.gov.pbs.formbuilder.models.MemberSection;
import pk.gov.pbs.formbuilder.models.RosterSection;
import pk.gov.pbs.formbuilder.models.LoginPayload;
import pk.gov.pbs.formbuilder.models.HouseholdSection;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.pojos.Annex;
import pk.gov.pbs.formbuilder.pojos.Assignment;
import pk.gov.pbs.formbuilder.pojos.DatumIdentifier;
import pk.gov.pbs.formbuilder.utils.ValueStore;

public abstract class ViewModelFormSection extends AndroidViewModel {
    private Future<List<Section>> mSectionEntriesFetcher;
    private Future<List<RosterSection>> mAllMembersFetcher;
    protected SectionContext mSectionContext;
    protected LoginPayload mLoginPayload;
    protected IMetaManifest mMetaManifest;
    protected LabelProvider mLabelProvider;

    protected FormBuilderRepository mFormBuilderRepository;
    protected Section mResumeModel;

    private List<RosterSection> mHouseholdMembers;
    private List<RosterSection> mHouseholdMembersFiltered;
    private List<Section> mSectionEntries;

    public ViewModelFormSection(@NonNull Application application) {
        super(application);
        mHouseholdMembersFiltered = new ArrayList<>();

        mFormBuilderRepository = new FormBuilderRepository(application);
        mLoginPayload = mFormBuilderRepository.getLoginDao().getLoginPayload();
    }

    public void init(IMetaManifest manifest, SectionContext sectionContext, LabelProvider labelProvider, Section resumeModel, boolean cacheAllMembers, boolean cacheSectionEntries) {
        mMetaManifest = manifest;
        mLabelProvider = labelProvider;
        mSectionContext = sectionContext;
        mResumeModel = resumeModel;

        if (cacheAllMembers) {
            mAllMembersFetcher = getRosterDao().getAll(sectionContext);
        }

        if (cacheSectionEntries) {
            mSectionEntriesFetcher = getFormRepository().getExecutorService().submit(() -> {
                Class<?> model = mMetaManifest.getModel(sectionContext.getSection());
                return (List<Section>) getFormRepository().getDatabase().selectRowsBySQL(
                        model,
                        "SELECT * FROM `" + model.getSimpleName() + "` WHERE `pcode`=? AND `hhno`=?",
                        new String[]{sectionContext.getBlockIdentifier(), String.valueOf(sectionContext.getHHNo())}
                );
            });
        }
    }

    /**
     * Determine the max MemberID from household members
     * because it is assumed that not all MemberIDs will be
     * in increasing order and also there could be gaps in the series
     * due to deleted entries, so use this method instead of taking
     * last last member's SNo
     *
     * @return Max Member ID
     */
    protected int getMaxSNo(){
        int sno = 0;
        for (RosterSection m : getHouseholdMembers())
            sno = Math.max(m.sno, sno);
        return sno;
    }

    protected int getMaxINo() {
        int ino = 0;
        for (Section m : getSectionEntries()) {
            IterativeMemberSection s = (IterativeMemberSection) m;
            if (s.sno.intValue() == getCurrentMemberID().intValue())
                ino = Math.max(s.ino, ino);
        }
        return ino;
    }

    /**
     * Abstract Methods
     */
    public abstract <T extends ModelBasedRepository> T getFormRepository();
    public abstract HouseholdMemberDao getRosterDao();
    public abstract Assignment getAssignment();

    public long insertSection(IterativeMemberSection ims) {
        ims.ino = getMaxINo() + 1;
        Long insertID = DatabaseUtils.getFutureValue(getFormRepository().insert(ims));
        ims.aid = insertID == null ? Constants.INVALID_NUMBER : insertID;
        if (ims.aid != Constants.INVALID_NUMBER) {
            getSectionEntries().add(ims);
        }

        return ims.aid;
    }

    public long insertSection(RosterSection member) {
        member.sno = getMaxSNo() + 1;
        Long insertID = DatabaseUtils.getFutureValue(getFormRepository().insert(member));
        member.aid = insertID == null ? Constants.INVALID_NUMBER : insertID;
        if (member.aid != Constants.INVALID_NUMBER) {
            getHouseholdMembers().add(member);
            getSectionEntries().add(member);
        }
        return member.aid;
    }

    public long insertSection(HouseholdSection section) {
        Long insertId = DatabaseUtils.getFutureValue(
                getFormRepository().insert(section)
        );

        if (insertId != null) {
            section.aid = insertId;
            getSectionEntries().add(section);
            return insertId;
        }

        return Constants.INVALID_NUMBER;
    }

    public long insertSection(Section section) {
        Long insertId = DatabaseUtils.getFutureValue(
                getFormRepository().insert(section)
        );

        return insertId != null ? insertId : Constants.INVALID_NUMBER;
    }

    public int updateSection(Section section) {
        Integer affectedRows = DatabaseUtils.getFutureValue(
                getFormRepository().update(section)
        );

        if (affectedRows != null &&  affectedRows > 0) {
            for (int i=0; i<getSectionEntries().size(); i++){
                if (getSectionEntries().get(i).aid.longValue() == section.aid.longValue()){
                    getSectionEntries().remove(i);
                    getSectionEntries().add(i, section);
                    break;
                }
            }
            return affectedRows;
        }

        return Constants.INVALID_NUMBER;
    }

    /**
     * Setters
     */
    public void setCurrentMemberID(Integer sno){
        mSectionContext.setSNo(sno);
    }
    public void setResumeModel(Section resumeModel){
        mResumeModel = resumeModel;
    }
    public void setHouseholdMembersFiltered(List<RosterSection> membersFiltered) {
        mHouseholdMembersFiltered = membersFiltered;
    }
    public long persistSectionContext(){
        return getFormBuilderRepository().getUtilsDao().setSectionContext(mSectionContext);
    }

    /**
     * Getters
     */
    public List<RosterSection> getHouseholdMembers() {
        if (mHouseholdMembers == null) {
            synchronized (this) {
                if (mAllMembersFetcher == null)
                    mAllMembersFetcher = getRosterDao().getAll(mSectionContext);

                mHouseholdMembers = DatabaseUtils.getFutureValue(mAllMembersFetcher);
                mAllMembersFetcher = null;

                return mHouseholdMembers;
            }
        }

        return mHouseholdMembers;
    }

    public List<RosterSection> getHouseholdMembersFiltered() {
        return mHouseholdMembersFiltered;
    }

    public List<Section> getSectionEntries() {
        if (mSectionEntries == null) {
            synchronized (this) {
                if (mSectionEntriesFetcher != null) {
                    mSectionEntries = DatabaseUtils.getFutureValue(mSectionEntriesFetcher);
                    mSectionEntriesFetcher = null;
                }

                return mSectionEntries;
            }
        }
        return mSectionEntries;
    }

    public RosterSection getMemberBySNo(int sno) {
        for (RosterSection member : getHouseholdMembers()) {
            if (member.getMemberId() == sno)
                return member;
        }
        return null;
    }

    public LoginPayload getLoginPayload(){
        return mLoginPayload;
    }
    public Section getResumeModel(){
        return mResumeModel;
    }
    public Integer getCurrentMemberID(){
        return mSectionContext.getMemberID();
    }
    public Integer getCurrentIteration(){
        return mSectionContext.getIterationNumber();
    }
    public IMetaManifest getMetaManifest() {
        return mMetaManifest;
    }
    /**
     * NullSafeEquals Current Member ID
     * @param toCompareWith sno to be compared with current member's id
     * @return true if sno matches
     */
    public boolean nseCurrentMemberId(Integer toCompareWith){
        if (getCurrentMemberID() == null && toCompareWith == null)
            return true;
        else if (getCurrentMemberID() == null || toCompareWith == null)
            return false;
        else
            return getCurrentMemberID().intValue() == toCompareWith.intValue();
    }
    public boolean nseCurrentIteration(Integer toCompareWith){
        if (getCurrentIteration() == null && toCompareWith == null)
            return true;
        else if (getCurrentIteration() == null || toCompareWith == null)
            return false;
        else
            return getCurrentIteration().intValue() == toCompareWith.intValue();
    }
    public RosterSection getCurrentMember(){
        if (mHouseholdMembersFiltered != null && mHouseholdMembersFiltered.size() > 0){
            for (RosterSection m : mHouseholdMembersFiltered){
                if (m.getMemberId().intValue() == getCurrentMemberID().intValue())
                    return m;
            }
        }
        return null;
    }

    public SectionContext getSectionContext(){
        return mSectionContext;
    }
    public FormBuilderRepository getFormBuilderRepository() {
        return mFormBuilderRepository;
    }

    /**
     * Special Getters
     */

    // it will return Member ID of member from Filtered member list
    // for whom current section entry has not yet completed (either pending or incomplete)
    // it exclude current member from FormContext
    public int getNextEligibleSNo(){
        if (getSectionEntries().size() == 0) {
            int nextSNo = Constants.INVALID_NUMBER;
            for (RosterSection rs : mHouseholdMembersFiltered) {
                if (rs.getMemberId().intValue() != getCurrentMemberID().intValue()) {
                    nextSNo = rs.sno;
                    break;
                }
            }
            return nextSNo;
        }
        if (getSectionEntries().get(0) instanceof MemberSection) {
            for (int i = 0; i < mHouseholdMembersFiltered.size(); i++) {
                if (
                        !nseCurrentMemberId(mHouseholdMembersFiltered.get(i).sno)
                        && !hasSectionCompletedForMember(mHouseholdMembersFiltered.get(i).sno)
                )
                    return mHouseholdMembersFiltered.get(i).sno;
            }
        }
        return Constants.INVALID_NUMBER;
    }

    private boolean hasSectionCompletedForMember(int sno){
        for (Section s : getSectionEntries()) {
            if (s.section_status == Constants.Status.SECTION_CLOSED && ((MemberSection) s).sno == sno)
                return true;
        }
        return false;
    }

    private boolean modelHasField(Class<?> model, String field){
        for (Field f : model.getFields()){
            if (f.getName().equalsIgnoreCase(field)) return true;
        }
        return false;
    }

    public Section getSectionEntryByFormContext(SectionContext sContext) {
        return getSectionEntryByFormContext(sContext, null);
    }

    public Section getSectionEntryByFormContext(SectionContext sContext, String additionalCriteria, String... additionalArgs){
        Class<?> model = mMetaManifest.getModel(sContext.getSection());
        Future<?> future = getFormRepository().getExecutorService().submit(() -> {
            List<String> args = new ArrayList<>();
            StringBuilder sb = new StringBuilder();

            sb.append("`bId`=?");
            args.add(sContext.getBlockIdentifier());

            if (sContext.getHHNo() != null && modelHasField(model, "hhno")) {
                sb.append(" AND `hhno`=?");
                args.add(String.valueOf(sContext.getHHNo()));
            }

            if (sContext.getMemberID() != null && modelHasField(model, "sno")){
                sb.append(" AND `sno`=?");
                args.add(String.valueOf(sContext.getMemberID()));
            }

            if (sContext.getIterationNumber() != null && modelHasField(model, "ino")){
                sb.append(" AND `ino`=?");
                args.add(String.valueOf(sContext.getIterationNumber()));
            }

            if (additionalCriteria != null && !additionalCriteria.isEmpty()) {
                sb.append(" AND ").append(additionalCriteria);
                args.addAll(Arrays.asList(additionalArgs));
            }

            //Field pk = DatabaseUtils.getPrimaryKeyField(model);
            //if (pk != null)
            //    sb.append(" ORDER BY `"+pk.getName()+"` DESC");

            if (modelHasField(model, "aid"))
                sb.append(" ORDER BY `aid` DESC LIMIT 1");

            String[] selectionArg = new String[args.size()];
            args.toArray(selectionArg);

            return DatabaseUtils.getFutureValue(
                getFormRepository().selectRowAs(
                    model, "SELECT * FROM " + model.getSimpleName() + " WHERE " + sb.toString(),
                    selectionArg
                )
            );
        });

        return (Section) DatabaseUtils.getFutureValue(future);
    }

    public String getLabelForSpecifiableSelectable(SpecifiableSelectable ab){
        String ab_label = ab.getValue().toString() + ". " + mLabelProvider.getLabel(ab.getIndex());
        String option = mFormBuilderRepository.getOptionsDao().getLabelFor(ab.getAnswer(1).toLong());
        return getApplication().getString(
                R.string.specifiable_selectable_label_template
                , ab_label
                , option
        );
    }

    public String getLabelForAnnexInput(ValueStore code, DatumIdentifier identifier){
        return mFormBuilderRepository.getAnnexDao().getLabelFor(identifier, code);
    }

    public List<Annex> getAnnexures(DatumIdentifier identifier){
        return mFormBuilderRepository.getAnnexDao().getAnnexuresByIdentifier(identifier);
    }
}
