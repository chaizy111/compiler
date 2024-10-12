package Tree;

import Analysis.Token.Token;
import Analysis.Token.TokenType;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class AddExp extends Node {
    private ArrayList<MulExp> mulExpArrayList;
    private ArrayList<Token> symbolList;

    public AddExp() {
        this.mulExpArrayList = new ArrayList<>();
        this.symbolList = new ArrayList<>();
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<AddExp>" + "\n");
    }

    public void addMulExpArrayList(MulExp mulExp){
        this.mulExpArrayList.add(mulExp);
    }

    public void addSymbolList(Token token) {
        this.symbolList.add(token);
    }
}
