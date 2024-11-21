package Llvmir.ValueType.Instruction;

import Llvmir.Type.IrType;
import Llvmir.Type.IrVoidTy;

import java.util.ArrayList;

public class IrRet extends IrInstruction{
    private int result;

    public IrRet() {
        super();
        result = -1;
    }

    public void setResult(int result) {
        this.result = result;
    }

    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        String s = "ret ";
        if(this.getType() instanceof IrVoidTy) {
            s = s + this.getType().output().get(0) + "\n";
        } else {
            s = s + this.getType().output().get(0) + " " + result + "\n";
        }
        res.add(s);
        return res;
    }
}
