package Llvmir.ValueType.Instruction;

import Llvmir.IrValue;
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
        String left = l.getRegisterName();
        String right;
        if (r instanceof IrConstantVal) {
            right = String.valueOf(((IrConstantVal) r).getVal());
        } else {
            right = r.getRegisterName();
        }
        switch (operationTy) {
            case Add:
                s = this.getRegisterName() + " = add " + l.getType().output().get(0) + " " + left + ", " + right + "\n";
                break;
            case Sub:
                s = this.getRegisterName() + " = sub " + l.getType().output().get(0) + " " + left + ", " + right + "\n";
                break;
            case Mul:
                s = this.getRegisterName() + " = mul " + l.getType().output().get(0) + " " + left + ", " + right + "\n";
                break;
            case Div:
                s = this.getRegisterName() + " = sdiv " + l.getType().output().get(0) + " " + left + ", " + right + "\n";
                break;
            case Mod:
                s = this.getRegisterName() + " = srem " + l.getType().output().get(0) + " " + left + ", " + right + "\n";
                break;
            default:
                s = "wrong";
                //TODO:剩下的是关于逻辑运算的指令，这次作业不涉及
        }
        res.add(s);
        return res;
    }
}
