package pk.gov.pbs.formbuilder.utils;

public class Range<T extends Number> {
    private T min;
    private T max;

    public Range(T min, T max){
        this.min = min;
        this.max = max;
    }

    public T getMin() {
        return min;
    }

    public T getMax() {
        return max;
    }

    public boolean isInsideInclusively(T value){
        return (value.doubleValue() <= max.doubleValue() && value.doubleValue() >= min.doubleValue());
    }

    public boolean isInsideExclusively(T value){
        return (value.doubleValue() < max.doubleValue() && value.doubleValue() > min.doubleValue());
    }
}
