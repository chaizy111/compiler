package Tree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FuncDef extends Node {
    private ArrayList<FuncParam> funcParamList;
    private Block block;

    public FuncDef() {
        funcParamList = null;
        block = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<FuncDef>" + "\n");
    }

    public void setFuncParamList(ArrayList<FuncParam> funcParamList) {
        this.funcParamList = funcParamList;
    }

    public void setBlock(Block block) {
        this.block = block;
    }
}
