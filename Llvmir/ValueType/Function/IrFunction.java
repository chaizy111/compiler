package Llvmir.ValueType.Function;

import Llvmir.IrNode;
import Llvmir.IrValue;
import Llvmir.Type.IrFunctionTy;
import Llvmir.Type.IrType;
import Llvmir.ValueType.IrBasicBlock;

import java.util.ArrayList;
import java.util.LinkedList;

public class IrFunction extends IrValue implements IrNode {
    private LinkedList<IrArgument> arguments;
    private IrBasicBlock irBlock;
    private CntUtils cntUtils;

    public IrFunction() {
        super();
        arguments = new LinkedList<>();
        irBlock = null;
        cntUtils = new CntUtils();
    }

    public void setArguments(LinkedList<IrArgument> arguments) {
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
        StringBuilder s = new StringBuilder("define dso_local " + this.getType().output().get(0) + " " + this.getName());
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
