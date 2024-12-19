package Analysis.frontend.Tree;

import Analysis.frontend.Token.Token;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class RelExp extends Node {
    private ArrayList<AddExp> addExpArrayList;
    private ArrayList<Token> symbolList;

    public RelExp() {
        this.addExpArrayList = new ArrayList<>();
        this.symbolList = new ArrayList<>();
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<RelExp>" + "\n");
    }

    public void addAddExpArrayList(AddExp addExp) {
        this.addExpArrayList.add(addExp);
    }

    public void addSymbolList(Token token) {
        this.symbolList.add(token);
    }

    public ArrayList<AddExp> getAddExpArrayList() {
        return addExpArrayList;
    }

    public ArrayList<Token> getSymbolList() {
        return symbolList;
    }
}
