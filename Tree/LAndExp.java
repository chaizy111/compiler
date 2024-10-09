package Tree;

import java.awt.font.TextHitInfo;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class LAndExp extends Node {
    private ArrayList<EqExp> eqExpArrayList;

    public LAndExp() {
        eqExpArrayList = new ArrayList<>();
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<LAndExp>" + "\n");
    }

    public void addEqExpArrayList(EqExp eqExp) {
        this.eqExpArrayList.add(eqExp);
    }
}
