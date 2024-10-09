package Tree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class LOrExp extends Node {
    public ArrayList<LAndExp> lAndExpArrayList;

    public LOrExp() {
        lAndExpArrayList = new ArrayList<>();
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<LOrExp>" + "\n");
    }
}
