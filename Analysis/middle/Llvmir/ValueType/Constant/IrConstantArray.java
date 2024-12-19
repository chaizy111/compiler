package Analysis.middle.Llvmir.ValueType.Constant;

import java.util.ArrayList;

public class IrConstantArray extends IrConstant{
    private ArrayList<IrConstantVal> constantVals;

    public IrConstantArray(ArrayList<IrConstantVal> arrays) {
        constantVals = arrays;
    }

    public ArrayList<IrConstantVal> getConstantVals() {
        return constantVals;
    }

    @Override
    public ArrayList<String> output() {
        StringBuilder s = new StringBuilder();
        s.append("[");
        s.append(this.getType().output().get(0));
        s.append(" ");
        s.append(constantVals.get(0).output().get(0));
        for (int i = 1; i < constantVals.size(); i++) {
            s.append(", ");
            s.append(this.getType().output().get(0));
            s.append(" ");
            s.append(constantVals.get(i).output().get(0));
        }
        s.append("]");
        ArrayList<String> res = new ArrayList<>();
        res.add(s.toString());
        return res;
    }
}
