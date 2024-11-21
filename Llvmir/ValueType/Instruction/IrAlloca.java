package Llvmir.ValueType.Instruction;

import java.util.ArrayList;

public class IrAlloca extends IrInstruction{
    public IrAlloca() {
        super();
    }

    // <result> = alloca <type>
    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        String s = this.getRegisterName() + " = alloca" + this.getType().output().get(0);
        res.add(s);
        return res;
    }
}
