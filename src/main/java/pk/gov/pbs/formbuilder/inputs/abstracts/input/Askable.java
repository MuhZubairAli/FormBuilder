package pk.gov.pbs.formbuilder.inputs.abstracts.input;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import pk.gov.pbs.formbuilder.core.ViewModelFormSection;
import pk.gov.pbs.formbuilder.core.LabelProvider;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.toolkits.NavigationToolkit;

public abstract class Askable {
    protected ValueStore[] mAnswers;
    final protected String mIndex;
    final protected int mResource;

    protected boolean IS_UNLOCKED = true;
    private boolean ANIMATE_CHANGES = false;
    private boolean IS_VISIBLE = true;

    protected Askable(String index, int resId) {
        this.mIndex = index;
        this.mResource = resId;
    }

    /**
     * Askable state getters
     * if askable is not visible it mean that askable
     * is irrelevant and is not considered in up for validation
     * @return boolean
     */
    public boolean isVisible(){
        return IS_VISIBLE;
    }
    public boolean isUnlocked(){
        return IS_UNLOCKED;
    }

    public boolean isAnimationEnabled(){
        return ANIMATE_CHANGES;
    }
    /**
     * Askable resource getters
     * @return index of the askable as string
     */
    public String getIndex() {
        return mIndex;
    }

    protected int getResId() {
        return mResource;
    }

    /**
     * Askable behaviour setters
     * @param enableAnimation
     */
    public void setAnimateChanges(boolean enableAnimation){
        ANIMATE_CHANGES = enableAnimation;
    }

    public void hide(){
        if(!IS_VISIBLE)
            return;

        if(!ANIMATE_CHANGES){
            quickHide();
            return;
        }

        if(getInputView() != null){
            getInputView().post(()->{
                getInputView()
                        .animate()
                        .setDuration(Constants.ANIM_DURATION)
                        .alpha(0f)
                        .translationYBy(Constants.ANIM_HIDE_TRANSLATE_Y)
                        .withEndAction(()->{
                            getInputView().setTranslationY(Constants.ANIM_SHOW_TRANSLATE_Y);
                            getInputView().setVisibility(ViewGroup.GONE);
                        });
            });
            IS_VISIBLE = false;
        }
    }
    public void show(){
        if(IS_VISIBLE)
            return;

        if(!ANIMATE_CHANGES){
            quickShow();
            return;
        }

        if(getInputView() != null){
            getInputView().post(()->{
                getInputView()
                        .animate()
                        .setDuration(Constants.ANIM_DURATION)
                        .alpha(1f)
                        .translationYBy(Constants.ANIM_HIDE_TRANSLATE_Y)
                        .withStartAction(()->{
                            getInputView().setVisibility(ViewGroup.VISIBLE);
                        });
            });
            IS_VISIBLE = true;
        }
    }

    protected void quickShow() {
        if(IS_VISIBLE)
            return;

        if(getInputView() != null){
            getInputView().setAlpha(1f);
            getInputView().setVisibility(View.VISIBLE);
        }

        IS_VISIBLE = true;
    }

    protected void quickHide() {
        if(!IS_VISIBLE)
            return;

        if(getInputView() != null){
            getInputView().setAlpha(0f);
            getInputView().setVisibility(View.GONE);
        }

        IS_VISIBLE = false;
    }

    public ValueStore[] getAnswers(){
        return mAnswers;
    }
    public ValueStore getAnswer(int index){
        return mAnswers[index];
    }

    /**
     * set answer to askable by accepting array
     * it's twin method is importAnswer which uses model to
     * set answers in askable
     * @param answers new answer values
     * @return true if successful
     */
    public boolean setAnswers(ValueStore... answers){
        if (answers != null && answers.length == mAnswers.length){
            System.arraycopy(answers, 0, mAnswers, 0, answers.length);
            return true;
        }
        return false;
    }

    public boolean setAnswer(int index, ValueStore answer){
        if (index < mAnswers.length){
            mAnswers[index] = answer;
            return true;
        }
        return false;
    }

    public void disable(){
        if (getInputView() != null) {
            disableRecursive(getInputView());
        }
    }

