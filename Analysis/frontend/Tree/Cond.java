package Analysis.frontend.Tree;

import java.io.FileWriter;
import java.io.IOException;

public class Cond extends Node {
    private LOrExp lOrExp;

    public Cond() {
        lOrExp = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<Cond>" + "\n");
    }

    public void setlOrExp(LOrExp lOrExp) {
        this.lOrExp = lOrExp;
    }

    public LOrExp getlOrExp() {
        return lOrExp;
    }
}
