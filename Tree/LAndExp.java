package Tree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class LAndExp extends Node {
    public ArrayList<EqExp> eqExpArrayList;

    public LAndExp() {
        eqExpArrayList = new ArrayList<>();
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<LAndExp>" + "\n");
    }
}
