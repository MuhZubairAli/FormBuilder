package pk.gov.pbs.formbuilder.core;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

import java.util.List;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.meta.ErrorStatementProvider;
import pk.gov.pbs.formbuilder.meta.IMetaManifest;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.pojos.ItemSpinnerMember;
import pk.gov.pbs.formbuilder.exceptions.InvalidQuestionStateException;
import pk.gov.pbs.formbuilder.inputs.singular.ButtonInput;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.models.SectionContext;
import pk.gov.pbs.formbuilder.models.HouseholdSection;
import pk.gov.pbs.formbuilder.toolkits.NavigationToolkit;
import pk.gov.pbs.utils.ExceptionReporter;
import pk.gov.pbs.utils.UXEventListeners;

public abstract class ActivityFormSection extends ActivityCustom {
    private static final String TAG = ActivityFormSection.class.getSimpleName();
    protected QuestionnaireManager<?> mQuestionnaireManager;
    protected QuestionnaireBuilder mQuestionnaireBuilder;
    protected QuestionnaireAdapter mAdapter;
    protected RecyclerView mContainerForm;
    protected TabLayout mTabLayout;
    protected ViewPager2 mViewPager;
    protected ViewGroup mDataDisplay;
    protected ViewGroup mSectionActionButtons;
    protected View mBtnToggleSectionActionButtons;
    protected ProgressBar mFormProgressBar;
    protected FragmentDataDisplay mCurrentSectionDataDisplayFragment;
    protected ViewModelFormSection mViewModel;
    protected QuestionnaireMap mMap;
    protected NavigationToolkit mNavigationToolkit;
    protected ErrorStatementProvider mErrorStatementProvider;
    protected IMetaManifest mMetaDataManifest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_section_survey);

        // Temporarily hiding of status bar and soft keys is not implemented
        // Because it has issues with old android versions
        //hideSystemControls();
        hideActionBar();

        mErrorStatementProvider = constructErrorStatementProvider();
        mMetaDataManifest = constructMetaManifest();

        /**
         * Current context of form section
         * This must be updated before starting new activity and added to intent extras
         */
        SectionContext fc = (SectionContext) getIntent()
                .getSerializableExtra(Constants.Index.INTENT_EXTRA_SECTION_CONTEXT);
        Section resumeSection = (Section) getIntent()
                .getSerializableExtra(Constants.Index.INTENT_EXTRA_SECTION_MODEL);

        // if only resume model is provided than derive context from it
        if (fc == null && resumeSection != null) {
            fc = resumeSection.getSectionContext();
            fc.setSeNo(getSectionNumber());
        }

        if (fc == null || fc.getSection() == null || fc.getSection().intValue() != getSectionNumber()){
            mUXToolkit.showAlertDialogue("Invalid Form Context, Kindly make sure you are in correct section. If problem persists please contact DPC@PBS");
            return;
        }

        mLabelProvider = constructLabelProvider();
        mViewModel = constructViewModel();
        mViewModel.init(mMetaDataManifest, fc, mLabelProvider, resumeSection, shouldDownloadHouseholdMembers(), shouldDownloadSectionEntries());

        if (resumeSection == null) {
            resumeSection = mViewModel.getSectionEntryByFormContext(
                    mViewModel.getSectionContext(),
                    "`section_status`=" + Constants.Status.SECTION_OPENED
            );

            if (resumeSection != null)
                mViewModel.setResumeModel(resumeSection);
        }

        if (mViewModel.getResumeModel() != null)
            mQuestionnaireBuilder = new QuestionnaireBuilder(mLabelProvider, resumeSection);
        else
            mQuestionnaireBuilder = new QuestionnaireBuilder(mLabelProvider);

        mMap = constructMap(mQuestionnaireBuilder);
        mQuestionnaireManager = constructQuestionnaireManager();
        mNavigationToolkit = new NavigationToolkit(this);
        mContainerForm = findViewById(R.id.rvSurveyForm);
        mContainerForm.setLayoutManager(new LinearLayoutManager(this));
        mContainerForm.setAdapter(constructAdapter());

        setupFormProgressBarView(); // this method depend on mQuestionnaireManager, so it must be after that
        setupDisplayDataViews();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        initTopContainer();
        determineCourseOfAction();
    }

    public ViewModelFormSection getViewModel(){
        return mViewModel;
    }
    public QuestionnaireManager<?> getQuestionnaireManager() {
        return mQuestionnaireManager;
    }
    public NavigationToolkit getNavigationToolkit(){
        return mNavigationToolkit;
    }

    public QuestionnaireMap getMap() {
        return mMap;
    }
    public RecyclerView getFormContainer(){
        return this.mContainerForm;
    }
    public ErrorStatementProvider getErrorStatementProvider(){
        return mErrorStatementProvider;
    }
    public IMetaManifest getMetaManifest() {
        return mMetaDataManifest;
    }
    public SectionContext getSectionContext(){
        return mViewModel.getSectionContext();
    }

    protected abstract void onActionClickPartiallyRefuse();
    protected abstract QuestionnaireMap constructMap(QuestionnaireBuilder questionnaireBuilder);
    protected abstract ViewModelFormSection constructViewModel();
    protected abstract LabelProvider constructLabelProvider();
    protected abstract ErrorStatementProvider constructErrorStatementProvider();
    protected abstract IMetaManifest constructMetaManifest();
    protected abstract QuestionnaireManager<?> constructQuestionnaireManager();
    //Todo: Remove this method, automate the process by using question index as placeholder in label string
    protected abstract void specifyLabelPlaceholders();
    public abstract boolean extractStoreSectionModel(int section_status) throws InvalidQuestionStateException;
    public abstract boolean updateStoreSectionModel(int section_status) throws InvalidQuestionStateException;

    protected void determineCourseOfAction(){
        // duh... it's obvious
        startSection();
    }

    protected void setupFormProgressBarView(){
        mFormProgressBar = findViewById(R.id.pb_form);
        mFormProgressBar.setVisibility(View.VISIBLE);
        mFormProgressBar.setMax(10000);

        if (mQuestionnaireManager != null){
            mQuestionnaireManager.setOnAdvanceQuestionEvent((currentQuestion, totalQuestions) -> {
                mFormProgressBar.post(()->{
                    int progress = (int) Math.ceil(((float) currentQuestion / (float) totalQuestions) * 10000.00F);
                    if (progress > 0) {
                        ObjectAnimator.ofInt(mFormProgressBar, "progress", progress)
                                .setDuration(2000)
                                .start();
                    } else {
                        mFormProgressBar.setProgress(progress);
                    }
                });
            });
        }
    }

    protected void setupDisplayDataViews(){
        mDataDisplay = findViewById(R.id.container_data_display);
        mTabLayout = findViewById(R.id.tab_layout);
        mViewPager = findViewById(R.id.view_pager);

        mViewPager.setAdapter(constructDataDisplayFragmentStateAdapter());

        for (int i=getSectionNumberFromDataTabPosition(0); i<= getSectionNumber(); i++) {
            mTabLayout.addTab(mTabLayout.newTab().setText(mMetaDataManifest.getSectionIdentifier(i)));
        }

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                mTabLayout.selectTab(mTabLayout.getTabAt(position));
                if (
                        getSectionNumberFromDataTabPosition(position) == getSectionNumber()
                        && mCurrentSectionDataDisplayFragment != null
                )
                    mCurrentSectionDataDisplayFragment.refreshData();
            }
        });
    }

    protected FragmentStateAdapter constructDataDisplayFragmentStateAdapter(){
        return new FragmentStateAdapter(getSupportFragmentManager(), getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                FragmentDataDisplay fragment = new FragmentDataDisplay(getSectionNumberFromDataTabPosition(position), mViewModel);
                if (getSectionNumberFromDataTabPosition(position) == getSectionNumber())
                    mCurrentSectionDataDisplayFragment = fragment;
                return fragment;
            }

            @Override
            public int getItemCount() {
                int diff = getSectionNumberFromDataTabPosition(0);
                return (getSectionNumber() - diff) + 1;
            }
        };
    }

    protected int getSectionNumberFromDataTabPosition(int position) {
        // roster section at 2 index, and first tab in data display is roster section
        // so position 0 is section number (0+2) = 2, same way position is converted to section number
        return position + 2;
    }

    /**
     * Because there is Askable of type HouseholdMembersSpinnerInput
     * which requires label maker for spinner options, and it have ActivityFormSection
     * context, so this method is moved from ActivityMemberSection
     * @return default label maker for Members Spinner Items
     */
    public ItemSpinnerMember.LabelMaker getLabelMakerSpinnerMembers() {
        return mModel -> {
            StringBuilder label = new StringBuilder();
            if (mModel.getMemberId() < 10)
                label.append("0").append(mModel.getMemberId());
            else
                label.append(mModel.getMemberId().toString());
            label.append(". ").append(mModel.getName());

            if (mModel.getGenderCode() != null && mModel.getGenderCode() != null)
                label.append(" - ")
                        .append(getMetaManifest().getRelationshipLabel(mModel.getRelationCode(), mModel.getGenderCode()));

            if (mModel.getAge() != null)
                label.append(" - ")
                        .append(mModel.getAge().toString())
                        .append((mModel.getAge() > 1) ? " years" : " year");

            return label.toString();
        };
    }

    public int getSectionNumber() {
        return mMetaDataManifest.getSectionNumberFromClass(this.getClass());
    }

    protected final void onActionClickToggleDataDisplay(){
        if (mDataDisplay == null || mDataDisplay.getTag() != null)
            return;

        if (mDataDisplay.getVisibility() == View.GONE) {
            mUXToolkit.hideKeyboardFrom(null);
            mDataDisplay.animate()
                    .alpha(1.0f)
                    .translationYBy(getResources().getDimensionPixelOffset(R.dimen.negativeSpacePrimary))
                    .setDuration(500).withStartAction(() -> {
                        mDataDisplay.setTag(new Object());
                        mDataDisplay.setVisibility(View.VISIBLE);
                    }).withEndAction(() -> {
                        mDataDisplay.setTag(null);
                        int lastPage = mTabLayout.getTabCount() - 1;
                        mViewPager.setCurrentItem(lastPage, false);
                        mTabLayout.selectTab(mTabLayout.getTabAt(lastPage), true);
                        if (
                                getSectionNumberFromDataTabPosition(mTabLayout.getSelectedTabPosition())==getSectionNumber()
                                        && mCurrentSectionDataDisplayFragment != null
                        )
                            mCurrentSectionDataDisplayFragment.refreshData();

            });
        } else
            mDataDisplay.animate()
                    .alpha(0.f)
                    .translationYBy(getResources().getDimensionPixelOffset(R.dimen.spacePrimary))
                    .setDuration(500)
                    .withStartAction(()->{
                        mDataDisplay.setTag(new Object());
                    })
                    .withEndAction(()->{
                        mDataDisplay.setVisibility(View.GONE);
                        mDataDisplay.setTag(null);
                    });
    }

    protected void clearForm(){
        int size = mQuestionnaireManager.getQuestions().size();
        if (size > 0) {
            mQuestionnaireManager.getQuestions().clear();
            mAdapter.notifyItemRangeRemoved(0, size);
        }
    }

    protected void loadTopContainerSectionInfo(ViewGroup sectionInfoContainer){
        if(mViewModel.getAssignment() != null){
            ((TextView) sectionInfoContainer.findViewById(R.id.tv_1)).setText(getString(R.string.concat_2_string, "PC-", mViewModel.getAssignment().getPCode()));
            ((TextView) sectionInfoContainer.findViewById(R.id.tv_2)).setText(getString(R.string.concat_2_string, "EB-", mViewModel.getAssignment().getEBCode()));
            ((TextView) sectionInfoContainer.findViewById(R.id.tv_3)).setText(getString(R.string.concat_2_string, "HH-", mViewModel.getAssignment().getHHNo() + ""));
        } else
            sectionInfoContainer.setVisibility(View.GONE);
    }

    protected void initTopContainer(){
        ViewGroup toolbox = findViewById(R.id.container_toolbox);
        if (toolbox == null)
            return;

        mSectionActionButtons = toolbox.findViewById(R.id.container_section_actions);

        if (mSectionActionButtons == null)
            return;

        mSectionActionButtons.setVisibility(View.GONE);

        for (int i = 0; i< mSectionActionButtons.getChildCount(); i++) {
            mSectionActionButtons.getChildAt(i).setOnClickListener((view) -> {
                handleActionClick(view.getId());
            });
        }

        mBtnToggleSectionActionButtons = toolbox.findViewById(R.id.btn_expand);
        mBtnToggleSectionActionButtons.setOnClickListener(v -> toggleActionButtonsDisplay());
    }

    private void toggleActionButtonsDisplay(){
        if (mSectionActionButtons == null)
            return;

        if (mSectionActionButtons.getVisibility() != View.VISIBLE) {
            mSectionActionButtons.post(()->{
                mSectionActionButtons
                        .animate()
                        .setDuration(Constants.ANIM_DURATION)
                        .alpha(1f)
                        .withStartAction(()->{
                            mSectionActionButtons.setVisibility(ViewGroup.VISIBLE);
                        });
            });
            ((TextView) mBtnToggleSectionActionButtons.findViewById(R.id.btn_expand_label))
                    .setText(R.string.label_symbol_up_arrow);
        } else {
            mSectionActionButtons.post(()->{
                mSectionActionButtons
                        .animate()
                        .setDuration(Constants.ANIM_DURATION)
                        .alpha(0f)
                        .withEndAction(()->{
                            mSectionActionButtons.setTranslationY(Constants.ANIM_SHOW_TRANSLATE_Y);
                            mSectionActionButtons.setVisibility(ViewGroup.GONE);
                        });
            });
            ((TextView) mBtnToggleSectionActionButtons.findViewById(R.id.btn_expand_label))
                    .setText(R.string.label_symbol_down_arrow);
        }
    }

    protected void loadTopContainer() {
        ViewGroup container = findViewById(R.id.container_toolbox);

        if (mViewModel == null || mViewModel.getAssignment() == null){
            container.setVisibility(View.GONE);
            return;
        }

        ViewGroup toolbox = (ViewGroup) getLayoutInflater().inflate(R.layout.toolbox_form_section_household_info, container);
        loadTopContainerSectionInfo(toolbox);
    }

    public QuestionButtonGroup getActionQuestion(){
        return new QuestionButtonGroup(
                new ButtonInput[]{
                        new ButtonInput(
                                Constants.Index.LABEL_BTN_NEXT_SECTION
                                , (View view) -> {
                                    try {
                                        if(saveOrUpdateModel(Constants.Status.SECTION_CLOSED)) {
                                            gotoNextSection();
                                        } else
                                            mUXToolkit.showToast("System failed to save data, Please report the issue to DP Center and try again later.");
                                    } catch (InvalidQuestionStateException e) {
                                        mUXToolkit.showAlertDialogue(R.string.e110);
                                        ExceptionReporter.printStackTrace(e);
                                    }
                                }
                        )
                }
        );
    }

    protected void gotoNextSection(){
        if (mMetaDataManifest.isValidIndex(getSectionNumber()+1)) {
            Intent intent = new Intent(this, mMetaDataManifest.getSection(getSectionNumber() + 1));

            mViewModel.getSectionContext().setSeNo(getSectionNumber()+1);
            if (mViewModel.getHouseholdMembersFiltered() != null && mViewModel.getHouseholdMembersFiltered().size() > 0)
                mViewModel.getSectionContext().setSNo(mViewModel.getHouseholdMembersFiltered().get(0).getMemberId());
            mViewModel.persistSectionContext();

            intent.putExtra(
                    Constants.Index.INTENT_EXTRA_SECTION_CONTEXT,
                    mViewModel.getSectionContext()
            );
            startActivity(intent);
            finish();
        } else
            mUXToolkit.showAlertDialogue("Section No." + (getSectionNumber() + 1) + " is invalid, Click OK to Force Exit.", this::onActionClickForceExit);
    }

    protected void startSection(){
        if (mContainerForm != null)
            mContainerForm.post(this::startSectionImmediate);
        else
            Log.d(TAG, "startSection: Can not start section probably because of premature termination of onCreate method");
    }

    protected void startSectionImmediate(){
        if (mViewModel == null)
            return;

        if(mViewModel.getResumeModel() != null) {
            resumeSection();
            return;
        }

        specifyLabelPlaceholders();

//        if(
//                mViewModel.getController() != null
//                && mViewModel.getController().getSectionStart() != null
//                && mViewModel.getController().getSectionEnd() != null
//        )
//            mQuestionnaireManager.changeQuestionRange(
//                    mViewModel.getController().getSectionStart()
//                    , mViewModel.getController().getSectionEnd()
//            );

        mNavigationToolkit.askNextQuestion();
    }


    protected void endSection(){
        mQuestionnaireManager.skipSection();
        mNavigationToolkit.askNextQuestion();
    }

    public void resumeSection(){
        mContainerForm.post(()->{
            RecyclerView.ItemAnimator animator = mContainerForm.getItemAnimator();
            mContainerForm.setItemAnimator(null);

            List<Question> questionList = mMap.getResumedQuestions();
            for(int i = questionList.size()-1; i >= 0; i--){
                mQuestionnaireManager.insertQuestion(questionList.get(i));
            }

            mMap.setModel(null);
            specifyLabelPlaceholders();
            mQuestionnaireManager.initLabelsPlaceholders();

            mAdapter.notifyItemRangeInserted(0,questionList.size());
            mContainerForm.setItemAnimator(animator);

            mContainerForm.post(()->{
                mContainerForm.post(()->{
                    mContainerForm.scrollToPosition(mAdapter.getItemCount()-1);
                });
                mContainerForm.post(mNavigationToolkit::askNextQuestion);
            });
        });
    }

    protected void preRepeatSection(){
        clearForm();
        mQuestionnaireManager.reset();
        mContainerForm.setAdapter(constructAdapter());
    }
    protected void postRepeatSection(){
        startSection();
    }
    protected void repeatSection(){
        preRepeatSection();
        mMap.reset();
        postRepeatSection();
    }

    protected QuestionnaireAdapter constructAdapter(){
        mAdapter = null;
        mAdapter = new QuestionnaireAdapter(this);
        return mAdapter;
    }

    public void restartFrom(@NonNull String qIndex){
        int size = mQuestionnaireManager.getQuestions().size();
        mQuestionnaireManager.reset();
        specifyLabelPlaceholders();
        mMap.initIndexes();
        RecyclerView.ItemAnimator animator = mContainerForm.getItemAnimator();
        mContainerForm.setItemAnimator(null);
        mAdapter.notifyItemRangeRemoved(0,size);
        mContainerForm.setAdapter(constructAdapter());
        List<Question> sp = mMap.getQuestionsAndReset(qIndex);
        mQuestionnaireManager.addToQuestionsAll(sp);
        mQuestionnaireManager.initLabelsPlaceholders();
        mAdapter.notifyItemRangeInserted(0,sp.size());
        mQuestionnaireManager.changeQuestionRangeNext(qIndex);
        mContainerForm.scrollToPosition(mAdapter.getItemCount() - 1);
        mContainerForm.setItemAnimator(animator);
    }

