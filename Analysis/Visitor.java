package Analysis;

import Analysis.Symbol.SymbolTable;
import Tree.*;
import Error.ErrorDealer;
import Tree.Character;
import Tree.Number;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;

public class Visitor {
    private FileWriter outputfile;
    private CompUnit compUnit;
    private LinkedHashMap<Integer, SymbolTable> symbolTables;
    private ErrorDealer errorDealer;
    private int nowTableId;

    public Visitor(CompUnit compUnit, FileWriter outputfile, ErrorDealer errorDealer) {
        this.compUnit = compUnit;
        this.outputfile = outputfile;
        this.errorDealer = errorDealer;
        this.symbolTables = new LinkedHashMap<>();
        this.nowTableId = 0;
    }
    //TODO
    public void visit() throws IOException {
        printSymbolTables();
    }

    private void visitCompUnit(CompUnit compUnit) {

    }

    private void visitDecl(Decl decl) {

    }

    private void visitConstDecl(ConstDecl constDecl) {

    }

    private void visitConstDef(ConstDef constDef) {

    }

    private void visitConstInitVal(ConstInitVal constInitVal) {

    }

    private void visitVarDecl(ValDecl valDecl) {

    }

    private void visitVarDef(ValDef valDef) {

    }

    private void visitInitVal(InitVal initVal) {

    }

    private void visitFuncDef(FuncDef funcDef) {

    }

    private void visitMainFuncDef(MainFuncDef mainFuncDef) {

    }

    private void visitFuncType(FuncType funcType) {

    }

    private void visitFuncFParams(FuncFParams funcFParams) {

    }

    private void visitFuncFParam(FuncFParam funcFParam) {

    }

    private void visitBlock(Block block) {

    }

    private void visitBlockItem(BlockItem blockItem) {

    }

    private void visitStmt(Stmt stmt) {

    }

    private void visitForStmt(ForStmt forStmt) {

    }

    private void visitExp(Exp exp) {

    }

    private void visitCond(Cond cond) {

    }

    private void visitLVal(LVal lVal) {

    }

    private void visitPrimaryExp(PrimaryExp primaryExp) {

    }

    private void visitNumber(Number number) {

    }

    private void visitCharacter(Character character) {

    }

    private void visitUnaryExp(UnaryExp unaryExp) {

    }

    private void visitUnaryOp(UnaryOp unaryOp) {

    }

    private void visitFuncRParams(FuncRParams funcRParams) {

    }

    private void visitMulExp(MulExp mulExp) {

    }

    private void visitAddExp(AddExp addExp) {

    }

    private void visitRelExp(RelExp relExp) {

    }

    private void visitEqExp(EqExp eqExp) {

    }

    private void visitLAndExp(LAndExp lAndExp) {

    }

    private void visitLOrExp(LOrExp lOrExp) {

    }

    private void visitConstExp(ConstExp constExp) {

    }

    public void printSymbolTables() throws IOException {
        for(SymbolTable s:symbolTables.values()) s.printSymbolTable(outputfile);
    }
}
