package pk.gov.pbs.formbuilder.pojos;

import androidx.annotation.NonNull;

import pk.gov.pbs.formbuilder.models.IterativeMemberSection;

public class ItemSpinnerIterativeMember {
    private String mName;
    private IterativeMemberSection mModel;
    private static LabelMaker mLabelMaker;

    public ItemSpinnerIterativeMember(IterativeMemberSection mModel, LabelMaker labelMaker) {
        if (mLabelMaker == null)
            mLabelMaker = labelMaker;
        this.mModel = mModel;
    }

    public ItemSpinnerIterativeMember(IterativeMemberSection mModel) {
        this.mModel = mModel;
    }

    public ItemSpinnerIterativeMember(String mName) {
        this.mName = mName;
    }

    protected String makeSpinnerLabel(){
        if (mLabelMaker != null)
            return mLabelMaker.makeLabel(mModel);

        StringBuilder label = new StringBuilder();
        label.append("Iteration #");
        if (mModel.getIterationNumber() < 10)
            label.append("0").append(mModel.getIterationNumber());
        else
            label.append(mModel.getIterationNumber().toString());

        return label.toString();
    }

    @NonNull
    @Override
    public String toString() {
        if (mModel != null)
            return makeSpinnerLabel();
        return mName;
    }

    public IterativeMemberSection getModel(){
        return mModel;
    }

    public interface LabelMaker {
        String makeLabel(IterativeMemberSection iterativeMemberSection);
    }
}
