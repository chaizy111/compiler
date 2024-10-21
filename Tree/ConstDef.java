package Tree;

import Analysis.Token.Token;

import java.io.FileWriter;
import java.io.IOException;

public class ConstDef extends Node {
    private Token ident;
    private ConstExp constExp;
    private ConstInitVal constInitVal;

    public ConstDef() {
        this.ident = null;
        this.constExp = null;
        this.constInitVal = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<ConstDef>" + "\n");
    }

    public void setIdent(Token ident) {
        this.ident = ident;
    }

    public void setConstExp(ConstExp constExp) {
        this.constExp = constExp;
    }

    public void setConstInitVal(ConstInitVal constInitVal) {
        this.constInitVal = constInitVal;
    }

    public Token getIdent() {
        return ident;
    }

    public ConstExp getConstExp() {
        return constExp;
    }

    public ConstInitVal getConstInitVal() {
        return constInitVal;
    }
}
