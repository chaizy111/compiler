package Analysis.middle.Llvmir.ValueType.Instruction;

import Analysis.middle.Llvmir.IrValue;

import java.util.ArrayList;

public class IrBr extends IrInstruction{
    private IrLabel iftrue;
    private IrLabel iffalse;

    public IrBr(IrLabel iftrue, IrLabel iffalse) {
        this.iftrue = iftrue;
        this.iffalse = iffalse;
    }

    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        IrValue cond = this.getOperand(0);
        String s = "br i1 " + cond.getRegisterName() + ", label " + iftrue.getLabelName() + ", label " + iffalse.getLabelName() + "\n";
        res.add(s);
        return res;
    }
}
