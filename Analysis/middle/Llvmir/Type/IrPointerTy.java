package Analysis.middle.Llvmir.Type;

import java.util.ArrayList;

public class IrPointerTy extends IrType {
    private IrType type;

    public IrPointerTy(IrType type) {
        this.type = type;
    }

    public IrType getType() {
        return type;
    }

    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        String s = type.output().get(0) + "*";
        res.add(s);
        return res;
    }
}
