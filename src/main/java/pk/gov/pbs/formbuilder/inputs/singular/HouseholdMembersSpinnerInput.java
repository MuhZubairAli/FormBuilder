package pk.gov.pbs.formbuilder.inputs.singular;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.core.ViewModelFormSection;
import pk.gov.pbs.formbuilder.core.LabelProvider;
import pk.gov.pbs.formbuilder.core.ActivityMemberSection;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.models.RosterSection;
import pk.gov.pbs.formbuilder.pojos.ItemSpinnerMember;

public class HouseholdMembersSpinnerInput extends SpinnerInput{
    private List<ItemSpinnerMember> mExtraOptions;
    public HouseholdMembersSpinnerInput(String index) {
        super(index);
    }

    public HouseholdMembersSpinnerInput(String index, List<ItemSpinnerMember> extras) {
        super(index);
        mExtraOptions = extras;
    }

    @Override
    public boolean loadAnswerIntoInputView(ViewModelFormSection viewModel) {
        if(inputElement != null) {
            for (int i = 0; i < inputElement.getAdapter().getCount(); i++) {
                int sno = ((ItemSpinnerMember) inputElement.getAdapter().getItem(i)).getModel() == null ? Constants.INVALID_NUMBER :
                        ((ItemSpinnerMember) inputElement.getAdapter().getItem(i)).getModel().getMemberId();
                if (getAnswer().toInt() == sno) {
                    inputElement.setSelection(i, true);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void bindListeners(ActivityFormSection context, Runnable onAnswerEvent) {
        inputElement.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                RosterSection member = ((ItemSpinnerMember) parent.getSelectedItem()).getModel();
                if (member != null) {
                    if (getAnswer() != null) {
                        getAnswer().setValue(member.getMemberId());
                    } else {
                        setAnswer(new ValueStore(member.getMemberId()));
                    }
                } else
                    setAnswer(null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                setAnswer(null);
            }
        });

        List<ItemSpinnerMember> optUser = new ArrayList<>();
        optUser.add(new ItemSpinnerMember("Household Members"));
        if (context.getViewModel().getHouseholdMembers().size() > 0) {
            for (RosterSection m : context.getViewModel().getHouseholdMembers()) {
                optUser.add(new ItemSpinnerMember(m, context.getLabelMakerSpinnerMembers()));
            }

            if (mExtraOptions != null && mExtraOptions.size() > 0){
                optUser.addAll(mExtraOptions);
            }
        }

        ArrayAdapter<ItemSpinnerMember> adapter = new ArrayAdapter<ItemSpinnerMember>(context, R.layout.item_list_sp, optUser);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputElement.setAdapter(adapter);
    }

    @Override
    public void inflate(LayoutInflater inflater, LabelProvider labels, ViewGroup parent) {
        Spinner item;
        if(getInputView() == null) {
            item = (Spinner) inflater.inflate(getResId(), parent, false);
            inputElement = item;
        }else {
            item = getInputView();
            ((ViewGroup) item.getParent()).removeView(item);
        }
        parent.addView(item);
    }
}
