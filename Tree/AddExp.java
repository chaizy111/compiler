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

    public ArrayList<MulExp> getMulExpArrayList() {
        return mulExpArrayList;
    }

    public ArrayList<Token> getSymbolList() {
        return symbolList;
    }

    public int getResult() {
        int sum = !mulExpArrayList.isEmpty() ? mulExpArrayList.get(0).getResult() : 0;
        for(int i = 1; i < mulExpArrayList.size(); i++) {
            if(i - 1 < symbolList.size()) {
                sum += symbolList.get(i - 1).getType() == TokenType.tokenType.PLUS ?
                        mulExpArrayList.get(i).getResult() : -mulExpArrayList.get(i).getResult();
            }
        }
        return sum;
    }
}
