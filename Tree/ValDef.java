package Tree;

import java.io.FileWriter;
import java.io.IOException;

public class ValDef extends Node {
    private ConstExp constExp;
    private InitVal initVal;

    public ValDef() {
        constExp = null;
        initVal = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<VarDef>" + "\n");
    }

    public void setConstExp(ConstExp constExp) {
        this.constExp = constExp;
    }

    public void setInitVal(InitVal initVal) {
        this.initVal = initVal;
    }
}
