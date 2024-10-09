package Tree;

import java.io.FileWriter;
import java.io.IOException;

public class Exp extends Node {
    private AddExp addExp;

    public Exp() {
        addExp = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<Exp>" + "\n");
    }

    public void setAddExp(AddExp addExp) {
        this.addExp = addExp;
    }
}
