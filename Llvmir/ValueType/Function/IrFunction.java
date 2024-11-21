package Llvmir.ValueType.Function;

import Llvmir.IrNode;
import Llvmir.IrValue;
import Llvmir.Type.IrFunctionTy;
import Llvmir.Type.IrType;
import Llvmir.ValueType.IrBasicBlock;

import java.util.ArrayList;

public class IrFunction extends IrValue implements IrNode {
    private IrFunctionTy type;
    private ArrayList<IrArgument> arguments;
    private IrBasicBlock irBlock;
    private CntUtils cntUtils;

    public IrFunction() {
        type = null;
        arguments = new ArrayList<>();
        irBlock = null;
        cntUtils = new CntUtils();
    }

    public void setType(IrFunctionTy type) {
        this.type = type;
    }

    public void setArguments(ArrayList<IrArgument> arguments) {
        this.arguments = arguments;
    }

    public void setIrBlock(IrBasicBlock irBlock) {
        this.irBlock = irBlock;
    }

    public int getNowRank() {
        return cntUtils.getCount();
    }

    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        StringBuilder s = new StringBuilder("define dso_local " + type.output().get(0) + " " + this.getName());
        if(arguments.isEmpty()) {
            s.append("() {\n");
        } else {
            s.append("(");
            s.append(arguments.get(0).output().get(0));
            for (int i = 1; i < arguments.size(); i++) {
                s.append(", ");
                s.append(arguments.get(i).output().get(0));
            }
            s.append(") {\n");
        }
        res.add(s.toString());
        res.addAll(irBlock.output());
        res.add("}\n");
        return res;
    }
}
