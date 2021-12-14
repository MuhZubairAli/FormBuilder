package pk.gov.pbs.formbuilder.inputs.singular.adapters;

import android.view.ViewGroup;

import java.util.ArrayList;
import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.core.Question;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.SingularInput;
import pk.gov.pbs.formbuilder.inputs.abstracts.adapters.SingularInputAdapter;
import pk.gov.pbs.formbuilder.validator.Validator;


public class KeyboardInputAdapter extends SingularInputAdapter {
    public KeyboardInputAdapter(SingularInput[] singularInputs) {
        super(R.layout.container_ll, singularInputs);
    }

    public KeyboardInputAdapter(SingularInput[] singularInputs, Validator validator) {
        super(R.layout.container_ll, singularInputs, validator);
    }

    public void init(ActivityFormSection context, ViewGroup container, Question question) {
        super.init(context, container, question);
        mSingularInputs[mSingularInputs.length-1]
                .setupImeAction(context.getNavigationToolkit());
    }

    @Override
    public ValueStore[][] getAnswers() {
        ArrayList<ValueStore[]> answers = new ArrayList<>();
        for(SingularInput ab : getAskables()) {
            if(ab.hasAnswer()){
                answers.add(ab.getAnswers());
            }
        }
        if(answers.size() > 0) {
            ValueStore[][] stores = new ValueStore[answers.size()][];
            answers.toArray(stores);
            return stores;
        }

        return null;
    }
}
