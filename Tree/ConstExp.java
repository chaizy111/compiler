package Tree;

import java.io.FileWriter;
import java.io.IOException;

public class ConstExp extends Node {
    private AddExp addExp;

    public ConstExp() {
        addExp = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<ConstExp>" + "\n");
    }

    public void setAddExp(AddExp addExp) {
        this.addExp = addExp;
    }

    public AddExp getAddExp() {
        return addExp;
    }

    public int getResult() {
        return addExp.getResult();
    }
}
