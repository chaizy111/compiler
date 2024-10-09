package Tree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MulExp extends Node {
    public ArrayList<UnaryExp> unaryExpArrayList;

    public MulExp() {
        unaryExpArrayList = new ArrayList<>();
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<MulExp>" + "\n");
    }

    public void addUnaryExpArrayList(UnaryExp unaryExp) {
        this.unaryExpArrayList.add(unaryExp);
    }
}
