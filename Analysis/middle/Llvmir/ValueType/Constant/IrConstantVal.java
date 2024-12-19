package Analysis.middle.Llvmir.ValueType.Constant;

import java.util.ArrayList;

public class IrConstantVal extends IrConstant {
    private int val;

    public IrConstantVal(int val) {
        super();
        this.val = val;
    }

    public void setVal(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        res.add(String.valueOf(val));
        return res;
    }

}
