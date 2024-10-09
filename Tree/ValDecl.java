package Tree;

import Analysis.Token.TokenType;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ValDecl extends Decl {
    private TokenType.tokenType bType;
    private ArrayList<ValDef> varDefList;

    public ValDecl() {
        bType = null;
        varDefList = new ArrayList<>();
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<VarDecl>" + "\n");
    }

    public void setbType(TokenType.tokenType bType) {
        this.bType = bType;
    }

    public void addVarDefList(ValDef valDef) {
        this.varDefList.add(valDef);
    }
}
