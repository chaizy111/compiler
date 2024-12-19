package Analysis.middle.Llvmir.Type;

import java.util.ArrayList;

public class IrIntegerTy extends IrType {
    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        res.add("i32");
        return res;
    }
}
