package Llvmir.Type;

import java.util.ArrayList;

public class IrCharTy extends IrType {
    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        res.add("i8");
        return res;
    }
}
