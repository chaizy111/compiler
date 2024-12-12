package Llvmir.ValueType.Instruction;

import java.util.ArrayList;

public class IrGotoBr extends IrInstruction{
    IrLabel label;

    public IrGotoBr(IrLabel label) {
        this.label = label;
    }

    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        String s = "br label " + label.getLabelName() + "\n";
        res.add(s);
        return res;
    }
}
