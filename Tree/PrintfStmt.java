package Tree;

import Analysis.Token.Token;

import java.util.ArrayList;

public class PrintfStmt extends Stmt {
    private Token stringConst;
    private ArrayList<Exp> expArrayList;

    public PrintfStmt() {
        this.stringConst = null;
        this.expArrayList = new ArrayList<>();
    }

    public void setStringConst(Token stringConst) {
        this.stringConst = stringConst;
    }

    public void addExpArrayLsit(Exp exp) {
        this.expArrayList.add(exp);
    }
}
