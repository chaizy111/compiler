package Tree;

import java.io.FileWriter;
import java.io.IOException;

public class ValDef extends Node {
    public ConstExp constExp;
    public ConstInitVal constInitVal;

    public ValDef() {
        constExp = null;
        constInitVal = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<VarDef>" + "\n");
    }
}
