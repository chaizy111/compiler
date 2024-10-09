package Tree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class CompUnit extends Node {
    public ArrayList<Decl> declArrayList;
    public ArrayList<FuncDef> funcDefArrayList;
    public MainFuncDef mainFuncDef;

    public CompUnit() {
        declArrayList = new ArrayList<>();
        funcDefArrayList = new ArrayList<>();
        mainFuncDef = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<CompUnit>" + "\n");
    }
}
