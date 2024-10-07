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

abstract class Decl extends BlockItem{
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

abstract class BlockItem extends Node{

}

class Stmt extends BlockItem{
    public Block b;
    public Exp e;

    public Stmt(){
        b = null;
        e = null;
    }
    @Override
    void print(FileWriter output) throws IOException{
        output.write("<Stmt>" + "\n");
    }
}

class IfStmt extends Stmt{
    public Cond c;
    public Stmt s1;
    public Stmt s2;

    public IfStmt(){
        c = null;
        s1 = null;
        s2 = null;
    }
}

class ForStmt extends Stmt{
    public LValStmt l1;
    public LValStmt l2;
    public Cond c;
    public Stmt s;

    public ForStmt(){
        l1 = null;
        l2 = null;
        c = null;
        s = null;
    }
}

class ReturnStmt extends Stmt{
    public Exp exp;

    public ReturnStmt(){
        exp = null;
    }
}

class PrintfStmt extends Stmt{
    public ArrayList<Exp> expArrayList;

    public PrintfStmt(){
        expArrayList = new ArrayList<>();
    }
}

class LValStmt extends Stmt{
    public LVal lVal;
    public Exp exp;
    public boolean isGetInt;
    public boolean isGetChar;

    public LValStmt(){
        lVal = null;
        exp = null;
        isGetInt = false;
        isGetChar = false;
    }

    void print1(FileWriter output) throws IOException{
        output.write("<ForStmt>" + "\n");
    }
}

class LVal extends Node{
    public Exp exp;

    public LVal(){
        exp = null;
    }
    @Override
    void print(FileWriter output) throws IOException{
        output.write("<LVal>" + "\n");
    }
}

class Block extends Node{
    public ArrayList<BlockItem> blockItemArrayList;

    public Block(){
        blockItemArrayList = new ArrayList<>();
    }
    @Override
    void print(FileWriter output) throws IOException{
        output.write("<Block>" + "\n");
    }
}

class Exp extends Node{
    public AddExp addExp;

    public Exp(){
        addExp = null;
    }

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
    public UnaryExp unaryExp;
    public PrimaryExp primaryExp;
    public ArrayList<FuncParam> funcParamArrayList;

    public UnaryExp(){
        unaryExp = null;
        primaryExp = null;
        funcParamArrayList = new ArrayList<>();
    }

    @Override
    void print(FileWriter output) throws IOException{
        output.write("<UnaryExp>" + "\n");
    }
}

class PrimaryExp extends Node{
    public Exp exp;
    public LVal lVal;
    public boolean isNumber;
    public boolean isChar;

    public PrimaryExp(){
        exp = null;
        lVal = null;
        isNumber = false;
        isChar = false;
    }

    @Override
    void print(FileWriter output) throws IOException{
        output.write("<PrimaryExp>" + "\n");
    }
}

class Cond extends Node{
    public LOrExp lOrExp;

    public Cond(){
        lOrExp = null;
    }
    @Override
    void print(FileWriter output) throws IOException{
        output.write("<Cond>" + "\n");
    }
}

class RelExp extends Node{
    public ArrayList<AddExp> addExpArrayList;

    public RelExp(){
        addExpArrayList = new ArrayList<>();
    }
    @Override
    void print(FileWriter output) throws IOException{
        output.write("<RelExp>" + "\n");
    }
}

class EqExp extends Node{
    public ArrayList<RelExp> relExpArrayList;

    public EqExp(){
        relExpArrayList = new ArrayList<>();
    }
    @Override
    void print(FileWriter output) throws IOException{
        output.write("<EqExp>" + "\n");
    }
}

class LAndExp extends Node{
    public ArrayList<EqExp> eqExpArrayList;

    public LAndExp(){
        eqExpArrayList = new ArrayList<>();
    }
    @Override
    void print(FileWriter output) throws IOException{
        output.write("<LAndExp>" + "\n");
    }
}

class LOrExp extends Node{
    public ArrayList<LAndExp> lAndExpArrayList;

    public LOrExp(){
        lAndExpArrayList = new ArrayList<>();
    }
    @Override
    void print(FileWriter output) throws IOException{
        output.write("<LOrExp>" + "\n");
    }
}