package Tree;

public class IfStmt extends Stmt {
    public Cond c;
    public Stmt s1;
    public Stmt s2;

    public IfStmt() {
        c = null;
        s1 = null;
        s2 = null;
    }
}
