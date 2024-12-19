package Analysis.middle.Llvmir.Type;

import java.util.ArrayList;

public class IrVoidTy extends IrType {
    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        res.add("void");
        return res;
    }
}
