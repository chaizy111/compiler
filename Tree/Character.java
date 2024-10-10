package Tree;

import Analysis.Token.Token;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Character extends Node{
    private Token token;

    public Character(){
        this.token = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
           output.write("<Character>" + "\n");
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }
}
