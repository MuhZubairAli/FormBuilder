package pk.gov.pbs.formbuilder.core;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.Askable;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.inputs.abstracts.adapters.AskableAdapter;
import pk.gov.pbs.formbuilder.inputs.abstracts.adapters.SingularInputAdapter;
import pk.gov.pbs.formbuilder.inputs.singular.adapters.TouchInputAdapter;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.meta.QuestionStates;
import pk.gov.pbs.formbuilder.utils.FormBuilderThemeHelper;
import pk.gov.pbs.utils.StaticUtils;

public class Question {
    protected String index;
    protected AskableAdapter mAdapter;
    private QuestionStates mState;
    private boolean isRigid = false;

    /**
     * isRemoved status tell that during restartFrom(String qIndex) call;
     * this question existed after the critical question (which is being reset)
     * so when questions are collected from map it ignores question which are removed
     *
     * #{this is workaround for previous questions (which have answer and not pending)
     * to appearing again when critical question is reset}
     */
    private boolean isRemoved = false;

    protected ArrayList<Question> dependencies;
    private Conditions mEvent;
    private OnQuestionStateChange mStateEvent;

    public Question(String index, AskableAdapter askableAdapter) {
        this.index = index;
        this.mAdapter = askableAdapter;
        mState = QuestionStates.PENDING;

        if (askableAdapter instanceof SingularInputAdapter) {
            if (((SingularInputAdapter) mAdapter).getValidator() != null) {
                if (((SingularInputAdapter) mAdapter).getValidator().hasJumps())
                    isRigid = true;
            }
        }
    }

    public String getIndex() {
        return index;
    }

    public boolean isRemoved(){
        return isRemoved;
    }
    public void setRemoved(boolean removed){
        isRemoved = removed;
    }

    public AskableAdapter getAdapter() {
        return mAdapter;
    }
    public QuestionStates getState() {
        return mState;
    }
    public boolean isCritical(){
        return isRigid;
    }
    public boolean isLocked(){
        return mState == QuestionStates.LOCKED;
    }

    public Question setCritical(){
        isRigid = true;
        return this;
    }

    public void setState(QuestionStates newState) {
        if (mState == newState)
            return;
        QuestionStates oldState = this.mState;
        this.mState = newState;
        if(mStateEvent != null) {
            StaticUtils.getHandler().post(()->{
                mStateEvent.onChange(Question.this, oldState, newState);
            });
        }
    }

    public void loadModel(Section model){
        if(getAdapter().loadModel(model)) {
            setState(QuestionStates.ANSWERED);
        }
    }

    public boolean loadAnswer(@NonNull String abIndex, ValueStore... answer){
        if(getAdapter().loadAnswer(abIndex, answer)) {
            if (getState() != QuestionStates.ANSWERED)
                setState(QuestionStates.ANSWERED);
            return true;
        }
        return false;
    }

    public ValueStore[][] getAnswers(){
        return getAdapter().getAnswers();
    }

    public boolean rigidify(){
        return lock(true);
    }

    public boolean lock(){
        if(isRigid)
            return lock(true);
        return lock(false);
    }

