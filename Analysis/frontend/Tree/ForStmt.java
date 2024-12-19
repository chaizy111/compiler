package Analysis.frontend.Tree;

import java.io.FileWriter;
import java.io.IOException;

public class ForStmt extends Node {
    private LVal lVal;
    private Exp exp;

    public ForStmt() {
        this.lVal = null;
        this.exp = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<ForStmt>" + "\n");
    }

    public LVal getlVal() {
        return lVal;
    }

    public Exp getExp() {
        return exp;
    }

    public void setlVal(LVal lVal) {
        this.lVal = lVal;
    }

    public void setExp(Exp exp) {
        this.exp = exp;
    }
}
