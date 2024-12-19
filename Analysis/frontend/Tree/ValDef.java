package Analysis.frontend.Tree;

import Analysis.frontend.Token.Token;

import java.io.FileWriter;
import java.io.IOException;

public class ValDef extends Node {
    private Token ident;
    private ConstExp constExp;
    private InitVal initVal;

    public ValDef() {
        this.ident = null;
        this.constExp = null;
        this.initVal = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<VarDef>" + "\n");
    }

    public void setIdent(Token ident) {
        this.ident = ident;
    }

    public void setConstExp(ConstExp constExp) {
        this.constExp = constExp;
    }

    public void setInitVal(InitVal initVal) {
        this.initVal = initVal;
    }

    public Token getIdent() {
        return ident;
    }

    public ConstExp getConstExp() {
        return constExp;
    }

    public InitVal getInitVal() {
        return initVal;
    }
}
