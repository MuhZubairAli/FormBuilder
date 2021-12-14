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
import pk.gov.pbs.formbuilder.toolkits.UXEventListeners;
import pk.gov.pbs.formbuilder.exceptions.InvalidQuestionStateException;
import pk.gov.pbs.formbuilder.inputs.singular.ButtonInput;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.models.IterativeMemberSection;
import pk.gov.pbs.formbuilder.models.HouseholdSection;
import pk.gov.pbs.formbuilder.pojos.ItemSpinnerIterativeMember;
import pk.gov.pbs.utils.ExceptionReporter;

public abstract class ActivityIterativeMemberSection extends ActivityMemberSection {
    private int SP_ITERATION_LAST_VALUE = -1;
    protected Spinner mSpinnerIterations;
    protected ArrayAdapter<ItemSpinnerIterativeMember> mAdapterSpinnerIterations;

    public abstract ItemSpinnerIterativeMember.LabelMaker getLabelMakerSpinnerIterations();

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
        if (hasEligibleMembers) {
            for (Section s : mViewModel.getSectionEntries()) {
                if (s.section_status == Constants.Status.SECTION_OPENED) {
                    IterativeMemberSection rm = (IterativeMemberSection) s;

                    mViewModel.getFormContext().setSNo(rm.getMemberId());
                    mViewModel.getFormContext().setINo(rm.getIterationNumber());

                    mViewModel.setResumeModel(rm);
                    resumeSection();
                    return;
                }
            }

            int nextSNO = (mViewModel.getSectionEntries().size() == 0) ?
                    mViewModel.getCurrentMemberID() : mViewModel.getNextEligibleSNo();

            if (nextSNO != Constants.INVALID_NUMBER){
                for (int i=1; i<mAdapterSpinnerUsers.getCount(); i++){
                    if (mAdapterSpinnerUsers.getItem(i).getModel().getMemberId() == nextSNO) {
                        //reset (if any) iteration number exist for normal iteration behaviour
                        mViewModel.getFormContext().setINo(null);
                        mSpinnerUsers.setSelection(i);
                        return;
                    }
                }
            }
        }

        //resetting sno and ino in case no selection is made on any spinner
        mViewModel.getFormContext().setSNo(null);
        mViewModel.getFormContext().setINo(null);

