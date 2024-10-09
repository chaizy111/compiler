package Tree;

import java.io.FileWriter;
import java.io.IOException;

public class LVal extends Node {
    private String ident;
    private Exp exp;

    public LVal() {
        ident = "";
        exp = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<LVal>" + "\n");
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }

    public void setExp(Exp exp) {
        this.exp = exp;
    }
}
