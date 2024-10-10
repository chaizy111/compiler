package Tree;

public class For extends Stmt {
    private LValStmt l1;
    private LValStmt l2;
    private Cond c;
    private Stmt s;

    public For() {
        l1 = null;
        l2 = null;
        c = null;
        s = null;
    }

    public void setL1(LValStmt l1) {
        this.l1 = l1;
    }

    public void setL2(LValStmt l2) {
        this.l2 = l2;
    }

    public void setC(Cond c) {
        this.c = c;
    }

    public void setS(Stmt s) {
        this.s = s;
    }
}
