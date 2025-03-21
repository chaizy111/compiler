package Analysis.middle.Llvmir.ValueType.Instruction;

import Analysis.middle.Llvmir.IrValue;
import Analysis.middle.Llvmir.Type.IrPointerTy;
import Analysis.middle.Llvmir.Type.IrType;
import Analysis.middle.Llvmir.ValueType.Constant.IrConstant;
import Analysis.middle.Llvmir.ValueType.Constant.IrConstantVal;

import java.util.ArrayList;

public class IrStore extends IrInstruction{
    public IrStore() {
        super();
    }

    public IrStore(IrValue operand0, IrValue operand1) {
        this.setOperand(operand0, 0);
        this.setOperand(operand1, 1);
    }

    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        IrValue l = this.getOperand(0);
        IrValue r = this.getOperand(1);
        String left;
        String right;
        if (r instanceof IrConstant) {
            right = String.valueOf(((IrConstantVal) r).getVal());
            if (l.getType() instanceof IrPointerTy) { //如果等号右边是常数，要与等号左边的类型保持一致
                r.setType(((IrPointerTy) l.getType()).getType());
            }
        } else {
            right = "%r."+r.getRegisterName();
        }
        if (r.getType() instanceof IrPointerTy && r instanceof IrConstantVal) { //会出现int类型突然变成指针的问题，不知道是怎么回事，用这个方法改
            IrType t = ((IrPointerTy) r.getType()).getType();
            r.setType(t);
        }
        if (l.getRegisterName().charAt(0) < '0' || l.getRegisterName().charAt(0) > '9') { //区分寄存器名
            left = "@"+l.getRegisterName();
        } else {
            left = "%r."+l.getRegisterName();
        }
        String s = "store " + r.getType().output().get(0) + " " + right + ", " + l.getType().output().get(0) + " " + left + "\n";
        res.add(s);
        return res;
    }
}
