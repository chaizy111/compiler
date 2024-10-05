package Analysis;

import com.sun.tools.javac.Main;

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
    public TokenType.tokenType bType;
    public ArrayList<ConstDef> constDefList;

    public ConstDecl(){
        bType = null;
        constDefList = new ArrayList<>();
    }

    @Override
    void print(FileWriter output) throws IOException{
        output.write("<ConstDecl>" + "\n");
    }
}

class ValDecl extends Decl{
    public TokenType.tokenType bType;
    public ArrayList<ValDef> varDefList;

    public ValDecl(){
        bType = null;
        varDefList = new ArrayList<>();
    }

    @Override
    void print(FileWriter output) throws IOException{
        output.write("<VarDecl>" + "\n");
    }
}

class ConstDef extends Node{
    public ConstExp constExp;
    public ConstInitVal constInitVal;

    public ConstDef() {
        constExp = null;
        constInitVal = null;
    }
    @Override
    void print(FileWriter output) throws IOException{
        output.write("<ConstDef>" + "\n");
    }
}

class FuncDef extends Node{
    public ArrayList<FuncParam> funcParamList;
    public Block block;

    public FuncDef(){
        funcParamList = null;
        block = null;
    }
    @Override
    void print(FileWriter output) throws IOException{
        output.write("<FuncDef>" + "\n");
    }
}

class ValDef extends Node{
    public ConstExp constExp;
    public ConstInitVal constInitVal;

    public ValDef() {
        constExp = null;
        constInitVal = null;
    }
    @Override
    void print(FileWriter output) throws IOException{
        output.write("<VarDef>" + "\n");
    }
}

class MainFuncDef extends Node{
    public Block block;

    public MainFuncDef(){
        block = null;
    }
    @Override
    void print(FileWriter output) throws IOException{
        output.write("<MainFuncDef>" + "\n");
    }
}

class ConstInitVal extends Node{
    public boolean isString;
    public ArrayList<ConstExp> constExpArrayList;

    public ConstInitVal(){
        isString = false;
        constExpArrayList = new ArrayList<>();
    }
    void print(FileWriter output) throws IOException{
        output.write("<ConstInitVal>" + "\n");
    }
}

class FuncParam extends Node{
    public boolean isArray;
    public TokenType.tokenType bType;

    public FuncParam(){
        isArray = false;
        bType = null;
    }
    void print(FileWriter output) throws IOException{
        output.write("<FuncParam>" + "\n");
    }
}

class Stmt extends Node{
    @Override
    void print(FileWriter output) throws IOException{
        output.write("<Stmt>" + "\n");
    }
}

class IfStmt extends Stmt{

}

class ForStmt extends Stmt{

}

class LVal extends Node{
    @Override
    void print(FileWriter output) throws IOException{
        output.write("<LVal>" + "\n");
    }
}

class Block extends Node{
    @Override
    void print(FileWriter output) throws IOException{
        output.write("<Block>" + "\n");
    }
}

class Exp extends Node{
    @Override
    void print(FileWriter output) throws IOException{
        output.write("<Exp>" + "\n");
    }
}

class ConstExp extends Node{
    public AddExp addExp;

    public ConstExp(){
        addExp = null;
    }
    @Override
    void print(FileWriter output) throws IOException{
        output.write("<ConstExp>" + "\n");
    }
}

class AddExp extends Node{
    public ArrayList<MulExp> mulExpArrayList;

    public AddExp(){
       mulExpArrayList = new ArrayList<>();
    }
    @Override
    void print(FileWriter output) throws IOException{
        output.write("<AddExp>" + "\n");
    }
}

class MulExp extends Node{
    public ArrayList<UnaryExp> unaryExpArrayList;

    public MulExp(){
        unaryExpArrayList = new ArrayList<>();
    }
    @Override
    void print(FileWriter output) throws IOException{
        output.write("<MulExp>" + "\n");
    }
}

class UnaryExp extends Node{
    @Override
    void print(FileWriter output) throws IOException{
        output.write("<UnaryExp>" + "\n");
    }
}

class PrimaryExp extends Node{
    @Override
    void print(FileWriter output) throws IOException{
        output.write("<PrimaryExp>" + "\n");
    }
}

class RelExp extends Node{
    @Override
    void print(FileWriter output) throws IOException{
        output.write("<RelExp>" + "\n");
    }
}

class EqExp extends Node{
    @Override
    void print(FileWriter output) throws IOException{
        output.write("<EqExp>" + "\n");
    }
}

class LAndExp extends Node{
    @Override
    void print(FileWriter output) throws IOException{
        output.write("<LAndExp>" + "\n");
    }
}

class LOrExp extends Node{
    @Override
    void print(FileWriter output) throws IOException{
        output.write("<LOrExp>" + "\n");
    }
}