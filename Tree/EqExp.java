package Tree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class EqExp extends Node {
    public ArrayList<RelExp> relExpArrayList;

    public EqExp() {
        relExpArrayList = new ArrayList<>();
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<EqExp>" + "\n");
    }
}
