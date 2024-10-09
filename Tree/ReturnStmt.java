package Tree;

public class ReturnStmt extends Stmt {
    private Exp exp;

    public ReturnStmt() {
        exp = null;
    }

    public void setExp(Exp exp) {
        this.exp = exp;
    }
}
