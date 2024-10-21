package Tree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class CompUnit extends Node {
    private ArrayList<Decl> declArrayList;
    private ArrayList<FuncDef> funcDefArrayList;
    private MainFuncDef mainFuncDef;

    public CompUnit() {
        declArrayList = new ArrayList<>();
        funcDefArrayList = new ArrayList<>();
        mainFuncDef = null;
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<CompUnit>" + "\n");
    }

    public void addDeclArrayList(Decl decl) {
        this.declArrayList.add(decl);
    }

    public void addFuncDefArrayList(FuncDef funcDef) {
        this.funcDefArrayList.add(funcDef);
    }

    public void setMainFuncDef(MainFuncDef mainFuncDef) {
        this.mainFuncDef = mainFuncDef;
    }

    public ArrayList<Decl> getDeclArrayList() {
        return declArrayList;
    }

    public ArrayList<FuncDef> getFuncDefArrayList() {
        return funcDefArrayList;
    }

    public MainFuncDef getMainFuncDef() {
        return mainFuncDef;
    }
}
