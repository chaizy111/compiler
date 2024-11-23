package Llvmir.ValueType;

import Llvmir.IrNode;
import Llvmir.IrValue;
import Llvmir.ValueType.Instruction.IrInstruction;

import java.util.ArrayList;

public class IrBasicBlock extends IrValue implements IrNode {
    private ArrayList<IrInstruction> instructions;

    public IrBasicBlock() {
        super();
        instructions = new ArrayList<>();
    }

    public void addInstruction(IrInstruction instruction) {
        instructions.add(instruction);
    }

    public void addAllInstruction(ArrayList<IrInstruction> instructions) {
        this.instructions.addAll(instructions);
    }

    public ArrayList<IrInstruction> getInstructions() {
        return instructions;
    }

    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        for (IrInstruction i:instructions) {
            if (i.output() == null || i.output().isEmpty()) continue;
            res.addAll(i.output());
        }
        return res;
    }
}
