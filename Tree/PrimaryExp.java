package Tree;

import java.io.FileWriter;
import java.io.IOException;

public class PrimaryExp extends Node {
    public Exp exp;
    public LVal lVal;
    public boolean isNumber;
    public boolean isChar;

    public PrimaryExp() {
        exp = null;
        lVal = null;
        isNumber = false;
        isChar = false;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<PrimaryExp>" + "\n");
    }
}
