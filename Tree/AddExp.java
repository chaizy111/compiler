package Tree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class AddExp extends Node {
    private ArrayList<MulExp> mulExpArrayList;

    public AddExp() {
        mulExpArrayList = new ArrayList<>();
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<AddExp>" + "\n");
    }

    public void addMulExpArrayList(MulExp mulExp){
        this.mulExpArrayList.add(mulExp);
    }
}
