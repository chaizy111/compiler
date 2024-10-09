package Tree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ConstInitVal extends Node {
    private String string;
    private ArrayList<ConstExp> constExpArrayList;

    public ConstInitVal() {
        string = "";
        constExpArrayList = new ArrayList<>();
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<ConstInitVal>" + "\n");
    }

    public void setString(String string) {
        this.string = string;
    }

    public void addConstExpArrayList(ConstExp constExp) {
        this.constExpArrayList.add(constExp);
    }
}
