package Llvmir.ValueType.Instruction;

import Llvmir.IrValue;

import java.util.ArrayList;

public class IrZext extends IrInstruction{ //用于 char 参与运算时转 int 例如：%8 = zext i8 %7 to i32        ;
    public IrZext() {
        super();
    }

    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        IrValue v = this.getOperand(0);
        String s = this.getRegisterName() + " = zext " + v.getType().output().get(0) + " "+ v.getRegisterName() + " to i32\n";
        res.add(s);
        return res;
    }
}
