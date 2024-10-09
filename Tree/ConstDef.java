package Tree;

import java.io.FileWriter;
import java.io.IOException;

public class ConstDef extends Node {
    private ConstExp constExp;
    private ConstInitVal constInitVal;

    public ConstDef() {
        constExp = null;
        constInitVal = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<ConstDef>" + "\n");
    }

    public void setConstExp(ConstExp constExp) {
        this.constExp = constExp;
    }

    public void setConstInitVal(ConstInitVal constInitVal) {
        this.constInitVal = constInitVal;
    }
}
