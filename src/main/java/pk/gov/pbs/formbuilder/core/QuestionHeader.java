package pk.gov.pbs.formbuilder.core;

import android.graphics.Color;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.utils.FormBuilderThemeHelper;
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

    @Override
    public void initialize(ActivityCustom context, ViewGroup container) {
        ViewGroup itemView = (ViewGroup) context.getLayoutInflater()
                .inflate(R.layout.item_layout_question, container, false);

        TextView questionStatement = itemView.findViewById(R.id.tv_question);
        if (context.getLabelProvider().hasLabel(getIndex())) {
            questionStatement.setGravity(Gravity.CENTER_HORIZONTAL);
            Spanned qHTM = Html.fromHtml(context.getLabelProvider().getLabel(getIndex()));
            questionStatement.setText(qHTM);
            questionStatement.setTextColor(Color.WHITE);
            questionStatement.setBackgroundColor(
                    FormBuilderThemeHelper.getColorByTheme(context, R.attr.colorAccentDark)
            );

        }

        itemView.findViewById(R.id.tv_question_hint).setVisibility(View.GONE);
        itemView.findViewById(R.id.question_controls).setVisibility(View.GONE);
        itemView.findViewById(R.id.container_answer).setVisibility(View.GONE);

        container.addView(itemView);
    }
}
