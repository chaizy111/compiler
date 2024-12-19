package Analysis.middle.Llvmir.ValueType.Instruction;

import Analysis.middle.Llvmir.IrValue;

import java.util.ArrayList;

public class IrTrunc extends IrInstruction{ // 用于 保存时 int 转 char 例如：%10 = trunc i32 %9 to i8      ;
    public IrTrunc() {
        super();
    }

    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        IrValue v = this.getOperand(0);
        String s = this.getRegisterName() + " = trunc i32 " + v.getRegisterName() + " to i8\n";
        res.add(s);
        return res;
    }
}
