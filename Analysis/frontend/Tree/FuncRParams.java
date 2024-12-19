package Analysis.frontend.Tree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FuncRParams extends Node {
    private ArrayList<Exp> expArrayList;

    public FuncRParams() {
        this.expArrayList = new ArrayList<>();
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<FuncRParams>" + "\n");
    }

    public void setExpArrayList(ArrayList<Exp> expArrayList) {
        this.expArrayList = expArrayList;
    }

    public ArrayList<Exp> getExpArrayList() {
        return expArrayList;
    }
}
