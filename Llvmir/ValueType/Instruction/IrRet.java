package Llvmir.ValueType.Instruction;

import Llvmir.Type.IrType;
import Llvmir.Type.IrVoidTy;

import java.util.ArrayList;

public class IrRet extends IrInstruction{
    private IrType type;
    private int result;

    public IrRet() {
        type = null;
        result = -1;
    }

    public void setType(IrType type) {
        this.type = type;
    }

    public void setResult(int result) {
        this.result = result;
    }

    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        String s = "ret ";
        if(type instanceof IrVoidTy) {
            s = s + type.output().get(0) + "\n";
        } else {
            s = s + type.output().get(0) + " " + result + "\n";
        }
        res.add(s);
        return res;
    }
}
