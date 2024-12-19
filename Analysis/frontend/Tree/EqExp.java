package Analysis.frontend.Tree;

import Analysis.frontend.Token.Token;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class EqExp extends Node {
    private ArrayList<RelExp> relExpArrayList;
    private ArrayList<Token> symbolList;

    public EqExp() {
        this.relExpArrayList = new ArrayList<>();
        this.symbolList = new ArrayList<>();
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<EqExp>" + "\n");
    }

    public void addRelExpArrayList(RelExp relExp) {
        this.relExpArrayList.add(relExp);
    }

    public void addSymbolList(Token token) {
        this.symbolList.add(token);
    }

    public ArrayList<RelExp> getRelExpArrayList() {
        return relExpArrayList;
    }

    public ArrayList<Token> getSymbolList() {
        return symbolList;
    }
}
