package Analysis.middle.Llvmir.ValueType.Instruction;

import Analysis.middle.Llvmir.IrValue;
import Analysis.middle.Llvmir.Type.IrType;
import Analysis.middle.Llvmir.ValueType.Global.IrGlobalVariable;

import java.util.ArrayList;

public class IrLoad extends IrInstruction{
    public IrLoad() {
        super();
    }

    public IrLoad(int rname, IrType type, IrValue operand0) {
        this.setRegisterName(String.valueOf(rname));
        this.setType(type);
        this.setOperand(operand0, 0);
    }

    //<result> = load <ty>, ptr <pointer>
    @Override
    public ArrayList<String> output() {
        IrValue v = this.getOperand(0);
        String r;
        if (v.getRegisterName().charAt(0) < '0' || v.getRegisterName().charAt(0) > '9') { //是全局变量
            r = "@"+v.getRegisterName();
        } else {
            r = "%r."+v.getRegisterName();
        }
        ArrayList<String> res = new ArrayList<>();
        String s = "%r."+this.getRegisterName() + " = load " + this.getType().output().get(0) +
                ", " + v.getType().output().get(0) + " " + r + "\n";
        res.add(s);
        return res;
    }
}
