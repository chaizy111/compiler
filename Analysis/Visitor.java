package Analysis;

import Analysis.Symbol.Symbol;
import Analysis.Symbol.SymbolTable;
import Analysis.Symbol.Value.FuncValue;
import Analysis.Symbol.Value.Value;
import Analysis.Token.Token;
import Analysis.Token.TokenType;
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
    private int maxTableId;

    public Visitor(CompUnit compUnit, FileWriter outputfile, ErrorDealer errorDealer) {
        this.compUnit = compUnit;
        this.outputfile = outputfile;
        this.errorDealer = errorDealer;
        this.symbolTables = new LinkedHashMap<>();
        this.nowTableId = 0;
        this.maxTableId = 0;
    }

    public void visit() throws IOException {
        // 先建好第一个symbolTable，再visitCompUnit，最后输出
        newSymbolTable();
        visitCompUnit(this.compUnit);
        printSymbolTables();
    }

    private void newSymbolTable() {
        // 新建一个symbolTable并加到tables中，再将当前表号改为新表号，说明当前已进入新表进行处理
        // 最后维护最大table的id，以便于为下个表分配序号
        SymbolTable symbolTable = new SymbolTable(maxTableId + 1, nowTableId);
        symbolTables.put(symbolTable.getId(), symbolTable);
        nowTableId = symbolTable.getId();
        maxTableId = Math.max(symbolTable.getId(), maxTableId);
    }

    private void returnFatherTable() {
        nowTableId = symbolTables.get(nowTableId).getFatherId();
    }

    private void addSymbol(Symbol s) {
        SymbolTable nowSymbolTable = symbolTables.get(nowTableId);
        nowSymbolTable.addSymbol(s);
        symbolTables.put(nowTableId, nowSymbolTable);
    }

    public Symbol getSymbol(String symbolName) {
        int index = nowTableId;
        while (index != 0) {
            SymbolTable s = symbolTables.get(index);
            for (String string : s.getDirectory().keySet()) {
                if (string.equals(symbolName)) return s.getDirectory().get(string);
            }
            index = s.getFatherId();
        }
        return null;
    }

    private boolean isDuplicateSymbol(String symbolName) {
        int index = nowTableId;
        SymbolTable s = symbolTables.get(index);
        for (String string : s.getDirectory().keySet()) {
            if (string.equals(symbolName)) return true;
        }
        return false;
    }

    private boolean isUnDefinedSymbol(String symbolName) {
        int index = nowTableId;
        while (index != 0) {
            SymbolTable s = symbolTables.get(index);
            for (String string : s.getDirectory().keySet()) {
                if (string.equals(symbolName)) return false;
            }
            index = s.getFatherId();
        }
        return true;
    }

    private boolean isConstSymbol(String symbolName) {
        int index = nowTableId;
        while (index != 0) { // 一直往上找直到0号符号表
            SymbolTable s = symbolTables.get(index);
            for (String string : s.getDirectory().keySet()) {
                if (string.equals(symbolName)) return s.getDirectory().get(symbolName).isConst();
            }
            index = s.getFatherId();
        }
        return false;
    }

    private void visitCompUnit(CompUnit compUnit) {
        if (compUnit == null) return;
        for (Decl d : compUnit.getDeclArrayList()) visitDecl(d);
        for (FuncDef f : compUnit.getFuncDefArrayList()) visitFuncDef(f);
        visitMainFuncDef(compUnit.getMainFuncDef());
    }

    private void visitDecl(Decl decl) {
        if (decl == null) return;
        if (decl instanceof ConstDecl) visitConstDecl((ConstDecl) decl);
        else if (decl instanceof ValDecl) visitVarDecl((ValDecl) decl);
    }

    private void visitConstDecl(ConstDecl constDecl) {
        if (constDecl == null) return;
        for (ConstDef c : constDecl.getConstDefList()) {
            visitConstDef(c, constDecl.getbType());
        }
    }

    private void visitConstDef(ConstDef constDef, TokenType.tokenType bType) {
        if (constDef == null) return;
        // 配置symbol
        Symbol s = new Symbol(nowTableId, constDef.getIdent().getString());
        if(constDef.getConstExp() == null) s.setType(0);
        else s.setType(1);
        if (bType.equals(TokenType.tokenType.INTTK)) s.setBtype(0);
        else s.setBtype(1);
        s.setConst(true);
        s.setValue(visitConstInitVal(constDef.getConstInitVal()));
        //错误处理
        if(isDuplicateSymbol(s.getSymbolName())) {
            int errorLine = constDef.getIdent().getLine();
            errorDealer.errorB(errorLine);
            return;
        }
        // 配置完成并实现错误处理后，在最后把symbol加到表中
        addSymbol(s);
    }

    //TODO
    private Value visitConstInitVal(ConstInitVal constInitVal) {
        return null;
    }

    private void visitVarDecl(ValDecl valDecl) {
        if (valDecl == null) return;
        for (ValDef v : valDecl.getVarDefList()) {
            visitVarDef(v, valDecl.getbType());
        }
    }

    private void visitVarDef(ValDef valDef, TokenType.tokenType bType) {
        if (valDef == null) return;
        // 配置symbol
        Symbol s = new Symbol(nowTableId, valDef.getIdent().getString());
        if(valDef.getConstExp() == null) s.setType(0);
        else s.setType(1);
        if (bType.equals(TokenType.tokenType.INTTK)) s.setBtype(0);
        else s.setBtype(1);
        s.setConst(false);
        // 与constDef的不同只有有没有initVal
        if (valDef.getInitVal() != null) s.setValue(visitInitVal(valDef.getInitVal()));
        //错误处理
        if(isDuplicateSymbol(s.getSymbolName())) {
            int errorLine = valDef.getIdent().getLine();
            errorDealer.errorB(errorLine);
            return;
        }
        // 配置完成并实现错误处理后，在最后把symbol加到表中
        addSymbol(s);
    }

    //TODO
    private Value visitInitVal(InitVal initVal) {
        return null;
    }

    private void visitFuncDef(FuncDef funcDef) {
        if (funcDef == null) return;
        // 遇到funcDef，要先把函数名这个symbol放到当前的symbolTable中，
        Symbol s = new Symbol(nowTableId, funcDef.getIdent().getString());
        s.setType(2);
        int bType = visitFuncType(funcDef.getFuncType());
        s.setBtype(bType);
        s.setConst(false);
        // 再新建一个symbolTable，
        newSymbolTable();
        // 进行后续的分析
        Value v = visitFuncFParams(funcDef.getFuncFParams());
        // 把存储参数的符号表的编号传给s
        s.setValue(v);
        visitFuncBlock(funcDef.getBlock(), bType == 2);
        // 最后再返回上一级symbolTable（即将nowTableId改成fatherTableId）
        returnFatherTable();
        //错误处理,将s加到符号表中
        if(isDuplicateSymbol(s.getSymbolName())) {
            int errorLine = funcDef.getIdent().getLine();
            errorDealer.errorB(errorLine);
        } else {
            addSymbol(s);
        }
    }

    private void visitMainFuncDef(MainFuncDef mainFuncDef) {
        if (mainFuncDef == null) return;
        newSymbolTable();
        visitFuncBlock(mainFuncDef.getBlock(), false);
        returnFatherTable();
    }

    private int visitFuncType(FuncType funcType) {
        if (funcType == null) return 0;
        TokenType.tokenType t = funcType.getToken().getType();
        if (t == TokenType.tokenType.INTTK) return 0;
        else if (t == TokenType.tokenType.CHARTK) return 1;
        else return 2;
    }

    private Value visitFuncFParams(FuncFParams funcFParams) {
        if (funcFParams == null || funcFParams.getFuncFParamArrayList().isEmpty())
            return new FuncValue(nowTableId, 0);
        for (FuncFParam f:funcFParams.getFuncFParamArrayList()) {
            visitFuncFParam(f);
        }
        return new FuncValue(nowTableId, funcFParams.getFuncFParamArrayList().size());
    }

    private void visitFuncFParam(FuncFParam funcFParam) {
        if (funcFParam == null) return;
        // 配置symbol
        Symbol s = new Symbol(nowTableId, funcFParam.getIdent().getString());
        if (funcFParam.isArray()) s.setType(1);
        else s.setType(0);
        if (funcFParam.getbType().equals(TokenType.tokenType.INTTK)) s.setBtype(0);
        else s.setBtype(1);
        s.setConst(false);
        //错误处理
        if(isDuplicateSymbol(s.getSymbolName())) {
            int errorLine = funcFParam.getIdent().getLine();
            errorDealer.errorB(errorLine);
            return;
        }
        // 配置完成并实现错误处理后，在最后把symbol加到表中
        addSymbol(s);
    }

    // 由于处理逻辑不同，所以把不同的block分开
    private void visitBlock(Block block, boolean isInForBlock) { // 两种情况，循环块与非循环块
        if (block == null) return;
        newSymbolTable();
        for (BlockItem b : block.getBlockItemArrayList()) {
            visitBlockItem(b, isInForBlock);
        }
        returnFatherTable();
    }

    private void visitFuncBlock(Block block, boolean isVoid) {
        if (block == null) return;
        ReturnStmt returnStmt = null;
        for (BlockItem b : block.getBlockItemArrayList()) {
            if (b instanceof ReturnStmt) { // returnStmt单独处理
                returnStmt = (ReturnStmt) b;
                // 错误处理
                if (isVoid) {
                    if (returnStmt.getExp() != null) {
                        int errorLine = returnStmt.getLine();
                        errorDealer.errorF(errorLine);
                    }
                }
            } else if (b instanceof Decl) {
                visitDecl((Decl) b);
            } else if (b instanceof Stmt) {
                visitStmt((Stmt) b, false);
            }
        }
        // 错误处理
        if (!isVoid && returnStmt == null) {
            int errorLine = block.getEndLine();
            errorDealer.errorG(errorLine);
        }
    }

    private void visitBlockItem(BlockItem blockItem, boolean isInForBlock) {
        if (blockItem == null) return;
        if (blockItem instanceof Decl) visitDecl((Decl) blockItem);
        else visitStmt((Stmt) blockItem, isInForBlock);
    }

    private void visitStmt(Stmt stmt, boolean isInForBlock) {
        if (stmt == null) return;
        // 由于要对for有关block进行判断，来看是否出现m错，设置了isInForBlock变量，这个变量的相关逻辑比较绕，
        // 主要就是如果是在for的stmt是block型，则在visitBlock时就将这个属性设为true，之后这个属性便会层层下传，直到for的block分析结束
        if (stmt instanceof IfStmt) visitIf((IfStmt) stmt, isInForBlock);
        else if (stmt instanceof For) visitFor((For) stmt);
        else if (stmt instanceof ReturnStmt) visitReturnStmt((ReturnStmt) stmt);
        else if (stmt instanceof PrintfStmt) visitPrintfStmt((PrintfStmt) stmt);
        else if (stmt instanceof LValStmt) visitLValStmt((LValStmt) stmt);
        else if (stmt.getbOrC() != null) { // 处理break和continue
            if (!isInForBlock) { //不是for循环中的break与continue，直接错误处理
                int errorLine = stmt.getbOrC().getLine();
                errorDealer.errorM(errorLine);
            }
        } else if (stmt.getB() != null) {
            visitBlock(stmt.getB(), isInForBlock);
        } else if (stmt.getE() != null) {
            visitExp(stmt.getE());
        }
    }

    private void visitIf(IfStmt ifStmt, boolean isInForBlock) {
        if (ifStmt == null) return;
        visitCond(ifStmt.getC());
        visitStmt(ifStmt.getS1(), isInForBlock);
        if (ifStmt.getS2() != null) visitStmt(ifStmt.getS2(), isInForBlock);
    }

    private void visitFor(For f) {
        if (f == null) return;
        visitForStmt(f.getForStmt1());
        visitCond(f.getC());
        visitForStmt(f.getForStmt2());
        if (f.getS().getB() != null) visitBlock(f.getS().getB(), true);
        else visitStmt(f.getS(), false);
    }

    private void visitReturnStmt(ReturnStmt returnStmt) {
        if (returnStmt == null) return;
        //这里的visit是非func里的returnStmt，按理来说不会走到这里
        System.out.println("There may be something wrong in func dealing\n");
    }

    private void visitPrintfStmt(PrintfStmt printfStmt) { // 先处理错误，再visit里边的属性
        if (printfStmt == null) return;
        //错误处理
        if (printfStmt.getFCharacterNumInString() != printfStmt.getExpArrayList().size()) {
            int errorLine = printfStmt.getLine();
            errorDealer.errorL(errorLine);
        }
        for (Exp e : printfStmt.getExpArrayList()) {
            visitExp(e);
        }
    }

    private void visitLValStmt(LValStmt lValStmt) {
        if (lValStmt == null) return;
        //先错误处理
        Token t = lValStmt.getlVal().getIdent();
        if (isConstSymbol(t.getString())) {
            int errorLine = t.getLine();
            errorDealer.errorH(errorLine);
        }
        //再处理LVal
        visitLVal(lValStmt.getlVal());
    }

    private void visitForStmt(ForStmt forStmt) {
        if (forStmt == null) return;
        //先错误处理
        Token t = forStmt.getlVal().getIdent();
        if (isConstSymbol(t.getString())) {
            int errorLine = t.getLine();
            errorDealer.errorH(errorLine);
        }
        //再处理LVal与exp
        visitLVal(forStmt.getlVal());
        visitExp(forStmt.getExp());
    }

    private void visitExp(Exp exp) {
        if (exp == null) return;
        visitAddExp(exp.getAddExp());
    }

    private void visitCond(Cond cond) {
        if (cond == null) return;
        visitLOrExp(cond.getlOrExp());
    }

    private void visitLVal(LVal lVal) {
        if (lVal == null) return;
        //先错误处理
        if (isUnDefinedSymbol(lVal.getIdent().getString())) {
            int errorLine = lVal.getIdent().getLine();
            errorDealer.errorC(errorLine);
        }
        //再处理exp
        visitExp(lVal.getExp());
    }

    private void visitPrimaryExp(PrimaryExp primaryExp) {
        if (primaryExp == null) return;
        if (primaryExp.getExp() != null) visitExp(primaryExp.getExp());
        else if (primaryExp.getlVal() != null) visitLVal(primaryExp.getlVal());
        else if (primaryExp.getNumber() != null) visitNumber(primaryExp.getNumber());
        else if (primaryExp.getCharacter() != null) visitCharacter(primaryExp.getCharacter());
    }

    private Value visitNumber(Number number) {
        return null;
    }

    private Value visitCharacter(Character character) {
        return null;
    }

    private void visitUnaryExp(UnaryExp unaryExp) {
        if (unaryExp == null) return;
        if (unaryExp.getPrimaryExp() != null) visitPrimaryExp(unaryExp.getPrimaryExp());
        else if (unaryExp.getUnaryExp() != null) {
            visitUnaryOp(unaryExp.getUnaryOp());
            visitUnaryExp(unaryExp.getUnaryExp());
        } else if (unaryExp.getIdent() != null) {
            Token t = unaryExp.getIdent();
            // 先处理错误c
            if (isUnDefinedSymbol(t.getString())) {
                int errorLine = t.getLine();
                errorDealer.errorC(errorLine);
            } else {
                Symbol s = getSymbol(t.getString());
                FuncValue sValue = (FuncValue) s.getValue();
                //再处理错误d
                if (unaryExp.getFuncRParams() != null) {
                    if (sValue.getParaNum() != unaryExp.getFuncRParams().getExpArrayList().size()) {
                        int errorLine = t.getLine();
                        errorDealer.errorD(errorLine);
                    } else {//再处理错误e

                    }
                }
            }
        }
    }

    private void visitUnaryOp(UnaryOp unaryOp) {

    }

    private void visitFuncRParams(FuncRParams funcRParams) {

    }

    private void visitMulExp(MulExp mulExp) {
        if (mulExp == null) return;
        for (UnaryExp u : mulExp.getUnaryExpArrayList()) {
            visitUnaryExp(u);
        }
    }

    private void visitAddExp(AddExp addExp) {
        if (addExp == null) return;
        for (MulExp m : addExp.getMulExpArrayList()) visitMulExp(m);
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
        visitAddExp(constExp.getAddExp());
    }

    public void printSymbolTables() throws IOException {
        for(SymbolTable s:symbolTables.values()) s.printSymbolTable(outputfile);
    }
}
