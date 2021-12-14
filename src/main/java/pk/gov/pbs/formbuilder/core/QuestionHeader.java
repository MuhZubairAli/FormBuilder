package pk.gov.pbs.formbuilder.core;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.inputs.abstracts.adapters.AskableAdapter;
import pk.gov.pbs.formbuilder.meta.QuestionStates;

public class QuestionHeader extends Question {
    public QuestionHeader(String index) {
        super(index, null);
        setState(QuestionStates.READ_ONLY);
    }

    @Override
    public AskableAdapter getAdapter() {
        return null;
    }

    @Override
    public void loadModel(Section model) {
    }

    @Override
    public boolean loadAnswer(@NonNull String abIndex, ValueStore... answer) {
        return false;
    }

    @Override
    public ValueStore[][] getAnswers() {
        return null;
    }

    @Override
    public boolean lock() {
        return true;
    }

    @Override
    public boolean unlock(boolean force) {
        return true;
    }

    @Override
    public void reset() {
    }

    @Override
    public boolean requestInputFocus() {
        return false;
    }

    @Override
    public boolean requestFocus() {
        return false;
    }

    @Override
    public String ExecPreCondition(QuestionnaireManager qm, Question self) {
        return null;
    }

    @Override
    public String ExecPostCondition(QuestionnaireManager qm, Question self) {
        return null;
    }

    @Override
    public boolean validateAnswer() {
        return true;
    }

    @Override
    public String getValidationErrorStatement() {
        return null;
    }

    @Override
    public String getValidationRuleStatement() {
        return null;
    }

    @Override
    public void flash() {
    }

    @Override
    public void initialize(ActivityFormSection context, ViewGroup container) {
        ViewGroup parent = (ViewGroup) container.getParent().getParent();
        parent.findViewById(R.id.question_controls).setVisibility(View.GONE);
        parent.findViewById(R.id.container_answer).setVisibility(View.GONE);
//        ThemeUtils.applyThemedDrawableToView(parent, R.attr.bgCardLocked);
    }
}
