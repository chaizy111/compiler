package Llvmir.ValueType.Instruction;

import Llvmir.IrValue;
import Llvmir.Type.IrType;
import Llvmir.ValueType.Constant.IrConstantVal;

import java.util.ArrayList;

public class IrBinaryOp extends IrInstruction{
    private IrInstructionType.irIntructionType operationTy;

    public IrBinaryOp() {
        super();
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
            left = l.getRegisterName();
            t = l.getType();
        }
        if (r instanceof IrConstantVal) {
            right = String.valueOf(((IrConstantVal) r).getVal());
        } else {
            right = r.getRegisterName();
            t = r.getType();
        }

        switch (operationTy) {
            case Add:
                s = this.getRegisterName() + " = add nsw " + t.output().get(0) + " " + left + ", " + right + "\n";
                break;
            case Sub:
                s = this.getRegisterName() + " = sub nsw " + t.output().get(0) + " " + left + ", " + right + "\n";
                break;
            case Mul:
                s = this.getRegisterName() + " = mul nsw " + t.output().get(0) + " " + left + ", " + right + "\n";
                break;
            case Div:
                s = this.getRegisterName() + " = sdiv " + t.output().get(0) + " " + left + ", " + right + "\n";
                break;
            case Mod:
                s = this.getRegisterName() + " = srem " + t.output().get(0) + " " + left + ", " + right + "\n";
                break;
            default:
                s = "wrong";
        }
        res.add(s);
        return res;
    }
}