    private void disableRecursive(View view){
        if (view instanceof ViewGroup){
            ViewGroup viewGroup = (ViewGroup) view;
            if (viewGroup.getChildCount() > 0) {
                for (int i=0; i<viewGroup.getChildCount(); i++)
                    disableRecursive(viewGroup.getChildAt(i));
                return;
            }
        }

        view.setClickable(false);
        view.setFocusable(false);
        view.setFocusableInTouchMode(false);
    }

    /**
     * this variant of Askable.loadAnswerIntoInputView(), it accepts new answers as array
     * instead of using internal mAnswers array
     * @param answers new answers
     * @param viewModel for internal refresh of view
     * @return true if successful
     */
    public boolean loadAnswerIntoInputView(@Nullable ViewModelFormSection viewModel, ValueStore... answers){
        if (mAnswers.length == answers.length){
            boolean isSameAnswer = false;
            for (int i = 0; i < mAnswers.length; i++){
                isSameAnswer = nse(mAnswers[i], answers[i]);
                if (!isSameAnswer){
                    break;
                }
            }
            if (isSameAnswer)
                return true;
        } else
            return false;

        if (getInputView() != null) {
            getInputView().post(()->{
                reset();
                getInputView().post(()->{
                    if (setAnswers(answers)) {
                        getInputView().post(()->{
                            loadAnswerIntoInputView(viewModel);
                        });
                    }
                });
            });
            return true;
        }
        return false;
    }

    /***************************************************************
     *                 Askable core abstract functions             *
     ***************************************************************/

    /**
     * This method load the mAnswers array onto inputViews
     * previously it was referred to as setupLoadedAnswer() with
     * new answers array. but now it uses mAnswers array to fulfil
     * the purpose of keeping mAnswers array and view in sync
     * @param viewModel to get data from database for input view i.e specifiableLable or annexure desc
     * @return true if successful
     */
    public abstract boolean loadAnswerIntoInputView(@Nullable ViewModelFormSection viewModel);

    public abstract boolean hasAnswer();

    public abstract boolean lock();
    public abstract boolean unlock();
    public abstract void reset();

    public abstract boolean requestFocus();
    public abstract boolean hasFocus();

    public abstract View getInputView();

    public abstract void bindListeners(ActivityFormSection context, Runnable onAnswerEvent);
    public abstract void inflate(LayoutInflater inflater, LabelProvider labels, ViewGroup parent);

    /**
     * this method setup the behaviour of navigating through inputs as well
     * as asking next question. like press enter on last KeyboardInput ask next question etc
     * @param toolkit navigation toolkit
     */
    public abstract void setupImeAction(NavigationToolkit toolkit);

    /**
     * Export of answers is intended for models
     * it will directly copy data to section model
     * it will enable export of answers without taking care of indexes
     * @param model target model object into which data will be copied
     * @throws NoSuchFieldException in case model don't have matching field
     * @throws IllegalAccessException in case model field is not public
     */
    public abstract void exportAnswer(Section model) throws NoSuchFieldException, IllegalAccessException;

    /**
     * Import of answers is from models
     * it will directly copy data from section model
     * it will enable import of answers without taking care of indexes
     * @param model target model object from which data will be copied
     * @throws NoSuchFieldException in case model don't have matching field
     * @throws IllegalAccessException in case model field is not public
     */
    public abstract boolean importAnswer(Section model) throws NoSuchFieldException, IllegalAccessException;

    /**
     * Null Safe Equals
     * this method and it's overrides compare ValueStore and Strings
     * for equality being null safe
     * @param value1 first object
     * @param value2 second object
     * @return true if equals
     */
    protected boolean nse(ValueStore value1, ValueStore value2){
        if (value1 == null && value2 == null)
            return true;
        else if (value1 == null || value2 == null)
            return false;
        return value1.equalsIgnoreCase(value2);
    }

    protected boolean nse(String value1, String value2){
        if (value1 == null && value2 == null)
            return true;
        else if (value1 == null || value2 == null)
            return false;
        return value1.equalsIgnoreCase(value2);
    }

    protected boolean nse(String value1, ValueStore value2){
        if (value1 == null && value2 == null)
            return true;
        else if (value1 == null || value2 == null)
            return false;
        return value1.equalsIgnoreCase(value2.toString());
    }

    protected boolean nse(ValueStore value1, String value2){
        if (value1 == null && value2 == null)
            return true;
        else if (value1 == null || value2 == null)
            return false;
        return value1.equalsIgnoreCase(value2);
    }
}
