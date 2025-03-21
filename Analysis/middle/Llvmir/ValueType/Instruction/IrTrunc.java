package Analysis.middle.Llvmir.ValueType.Instruction;

import Analysis.middle.Llvmir.IrValue;
import Analysis.middle.Llvmir.Type.IrCharTy;
import Analysis.middle.Llvmir.Type.IrType;

import java.util.ArrayList;

public class IrTrunc extends IrInstruction{ // 用于 保存时 int 转 char 例如：%10 = trunc i32 %9 to i8      ;
    public IrTrunc() {
        super();
    }

    public IrTrunc(int rname, IrValue operand) {
        this.setRegisterName(String.valueOf(rname));
        this.setType(new IrCharTy());
        this.setOperand(operand, 0);
    }

    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        IrValue v = this.getOperand(0);
        String s = "%r."+this.getRegisterName() + " = trunc i32 " + "%r."+v.getRegisterName() + " to " + this.getType().output().get(0) + "\n";
        res.add(s);
        return res;
    }
}
