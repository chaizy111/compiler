package Llvmir.Type;

import Llvmir.ValueType.Constant.IrConstant;

import java.util.ArrayList;

public class IrArrayTy extends IrType{
    private int arraySize;
    private IrType arrayType;

    public void setArrayType(IrType arrayType) {
        this.arrayType = arrayType;
    }

    public IrType getArrayType() {
        return arrayType;
    }

    public void setArraySize(int arraySize) {
        this.arraySize = arraySize;
    }

    public int getArraySize() {
        return arraySize;
    }

    @Override
    public ArrayList<String> output() {
       ArrayList<String> res = new ArrayList<>();
       String s;
       s = "[" + arraySize + " x " + arrayType.output().get(0) + "]";
       res.add(s);
       return res;
    }
}
