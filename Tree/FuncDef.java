package Tree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FuncDef extends Node {
    public ArrayList<FuncParam> funcParamList;
    public Block block;

    public FuncDef() {
        funcParamList = null;
        block = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<FuncDef>" + "\n");
    }
}
