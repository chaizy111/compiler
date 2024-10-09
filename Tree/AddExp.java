package Tree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class AddExp extends Node {
    public ArrayList<MulExp> mulExpArrayList;

    public AddExp() {
        mulExpArrayList = new ArrayList<>();
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<AddExp>" + "\n");
    }
}
