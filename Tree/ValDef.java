package Tree;

import java.io.FileWriter;
import java.io.IOException;

public class ValDef extends Node {
    private ConstExp constExp;
    private ConstInitVal constInitVal;

    public ValDef() {
        constExp = null;
        constInitVal = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<VarDef>" + "\n");
    }

    public void setConstExp(ConstExp constExp) {
        this.constExp = constExp;
    }

    public void setConstInitVal(ConstInitVal constInitVal) {
        this.constInitVal = constInitVal;
    }
}
