package Llvmir.ValueType.Constant;

import java.util.ArrayList;

public class IrConstantVal extends IrConstant {
    private int val;

    public IrConstantVal(int val) {
        super();
        this.val = val;
    }

    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        res.add(String.valueOf(val));
        return res;
    }

}
