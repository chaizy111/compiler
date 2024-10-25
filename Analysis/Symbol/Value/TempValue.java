package Analysis.Symbol.Value;

import java.util.ArrayList;

public class TempValue extends Value{
    char c;
    ArrayList<Value> values;

    public TempValue() {
        this.c = ' ';
        this.values = new ArrayList<>();
    }

    public void setC(char c) {
        this.c = c;
    }

    public char getC() {
        return c;
    }

    public void addValue(Value value) {
        values.add(value);
    }

    public ArrayList<Value> getValues() {
        return values;
    }
}
