package Analysis.frontend.Tree;

import Analysis.frontend.Token.Token;

import java.io.FileWriter;
import java.io.IOException;

public class FuncDef extends Node {
    private FuncType funcType;
    private Token ident;
    private FuncFParams funcFParams;
    private Block block;

    public FuncDef() {
        funcType = null;
        ident = null;
        funcFParams = null;
        block = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<FuncDef>" + "\n");
    }

    public void setFuncFParams(FuncFParams funcFParams) {
        this.funcFParams = funcFParams;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public void setFuncType(FuncType funcType) {
        this.funcType = funcType;
    }

    public void setIdent(Token ident) {
        this.ident = ident;
    }

    public FuncType getFuncType() {
        return funcType;
    }

    public Block getBlock() {
        return block;
    }

    public Token getIdent() {
        return ident;
    }

    public FuncFParams getFuncFParams() {
        return funcFParams;
    }
}
