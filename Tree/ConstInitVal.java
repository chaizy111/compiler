package Tree;

import Analysis.Token.Token;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ConstInitVal extends Node {
    private ConstExp constExp;
    private Token stringConst;
    private ArrayList<ConstExp> constExpArrayList;

    public ConstInitVal() {
        this.constExp = null;
        this.stringConst = null;
        this.constExpArrayList = new ArrayList<>();
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<ConstInitVal>" + "\n");
    }

    public void setConstExp(ConstExp constExp) {
        this.constExp = constExp;
    }

    public void setStringConst(Token stringConst) {
        this.stringConst = stringConst;
    }

    public void addConstExpArrayList(ConstExp constExp) {
        this.constExpArrayList.add(constExp);
    }
}
