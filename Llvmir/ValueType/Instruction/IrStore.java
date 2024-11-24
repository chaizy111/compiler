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
        String s = "store " + r.getType().output().get(0) + " " + right + ", " + l.getType().output().get(0) + " " + l.getRegisterName() + "\n";
        res.add(s);
        return res;
    }
}
