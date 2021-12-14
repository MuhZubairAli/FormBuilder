package pk.gov.pbs.formbuilder.pojos;

import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.utils.Range;

public class Jump {
    private Integer mSection; //class of target section activity because SectionsModels class is deprecated
    private String mStart;
    private String mEnd;
    private ValueStore mValue; // if answer of question is this value than Jump is applicable
    private Range<Integer> mRange; //Range for value i,e if value is between 1 and 10 (for checking if applicable based on answer)

    public Jump(ValueStore value, String start, String end, Integer section) {
        this.mValue = value;
        this.mSection = section;
        this.mStart = start;
        this.mEnd = end;
    }

    public Jump(ValueStore value, String start, Integer section) {
        this(value,start,null,section);
    }

    public Jump(Range<Integer> range, String start, String end, Integer section) {
        this.mRange = range;
        this.mSection = section;
        this.mStart = start;
        this.mEnd = end;
    }

    public Jump(Range<Integer> range, String start, Integer section) {
        this(range, start, null, section);
    }

    public Jump(ValueStore value, String start) {
        this(value,start,null,null);
    }

    public Jump(ValueStore value, String start, String end) {
        this(value, start, end, null);
    }

    public Jump(Range<Integer> range, String start) {
        this(range, start, null, null);
    }

    public Jump(Range<Integer> range, String start, String end) {
        this(range, start, end, null);
    }

    public Jump(ValueStore value) {
        this(value, null, null, null);
    }

    public Integer getSection() {
        return mSection;
    }

    public String getStart() {
        return mStart;
    }

    public String getEnd() {
        return mEnd;
    }

    public ValueStore getValue() {
        return mValue;
    }

    public boolean isActionable(){
        return mSection != null || mStart != null || mEnd != null;
    }

    public boolean isApplicable(ValueStore data){
        if(data != null) {
            if (mValue != null)
                return data.equalsIgnoreCase(mValue);
            else if (mRange != null)
                return (mRange.getMax() >= data.toInt() && mRange.getMin() <= data.toInt());
        }
        return false;
    }
}
