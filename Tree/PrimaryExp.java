package Tree;

import Analysis.Token.Token;

import java.io.FileWriter;
import java.io.IOException;

public class PrimaryExp extends Node {
    private Exp exp;
    private LVal lVal;
    private Number number;
    private Character char1;

    public PrimaryExp() {
        exp = null;
        lVal = null;
        number = null;
        char1 = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<PrimaryExp>" + "\n");
    }

    public void setlVal(LVal lVal) {
        this.lVal = lVal;
    }

    public void setExp(Exp exp) {
        this.exp = exp;
    }

    public void setNumber(Number number) {
        this.number = number;
    }

    public void setChar1(Character char1) {
        this.char1 = char1;
    }

    public Exp getExp() {
        return exp;
    }

    public LVal getlVal() {
        return lVal;
    }

    public Character getCharacter() {
        return char1;
    }

    public Number getNumber() {
        return number;
    }

    public int getResult() {
        if(char1 != null) {
            return char1.getResult();
        } else if (number != null) {
            return Integer.parseInt(number.getToken().getString());
        } else if (exp != null) {
            return exp.getAddExp().getResult();
        } else {
            return 0;
        }
    }
}
