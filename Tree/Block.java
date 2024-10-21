package Tree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Block extends Node {
    private ArrayList<BlockItem> blockItemArrayList;
    private int endLine;

    public Block() {
        blockItemArrayList = new ArrayList<>();
        endLine = -1;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<Block>" + "\n");
    }

    public void addBlockItemArrayList(BlockItem blockItem) {
        this.blockItemArrayList.add(blockItem);
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public ArrayList<BlockItem> getBlockItemArrayList() {
        return blockItemArrayList;
    }
}
