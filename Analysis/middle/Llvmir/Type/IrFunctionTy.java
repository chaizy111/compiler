package Analysis.middle.Llvmir.Type;

import Analysis.middle.Llvmir.IrValue;

import java.util.ArrayList;

public class IrFunctionTy extends IrType {
    private IrType funcType;
    private ArrayList<IrValue> params;

    public IrFunctionTy() {
        funcType = null;
        params = new ArrayList<>();
    }

    public void setFuncType(IrType funcType) {
        this.funcType = funcType;
    }

    public IrType getFuncType() {
        return funcType;
    }

    public void setParams(ArrayList<IrValue> params) {
        this.params = params;
    }

    @Override
    public ArrayList<String> output() {
        return funcType.output();
    }
}
