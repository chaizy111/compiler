package Tree;

import Analysis.Token.Token;
import Analysis.Token.TokenType;

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

    public ArrayList<UnaryExp> getUnaryExpArrayList() {
        return unaryExpArrayList;
    }

    public ArrayList<Token> getSymbolList() {
        return symbolList;
    }

    public int getResult() {
        int sum = !unaryExpArrayList.isEmpty() ? unaryExpArrayList.get(0).getResult() : 0;
        for(int i = 1; i < unaryExpArrayList.size(); i++) {
            if(i - 1 < symbolList.size()) {
                if(symbolList.get(i - 1).getType() == TokenType.tokenType.MULT) {
                    sum = sum * unaryExpArrayList.get(i).getResult();
                } else if (symbolList.get(i - 1).getType() == TokenType.tokenType.DIV) {
                    sum = sum / unaryExpArrayList.get(i).getResult();
                } else {
                    sum = sum % unaryExpArrayList.get(i).getResult();
                }
            }
        }
        return sum;
    }
}
