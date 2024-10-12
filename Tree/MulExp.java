package Tree;

import Analysis.Token.Token;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MulExp extends Node {
    private ArrayList<UnaryExp> unaryExpArrayList;
    private ArrayList<Token> symbolList;

    public MulExp() {
        this.unaryExpArrayList = new ArrayList<>();
        this.symbolList = new ArrayList<>();
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<MulExp>" + "\n");
    }

    public void addUnaryExpArrayList(UnaryExp unaryExp) {
        this.unaryExpArrayList.add(unaryExp);
    }

    public void addSymbolList(Token token) {
        this.symbolList.add(token);
    }
}
