package Analysis.middle.Llvmir.ValueType.Instruction;

import Analysis.middle.Llvmir.IrNode;

import java.util.ArrayList;

public class IrLabel extends IrInstruction implements IrNode {
    public IrLabel(int lname) {
        this.setRegisterName(String.valueOf(lname));
    }

    public IrLabel(){

    }

    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        String s = "Label." + this.getRegisterName() + ":\n";
        res.add(s);
        return res;
    }

    public String getLabelName() {
        return "%Label." + this.getRegisterName();
    }
}
