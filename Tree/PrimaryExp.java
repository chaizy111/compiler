package Tree;

import java.io.FileWriter;
import java.io.IOException;

public class PrimaryExp extends Node {
    private Exp exp;
    private LVal lVal;
    private boolean isNumber;
    private boolean isChar;

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

    public void setlVal(LVal lVal) {
        this.lVal = lVal;
    }

    public void setExp(Exp exp) {
        this.exp = exp;
    }

    public void setIsNumber(boolean isNumber) {
        this.isNumber = isNumber;
    }

    public void setIsChar(boolean isChar) {
        this.isChar = isChar;
    }
}
