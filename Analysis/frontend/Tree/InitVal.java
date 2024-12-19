package Analysis.frontend.Tree;

import Analysis.frontend.Token.Token;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class InitVal extends Node {
    // InitVal → Exp | '{' [ Exp { ',' Exp } ] '}' | StringConst
    private Exp exp;
    private ArrayList<Exp> expArrayList;
    private Token stringConst;

    public InitVal() {
        this.exp = null;
        this.expArrayList = new ArrayList<>();
        this.stringConst = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<InitVal>" + "\n");
    }

    public Exp getExp() {
        return exp;
    }

    public ArrayList<Exp> getExpArrayList() {
        return expArrayList;
    }

    public Token getStringConst() {
        return stringConst;
    }

    public void setExp(Exp exp) {
        this.exp = exp;
    }

    public void setExpArrayList(ArrayList<Exp> expArrayList) {
        this.expArrayList = expArrayList;
    }

    public void setStringConst(Token stringConst) {
        this.stringConst = stringConst;
    }

    public void addExpArrayList(Exp exp) {
        this.expArrayList.add(exp);
    }
}
