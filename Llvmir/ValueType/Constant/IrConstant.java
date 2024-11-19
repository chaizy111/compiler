package Llvmir.ValueType.Constant;

import Llvmir.IrNode;
import Llvmir.Type.IrType;

import java.util.ArrayList;

public class IrConstant implements IrNode {
    private IrType type;

    public IrConstant() {
    }

    @Override
    public ArrayList<String> output() {
        return null;
    }

    public void setType(IrType type) {
        this.type = type;
    }

    public IrType getType() {
        return type;
    }
}
