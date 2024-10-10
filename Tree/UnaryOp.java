package Tree;

import Analysis.Token.Token;

import java.io.FileWriter;
import java.io.IOException;

public class UnaryOp extends Node {
    private Token token;

    public UnaryOp() {
        this.token = null;
    }
    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<UnaryOp>" + "\n");
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }
}
