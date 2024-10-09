package Tree;

import Analysis.Token.TokenType;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ValDecl extends Decl {
    public TokenType.tokenType bType;
    public ArrayList<ValDef> varDefList;

    public ValDecl() {
        bType = null;
        varDefList = new ArrayList<>();
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<VarDecl>" + "\n");
    }
}
