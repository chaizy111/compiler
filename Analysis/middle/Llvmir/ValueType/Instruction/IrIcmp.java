package Analysis.middle.Llvmir.ValueType.Instruction;

import Analysis.middle.Llvmir.IrValue;
import Analysis.middle.Llvmir.Type.IrType;
import Analysis.middle.Llvmir.ValueType.Constant.IrConstantVal;

import java.util.ArrayList;

public class IrIcmp extends IrInstruction{
    private String kind;

    public IrIcmp(String kind, int rname, IrType type, IrValue operand0, IrValue operand1) {
        this.kind = kind;
        this.setRegisterName(String.valueOf(rname));
        this.setType(type);
        this.setOperand(operand0, 0);
        this.setOperand(operand1, 1);
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
        IrType t; //t的设置防止保证左右出现常数时kind后输出的类型名的准确
        if (l instanceof IrConstantVal) {
            left = String.valueOf(((IrConstantVal) l).getVal());
            t = r.getType();
        } else {
            left = "%r."+l.getRegisterName();
            t = l.getType();
        }
        if (r instanceof IrConstantVal) {
            right = String.valueOf(((IrConstantVal) r).getVal());
            t = l.getType();
        } else {
            right = "%r."+r.getRegisterName();
            t = r.getType();
        }
        s = "%r."+this.getRegisterName() + " = icmp " + kind + " " + t.output().get(0) + " " + left + ", " + right + "\n";
        res.add(s);
        return res;
    }
}
