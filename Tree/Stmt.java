package Tree;

import java.io.FileWriter;
import java.io.IOException;

public class Stmt extends BlockItem {
    public Block b;
    public Exp e;

    public Stmt() {
        b = null;
        e = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<Stmt>" + "\n");
    }
}
