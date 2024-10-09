package Tree;

import java.io.FileWriter;
import java.io.IOException;

public class MainFuncDef extends Node {
    public Block block;

    public MainFuncDef() {
        block = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<MainFuncDef>" + "\n");
    }
}
