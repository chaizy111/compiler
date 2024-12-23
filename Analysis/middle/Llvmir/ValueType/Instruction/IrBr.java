package Analysis.middle.Llvmir.ValueType.Instruction;

import Analysis.middle.Llvmir.IrValue;

import java.util.ArrayList;

public class IrBr extends IrInstruction{
    private IrLabel iftrue;
    private IrLabel iffalse;

    public IrBr(IrLabel iftrue, IrLabel iffalse, IrValue operand0) {
        this.iftrue = iftrue;
        this.iffalse = iffalse;
        this.setOperand(operand0, 0);
    }

    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        IrValue cond = this.getOperand(0);
        String s = "br i1 " + "%r."+cond.getRegisterName() + ", label " + iftrue.getLabelName() + ", label " + iffalse.getLabelName() + "\n";
        res.add(s);
        return res;
    }
}
