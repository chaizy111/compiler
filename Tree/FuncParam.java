package Tree;

import Analysis.Token.TokenType;

import java.io.FileWriter;
import java.io.IOException;

public class FuncParam extends Node {
    public boolean isArray;
    public TokenType.tokenType bType;

    public FuncParam() {
        isArray = false;
        bType = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<FuncParam>" + "\n");
    }
}
