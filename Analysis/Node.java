package Analysis;

import java.util.ArrayList;

public class Node {

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
}

class Decl extends Node{

}

class ConstDecl extends Node{

}

class ValDecl extends Node{

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