package Tree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FuncDef extends Node {
    private ArrayList<FuncFParam> funcFParamList;
    private Block block;

    public FuncDef() {
        funcFParamList = null;
        block = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<FuncDef>" + "\n");
    }

    public void setFuncParamList(ArrayList<FuncFParam> funcFParamList) {
        this.funcFParamList = funcFParamList;
    }

    public void setBlock(Block block) {
        this.block = block;
    }
}
