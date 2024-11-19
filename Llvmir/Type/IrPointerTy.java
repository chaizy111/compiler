package Llvmir.Type;

import java.util.ArrayList;

public class IrPointerTy extends IrType {
    private IrType type;

    public IrPointerTy() {
        type = null;
    }

    public void setType(IrType type) {
        this.type = type;
    }

    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        String s = type.output().get(0) + "*";
        res.add(s);
        return res;
    }
}
