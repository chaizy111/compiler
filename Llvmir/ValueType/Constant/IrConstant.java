package Llvmir.ValueType.Constant;

import Llvmir.IrNode;
import Llvmir.IrUser;
import Llvmir.Type.IrType;

import java.util.ArrayList;

public class IrConstant extends IrUser implements IrNode {
    public IrConstant() {
    }

    @Override
    public ArrayList<String> output() {
        return null;
    }
}
