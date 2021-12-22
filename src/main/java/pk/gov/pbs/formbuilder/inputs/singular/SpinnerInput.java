package pk.gov.pbs.formbuilder.inputs.singular;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pk.gov.pbs.formbuilder.core.ViewModelFormSection;
import pk.gov.pbs.formbuilder.core.LabelProvider;
import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.SingularInput;
import pk.gov.pbs.utils.ThemeUtils;

public class SpinnerInput extends SingularInput {
    protected Spinner inputElement;
    private List<String> options;

    public SpinnerInput(String index){
        super(index, R.layout.input_sp);
        mAnswers = new ValueStore[1];
    }

    public SpinnerInput(String index, List<String> options){
        this(index);
        this.options = options;
    }

    @Override
    public boolean lock() {
        if (!IS_UNLOCKED)
            return true;

        if(inputElement != null){
            inputElement.setEnabled(false);
            IS_UNLOCKED = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean unlock() {
        if (IS_UNLOCKED)
            return true;

        if(inputElement != null){
            inputElement.setEnabled(true);
            IS_UNLOCKED = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean loadAnswerIntoInputView(ViewModelFormSection viewModel) {
        if(inputElement != null) {
            inputElement.setSelection(getAnswer().toInt(), true);
            return true;
        }
        return false;
    }

    @Override
    public void bindListeners(ActivityFormSection context, Runnable onAnswerEvent) {
        inputElement.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(getAnswer() != null){
                    getAnswer().setValue(id);
                }else {
                    setAnswer(new ValueStore(id));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                setAnswer(null);
            }
        });
    }

    public Spinner getInputView() {
        return inputElement;
    }

    @Override
    public boolean requestFocus() {
        return getInputView().requestFocus();
    }

    public List<String> getOptions(LabelProvider labelProvider) {
        if(options == null) {
            options = new ArrayList<>();
            String label = "Select Appropriate Option";
            int count = 0;
            String index;
            while (label != null) {
                options.add(label);
                index = getIndex() + "_" + (++count);
                label = labelProvider.getLabel(index);
            }
        }
        return options;
    }

    @Override
    public void reset() {
        if(getInputView() != null){
            super.setAnswer(null);
            getInputView().setSelection(0,true);

            if (!isVisible())
                quickShow();
        }
    }

    @Override
    public boolean hasFocus() {
        if (getInputView() != null)
            return getInputView().hasFocus();
        return false;
    }

    @Override
    public boolean hasAnswer() {
        return getAnswer() != null;
    }

    @Override
    public void exportAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        model.set(getIndex(), getAnswer());
    }

    @Override
    public boolean importAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        if (model.get(getIndex()) != null) {
            setAnswer(model.get(getIndex()));
            return true;
        }
        return false;
    }

    @Override
    public void inflate(LayoutInflater inflater, LabelProvider labels, ViewGroup parent) {
        Spinner item;
        if(getInputView() == null) {
            item = (Spinner) inflater.inflate(getResId(), parent, false);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(inflater.getContext(), R.layout.item_list_sp, getOptions(labels)) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView view = (TextView) super.getView(position, convertView, parent);
                    ThemeUtils.setupTextViewStylesByLocale(labels.getLocale(), view);
                    return view;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                    ThemeUtils.setupTextViewStylesByLocale(labels.getLocale(), view);
                    return view;
                }
            };

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            item.setAdapter(adapter);
            inputElement = item;
        } else {
            item = getInputView();
            ((ViewGroup) item.getParent()).removeView(item);
        }
        parent.addView(item);
    }
}
