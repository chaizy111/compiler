package Tree;

import java.io.FileWriter;
import java.io.IOException;

public class LVal extends Node {
    public Exp exp;

    public LVal() {
        exp = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<LVal>" + "\n");
    }
}
