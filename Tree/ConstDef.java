package Tree;

import java.io.FileWriter;
import java.io.IOException;

public class ConstDef extends Node {
    public ConstExp constExp;
    public ConstInitVal constInitVal;

    public ConstDef() {
        constExp = null;
        constInitVal = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<ConstDef>" + "\n");
    }
}
