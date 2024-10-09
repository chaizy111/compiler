package Tree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class EqExp extends Node {
    private ArrayList<RelExp> relExpArrayList;

    public EqExp() {
        relExpArrayList = new ArrayList<>();
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<EqExp>" + "\n");
    }

    public void addRelExpArrayList(RelExp relExp) {
        this.relExpArrayList.add(relExp);
    }
}
