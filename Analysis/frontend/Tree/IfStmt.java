package Analysis.frontend.Tree;

public class IfStmt extends Stmt {
    private Cond c;
    private Stmt s1;
    private Stmt s2;

    public IfStmt() {
        c = null;
        s1 = null;
        s2 = null;
    }

    public void setC(Cond c) {
        this.c = c;
    }

    public void setS1(Stmt s1) {
        this.s1 = s1;
    }

    public void setS2(Stmt s2) {
        this.s2 = s2;
    }

    public Cond getC() {
        return c;
    }

    public Stmt getS1() {
        return s1;
    }

    public Stmt getS2() {
        return s2;
    }
}
