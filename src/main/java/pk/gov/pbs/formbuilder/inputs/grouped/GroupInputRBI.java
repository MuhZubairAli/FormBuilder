package pk.gov.pbs.formbuilder.inputs.grouped;

import android.widget.RadioGroup;

import pk.gov.pbs.formbuilder.core.ViewModelFormSection;
import pk.gov.pbs.formbuilder.core.Question;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.Askable;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.inputs.singular.Selectable;
import pk.gov.pbs.formbuilder.meta.ColumnCount;
import pk.gov.pbs.formbuilder.validator.Validator;

public class GroupInputRBI extends GroupInputSelectable {
    private static boolean isAskNextCallScheduled = false;
    public static boolean IGNORE_CLEAR_CHECK_ALL = false;
    public static boolean IGNORE_ASK_NEXT_QUESTION_CALL = false;

    public GroupInputRBI(String index, Selectable[] selectables, ColumnCount columnCount, Validator validator, String... extras) {
        super(index, selectables, columnCount, validator, extras);

        int maxLength = 1;
        for (Selectable ab : selectables)
            if (ab.getAnswers().length == 2)
                maxLength = 2;

        mAnswers = new ValueStore[maxLength];
    }

    public GroupInputRBI(String index, Selectable[] selectables, ColumnCount columnCount, String... extras) {
        this(index, selectables, columnCount, null, extras);
    }

    @Override
    public boolean hasAnswer() {
        return mAnswers != null && mAnswers.length > 0 && mAnswers[0] != null;
    }

    @Override
    public void exportAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        model.set(getIndex(), getAnswer(0));
        if (mAnswers.length == 2 && mAnswers[1] != null)
            model.set("__"+getIndex(), mAnswers[1]);
        if (mExtras != null && mExtras.length > 0) {
            char suffix = 'a';
            for (ValueStore extra : mExtras){
                model.set(getIndex()+suffix++, extra);
            }
        }
    }

    @Override
    public boolean importAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        ValueStore answer = model.get(getIndex());
        if(answer != null) {
            setAnswer(0, answer);
            if (model.hasField("__"+getIndex()) && model.get("__"+getIndex()) != null)
                setAnswer(1, model.get("__" + getIndex()));
            return true;
        }
        return false;
    }

    @Override
    public boolean loadAnswerIntoInputView(ViewModelFormSection viewModel) {
        IGNORE_ASK_NEXT_QUESTION_CALL = true;
        boolean result = false;
        for (Selectable ri : mSelectables){
            if(ri.getValue().equalsIgnoreCase(getAnswer(0))){
                if(!ri.setAnswers(mAnswers))
                    ri.setAnswerAsChecked();

                result = ri.loadAnswerIntoInputView(viewModel);
            }
        }
        IGNORE_ASK_NEXT_QUESTION_CALL = false;
        return result;
    }

    @Override
    public void bindListeners(ActivityFormSection context, Runnable onAnswerEventLambda) {
        for (Selectable ab : mSelectables) {
            ab.bindListeners(context, () -> {

                if (!IGNORE_CLEAR_CHECK_ALL) {
                    for (int i = 0; i < mSelectables.length; i += mColumnCount) {
                        ((RadioGroup) mSelectables[i].getInputView().getParent()).clearCheck();
                    }
                } else
                    IGNORE_CLEAR_CHECK_ALL = false;

                if (onAnswerEventLambda != null) {
                    onAnswerEventLambda.run();
                }

                //If this askable is has answer and all next askables are hidden then goto next question
                if (!IGNORE_ASK_NEXT_QUESTION_CALL && !isAskNextCallScheduled) {
                    int delayMillis = 10;
                    if (isAnimationEnabled())
                        delayMillis = (int) Constants.ANIM_DURATION;

                    getInputView().postDelayed(() -> {
                        int qNo = context.getNavigationToolkit().getQuestionIndexByAskableIndex(getIndex());
                        if (qNo != Constants.INVALID_NUMBER) {
                            Question question = context.getQuestionnaireManager().getQuestion(qNo);
                            if (question != null) {
                                boolean gotoNextQuestion = true;
                                for (Askable ask : question.getAdapter().getAskables()) {
                                    if (ask.isVisible() && !ask.hasAnswer()) {
                                        gotoNextQuestion = false;
                                        break;
                                    }
                                }

                                if (gotoNextQuestion)
                                    context.getNavigationToolkit().askNextQuestion(qNo);
                                isAskNextCallScheduled = false;
                            }
                        }
                    }, delayMillis);
                    isAskNextCallScheduled = true;
                }
            });

            ab.setOnAnswerEventListener((oldAnswers, newAnswers) -> {
                /**
                 * In order to prevent event bubbling instead of
                 * calling setAnswers(ValueStore...) setting array directly
                 * and it also bypasses the length check
                 */
                if (mAnswers == newAnswers)
                    return;

                if (newAnswers != null && newAnswers.length > 0 && newAnswers[0] != null) {
                    System.arraycopy(newAnswers, 0, mAnswers, 0, newAnswers.length);
                }
            });
        }
    }
}