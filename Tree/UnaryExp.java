package Tree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class UnaryExp extends Node {
    public UnaryExp unaryExp;
    public PrimaryExp primaryExp;
    public ArrayList<FuncParam> funcParamArrayList;

    public UnaryExp() {
        unaryExp = null;
        primaryExp = null;
        funcParamArrayList = new ArrayList<>();
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<UnaryExp>" + "\n");
    }
}
