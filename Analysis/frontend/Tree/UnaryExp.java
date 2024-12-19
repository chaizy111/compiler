package Analysis.frontend.Tree;

import Analysis.frontend.Token.Token;
import Analysis.frontend.Token.TokenType;

import java.io.FileWriter;
import java.io.IOException;

public class UnaryExp extends Node {
    private Token ident;
    private UnaryOp unaryOp;
    private UnaryExp unaryExp;
    private PrimaryExp primaryExp;
    private FuncRParams funcRParams;

    public UnaryExp() {
        this.ident = null;
        this.unaryOp = null;
        this.unaryExp = null;
        this.primaryExp = null;
        this.funcRParams = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<UnaryExp>" + "\n");
    }

    public void setIdent(Token ident) {
        this.ident = ident;
    }

    public void setUnaryOp(UnaryOp unaryOp) {
        this.unaryOp = unaryOp;
    }

    public void setUnaryExp(UnaryExp unaryExp) {
        this.unaryExp = unaryExp;
    }

    public void setPrimaryExp(PrimaryExp primaryExp) {
        this.primaryExp = primaryExp;
    }

    public void setFuncRParams(FuncRParams funcRParams) {
        this.funcRParams = funcRParams;
    }

    public Token getIdent() {
        return ident;
    }

    public UnaryExp getUnaryExp() {
        return unaryExp;
    }

    public UnaryOp getUnaryOp() {
        return unaryOp;
    }

    public PrimaryExp getPrimaryExp() {
        return primaryExp;
    }

    public FuncRParams getFuncRParams() {
        return funcRParams;
    }

    public int getResult() {
        if(unaryOp != null) {
            return unaryOp.getToken().getType() == TokenType.tokenType.PLUS ? unaryExp.getResult() : -unaryExp.getResult();
        } else if (primaryExp != null) {
            return primaryExp.getResult();
        } else {
            return 0;
        }
    }
}
