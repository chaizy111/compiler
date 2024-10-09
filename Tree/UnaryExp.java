package Tree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class UnaryExp extends Node {
    private UnaryExp unaryExp;
    private PrimaryExp primaryExp;
    private ArrayList<FuncParam> funcParamArrayList;

    public UnaryExp() {
        unaryExp = null;
        primaryExp = null;
        funcParamArrayList = new ArrayList<>();
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<UnaryExp>" + "\n");
    }

    public void setUnaryExp(UnaryExp unaryExp) {
        this.unaryExp = unaryExp;
    }

    public void setPrimaryExp(PrimaryExp primaryExp) {
        this.primaryExp = primaryExp;
    }

    public void setFuncParamArrayList(ArrayList<FuncParam> funcParamArrayList) {
        this.funcParamArrayList = funcParamArrayList;
    }
}
