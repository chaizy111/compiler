package Tree;

import java.io.FileWriter;
import java.io.IOException;

public class Cond extends Node {
    public LOrExp lOrExp;

    public Cond() {
        lOrExp = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<Cond>" + "\n");
    }
}
