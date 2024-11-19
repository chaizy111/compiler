package Llvmir.ValueType.Function;

public class CntUtils {
    private int count;

    public CntUtils() {
        count = 0;
    }

    public int getCount() {
        int temp = count;
        count++;
        return temp;
    }
}
