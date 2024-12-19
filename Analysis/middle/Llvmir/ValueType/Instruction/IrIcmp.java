package Analysis.middle.Llvmir.ValueType.Instruction;

import Analysis.middle.Llvmir.IrValue;
import Analysis.middle.Llvmir.Type.IrType;
import Analysis.middle.Llvmir.ValueType.Constant.IrConstantVal;

import java.util.ArrayList;

public class IrIcmp extends IrInstruction{
    private String kind;

    public IrIcmp(String kind, int rname, IrType type) {
        this.kind = kind;
        this.setRegisterName("%" + rname);
        this.setType(type);
    }

    public IrIcmp(IrIcmp icmp){
        this.kind = icmp.getKind();
        this.setRegisterName(icmp.getRegisterName());
        this.setType(icmp.getType());
        this.setOperand(icmp.getOperand(0), 0);
        this.setOperand(icmp.getOperand(1), 1);
    }

    public String getKind() {
        return kind;
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
