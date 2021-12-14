package pk.gov.pbs.formbuilder.pojos;

public class ItemSpinnerSection {
    protected String mLabel;
    protected Integer mSection;

    public ItemSpinnerSection(String sectionLabel, Integer sectionNumber) {
        mLabel = sectionLabel;
        mSection = sectionNumber;
    }

    public ItemSpinnerSection(String sectionLabel) {
        mLabel = sectionLabel;
        mSection = null;
    }

    @Override
    public String toString() {
        return mLabel;
    }

    public Integer getSectionNumber() {
        return mSection;
    }
}
