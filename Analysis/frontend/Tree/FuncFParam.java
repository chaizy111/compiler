package Analysis.frontend.Tree;

import Analysis.frontend.Token.Token;
import Analysis.frontend.Token.TokenType;

import java.io.FileWriter;
import java.io.IOException;

public class FuncFParam extends Node {
    private boolean isArray;
    private TokenType.tokenType bType;
    private Token ident;

    public FuncFParam() {
        this.isArray = false;
        this.bType = null;
        this.ident = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<FuncFParam>" + "\n");
    }

    public void setIsArray(boolean isArray) {
        this.isArray = isArray;
    }

    public void setbType(TokenType.tokenType bType) {
        this.bType = bType;
    }

    public void setIdent(Token ident) {
        this.ident = ident;
    }

    public TokenType.tokenType getbType() {
        return bType;
    }

    public Token getIdent() {
        return ident;
    }

    public boolean isArray() {
        return isArray;
    }
}