    private boolean lock(boolean rigidify){
        if (!mAdapter.performValidationCheck()){
            Toast.makeText(
                    getAdapter()
                            .getAskables()[0]
                            .getInputView()
                            .getContext()
                    , mAdapter.getValidationRulesStatement()
                    , Toast.LENGTH_SHORT
            ).show();
            return false;
        }

        if (this.mState == QuestionStates.LOCKED)
            return true;

        if(mAdapter.lockAskables()){
            mAdapter.hideUnansweredAskables();
            ViewGroup container = (ViewGroup) (
                    mAdapter.getAnswerContainerView()
                            .getParent().getParent()
            );
            View btnLock = container.findViewById(R.id.btn_lock_img);
            View tvQuestion = container.findViewById(R.id.tv_question);
            View tvQuestionHint = container.findViewById(R.id.tv_question_hint);

            tvQuestion.setBackgroundColor(FormBuilderThemeHelper.getColorByTheme(container.getContext(), R.attr.colorQuestionStatementBackgroundDim));
            tvQuestionHint.setBackgroundColor(FormBuilderThemeHelper.getColorByTheme(container.getContext(), R.attr.colorQuestionStatementBackgroundDim));
            FormBuilderThemeHelper.applyThemedDrawableToView(container, R.attr.bgCardLocked);

            if(rigidify) {
                btnLock.setBackgroundResource(R.drawable.ic_rigid_lock);
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
                    btnLock.setBackgroundColor(Color.RED);
                }
                isRigid = true;
            }else {
                btnLock.setBackgroundResource(R.drawable.ic_locked);
            }

            container.setFocusable(false);
            container.setFocusableInTouchMode(false);
            setState(QuestionStates.LOCKED);
            return true;
        }
        return false;
    }

    public boolean unlock(){
        return unlock(false);
    }

    public boolean unlock(boolean force){
        if(!force && isRigid) {
            Toast.makeText(
                    getAdapter().getAnswerContainerView()
                            .getContext()
                    , R.string.e14
                    , Toast.LENGTH_LONG
            ).show();
            return false;
        }

        if (this.mState == QuestionStates.UNLOCKED)
            return true;

        if(mAdapter.unlockAskables()){
            mAdapter.showAllAskables();
            View container = (View) (
                    mAdapter.getAnswerContainerView()
                            .getParent().getParent()
            );
            View btnLock = container.findViewById(R.id.btn_lock_img);
            View tvQuestion = container.findViewById(R.id.tv_question);
            View tvQuestionHint = container.findViewById(R.id.tv_question_hint);

            tvQuestion.setBackgroundColor(FormBuilderThemeHelper.getColorByTheme(container.getContext(), R.attr.colorQuestionStatementBackground));
            tvQuestionHint.setBackgroundColor(FormBuilderThemeHelper.getColorByTheme(container.getContext(), R.attr.colorGray5));

            btnLock.setBackgroundResource(R.drawable.ic_unlocked);

            FormBuilderThemeHelper.applyThemedDrawableToView(container, R.attr.bgCardUnlocked);

            container.setFocusable(true);
            container.setFocusableInTouchMode(true);
            setState(QuestionStates.UNLOCKED);
            return true;
        }
        return false;
    }

    public boolean hasAnswers(){
        boolean result = true;
        for (Askable ab : mAdapter.getAskables()) {
            if (ab.isVisible())
                result &= ab.hasAnswer();
        }

        return result;
    }

    /**
     * Resets every askable to default state
     * answer to null and inputElement to unchecked/empty state
     */
    public void reset(){
        unlock(true);
        StaticUtils.getHandler().post(()->{
            getAdapter().resetAskables();
            if (mAdapter instanceof SingularInputAdapter) {
                SingularInputAdapter adapter = (SingularInputAdapter) mAdapter;
                if (adapter.getValidator() != null) {
                    adapter.getValidator().reset();
                    if (adapter.getJumps() != null)
                        adapter.getJumps().clear();
                }
            }

            if(dependencies != null){
                for (short i = 0; i < dependencies.size(); i++){
                    dependencies.get(i).reset();
                }
            }
            isRemoved = false;
            //requestInputFocus();
        });
    }

    public boolean requestInputFocus(){
        if(!(mAdapter instanceof TouchInputAdapter)) {
            if (
                mAdapter.getAskables() != null
                && mAdapter.getAskables().length > 0
                && mAdapter.getAskables()[0] != null
            )
                return getAdapter()
                    .getAskables()[0]
                    .requestFocus();
        } else
            return requestFocus();
        return false;
    }

    public boolean requestFocus(){
        if(mAdapter.getAnswerContainerView() != null) {
            ViewParent parent = mAdapter.getAnswerContainerView().getParent();
            if (parent != null)
                parent = parent.getParent();
            if (parent != null) {
                View questionView = (View) parent;
                questionView.setClickable(true);
                questionView.setFocusable(true);
                questionView.setFocusableInTouchMode(true);

                questionView.post(()->{
                    questionView.requestFocus();
                    questionView.post(()->{
                        questionView.setClickable(false);
                        questionView.setFocusable(false);
                        questionView.setFocusableInTouchMode(false);
                    });
                });
                return true;
            }
        }
        return false;
    }

    public void flash(){
        StaticUtils.getHandler().post(()->{
            try {
                ViewGroup container = (ViewGroup) (
                        mAdapter.getAnswerContainerView()
                                .getParent().getParent()
                );

                ObjectAnimator anim = ObjectAnimator.ofFloat(container, "alpha", 1f, 0f, 1f);
                anim.setDuration(Constants.ANIM_DURATION);
                anim.setRepeatMode(ValueAnimator.REVERSE);
                anim.setRepeatCount(1);
                anim.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    public Question setConditions(Conditions event){
        this.mEvent = event;
        return this;
    }

    public void addDependentQuestion(Question slave){
        if(this.dependencies == null)
            this.dependencies = new ArrayList<>();
        this.dependencies.add(slave);
        this.isRigid = true;
    }

    public boolean removeDependentQuestion(@NonNull Question free){
        if(dependencies != null) {
            boolean result = dependencies.remove(free);
            if (dependencies.size() == 0)
                this.isRigid = false;
            return result;
        }
        return false;
    }

    public void setOnStateChangeListener(OnQuestionStateChange event){
        this.mStateEvent = event;
    }

    public String ExecPreCondition(QuestionnaireManager<?> qm, Question self){
        if(this.mEvent != null){
            return mEvent.PreCondition(qm, self);
        }
        return null;
    }

    public String ExecPostCondition(QuestionnaireManager<?> qm, Question self){
        if(this.mEvent != null){
            return mEvent.PostCondition(qm, self);
        }
        return null;
    }

    public boolean validateAnswer(){
        return mAdapter.performValidationCheck();
    }

    public String getValidationErrorStatement(){
        return mAdapter.getValidationErrorStatement();
    }

    public String getValidationRuleStatement(){
        return mAdapter.getValidationRulesStatement();
    }

    public void initialize(ActivityFormSection context, ViewGroup container){
        getAdapter().init(context, container, this);
    }

    public void setOnAnswerEventListener(AskableAdapter.OnAnswerEvent eventListener){
        mAdapter.setOnAnswerEventListener(eventListener);
    }

    public interface Conditions {
        /**
         * When question is fetched from map, fist it's precondition is called on that question
         * @param manager IQuestionnaireManager of current section
         * @param self current question
         * @returns Error Code; it will return null if it is allowed to add this question to mQuestions
         * else returns empty string
         * if there is validation error then returns error code as string
         */
        String PreCondition(IQuestionnaireManager manager, Question self);

        /**
         * When a valid answer has been given than this method would be called
         * @param manager from activity as Questionnaire Manager
         * @param self current question
         * @returns null there is no validation error and need to continue
         * else return error code as string
         * empty string is considered equivalent to null here
         */
        String PostCondition(IQuestionnaireManager manager, Question self);
    }

    public interface OnQuestionStateChange {
        /**
         * When question state is changed this event would be fired
         * @param self reference to question object on which event is invoked
         * @param fromState existing state of question from which it will set to toState
         * @param toState the new state of question after change
         */
        void onChange(Question self, QuestionStates fromState, QuestionStates toState);
    }
}
