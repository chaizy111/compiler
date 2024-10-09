package Tree;

import java.util.ArrayList;

public class PrintfStmt extends Stmt {
    private ArrayList<Exp> expArrayList;

    public PrintfStmt() {
        expArrayList = new ArrayList<>();
    }

    public void addExpArrayLsit(Exp exp) {
        this.expArrayList.add(exp);
    }
}
