package pk.gov.pbs.formbuilder.core;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.exceptions.InvalidQuestionStateException;
import pk.gov.pbs.formbuilder.inputs.singular.ButtonInput;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.models.MemberSection;
import pk.gov.pbs.formbuilder.models.RosterSection;
import pk.gov.pbs.formbuilder.models.HouseholdSection;
import pk.gov.pbs.formbuilder.pojos.ItemSpinnerMember;
import pk.gov.pbs.utils.ExceptionReporter;
import pk.gov.pbs.utils.UXEventListeners;

public abstract class ActivityMemberSection extends ActivityHouseholdSection {
    protected int SP_MEMBERS_LAST_VALUE = -1;
    protected Spinner mSpinnerUsers;
    protected ArrayAdapter<ItemSpinnerMember> mAdapterSpinnerUsers;

    @Override
    protected void determineCourseOfAction() {
        //if section have been started than do nothing and return
        if (mQuestionnaireManager.getQuestions().size() > 0)
            return;

        if (mViewModel.getResumeModel() != null) {
            resumeSection();
            return;
        }

        //in case if there is no filtered member or section already completed for all members
        //show action question to either modify existing entry or goto next section
        boolean hasEligibleMembers = mViewModel.getHouseholdMembersFiltered().size() != 0;
        boolean startSectionForMember = false;
        if (hasEligibleMembers) {
            for (MemberSection ms : mViewModel.getHouseholdMembersFiltered()) {
                startSectionForMember = true;
                for (Section s : mViewModel.getSectionEntries()) {
                    if (((MemberSection) s).sno.intValue() == ms.sno.intValue()) {
                        if (s.section_status == Constants.Status.SECTION_CLOSED)
                            startSectionForMember = false;

                        break;
                    }
                }

                if (startSectionForMember) {
                    //exclude 0th position because it have no model, it is label for spinner
                    for (int i=1; i<mAdapterSpinnerUsers.getCount(); i++) {
                        if (mAdapterSpinnerUsers.getItem(i).getModel().getMemberId().intValue() == ms.sno.intValue()) {
                            mSpinnerUsers.setSelection(i, true);
                        }
                    }
                    return;
                }
            }
        }

        endSection();
    }

    @Override
    protected boolean shouldDownloadHouseholdMembers() {
        return true;
    }

    protected List<ItemSpinnerMember> getMembersSpinnerOptions(){
        List<ItemSpinnerMember> optUser = new ArrayList<>();
        ItemSpinnerMember.LabelMaker labelMaker = getLabelMakerSpinnerMembers();
        optUser.add(new ItemSpinnerMember("Household Members"));
        if (mViewModel.getHouseholdMembersFiltered().size() > 0) {
            for (RosterSection m : mViewModel.getHouseholdMembersFiltered()) {
                optUser.add(new ItemSpinnerMember(m, labelMaker));
            }
        }
        return optUser;
    }

    protected void resetSelectionSpinnerUsers(){
        mSpinnerUsers.setSelection(0);
        SP_MEMBERS_LAST_VALUE = 0;
    }

    @Override
    public void resumeSection() {
        super.resumeSection();
        for (int i=1; i<mAdapterSpinnerUsers.getCount(); i++){
            if (mViewModel.nseCurrentMemberId(mAdapterSpinnerUsers.getItem(i).getModel().getMemberId())){
                SP_MEMBERS_LAST_VALUE = mViewModel.getCurrentMemberID();
                mSpinnerUsers.setSelection(i);
                break;
            }
        }
    }

    protected void changeCurrentMember(int position, ItemSpinnerMember selectedUser){
        //checking if any previous member data not collected
//        for (int i = 0; i < mViewModel.getHouseholdMembersFiltered().size(); i++){
//            HouseholdMember m = mViewModel.getHouseholdMembersFiltered().get(i);
//            if(m.getMemberId().intValue() == selectedUser.getModel().getMemberId().intValue()){
//                break;
//            }
//
//            /**
//             * TODO allow navigating to other entries, when going to next section then
//             * check if any entry is incomplete or not closed yet
//             */
//            if(mController.getModelForSectionBySNo(getSectionNumber(), m.sno) == null){
//                mUXToolkit.showAlertDialogue("Can not select this until this section is completed for all previous members");
//                spUsers.setSelection(SP_MEMBERS_LAST_VALUE);
//                return;
//            }
//        }

        SP_MEMBERS_LAST_VALUE = position;
        mViewModel.setCurrentMemberID(selectedUser.getModel().getMemberId());
        HouseholdSection currentModel = (MemberSection) mViewModel
                .getSectionEntryByFormContext(mViewModel.getFormContext());

        mViewModel.setResumeModel(currentModel);

        clearForm();

        //order of below two method's invocation is important
        mMap.setModel(currentModel);
        mMap.reset();

        mQuestionnaireManager.reset();

        mContainerForm.setAdapter(constructAdapter());

        List<Question> questionList = mMap.getResumedQuestions();
        for(int i = questionList.size()-1; i >= 0; i--){
            mQuestionnaireManager.addQuestion(questionList.get(i));
        }

        mMap.getQuestionBuilder().setModel(null);
        specifyLabelPlaceholders();
        mQuestionnaireManager.initLabelsPlaceholders();

        mAdapter.notifyItemRangeInserted(0,questionList.size());
        mNavigationToolkit.quickScrollTo(questionList.size()-1, false);

        mContainerForm.post(()->{
            mNavigationToolkit.askNextQuestion();
        });
    }

