package Analysis.frontend.Tree;

public class For extends Stmt {
    private ForStmt forStmt1;
    private ForStmt forStmt2;
    private Cond c;
    private Stmt s;

    public For() {
        forStmt1 = null;
        forStmt2 = null;
        c = null;
        s = null;
    }

    public void setForStmt1(ForStmt forStmt1) {
        this.forStmt1 = forStmt1;
    }

    public void setForStmt2(ForStmt forStmt2) {
        this.forStmt2 = forStmt2;
    }

    public void setC(Cond c) {
        this.c = c;
    }

    public void setS(Stmt s) {
        this.s = s;
    }

    public ForStmt getForStmt1() {
        return forStmt1;
    }

    public ForStmt getForStmt2() {
        return forStmt2;
    }

    public Cond getC() {
        return c;
    }

    public Stmt getS() {
        return s;
    }
}
