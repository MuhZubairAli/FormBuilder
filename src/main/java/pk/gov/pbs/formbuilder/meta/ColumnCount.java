package pk.gov.pbs.formbuilder.meta;

public enum ColumnCount {
    DOUBLE(2),
    TRIPLE(3),
    QUADRUPLE(4),
    QUINTUPLE(5);

    private final int value;

    ColumnCount(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}