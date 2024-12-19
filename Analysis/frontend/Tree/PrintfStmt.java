package Analysis.frontend.Tree;

import Analysis.frontend.Token.Token;

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

    public Token getStringConst() {
        return stringConst;
    }

    public ArrayList<Exp> getExpArrayList() {
        return expArrayList;
    }

    public int getLine() {
        return stringConst.getLine();
    }

    public int getFCharacterNumInString() {
        int sum = 0;
        String s = stringConst.getString();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '%' && i != s.length() - 1) {
                if (s.charAt(i + 1) == 'd' || s.charAt(i + 1) == 'c') sum++;
            }
        }
        return sum;
    }
}
