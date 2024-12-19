package Analysis.frontend.Tree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class LOrExp extends Node {
    private ArrayList<LAndExp> lAndExpArrayList;

    public LOrExp() {
        lAndExpArrayList = new ArrayList<>();
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<LOrExp>" + "\n");
    }

    public void addLAndExpArrayList(LAndExp lAndExp) {
        this.lAndExpArrayList.add(lAndExp);
    }

    public ArrayList<LAndExp> getlAndExpArrayList() {
        return lAndExpArrayList;
    }
}
