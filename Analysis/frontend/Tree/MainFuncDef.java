package Analysis.frontend.Tree;

import java.io.FileWriter;
import java.io.IOException;

public class MainFuncDef extends Node {
    private Block block;

    public MainFuncDef() {
        block = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<MainFuncDef>" + "\n");
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }
}
