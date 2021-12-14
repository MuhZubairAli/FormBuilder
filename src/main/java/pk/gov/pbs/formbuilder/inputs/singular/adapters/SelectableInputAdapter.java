package pk.gov.pbs.formbuilder.inputs.singular.adapters;

import android.view.ViewGroup;
import android.widget.RadioGroup;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.core.Question;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.SingularInput;
import pk.gov.pbs.formbuilder.inputs.grouped.GroupInputRBI;
import pk.gov.pbs.formbuilder.inputs.singular.RadioInput;
import pk.gov.pbs.formbuilder.inputs.singular.Selectable;
import pk.gov.pbs.formbuilder.inputs.singular.SpecifiableRadioInput;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.validator.Validator;

public abstract class SelectableInputAdapter extends TouchInputAdapter{
    private final int mColumnCount;
    public SelectableInputAdapter(SingularInput[] singularInputs) {
        super(R.layout.container_rg, singularInputs);
        mColumnCount = 1;
    }

    public SelectableInputAdapter(SingularInput[] singularInputs, Validator validator) {
        this(singularInputs);
        mValidator = validator;
    }

    public SelectableInputAdapter(SingularInput[] singularInputs, int columnCount) {
        super(R.layout.container_rg_col, singularInputs);
        mColumnCount = columnCount;
    }

    public SelectableInputAdapter(SingularInput[] singularInputs, int columnCount, Validator validator) {
        this(singularInputs, columnCount);
        mValidator = validator;
    }

    @Override
    public void init(ActivityFormSection context, ViewGroup container, Question question) {
        if (mColumnCount > 1) {

            mAnswerContainer = container;
            //---------------------------------------------
            //Inflate all askables
            //---------------------------------------------
            final int itemCount = mSingularInputs.length;
            final int rowCount = (int) Math.ceil((float) itemCount / mColumnCount);

            int abCount = -1;
            for (int i = 0; i < rowCount; i++) {
                RadioGroup answerContainer = (RadioGroup) context.getLayoutInflater().inflate(mContainerResID, container, false);
                for (int j = 0; j < mColumnCount; j++) {
                    if (++abCount < itemCount) {
                        mSingularInputs[abCount].inflate(
                                context.getLayoutInflater(),
                                context.getLabelProvider(),
                                answerContainer
                        );
                    }
                }
                container.addView(answerContainer);
            }

            //-------------------------------------------
            //Bind Listeners to All Askables
            //-------------------------------------------
            mQuestionnaireManager = context.getQuestionnaireManager();
            for (SingularInput ab : mSingularInputs) {
                ab.bindListeners(context, () -> {
                    if (!GroupInputRBI.IGNORE_CLEAR_CHECK_ALL) {
                        for (int i = 0; i < mSingularInputs.length; i += mColumnCount)
                            ((RadioGroup) mSingularInputs[i].getInputView().getParent()).clearCheck();
                    } else
                        GroupInputRBI.IGNORE_CLEAR_CHECK_ALL = false;

                    if (mOnAnswerEvent != null)
                        mOnAnswerEvent.onAnswer(mQuestionnaireManager, mSingularInputs);
                });
            }

            //---------------------------------------------
            //Process question state
            //----------------------------------------------
            initQuestion(context, question);
        } else
            super.init(context, container, question);

        // Automatically goto next section upon selection of radio button
        if (mSingularInputs[0] instanceof RadioInput || mSingularInputs[0] instanceof SpecifiableRadioInput) {
            final int questionIndex = context.getNavigationToolkit().getQuestionIndex(question);
            for (SingularInput ab : mSingularInputs) {
                ((Selectable) ab).setOnAnswerEventListener((oldAnswers, newAnswers) -> {
                    // make sure it is being called due to setting answer
                    // not clearing old answer (due to unselecting old radio button)
                    if (newAnswers != null && newAnswers.length > 0 && newAnswers[0] != null) {
                        // it is delayed to make sure current question is locked
                        container.postDelayed(() -> {
                            if (questionIndex != Constants.INVALID_NUMBER)
                                context.getNavigationToolkit().askNextQuestion(questionIndex);
                            else
                                context.getNavigationToolkit().askNextQuestion();
                        }, 50);
                    }
                });
            }
        }
    }
}