//    Below method id suitable for list view
//    Recycler view causes error due to mixed up question indexes
//    public void restartFrom(@NonNull String qIndex){
//        int qIndexInt = mQuestionnaireManager.getQuestionIndex(qIndex);
//        if (qIndexInt == Constants.INVALID_NUMBER)
//            return;
//
//        if (mQuestionnaireManager.getQuestions().size() == (qIndexInt+1)){
//            ((Question) mQuestionnaireManager.getQuestion(qIndex)).reset();
//            return;
//        }
//
//        int removeCount = 0;
//        for (int i = mQuestionnaireManager.getQuestions().size() - 1; i > qIndexInt; i--, removeCount++){
//            ((Question) mQuestionnaireManager.getQuestions().get(i)).unlock(true);
//            mQuestionnaireManager.getQuestions().remove(i);
//        }
//
//        mAdapter.notifyItemRangeRemoved(qIndexInt+1, removeCount );
//        mQuestionnaireManager.changeQuestionRangeNext(qIndex);
//        mContainerForm.scrollToPosition(qIndexInt);
//        mContainerForm.postDelayed(()->{
//            ((Question) mQuestionnaireManager.getQuestions().get(qIndexInt)).unlock(true);
//        }, 100);
//    }

    protected boolean saveOrUpdateModel(int status) throws InvalidQuestionStateException{
        if (mViewModel == null)
            return true;

        return (mViewModel.getResumeModel() == null) ?
                extractStoreSectionModel(status)
                : updateStoreSectionModel(status);
    }

    protected boolean saveOrUpdateModel() throws InvalidQuestionStateException{
        return saveOrUpdateModel(Constants.Status.SECTION_OPENED);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_survey, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return handleActionClick(item.getItemId());
    }

    protected boolean handleActionClick(int itemId){
        if (itemId == R.id.action_partially_refuse) {
            onActionClickPartiallyRefuse();
            return true;
        } else if (itemId == R.id.action_force_exit) {
            onActionClickForceExit();
            return true;
        } else if (itemId == R.id.action_debug) {
            onActionClickDebug();
            return true;
        } else if (itemId == R.id.action_show_data) {
            onActionClickToggleDataDisplay();
            return true;
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        if (mDataDisplay != null && mDataDisplay.getVisibility() != View.GONE) {
            onActionClickToggleDataDisplay();
        } else if (mSectionActionButtons != null && mSectionActionButtons.getVisibility() == View.VISIBLE) {
            toggleActionButtonsDisplay();
        }
    }

    protected void onActionClickForceExit() {
        mUXToolkit.showConfirmDialogue(
                "Force Exit"
                , "System will attempt to save current entry and then exit the form, If system fails to save data it will not show any error and data will be lost."
                , new UXEventListeners.ConfirmDialogueEventsListener() {
                    @Override
                    public void onOK() {
                        try {
                            if(!saveOrUpdateModel())
                                mUXToolkit.showToast("Save request failed due to invalid or no data!");
                            Intent intent = new Intent(ActivityFormSection.this, mMetaDataManifest.getStarterActivity());
                            startActivity(intent);
                            finish();
                        } catch (InvalidQuestionStateException e) {
                            ExceptionReporter.printStackTrace(e);
                            mUXToolkit.showAlertDialogue("Invalid Questions State", getString(R.string.e110), "Force Exit", new UXEventListeners.ConfirmDialogueEventsListener() {
                                @Override
                                public void onCancel() {
                                }

                                @Override
                                public void onOK() {
                                    Intent intent = new Intent(ActivityFormSection.this, mMetaDataManifest.getStarterActivity());
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancel() { }
                }
        );
    }

    protected boolean shouldDownloadHouseholdMembers() {
        return false;
    }

    protected boolean shouldDownloadSectionEntries(){
        return false;
    }

    protected void refreshTopContainerSpinner(){ }

    protected void onActionClickDebug(){
        int questionCount = mQuestionnaireManager.getQuestions().size();
        mQuestionnaireManager.getQuestion(questionCount-1).requestFocus();
    }
}
