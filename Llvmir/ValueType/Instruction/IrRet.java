package Llvmir.ValueType.Instruction;

import Llvmir.Type.IrType;
import Llvmir.Type.IrVoidTy;

import java.util.ArrayList;

public class IrRet extends IrInstruction{
    private int result;

    public IrRet() {
        super();
        result = 2147483647;
    }

    public void setResult(int result) {
        this.result = result;
    }

    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        String s = "ret ";
        if(this.getType() instanceof IrVoidTy) { // void型
            s = s + this.getType().output().get(0) + "\n";
        } else if (result != 2147483647){ //常值型
            s = s + this.getType().output().get(0) + " " + result + "\n";
        } else { // 返回结果存储在寄存器中的类型
            s = s + this.getType().output().get(0) + " " + this.getRegisterName() + "\n";
        }
        res.add(s);
        return res;
    }
}
