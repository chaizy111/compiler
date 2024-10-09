package Tree;

import java.io.FileWriter;
import java.io.IOException;

public class LValStmt extends Stmt {
    public LVal lVal;
    public Exp exp;
    public boolean isGetInt;
    public boolean isGetChar;

    public LValStmt() {
        lVal = null;
        exp = null;
        isGetInt = false;
        isGetChar = false;
    }

    public void print1(FileWriter output) throws IOException {
        output.write("<ForStmt>" + "\n");
    }
}
