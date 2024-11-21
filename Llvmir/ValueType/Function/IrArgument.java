package Llvmir.ValueType.Function;

import Llvmir.IrNode;
import Llvmir.IrValue;
import Llvmir.Type.IrType;

import java.util.ArrayList;

public class IrArgument extends IrValue implements IrNode {
    private int rank;

    public IrArgument() {
        super();
        rank = -1;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        String s = this.getType().output().get(0) + " %" + rank;
        res.add(s);
        return res;
    }
}
