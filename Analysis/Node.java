package Analysis;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public abstract class Node {
    abstract void print(FileWriter output) throws IOException;
}

class CompUnit extends Node{
    public ArrayList<Decl> declArrayList;
    public ArrayList<FuncDef> funcDefArrayList;
    public MainFuncDef mainFuncDef;

    public CompUnit(){
        declArrayList = new ArrayList<>();
        funcDefArrayList = new ArrayList<>();
        mainFuncDef = null;
    }

    @Override
    void print(FileWriter output) throws IOException {
        output.write("<CompUnit>" + "\n");
    }
}

abstract class Decl extends Node{
}

class ConstDecl extends Decl{
    @Override
    void print(FileWriter output) throws IOException{
        output.write("<ConstDecl>" + "\n");
    }
}

class ValDecl extends Decl{
    @Override
    void print(FileWriter output) throws IOException{
        output.write("<VarDecl>" + "\n");
    }
}

class ConstDef extends Node{

}

class FuncDef extends Node{

}

class MainFuncDef extends Node{

}

class Stmt extends Node{

}

class IfStmt extends Node{

}

class ForStmt extends Node{

}

class Indent extends Node{

}