        endSection();
    }

    @Override
    public void resumeSection() {
        super.resumeSection();
        refreshTopContainerSpinner();
        for (int i=1; i<mAdapterSpinnerIterations.getCount(); i++){
            if (mViewModel.nseCurrentIteration(mAdapterSpinnerIterations.getItem(i).getModel().getIterationNumber())){
                SP_ITERATION_LAST_VALUE = mViewModel.getCurrentIteration();
                mSpinnerIterations.setSelection(i);
                break;
            }
        }
    }

    protected List<ItemSpinnerIterativeMember> getIterationsSpinnerOptions(){
        List<ItemSpinnerIterativeMember> optUser = new ArrayList<>();
        ItemSpinnerIterativeMember.LabelMaker labelMaker = getLabelMakerSpinnerIterations();
        optUser.add(new ItemSpinnerIterativeMember("Member Iterations"));
        if (mViewModel.getSectionEntries().size() > 0 && mViewModel.getCurrentMemberID() != null) {
            for (Section m : mViewModel.getSectionEntries()) {
                if (((IterativeMemberSection) m).sno.intValue() == mViewModel.getCurrentMemberID().intValue())
                    optUser.add(new ItemSpinnerIterativeMember((IterativeMemberSection) m, labelMaker));
            }
        }
        return optUser;
    }

    protected void resetSelectionSpinnerIterations(){
        mViewModel.getFormContext().setINo(null);
        mSpinnerIterations.setSelection(0);
        SP_ITERATION_LAST_VALUE = 0;
    }

    @Override
    protected void refreshTopContainerSpinner() {
        mAdapterSpinnerIterations.clear();
        mAdapterSpinnerIterations.addAll(getIterationsSpinnerOptions());
        mAdapterSpinnerIterations.notifyDataSetChanged();
    }

    protected void changeCurrentIteration(int position, ItemSpinnerIterativeMember selectedUser){
        SP_ITERATION_LAST_VALUE = position;
        mViewModel.getFormContext().setINo(selectedUser.getModel().getIterationNumber());

        HouseholdSection currentModel = mViewModel
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

        mContainerForm.post(mNavigationToolkit::askNextQuestion);
    }

    protected void setupSpiIterations(Spinner mSpinnerIterations){
        List<ItemSpinnerIterativeMember> optUser = new ArrayList<>();
        optUser.add(new ItemSpinnerIterativeMember("Member Iterations"));

        mAdapterSpinnerIterations = new ArrayAdapter<>(this, R.layout.item_list_sp, optUser);
        mAdapterSpinnerIterations.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        mSpinnerIterations.setAdapter(mAdapterSpinnerIterations);

        mSpinnerIterations.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0 || SP_ITERATION_LAST_VALUE == position)
                    return;

                try {
                    if (mQuestionnaireManager.isSectionEnded()) {
                        //save or update implementation
                        if (!saveOrUpdateModel(Constants.Status.SECTION_CLOSED)) {
                            mSpinnerIterations.setSelection(SP_ITERATION_LAST_VALUE);
                            mUXToolkit.showToast("Failed to close current entry!");
                            return;
                        }
                    } else if (mQuestionnaireManager.getQuestions().size() > 1) {
                        if (!saveOrUpdateModel()) {
                            mSpinnerIterations.setSelection(SP_ITERATION_LAST_VALUE);
                            mUXToolkit.showToast("Failed to save current entry!");
                            return;
                        }
                    }
                } catch (InvalidQuestionStateException e){
                    mUXToolkit.showAlertDialogue(R.string.e110);
                    ExceptionReporter.printStackTrace(e);
                    mSpinnerUsers.setSelection(SP_ITERATION_LAST_VALUE);
                    return;
                }

                changeCurrentIteration(position, (ItemSpinnerIterativeMember) parent.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        mSpinnerUsers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0)
                    return;

                if (
                        mViewModel.nseCurrentMemberId(
                                mAdapterSpinnerUsers.getItem(position).getModel().getMemberId()
                        ) && mViewModel.nseCurrentMemberId(SP_MEMBERS_LAST_VALUE)
                ){
                    refreshTopContainerSpinner();
                    return;
                }

                if (mQuestionnaireManager.getQuestions().size() > 0) {
                    try {
                        if (mQuestionnaireManager.isSectionEnded()) {
                            if (!saveOrUpdateModel(Constants.Status.SECTION_CLOSED)) {
                                mSpinnerUsers.setSelection(SP_MEMBERS_LAST_VALUE);
                                mUXToolkit.showToast("Failed to save!");
                                return;
                            }
                        } else if(mQuestionnaireManager.getQuestions().size() > 1){
                            if (!saveOrUpdateModel()) {
                                mSpinnerUsers.setSelection(SP_MEMBERS_LAST_VALUE);
                                mUXToolkit.showToast("Failed to save!");
                                return;
                            }
                        }
                    } catch (InvalidQuestionStateException e) {
                        mSpinnerUsers.setSelection(SP_MEMBERS_LAST_VALUE);
                        mUXToolkit.showAlertDialogue(R.string.e110);
                        ExceptionReporter.printStackTrace(e);
                        return;
                    }

                    mViewModel.setCurrentMemberID(
                            mAdapterSpinnerUsers.getItem(position).getModel().getMemberId()
                    );

                    refreshTopContainerSpinner();
                    preRepeatSection();
                    mMap.reset();

                    if (!hasIterations(mViewModel.getCurrentMemberID())) {
                        startSection();
                    } else {
                        endSection();
                    }
                } else {
                    mViewModel.setCurrentMemberID(
                            mAdapterSpinnerUsers.getItem(position).getModel().getMemberId()
                    );

                    refreshTopContainerSpinner();
                    if (!hasIterations(mViewModel.getCurrentMemberID())) {
                        startSection();
                    } else {
                        preRepeatSection();
                        endSection();
                    }
                }

                //Clear iteration number as it is irrelevant after change of member
                //So when selection is made from iterations spinner it wont hinder
                resetSelectionSpinnerIterations();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    protected List<IterativeMemberSection> getIterationsForMember(int sno){
        List<IterativeMemberSection> iterations = new ArrayList<>();
        if (mViewModel.getSectionEntries().size() > 0 && mViewModel.getCurrentMemberID() != null) {
            for (Section m : mViewModel.getSectionEntries()) {
                IterativeMemberSection model = (IterativeMemberSection) m;
                if (model.sno == sno)
                    iterations.add(model);
            }
        }
        return iterations;
    }

    protected boolean hasIterations(int sno){
        if (mViewModel.getSectionEntries().size() > 0 && mViewModel.getCurrentMemberID() != null) {
            for (Section m : mViewModel.getSectionEntries()) {
                if (((IterativeMemberSection) m).sno == sno)
                    return true;
            }
        }
        return false;
    }

    protected void loadTopContainer(){
        if (mViewModel != null && mViewModel.getAssignment() != null) {
            ViewGroup containerTop = findViewById(R.id.container_toolbox);
            ViewGroup toolbox = (ViewGroup) getLayoutInflater().inflate(R.layout.toolbox_form_iterative_member_section_filter_spi_3x, containerTop);

            loadTopContainerSectionInfo(toolbox);

            mSpinnerSections = toolbox.findViewById(R.id.spi_1);
            setupSpiSections(mSpinnerSections);

            mSpinnerUsers = toolbox.findViewById(R.id.spi_2);
            setupSpiUsers(mSpinnerUsers);

            mSpinnerIterations = toolbox.findViewById(R.id.spi_3);
            setupSpiIterations(mSpinnerIterations);
        }
    }

    @Override
    public QuestionActor getActionQuestion() {
        List<ButtonInput> inputList = new ArrayList<>();
        inputList.add((
                new ButtonInput(
                        Constants.Index.LABEL_BTN_NEXT_ITERATION
                        , (view) -> {
                            try {
                                if (saveOrUpdateModel(Constants.Status.SECTION_CLOSED)) {
                                    refreshTopContainerSpinner();
                                    repeatSection();
                                } else
                                    mUXToolkit.showToast("System failed to insert data");
                            } catch (InvalidQuestionStateException e) {
                                mUXToolkit.showAlertDialogue(R.string.e110);
                                ExceptionReporter.printStackTrace(e);
                            }
                        }
                )
        ));

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
            if (mSpinnerUsers.getSelectedItemPosition() == 0)
                askables[0].lock();
            if (nextSNo == Constants.INVALID_NUMBER)
                askables[1].lock();
        });

        return actionQuestion;
    }
}
