package Tree;

import Analysis.Token.TokenType;

import java.io.FileWriter;
import java.io.IOException;

public class FuncFParam extends Node {
    private boolean isArray;
    private TokenType.tokenType bType;

    public FuncFParam() {
        isArray = false;
        bType = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<FuncParam>" + "\n");
    }

    public void setIsArray(boolean isArray) {
        this.isArray = isArray;
    }

    public void setbType(TokenType.tokenType bType) {
        this.bType = bType;
    }
}
