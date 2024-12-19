package Analysis.middle.Llvmir.ValueType.Function;

import Analysis.middle.Llvmir.IrNode;
import Analysis.middle.Llvmir.IrValue;

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
