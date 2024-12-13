package Llvmir.ValueType.Instruction;

import Llvmir.IrValue;
import Llvmir.Type.IrType;
import Llvmir.ValueType.Constant.IrConstantVal;

import java.util.ArrayList;

public class IrIcmp extends IrInstruction{
    private String kind;

    public IrIcmp(String kind, int rname, IrType type) {
        this.kind = kind;
        this.setRegisterName("%" + rname);
        this.setType(type);
    }

    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        IrValue l = this.getOperand(0);
        IrValue r = this.getOperand(1);
        String s;
        String left;
        String right;
        if (l instanceof IrConstantVal) {
            left = String.valueOf(((IrConstantVal) l).getVal());
        } else {
            left = l.getRegisterName();
        }
        if (r instanceof IrConstantVal) {
            right = String.valueOf(((IrConstantVal) r).getVal());
        } else {
            right = r.getRegisterName();
        }
        s = this.getRegisterName() + " = icmp " + kind + " " + l.getType().output().get(0) + " " + left + ", " + right + "\n";
        res.add(s);
        return res;
    }
}
