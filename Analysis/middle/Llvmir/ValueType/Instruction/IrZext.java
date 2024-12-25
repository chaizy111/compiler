package Analysis.middle.Llvmir.ValueType.Instruction;

import Analysis.middle.Llvmir.IrValue;
import Analysis.middle.Llvmir.Type.IrIntegerTy;
import Analysis.middle.Llvmir.Type.IrType;

import java.util.ArrayList;

public class IrZext extends IrInstruction{ //用于 char 参与运算时转 int 例如：%8 = zext i8 %7 to i32        ;
    public IrZext() {
        super();
    }

    public IrZext(int rname, IrType type, IrValue operand){
        this.setRegisterName(String.valueOf(rname));
        this.setType(type);
        this.setOperand(operand, 0);
    }

    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        IrValue v = this.getOperand(0);
        String s = "%r."+this.getRegisterName() + " = zext " + v.getType().output().get(0) + " "+ "%r."+v.getRegisterName() + " to i32\n";
        res.add(s);
        return res;
    }
}
