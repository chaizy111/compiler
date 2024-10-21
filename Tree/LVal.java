package Tree;

import Analysis.Token.Token;

import java.io.FileWriter;
import java.io.IOException;

public class LVal extends Node {
    private Token ident;
    private Exp exp;

    public LVal() {
        ident = null;
        exp = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<LVal>" + "\n");
    }

    public void setIdent(Token ident) {
        this.ident = ident;
    }

    public void setExp(Exp exp) {
        this.exp = exp;
    }

    public Exp getExp() {
        return exp;
    }

    public Token getIdent() {
        return ident;
    }
}
