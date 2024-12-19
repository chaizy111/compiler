package Analysis.frontend.Tree;

import Analysis.frontend.Token.Token;

import java.io.FileWriter;
import java.io.IOException;

public class Stmt extends BlockItem {
    private Block b;
    private Exp e;
    private Token bOrC;

    public Stmt() {
        b = null;
        e = null;
        bOrC = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<Stmt>" + "\n");
    }

    public void setB(Block b) {
        this.b = b;
    }

    public void setE(Exp e) {
        this.e = e;
    }

    public void setbOrC(Token bOrC) {
        this.bOrC = bOrC;
    }

    public Block getB() {
        return b;
    }

    public Exp getE() {
        return e;
    }

    public Token getbOrC() {
        return bOrC;
    }
}
