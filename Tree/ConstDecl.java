package Tree;

import Analysis.Token.TokenType;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ConstDecl extends Decl {
    public TokenType.tokenType bType;
    public ArrayList<ConstDef> constDefList;

    public ConstDecl() {
        bType = null;
        constDefList = new ArrayList<>();
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<ConstDecl>" + "\n");
    }
}
