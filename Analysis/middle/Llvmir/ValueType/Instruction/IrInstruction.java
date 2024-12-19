package Analysis.middle.Llvmir.ValueType.Instruction;

import Analysis.middle.Llvmir.IrNode;
import Analysis.middle.Llvmir.IrUser;

import java.util.ArrayList;

public class IrInstruction extends IrUser implements IrNode {
    public IrInstruction() {
        super();
    }

    @Override
    public ArrayList<String> output() {
        return null;
    }
}
