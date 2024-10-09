package Tree;

import java.io.FileWriter;
import java.io.IOException;

public class ConstExp extends Node {
    public AddExp addExp;

    public ConstExp() {
        addExp = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<ConstExp>" + "\n");
    }
}
