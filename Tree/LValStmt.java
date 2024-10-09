package Tree;

import java.io.FileWriter;
import java.io.IOException;

public class LValStmt extends Stmt {
    private LVal lVal;
    private Exp exp;
    private boolean isGetInt;
    private boolean isGetChar;

    public LValStmt() {
        lVal = null;
        exp = null;
        isGetInt = false;
        isGetChar = false;
    }

    public void print1(FileWriter output) throws IOException {
        output.write("<ForStmt>" + "\n");
    }

    public void setlVal(LVal lVal) {
        this.lVal = lVal;
    }

    public void setExp(Exp exp) {
        this.exp = exp;
    }

    public void setIsGetInt(boolean isGetInt) {
        this.isGetInt = isGetInt;
    }

    public void setIsGetChar(boolean isGetChar) {
        this.isGetChar = isGetChar;
    }
}