    protected void setupSpiUsers(Spinner spUsers){
        List<ItemSpinnerMember> optUser = getMembersSpinnerOptions();
        mAdapterSpinnerUsers = new ArrayAdapter<>(this, R.layout.item_list_sp, optUser);
        mAdapterSpinnerUsers.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        spUsers.setAdapter(mAdapterSpinnerUsers);

        spUsers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0 || SP_MEMBERS_LAST_VALUE == position)
                    return;

                if (mViewModel.nseCurrentMemberId(
                        mAdapterSpinnerUsers.getItem(position).getModel().getMemberId()
                ) && mViewModel.nseCurrentMemberId(SP_MEMBERS_LAST_VALUE))
                    return;

                try {
                    if (mQuestionnaireManager.isSectionEnded()) {
                        //save or update implementation
                        if (!saveOrUpdateModel(Constants.Status.SECTION_CLOSED)) {
                            spUsers.setSelection(SP_MEMBERS_LAST_VALUE);
                            mUXToolkit.showToast("Failed to close current entry!");
                            return;
                        }
                    } else if (mQuestionnaireManager.getQuestions().size() > 1) {
                        if (!saveOrUpdateModel()) {
                            spUsers.setSelection(SP_MEMBERS_LAST_VALUE);
                            mUXToolkit.showToast("Failed to save current entry!");
                            return;
                        }
                    }
                } catch (InvalidQuestionStateException e){
                    mUXToolkit.showAlertDialogue(R.string.e110);
                    ExceptionReporter.printStackTrace(e);
                    mSpinnerUsers.setSelection(SP_MEMBERS_LAST_VALUE);
                    return;
                }

                changeCurrentMember(position, (ItemSpinnerMember) parent.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    protected void loadTopContainer(){
        if (mViewModel != null && mViewModel.getAssignment() != null) {
            ViewGroup containerTop = findViewById(R.id.container_toolbox);
            ViewGroup toolbox = (ViewGroup) getLayoutInflater().inflate(R.layout.toolbox_form_member_section_filter_spi_2x, containerTop);

            loadTopContainerSectionInfo(toolbox);

            mSpinnerSections = toolbox.findViewById(R.id.spi_1);
            setupSpiSections(mSpinnerSections);

            mSpinnerUsers = toolbox.findViewById(R.id.spi_2);
            setupSpiUsers(mSpinnerUsers);
        }
    }

    @Override
    protected void refreshTopContainerSpinner() {
        mAdapterSpinnerUsers.clear();
        mAdapterSpinnerUsers.addAll(getMembersSpinnerOptions());
        mAdapterSpinnerUsers.notifyDataSetChanged();
    }

    @Override
    public QuestionActor getActionQuestion() {
        List<ButtonInput> inputList = new ArrayList<>();

        int nextSNo = mViewModel.getNextEligibleSNo();
        inputList.add((
                new ButtonInput(
                        Constants.Index.LABEL_BTN_REPEAT
                        , (view) -> {
                            //No need to save model because when user is changed spinner shall save model
                            //exclude 0th position because it have no model, it is label for spinner
                            for (int i = 1; i < mAdapterSpinnerUsers.getCount(); i++){
                                if (mAdapterSpinnerUsers.getItem(i).getModel().getMemberId() == nextSNo) {
                                    mSpinnerUsers.setSelection(i);
                                    break;
                                }
                            }
                        }
                )
        ));

        inputList.add(
            new ButtonInput(
                Constants.Index.LABEL_BTN_NEXT_SECTION
                , (view) -> {
                    boolean result = mUXToolkit.showConfirmDialogue(R.string.alert_goto_next_section_message,
                            new UXEventListeners.ConfirmDialogueEventsListener() {
                        @Override
                        public void onOK() {
                            try {
                                if (saveOrUpdateModel(Constants.Status.SECTION_CLOSED)) {
                                    gotoNextSection();
                                }else
                                    mUXToolkit.showToast("System failed to insert data");
                            } catch (InvalidQuestionStateException e) {
                                mUXToolkit.showAlertDialogue(R.string.e110);
                                ExceptionReporter.printStackTrace(e);
                            }
                        }

                        @Override
                        public void onCancel() { }
                    });

                    // in case system failed to show dialogue, it will exec onOK implementation
                    if(!result){
                        try {
                            if (extractStoreSectionModel(Constants.Status.SECTION_CLOSED)) {
                                gotoNextSection();
                            } else
                                mUXToolkit.showToast("System failed to insert data");
                        } catch (InvalidQuestionStateException e) {
                            mUXToolkit.showAlertDialogue(R.string.e110);
                            ExceptionReporter.printStackTrace(e);
                        }
                    }
                }
            )
        );

        ButtonInput[] buttons = new ButtonInput[inputList.size()];
        inputList.toArray(buttons);

        QuestionActor actionQuestion = new QuestionActor(buttons);
        actionQuestion.getAdapter().setOnAnswerEventListener((manager, askables) -> {
            if (nextSNo == Constants.INVALID_NUMBER)
                askables[0].lock();
        });

        return actionQuestion;
    }
}
