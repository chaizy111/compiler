package Analysis.frontend.Tree;

import Analysis.frontend.Token.Token;

import java.io.FileWriter;
import java.io.IOException;

public class Number extends Node {
    private Token token;

    public Number() {
        this.token = null;
    }
    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<Number>" + "\n");
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }
}
