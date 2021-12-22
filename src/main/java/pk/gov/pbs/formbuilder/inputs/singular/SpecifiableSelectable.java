package pk.gov.pbs.formbuilder.inputs.singular;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

import pk.gov.pbs.database.DatabaseUtils;
import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.core.ViewModelFormSection;
import pk.gov.pbs.formbuilder.inputs.grouped.GroupInputRBI;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.models.Option;
import pk.gov.pbs.formbuilder.pojos.DatumIdentifier;
import pk.gov.pbs.formbuilder.pojos.OptionTuple;
import pk.gov.pbs.utils.ThemeUtils;

public abstract class SpecifiableSelectable extends Selectable {
    protected DatumIdentifier identifier;
    private boolean mIgnoreDialogueCall = false;
    private HashMap<String,Long> mOptions;
    private List<String> suggestions;

    public SpecifiableSelectable(String index, DatumIdentifier identifier, int resId) {
        super(index, identifier.value, resId);
        this.identifier = identifier;
        mAnswers = new ValueStore[2];
    }

    protected String getCodeDescription(Long code){
        if (mOptions != null && mOptions.size() > 0){
            for (String desc : mOptions.keySet())
                if (mOptions.get(desc).equals(code))
                    return desc;
        }
        return null;
    }

    public DatumIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public boolean hasAnswer() {
        return getAnswers() != null && getAnswer(0) != null && getAnswer(1) != null;
    }

    @Override
    public boolean loadAnswerIntoInputView(ViewModelFormSection viewModel) {
        if (getInputView() != null && hasAnswer()){
            mIgnoreDialogueCall = true;
            Spanned opString = Html.fromHtml(
                    viewModel.getLabelForSpecifiableSelectable(this)
            );
            getInputView().setChecked(true);
            getInputView().setText(opString);
            mIgnoreDialogueCall = false;
            return true;
        }
        return false;
    }

    protected String appendValueToLabel(String label){
        return getValue().toString() + ". " + label;
    }

    @Override
    public void bindListeners(ActivityFormSection context, Runnable onAnswerEvent) {
        if (mOptions == null || suggestions == null){
            mOptions = new HashMap<>();
            suggestions = new ArrayList<>();

            List<OptionTuple> tuples = context.getViewModel()
                    .getFormBuilderRepository()
                    .getOptionsDao()
                    .getOptionsByIdentifier(getIdentifier());

            if (tuples != null) {
                for (OptionTuple ot : tuples) {
                    if (ot.sid == null)
                        mOptions.put(ot.desc, ot.aid);
                    else
                        mOptions.put(ot.desc, ot.sid);

                    suggestions.add(ot.desc);
                }
            }
        }

        Spanned opString = Html.fromHtml(
                context.getString(
                        R.string.specifiable_selectable_label_template,
                        appendValueToLabel(context.getLabelProvider().getLabel(getIndex())),
                        context.getString(R.string.s_s_l_2nd_param_placeholder)
                )
        );

        getInputView().setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showSpecifiableInputDialog(context, onAnswerEvent);
            } else {
                setAnswers(null, null);
                getInputView().setText(opString);
            }

            if (onAnswerEvent != null)
                onAnswerEvent.run();
        });
    }

    private void showSpecifiableInputDialog(ActivityFormSection context, Runnable onAnswerEvent){
        if (mIgnoreDialogueCall)
            return;

        View mDialogue = context.getLayoutInflater().inflate(R.layout.custom_dialogue_alert_input_actv, null);
        AutoCompleteTextView input = mDialogue.findViewById(R.id.kbi);
        input.requestFocus();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,android.R.layout.simple_spinner_dropdown_item, suggestions);
        input.setAdapter(adapter);

        Spanned htm = Html.fromHtml(
                context.getString(
                        R.string.concat_specifiable_question
                        , context.getLabelProvider()
                                .getLabel(getIndex())
                        , context.getString(R.string.specify_option_label)
                )
        );

        TextView tvLabel =  mDialogue.findViewById(R.id.tv);
        ThemeUtils.setupTextViewStylesByLocale(context.getLabelProvider().getLocale(), tvLabel);
        tvLabel.setText(htm);

        AlertDialog alert = context.getUXToolkit().getDialogBuilder()
                .setView(mDialogue)
                .setCancelable(false)
                .setPositiveButton(
                        context
                                .getResources()
                                .getString(R.string.specify_option_action_btn_title)
                        , null
                )
                .setNegativeButton(
                        context
                                .getResources()
                                .getString(R.string.label_btn_cancel)
                        ,(dialog, id) -> {
                            long insertId = context
                                    .getViewModel()
                                    .getFormBuilderRepository()
                                    .getOptionsDao()
                                    .insertZeroOption(getIdentifier());

                            setAnswers(getValue(), new ValueStore(insertId));
                            Spanned optionHtm = Html.fromHtml(
                                    context.getString(
                                            R.string.specifiable_selectable_label_template
                                            , appendValueToLabel(context.getLabelProvider().getLabel(getIndex()))
                                            , context.getString(R.string.s_s_l_2nd_default_value)
                                    )
                            );
                            getInputView().setText(optionHtm);
                            getInputView().setChecked(true);

                            if (onAnswerEvent != null) {
                                GroupInputRBI.IGNORE_CLEAR_CHECK_ALL = true;
                                onAnswerEvent.run();
                            }

                            dialog.cancel();
                        })
                .create();

        alert.setOnShowListener(dialogInterface -> {
            Button button = alert.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String inputText = input.getText().toString();

                if(inputText.isEmpty()){
                    input.setError("Provide some input");
                    return;
                }else if(inputText.length() < 4){
                    input.setError("Too few characters");
                    return;
                }else if(inputText.length() > Constants.INPUT_MAX_CHARACTERS_LIMIT){
                    input.setError("Max character limit exceeded");
                    input.setText(inputText.substring(0, Constants.INPUT_MAX_CHARACTERS_LIMIT));
                    input.setSelection(Constants.INPUT_MAX_CHARACTERS_LIMIT);
                    return;
                }

                Long selectedOptionId = mOptions.get(inputText);
                if(selectedOptionId != null) {
                    setAnswers(getValue(), new ValueStore(selectedOptionId));
                }else{
                    Future<Long> insert = context
                            .getViewModel()
                            .getFormBuilderRepository()
                            .insert(new Option(getIdentifier(), inputText));
                    long insertId = DatabaseUtils.getFutureValue(insert);
                    setAnswers(getValue(), new ValueStore(insertId));
                }

                Spanned optionHtm = Html.fromHtml(
                        context.getString(
                                R.string.specifiable_selectable_label_template
                                , appendValueToLabel(context.getLabelProvider().getLabel(getIndex()))
                                , input.getText().toString()
                        )
                );

                getInputView().setText(optionHtm);
                getInputView().setChecked(true);

                if (onAnswerEvent != null) {
                    GroupInputRBI.IGNORE_CLEAR_CHECK_ALL = true;
                    onAnswerEvent.run();
                }

                alert.dismiss();
            });
        });

        alert.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        ;

        alert.show();
    }
}
