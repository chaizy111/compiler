package Tree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ConstInitVal extends Node {
    public boolean isString;
    public ArrayList<ConstExp> constExpArrayList;

    public ConstInitVal() {
        isString = false;
        constExpArrayList = new ArrayList<>();
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<ConstInitVal>" + "\n");
    }
}
