package Tree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Block extends Node {
    public ArrayList<BlockItem> blockItemArrayList;

    public Block() {
        blockItemArrayList = new ArrayList<>();
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<Block>" + "\n");
    }
}
