package Llvmir.ValueType.Instruction;

import Llvmir.IrNode;
import Llvmir.IrUser;
import Llvmir.IrValue;

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
