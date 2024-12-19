package Analysis.frontend.Tree;

public class ReturnStmt extends Stmt {
    private Exp exp;
    private int line;

    public ReturnStmt() {
        exp = null;
        line = -1;
    }

    public void setExp(Exp exp) {
        this.exp = exp;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public Exp getExp() {
        return exp;
    }

    public int getLine() {
        return line;
    }
}
