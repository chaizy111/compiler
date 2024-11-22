package Llvmir.ValueType.Instruction;

import Llvmir.IrValue;
import Llvmir.Type.IrPointerTy;

import java.util.ArrayList;

public class IrLoad extends IrInstruction{
    public IrLoad() {
        super();
    }

    //<result> = load <ty>, ptr <pointer>
    @Override
    public ArrayList<String> output() {
        IrValue v = this.getOperand(0);
        IrPointerTy ptr = new IrPointerTy();
        ptr.setType(v.getType());
        ArrayList<String> res = new ArrayList<>();
        String s = this.getRegisterName() + " = load " + this.getType().output().get(0) +
                ", " + ptr.output().get(0) + v.getRegisterName() + "\n";
        res.add(s);
        return res;
    }
}
