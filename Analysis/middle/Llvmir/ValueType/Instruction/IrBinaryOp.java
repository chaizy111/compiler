package Analysis.middle.Llvmir.ValueType.Instruction;

import Analysis.middle.Llvmir.IrValue;
import Analysis.middle.Llvmir.Type.IrType;
import Analysis.middle.Llvmir.ValueType.Constant.IrConstantVal;

import java.util.ArrayList;

public class IrBinaryOp extends IrInstruction{
    private IrInstructionType.irIntructionType operationTy;

    public IrBinaryOp() {
        super();
    }

    public IrBinaryOp(int rname, IrType type, IrValue operand0, IrValue operand1) {
        this.setRegisterName(String.valueOf(rname));
        this.setType(type);
        this.setOperand(operand0, 0);
        this.setOperand(operand1, 1);
    }

    public void setOperationTy(IrInstructionType.irIntructionType operationTy) {
        this.operationTy = operationTy;
    }

    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        IrValue l = this.getOperand(0);
        IrValue r = this.getOperand(1);
        String s;
        String left;
        String right;
        IrType t = null;
        if (l instanceof IrConstantVal) {
            left = String.valueOf(((IrConstantVal) l).getVal());
        } else {
            left = "%r."+l.getRegisterName();
            t = l.getType();
        }
        if (r instanceof IrConstantVal) {
            right = String.valueOf(((IrConstantVal) r).getVal());
        } else {
            right = "%r."+r.getRegisterName();
            t = r.getType();
        }
        if (this.getType() != null) {
            t = this.getType();
        }
        switch (operationTy) {
            case Add:
                s = "%r."+this.getRegisterName() + " = add nsw " + t.output().get(0) + " " + left + ", " + right + "\n";
                break;
            case Sub:
                s = "%r."+this.getRegisterName() + " = sub nsw " + t.output().get(0) + " " + left + ", " + right + "\n";
                break;
            case Mul:
                s = "%r."+this.getRegisterName() + " = mul nsw " + t.output().get(0) + " " + left + ", " + right + "\n";
                break;
            case Div:
                s = "%r."+this.getRegisterName() + " = sdiv " + t.output().get(0) + " " + left + ", " + right + "\n";
                break;
            case Mod:
                s = "%r."+this.getRegisterName() + " = srem " + t.output().get(0) + " " + left + ", " + right + "\n";
                break;
            default:
                s = "wrong";
        }
        res.add(s);
        return res;
    }
}
