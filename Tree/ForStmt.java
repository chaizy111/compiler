package Tree;

public class ForStmt extends Stmt {
    public LValStmt l1;
    public LValStmt l2;
    public Cond c;
    public Stmt s;

    public ForStmt() {
        l1 = null;
        l2 = null;
        c = null;
        s = null;
    }
}
