package Tree;

import java.io.FileWriter;
import java.io.IOException;

public class Stmt extends BlockItem {
    private Block b;
    private Exp e;

    public Stmt() {
        b = null;
        e = null;
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
}
