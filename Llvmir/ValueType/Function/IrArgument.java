package Llvmir.ValueType.Function;

import Llvmir.IrNode;
import Llvmir.IrValue;
import Llvmir.Type.IrType;

import java.util.ArrayList;

public class IrArgument extends IrValue implements IrNode {
    private IrType argumentType;
    private int rank;
    private String name;

    public IrArgument() {
        argumentType = null;
        rank = -1;
        name = null;
    }

    public void setArgumentType(IrType argumentType) {
        this.argumentType = argumentType;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRank() {
        return rank;
    }

    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        String s = argumentType.output().get(0) + " %" + rank;
        res.add(s);
        return res;
    }
}
