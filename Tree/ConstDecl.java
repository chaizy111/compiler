package Tree;

import Analysis.Token.TokenType;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ConstDecl extends Decl {
    private TokenType.tokenType bType;
    private ArrayList<ConstDef> constDefList;

    public ConstDecl() {
        bType = null;
        constDefList = new ArrayList<>();
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<ConstDecl>" + "\n");
    }

    public void setbType(TokenType.tokenType bType) {
        this.bType = bType;
    }

    public void addConstDefList(ConstDef constDef) {
        this.constDefList.add(constDef);
    }
}
