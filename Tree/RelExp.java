package Tree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class RelExp extends Node {
    public ArrayList<AddExp> addExpArrayList;

    public RelExp() {
        addExpArrayList = new ArrayList<>();
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<RelExp>" + "\n");
    }
}
