package Llvmir.Type;

import java.util.ArrayList;

public class IrBooleanTy extends IrType{
    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        res.add("i1");
        return res;
    }
}
