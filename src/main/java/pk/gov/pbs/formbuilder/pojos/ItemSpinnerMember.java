package pk.gov.pbs.formbuilder.pojos;

import androidx.annotation.NonNull;

import pk.gov.pbs.formbuilder.models.RosterSection;

public class ItemSpinnerMember {
    private String mName;
    private final RosterSection mModel;
    private static LabelMaker mLabelMaker = null;

    public ItemSpinnerMember(RosterSection model){
        mModel = model;
    }

    public ItemSpinnerMember(RosterSection model, LabelMaker labelMaker){
        if (mLabelMaker == null)
            mLabelMaker = labelMaker;
        mModel = model;
    }

    public ItemSpinnerMember(String name){
        mName = name;
        mModel = null;
    }

    private String makeSpinnerLabel(){
        if (mLabelMaker != null)
            return mLabelMaker.makeLabel(mModel);

        StringBuilder label = new StringBuilder();
        if (mModel.getMemberId() < 10)
            label.append("0").append(mModel.getMemberId());
        else
            label.append(mModel.getMemberId().toString());

        return label.append(". ").append(mModel.getName()).toString();
    }

    @NonNull
    @Override
    public String toString() {
        if (mModel != null)
            return makeSpinnerLabel();
        return mName;
    }

    public RosterSection getModel() {
        return mModel;
    }

    public interface LabelMaker {
        String makeLabel(RosterSection memberSection);
    }
}
