package Analysis.frontend.Tree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FuncFParams extends Node {
    private ArrayList<FuncFParam> funcFParamArrayList;

    public FuncFParams() {
        this.funcFParamArrayList = new ArrayList<>();
    }

    @Override
    public void print(FileWriter output) throws IOException {
        output.write("<FuncFParams>" + "\n");
    }

    public void setFuncFParamArrayList(ArrayList<FuncFParam> funcFParamArrayList) {
        this.funcFParamArrayList = funcFParamArrayList;
    }

    public ArrayList<FuncFParam> getFuncFParamArrayList() {
        return funcFParamArrayList;
    }
}
