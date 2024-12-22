package Analysis.middle.Llvmir.ValueType.Instruction;

import Analysis.middle.Llvmir.Type.IrType;

import java.util.ArrayList;

public class IrAlloca extends IrInstruction{
    private IrType allocaType;

    public void setAllocaType(IrType allocaType) {
        this.allocaType = allocaType;
    }

    public IrType getAllocaType() {
        return allocaType;
    }

    public IrAlloca() {
        super();
    }

    // <result> = alloca <type>
    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        String s = "%r."+this.getRegisterName() + " = alloca " + allocaType.output().get(0) + "\n";
        res.add(s);
        return res;
    }
}
