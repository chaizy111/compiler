package Llvmir.ValueType.Instruction;

import Llvmir.IrValue;
import Llvmir.Type.IrPointerTy;
import Llvmir.ValueType.Constant.IrConstantVal;

import java.util.ArrayList;

public class IrStore extends IrInstruction{
    public IrStore() {
        super();
    }

    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        IrValue l = this.getOperand(0);
        IrValue r = this.getOperand(1);
        String right;
        if (r instanceof IrConstantVal) {
            right = String.valueOf(((IrConstantVal) r).getVal());
        } else {
            right = r.getRegisterName();
        }
        IrPointerTy t = new IrPointerTy(); //为了保证store ty1, ty2*中ty1==ty2我们统一用ty1的类型
        t.setType(l.getType());
        String s = "store " + l.getType().output().get(0) + " " + right + ", " + t.output().get(0) + " " + l.getRegisterName() + "\n";
        res.add(s);
        return res;
    }
}
