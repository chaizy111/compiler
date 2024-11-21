package Llvmir.Type;

import java.util.ArrayList;

public class IrFunctionTy extends IrType {
    private IrType funcType;

    public IrFunctionTy() {
        funcType = null;
    }

    public void setFuncType(IrType funcType) {
        this.funcType = funcType;
    }

    public IrType getFuncType() {
        return funcType;
    }

    @Override
    public ArrayList<String> output() {
        return funcType.output();
    }
}
