package Analysis.middle;


import Analysis.frontend.Token.Token;
import Analysis.frontend.Token.TokenType;
import Analysis.frontend.Tree.*;
import Analysis.frontend.Tree.Character;
import Analysis.frontend.Tree.Number;
import Analysis.middle.Symbol.Symbol;
import Analysis.middle.Symbol.SymbolTable;
import Analysis.middle.Symbol.Value.FuncValue;
import Analysis.middle.Symbol.Value.Value;
import Error.ErrorDealer;


import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class SymbolVisitor {
    private FileWriter outputfile;
    private CompUnit compUnit;
    private LinkedHashMap<Integer, SymbolTable> symbolTables;
    private ErrorDealer errorDealer;
    private int nowTableId;
    private int maxTableId;

    public SymbolVisitor(CompUnit compUnit, FileWriter outputfile, ErrorDealer errorDealer) {
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
        else {
            visitConstExp(constDef.getConstExp());
            s.setType(1);
        }
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

    //这里的分析先将等号右边作为一个整体传入Value，然后value保存在左边符号的符号表内，之后再对右边进行分析
    private Value visitConstInitVal(ConstInitVal constInitVal) {
        Value value = new Value();
        //value.setConstInitVal(constInitVal);
        if (constInitVal.getConstExp() != null) visitConstExp(constInitVal.getConstExp());
        else if (!constInitVal.getConstExpArrayList().isEmpty()) {
            for (ConstExp c: constInitVal.getConstExpArrayList()) {
                visitConstExp(c);
            }
        }
        return value;
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
        else {
            visitConstExp(valDef.getConstExp());
            s.setType(1);
        }
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

    //与constInitVal逻辑一致
    private Value visitInitVal(InitVal initVal) {
        Value value = new Value();
        //value.setInitVal(initVal);
        if (initVal.getExp() != null) visitExp(initVal.getExp());
        else if (!initVal.getExpArrayList().isEmpty()) {
            for (Exp e: initVal.getExpArrayList()) {
                visitExp(e);
            }
        }
        return value;
    }

    private void visitFuncDef(FuncDef funcDef) {
        if (funcDef == null) return;
        // 遇到funcDef，要先把函数名这个symbol放到当前的symbolTable中，
        Symbol s = new Symbol(nowTableId, funcDef.getIdent().getString());
        s.setType(2);
        int bType = visitFuncType(funcDef.getFuncType());
        s.setBtype(bType);
        s.setConst(false);
        if(isDuplicateSymbol(s.getSymbolName())) {
            int errorLine = funcDef.getIdent().getLine();
            errorDealer.errorB(errorLine);
        } else {
            addSymbol(s);
        }
        // 再新建一个symbolTable，
        // 注意函数的block与普通block的不同，函数的block需要在上一层建立，因为需要将参数进行存储，普通block在visitBlock函数中建立即可
        newSymbolTable();
        // 进行后续的分析，这里返回的value存储了参数相关的信息
        Value v = visitFuncFParams(funcDef.getFuncFParams());
        // 把存储参数的符号表的编号传给s
        s.setValue(v);
        // 分析后边的block，需要将函数是否为void类型传入函数以供后续判断
        visitFuncBlock(funcDef.getBlock(), bType == 2);
        // 最后再返回上一级symbolTable（即将nowTableId改成fatherTableId）
        returnFatherTable();
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
        //返回一个存储函数参数信息的符号表的序号以及函数参数的个数的value对象
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
    private void visitBlock(Block block, boolean isInForBlock, boolean isInVoidFunc) {
        // 多种情况，循环块与非循环块，是否为void函数中的块
        if (block == null) return;
        newSymbolTable();
        for (BlockItem b : block.getBlockItemArrayList()) {
            visitBlockItem(b, isInForBlock, isInVoidFunc);
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
                visitStmt((Stmt) b, false, isVoid);
            }
        }
        // 错误处理
        if (!isVoid && returnStmt == null) { //不是void型函数且没有返回语句
            int errorLine = block.getEndLine();
            errorDealer.errorG(errorLine);
        }
    }

    private void visitBlockItem(BlockItem blockItem, boolean isInForBlock, boolean isInVoidFunc) {
        if (blockItem == null) return;
        if (blockItem instanceof Decl) visitDecl((Decl) blockItem);
        else visitStmt((Stmt) blockItem, isInForBlock, isInVoidFunc);
    }

    private void visitStmt(Stmt stmt, boolean isInForBlock, boolean isInVoidFunc) {
        if (stmt == null) return;
        // 由于要对for有关block进行判断，来看是否出现m错，设置了isInForBlock变量，这个变量的相关逻辑比较绕，
        // 主要就是如果是在for的stmt是block型，则在visitBlock时就将这个属性设为true，之后这个属性便会层层下传，直到for的block分析结束
        // 还要对是否是在void函数中进行判断，如果是在void函数中，那if，for，以及return语句就需要注意进行报错，所以需要传递isInVoidFunc参数
        if (stmt instanceof IfStmt) visitIf((IfStmt) stmt, isInForBlock, isInVoidFunc);
        else if (stmt instanceof For) visitFor((For) stmt, isInVoidFunc);
        else if (stmt instanceof ReturnStmt) visitReturnStmt((ReturnStmt) stmt, isInVoidFunc);
        else if (stmt instanceof PrintfStmt) visitPrintfStmt((PrintfStmt) stmt);
        else if (stmt instanceof LValStmt) visitLValStmt((LValStmt) stmt);
        else if (stmt.getbOrC() != null) { // 处理break和continue
            if (!isInForBlock) { //不是for循环中的break与continue，直接错误处理
                int errorLine = stmt.getbOrC().getLine();
                errorDealer.errorM(errorLine);
            }
        } else if (stmt.getB() != null) {
            visitBlock(stmt.getB(), isInForBlock, isInVoidFunc);
        } else if (stmt.getE() != null) {
            visitExp(stmt.getE()); //
        }
    }

    private void visitIf(IfStmt ifStmt, boolean isInForBlock, boolean isInVoidFunc) {
        if (ifStmt == null) return;
        visitCond(ifStmt.getC());
        visitStmt(ifStmt.getS1(), isInForBlock, isInVoidFunc);
        if (ifStmt.getS2() != null) visitStmt(ifStmt.getS2(), isInForBlock, isInVoidFunc);
    }

    private void visitFor(For f, boolean isInVoidFunc) {
        if (f == null) return;
        visitForStmt(f.getForStmt1());
        visitCond(f.getC());
        visitForStmt(f.getForStmt2());
        if (f.getS().getB() != null) visitBlock(f.getS().getB(), true, isInVoidFunc);
        else visitStmt(f.getS(), true, isInVoidFunc);
    }

    private void visitReturnStmt(ReturnStmt returnStmt, boolean isInVoidFunc) {
        if (returnStmt == null) return;
        if (isInVoidFunc) { // 如果在void函数中，就报错
            if (returnStmt.getExp() != null) {
                int errorLine = returnStmt.getLine();
                errorDealer.errorF(errorLine);
                return;
            }
        }
        if (returnStmt.getExp() != null) visitExp(returnStmt.getExp());
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
        visitExp(lValStmt.getExp());
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

    //从这往下所有visit方法大多返回一个int型的返回值，代表其分析到底是什么类型，这些返回值都只为error e的判断服务，如果想要具体的value值，需要更改实现
    private int visitExp(Exp exp) {
        if (exp == null) return -1;
        return visitAddExp(exp.getAddExp());
    }

    private void visitCond(Cond cond) {
        if (cond == null) return;
        visitLOrExp(cond.getlOrExp());
    }

    private int visitLVal(LVal lVal) {
        if (lVal == null) return -1;
        //先错误处理
        if (isUnDefinedSymbol(lVal.getIdent().getString())) {
            int errorLine = lVal.getIdent().getLine();
            errorDealer.errorC(errorLine);
            return -1;
        }
        //再处理exp，这里使用t1与t2是看是数组还是变量，类似S[0]的情况
        int t1 = visitExp(lVal.getExp());
        int t2 = getSymbol(lVal.getIdent().getString()).judgeKindN();
        if (t2 == 2 || t2 == 3) {
            if (t1 == 0) return 0;
        }
        return t2;
    }

    private int visitPrimaryExp(PrimaryExp primaryExp) {
        if (primaryExp == null) return -1;
        if (primaryExp.getExp() != null) return visitExp(primaryExp.getExp());
        else if (primaryExp.getlVal() != null) return visitLVal(primaryExp.getlVal());
        else if (primaryExp.getNumber() != null) return visitNumber(primaryExp.getNumber());
        else if (primaryExp.getCharacter() != null) return visitCharacter(primaryExp.getCharacter());
        return -1;
    }

    private int visitNumber(Number number) {
        return 0;
    }

    private int visitCharacter(Character character) {
        return 0;
    }

    private int visitUnaryExp(UnaryExp unaryExp) {
        if (unaryExp == null) return -1;
        if (unaryExp.getPrimaryExp() != null) return visitPrimaryExp(unaryExp.getPrimaryExp());
        else if (unaryExp.getUnaryExp() != null) {
            visitUnaryOp(unaryExp.getUnaryOp());
            return visitUnaryExp(unaryExp.getUnaryExp());
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
                    } else {//再处理错误e，依次与定义比较每一个参数的类型
                        ArrayList<Integer> paraList = visitFuncRParams(unaryExp.getFuncRParams());
                        SymbolTable symbolTable = symbolTables.get(sValue.getParaTableId());
                        int i = 0;
                        for (Symbol ss:symbolTable.getDirectory().values()) {
                            if (ss.judgeKindN() != paraList.get(i)) {
                                int errorLine = t.getLine();
                                errorDealer.errorE(errorLine);
                                break;
                            }
                            i++;
                            if (i >= paraList.size())break;
                        }
                    }
                }
                return s.judgeKindN();
            }
        }
        return -1;
    }

    private void visitUnaryOp(UnaryOp unaryOp) {

    }

    private ArrayList<Integer> visitFuncRParams(FuncRParams funcRParams) {
        if (funcRParams == null) return null;
        ArrayList<Integer> l = new ArrayList<>();
        for (Exp e: funcRParams.getExpArrayList()) {
            l.add(visitExp(e));
        }
        return l;
    }

    private int visitMulExp(MulExp mulExp) {
        if (mulExp == null) return -1;
        int t = -2;
        for (UnaryExp u : mulExp.getUnaryExpArrayList()) {
            if(t == -2) { // 返回第一个unaryExp的类型即可
                t = visitUnaryExp(u);
            } else { //其他的也需要分析，可能会有错误出现
                visitUnaryExp(u);
            }
        }
        return t;
    }

    private int visitAddExp(AddExp addExp) {
        if (addExp == null) return -1;
        int t = -2;
        for (MulExp m : addExp.getMulExpArrayList()) {
            if(t == -2) { // 返回第一个MulExp的类型即可
                t = visitMulExp(m);
            } else {//其他的也需要分析，可能会有错误出现
                visitMulExp(m);
            }
        }
        return t;
    }

    private void visitRelExp(RelExp relExp) {
        for (AddExp a: relExp.getAddExpArrayList()) {
            visitAddExp(a);
        }
    }

    private void visitEqExp(EqExp eqExp) {
        for (RelExp r: eqExp.getRelExpArrayList()) {
            visitRelExp(r);
        }
    }

    private void visitLAndExp(LAndExp lAndExp) {
        for (EqExp e: lAndExp.getEqExpArrayList()) {
            visitEqExp(e);
        }
    }

    private void visitLOrExp(LOrExp lOrExp) {
        for (LAndExp l: lOrExp.getlAndExpArrayList()) {
            visitLAndExp(l);
        }
    }

    private void visitConstExp(ConstExp constExp) {
        visitAddExp(constExp.getAddExp());
    }

    public void printSymbolTables() throws IOException {
        for(SymbolTable s:symbolTables.values()) s.printSymbolTable(outputfile);
    }
}
