package Analysis.middle.Symbol.Value;

import java.util.ArrayList;

public class ArrayValue extends Value {
    private ArrayList<Integer> arrayList;

    public ArrayValue() {
        super();
        arrayList = new ArrayList<>();
    }

    public void addItem(int item) {
        arrayList.add(item);
    }

    public ArrayList<Integer> getArray(int size) {
        ArrayList<Integer> res = new ArrayList<>(arrayList);
        if(size > arrayList.size()) {
            for (int i = arrayList.size(); i < size; i++) {
                res.add(0);
            }
        }
        return res;
    }
}
