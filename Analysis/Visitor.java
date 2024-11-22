package Analysis;

import Llvmir.IrModule;
import Llvmir.IrValue;
import Llvmir.Type.*;
import Llvmir.ValueType.Constant.IrConstant;
import Llvmir.ValueType.Constant.IrConstantArray;
import Llvmir.ValueType.Constant.IrConstantVal;
import Llvmir.ValueType.Function.IrArgument;
import Llvmir.ValueType.Function.IrFunction;
import Llvmir.ValueType.Instruction.*;
import Llvmir.ValueType.IrBasicBlock;
import Llvmir.ValueType.IrGlobalVariable;
import Symbol.Symbol;
import Symbol.SymbolTable;
import Symbol.Value.ArrayValue;
import Symbol.Value.FuncValue;
import Symbol.Value.Value;
import Analysis.Token.Token;
import Analysis.Token.TokenType;
import Symbol.Value.VarValue;
import Tree.*;
import Error.ErrorDealer;
import Tree.Character;
import Tree.Number;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class Visitor {
    private FileWriter outputfile;
    private CompUnit compUnit;
    private LinkedHashMap<Integer, SymbolTable> symbolTables;
    private ErrorDealer errorDealer;
    private int nowTableId;
    private int maxTableId;
    private IrModule irModule;
    private IrFunction nowIrFunction;

    public Visitor(CompUnit compUnit, FileWriter outputfile, ErrorDealer errorDealer) {
        this.compUnit = compUnit;
        this.outputfile = outputfile;
        this.errorDealer = errorDealer;
        this.symbolTables = new LinkedHashMap<>();
        this.nowTableId = 0;
        this.maxTableId = 0;
        this.irModule = new IrModule();
        this.nowIrFunction = new IrFunction();
    }

    //TODO：在各个visit中完善ir各个部分的构建，最后统一输出。visit需要返回分析得到的ir成分，最后统一输出（中间代码生成的主要逻辑）
    //TODO:要注意的一些问题：1.对于全局变量中的常量表达式，在生成的 LLVM IR 中需要直接算出其具体的值
    //TODO：2.对于局部变量，我们首先需要通过 alloca 指令分配一块内存，才能对其进行 load/store 操作
    //TODO：3.对于局部变量，要注意类型转换的情况
    //TODO：4.修改各个exp中getResult的逻辑，需要符号表的参与
    //TODO：将symbol相关方法拿出去（不重要，最后再做）
    public void visit() throws IOException {
        // 先建好第一个symbolTable，再visitCompUnit，最后输出
        newSymbolTable();
        visitCompUnit(compUnit);
        //printSymbolTables();
        printIrCode();
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

    public Symbol getSymbolByRegister(String reName) {
        int index = nowTableId;
        while (index != 0) {
            SymbolTable s = symbolTables.get(index);
            for (Symbol sym : s.getDirectory().values()) {
                if (sym.getIrValue().getRegisterName().equals(reName)) return sym;
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
        for (Decl d : compUnit.getDeclArrayList()) {
            irModule.addGlobalVariable(visitDecl(d));
        }
        for (FuncDef f : compUnit.getFuncDefArrayList()) {
            irModule.addFunction(visitFuncDef(f));
        }
        irModule.addFunction(visitMainFuncDef(compUnit.getMainFuncDef()));
    }

    private ArrayList<IrGlobalVariable> visitDecl(Decl decl) {
        //原visitDecl，用于全局声明语句的分析
        if (decl == null) return null;
        if (decl instanceof ConstDecl) {
            return visitConstDecl((ConstDecl) decl);
        } else if (decl instanceof ValDecl) {
            return visitVarDecl((ValDecl) decl);
        } else {
            return null;
        }
    }

    private ArrayList<IrInstruction> visitDeclInFunc(Decl decl) {
        //一个新的visitDecl，区别与原来的，用于局部声明语句的分析
        if (decl == null) return null;
        if (decl instanceof ConstDecl) {
            return visitConstDeclInFunc((ConstDecl) decl);
        } else if (decl instanceof ValDecl) {
            return visitVarDeclInFunc((ValDecl) decl);
        } else {
            return null;
        }
    }

    private ArrayList<IrGlobalVariable> visitConstDecl(ConstDecl constDecl) {
        //返回类型与VarDecl保持一致，但实际上什么都不返回，因为const类型的常量可以直接存在符号表中，不需要在中间代码输出
        ArrayList<IrGlobalVariable> res = new ArrayList<>();
        if (constDecl == null) return null;
        for (ConstDef c : constDecl.getConstDefList()) {
            visitConstDef(c, constDecl.getbType());
        }
        return res;
    }

    private ArrayList<IrInstruction> visitConstDeclInFunc(ConstDecl constDecl) {
        ArrayList<IrInstruction> res = new ArrayList<>();
        if (constDecl == null) return null;
        for (ConstDef c : constDecl.getConstDefList()) {
            res.addAll(visitConstDefInFunc(c, constDecl.getbType()));
        }
        return res;
    }

    private void visitConstDef(ConstDef constDef, TokenType.tokenType bType) { // 配置好符号表后直接返回void即可
        if (constDef == null) return;
        // 配置symbol
        Symbol s = new Symbol(nowTableId, constDef.getIdent().getString());
        if(constDef.getConstExp() == null) s.setType(0);
        else {
            //TODO：实验中所有中括号中的表达式全部为常量表达式，所以可以为constDef设置一个计算方法，直接得到其结果后存储起来，同理后边的Initval
            visitConstExp(constDef.getConstExp());
            s.setArraySize(constDef.getConstExp().getResult());
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
        } else {
            // 配置完成并实现错误处理后，在最后把symbol加到表中
            addSymbol(s);
        }
        //不需要配置IrGlobalVariable，直接将IrConstant存到symbol的IrValue中即可，到时候如果要使用，直接通过常量名字搜索并取IrValue中的值即可
        IrType t;
        if (s.getBtype() == 0) {
            t = new IrIntegerTy();
        } else {
            t = new IrCharTy();
        }
        if (s.getArraySize() == 0) { // 非数组型
            IrConstant constant = new IrConstantVal(((VarValue) s.getValue()).getItem());
            s.setIrValue(constant);
        } else { //数组型
            ArrayList<Integer> temp = ((ArrayValue) s.getValue()).getArray(s.getArraySize());
            ArrayList<IrConstantVal> arrays = new ArrayList<>();
            for (Integer i:temp) {
                IrConstantVal c = new IrConstantVal(i);
                arrays.add(c);
            }
            IrConstant constant = new IrConstantArray(arrays);
            constant.setType(t);
            s.setIrValue(constant);
        }
    }

    private ArrayList<IrInstruction> visitConstDefInFunc(ConstDef constDef, TokenType.tokenType bType) {
        //这里返回类型不是void原因是有可能会在InitVal的定义中出现计算语句，所以要返回一个instruction的list
        ArrayList<IrInstruction> instructions = new ArrayList<>();
        if (constDef == null) return null;
        // 配置symbol
        Symbol s = new Symbol(nowTableId, constDef.getIdent().getString());
        if(constDef.getConstExp() == null) s.setType(0);
        else {
            instructions.addAll(visitConstExp(constDef.getConstExp()).getTempInstructions());
            s.setArraySize(constDef.getConstExp().getResult());
            s.setType(1);
        }
        if (bType.equals(TokenType.tokenType.INTTK)) s.setBtype(0);
        else s.setBtype(1);
        s.setConst(true);

        IrType t; // 判断该符号的种类
        if (s.getBtype() == 0) {
            t = new IrIntegerTy();
        } else {
            t = new IrCharTy();
        }
        IrArrayTy nt = new IrArrayTy();
        if (s.getArraySize() != 0) {
            nt.setArrayType(t);
            nt.setArraySize(s.getArraySize());
            t = nt;
        }

        IrAlloca irAlloca = new IrAlloca(); //先分配内存，将内存分配指令alloca填到list中，并将这个内存分配指令作为该符号的IrValue
        irAlloca.setType(t);
        irAlloca.setType(nt);
        irAlloca.setRegisterName("%" + nowIrFunction.getNowRank());
        instructions.add(irAlloca);
        s.setIrValue(irAlloca);

        IrValue v = visitConstInitValInFunction(constDef.getConstInitVal());
        instructions.addAll(v.getTempInstructions()); // 分析右边部分，将产生的代码放到list中
        s.setIrValue(v);
        //错误处理
        if(isDuplicateSymbol(s.getSymbolName())) {
            int errorLine = constDef.getIdent().getLine();
            errorDealer.errorB(errorLine);
        } else {
            // 配置完成并实现错误处理后，在最后把symbol加到表中
            addSymbol(s);
        }
        return instructions;
    }

    //这里的分析先将等号右边作为一个整体传入Value，然后value保存在左边符号的符号表内，之后再对右边进行分析
    private Value visitConstInitVal(ConstInitVal constInitVal) {
        // TODO：已经实现了对于Initval的存储，现在需要优化代码实现，现在的逻辑有些冗余
        if (constInitVal.getConstExp() != null) { //只有1个exp
            visitConstExp(constInitVal.getConstExp());
            VarValue value = new VarValue();
            value.setItem(constInitVal.getConstExp().getResult());
            return value;
        } else if (!constInitVal.getConstExpArrayList().isEmpty()) { // 多个Exp
            ArrayValue value = new ArrayValue();
            for (ConstExp c: constInitVal.getConstExpArrayList()) {
                visitConstExp(c);
                value.addItem(c.getResult());
            }
            return value;
        } else { //String型
            ArrayValue value = new ArrayValue();
            String s = constInitVal.getStringConst().getString();
            for (java.lang.Character c: s.toCharArray()) {
                value.addItem(c);
            }
            return value;
        }
    }

    private IrValue visitConstInitValInFunction(ConstInitVal constInitVal) {
        //一个Exp用irConstantVal，多个或String型irConstantVal
        if (constInitVal.getConstExp() != null) { //只有1个exp
            return visitConstExp(constInitVal.getConstExp());
        } else if (!constInitVal.getConstExpArrayList().isEmpty()) { // 多个Exp
            IrValue res = new IrValue();
            for (ConstExp c: constInitVal.getConstExpArrayList()) {
                IrValue t = visitConstExp(c);
                res.addAllTempInstruction(t.getTempInstructions());
                res.addTempValue(t);
            }
            return res;
        } else { //String型
            ArrayList<IrConstantVal> list = new ArrayList<>();
            String s = constInitVal.getStringConst().getString();
            for (java.lang.Character c: s.toCharArray()) {
                list.add(new IrConstantVal(c));
            }
            IrConstantArray res = new IrConstantArray(list);
            res.setType(new IrCharTy());
            return res;
        }
    }

    private ArrayList<IrGlobalVariable> visitVarDecl(ValDecl valDecl) {
        ArrayList<IrGlobalVariable> res = new ArrayList<>();
        if (valDecl == null) return null;
        for (ValDef v : valDecl.getVarDefList()) {
            res.add(visitVarDef(v, valDecl.getbType()));
        }
        return res;
    }

    public ArrayList<IrInstruction> visitVarDeclInFunc(ValDecl valDecl) {
        ArrayList<IrInstruction> res = new ArrayList<>();
        if (valDecl == null) return null;
        for (ValDef v : valDecl.getVarDefList()) {
            res.addAll(visitVarDefInFunc(v, valDecl.getbType()));
        }
        return res;
    }

    private IrGlobalVariable visitVarDef(ValDef valDef, TokenType.tokenType bType) {
        if (valDef == null) return null;
        // 配置symbol
        Symbol s = new Symbol(nowTableId, valDef.getIdent().getString());
        if(valDef.getConstExp() == null) s.setType(0);
        else { //将中括号中的信息进行分析并传入符号表
            visitConstExp(valDef.getConstExp());
            s.setArraySize(valDef.getConstExp().getResult());
            s.setType(1);
        }
        if (bType.equals(TokenType.tokenType.INTTK)) s.setBtype(0);
        else s.setBtype(1);
        s.setConst(false);
        // 与constDef的不同只有有没有initVal
        if (valDef.getInitVal() != null) {
            s.setValue(visitInitVal(valDef.getInitVal()));
        }
        //错误处理
        if(isDuplicateSymbol(s.getSymbolName())) {
            int errorLine = valDef.getIdent().getLine();
            errorDealer.errorB(errorLine);
        } else {
            // 配置完成并实现错误处理后，在最后把symbol加到表中
            addSymbol(s);
        }
        //配置IrGlobalVariable
        IrGlobalVariable irGlobalVariable = new IrGlobalVariable("@" + s.getSymbolName());
        IrType t;
        if (s.getBtype() == 0) {
            t = new IrIntegerTy();
        } else {
            t = new IrCharTy();
        }
        irGlobalVariable.setType(t);
        if (s.getValue() != null) {
            if (s.getArraySize() == 0) { // 非数组型
                IrConstant constant = new IrConstantVal(((VarValue) s.getValue()).getItem());
                irGlobalVariable.setConstant(constant);
            } else { //数组型
                ArrayList<Integer> temp = ((ArrayValue) s.getValue()).getArray(s.getArraySize());
                ArrayList<IrConstantVal> arrays = new ArrayList<>();
                for (Integer i:temp) {
                    IrConstantVal c = new IrConstantVal(i);
                    arrays.add(c);
                }
                IrConstant constant = new IrConstantArray(arrays);
                constant.setType(t);
                irGlobalVariable.setConstant(constant);
                IrArrayTy nt = new IrArrayTy();
                nt.setArrayType(t);
                nt.setArraySize(s.getArraySize());
                irGlobalVariable.setType(nt);
            }
        } else {
            irGlobalVariable.setConstant(null);
        }
        irGlobalVariable.setRegisterName("@" + s.getSymbolName());
        s.setIrValue(irGlobalVariable); // 配置完成后将irGlobalVariable加到symbol中
        return irGlobalVariable;
    }

    private ArrayList<IrInstruction> visitVarDefInFunc(ValDef valDef, TokenType.tokenType bType) {
        ArrayList<IrInstruction> instructions = new ArrayList<>();
        if (valDef == null) return null;
        // 配置symbol
        Symbol s = new Symbol(nowTableId, valDef.getIdent().getString());
        if(valDef.getConstExp() == null) s.setType(0);
        else {
            instructions.addAll(visitConstExp(valDef.getConstExp()).getTempInstructions());
            s.setArraySize(valDef.getConstExp().getResult());
            s.setType(1);
        }
        if (bType.equals(TokenType.tokenType.INTTK)) s.setBtype(0);
        else s.setBtype(1);
        s.setConst(true);

        IrType t; // 判断该符号的种类
        if (s.getBtype() == 0) {
            t = new IrIntegerTy();
        } else {
            t = new IrCharTy();
        }
        IrArrayTy nt = new IrArrayTy();
        if (s.getArraySize() != 0) {
            nt.setArrayType(t);
            nt.setArraySize(s.getArraySize());
            t = nt;
        }

        IrAlloca irAlloca = new IrAlloca(); //先分配内存，将内存分配指令alloca填到list中，并将这个内存分配指令作为该符号的IrValue
        irAlloca.setType(t);
        irAlloca.setRegisterName("%" + nowIrFunction.getNowRank());
        instructions.add(irAlloca);
        s.setIrValue(irAlloca);

        IrValue v = visitInitValInFunction(valDef.getInitVal());
        instructions.addAll(v.getTempInstructions()); // 分析右边部分，将产生的代码放到list中
        //与constVarDef的区别，varDef不能直接使用，必须输出store语句进行store存储
        if (s.getArraySize() != 0) { //TODO:数组类型，多条IrStore

        } else { //val类型，一条IrStore
            IrStore irStore = new IrStore();
            instructions.add(irStore);
        }

        //错误处理
        if(isDuplicateSymbol(s.getSymbolName())) {
            int errorLine = valDef.getIdent().getLine();
            errorDealer.errorB(errorLine);
        } else {
            // 配置完成并实现错误处理后，在最后把symbol加到表中
            addSymbol(s);
        }
        return instructions;
    }

    //与constInitVal逻辑一致
    private Value visitInitVal(InitVal initVal) {
        // TODO：已经实现了对于Initval的存储，现在需要优化代码实现，现在的逻辑有些冗余
        if (initVal.getExp() != null) { // 只有一个Exp
            visitExp(initVal.getExp());
            VarValue value = new VarValue();
            value.setItem(initVal.getExp().getAddExp().getResult());
            return value;
        } else if (!initVal.getExpArrayList().isEmpty()) { // 多个Exp
            ArrayValue value = new ArrayValue();
            for (Exp e: initVal.getExpArrayList()) {
                visitExp(e);
                value.addItem(e.getAddExp().getResult());
            }
            return value;
        } else { //String型
            ArrayValue value = new ArrayValue();
            String s = initVal.getStringConst().getString();
            for (java.lang.Character c: s.toCharArray()) {
                value.addItem(c);
            }
            return value;
        }
    }

    private IrValue visitInitValInFunction(InitVal initVal) {
        //一个Exp用irConstantVal，多个或String型irConstantVal
        if (initVal.getExp() != null) { //只有1个exp
            return visitExp(initVal.getExp());
        } else if (!initVal.getExpArrayList().isEmpty()) { // 多个Exp
            IrValue res = new IrValue();
            for (Exp e: initVal.getExpArrayList()) {
                IrValue t = visitExp(e);
                res.addAllTempInstruction(t.getTempInstructions());
                res.addTempValue(t);
            }
            return res;
        } else { //String型
            ArrayList<IrConstantVal> list = new ArrayList<>();
            String s = initVal.getStringConst().getString();
            for (java.lang.Character c: s.toCharArray()) {
                list.add(new IrConstantVal(c));
            }
            IrConstantArray res = new IrConstantArray(list);
            res.setType(new IrCharTy());
            return res;
        }
    }

    private IrFunction visitFuncDef(FuncDef funcDef) {
        IrFunction function = new IrFunction();
        nowIrFunction = function;// 将nowIrFunction属性改为新的function
        if (funcDef == null) return null;
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
        newSymbolTable(); // 进入新符号表的同时配置IrFunction
        function.setName("@" + s.getSymbolName());
        function.setRegisterName("@" + s.getSymbolName());
        IrFunctionTy type = new IrFunctionTy();
        if(s.getBtype() == 0) {
            type.setFuncType(new IrIntegerTy());
        } else if (s.getBtype() == 1) {
            type.setFuncType(new IrCharTy());
        } else {
            type.setFuncType(new IrVoidTy());
        }
        function.setType(type);
        // 进行后续的分析，这里返回的value存储了参数相关的信息
        Value v = visitFuncFParams(funcDef.getFuncFParams());
        // 把存储参数的符号表的编号传给s
        s.setValue(v);
        // 从v中取出关于参数的描述，给每个参数分配临时寄存器之后加到irFunction中
        LinkedList<IrArgument> temp = ((FuncValue) v).getArguments();
        if (temp.isEmpty()) { // 没有参数的从0开始
            nowIrFunction.getNowRank();
        }
        for(int i = 0; i < temp.size(); i++) {
            IrArgument a = temp.get(i);
            a.setRank(function.getNowRank());
            a.setRegisterName("@" + a.getRank());
        }
        function.setArguments(temp);
        // 分析后边的block，需要将函数是否为void类型传入函数以供后续判断
        function.setIrBlock(visitFuncBlock(funcDef.getBlock(), bType));
        function.setRegisterName("-1");
        s.setIrValue(function);
        // 最后再返回上一级symbolTable（即将nowTableId改成fatherTableId）
        returnFatherTable();
        return function;
    }

    private IrFunction visitMainFuncDef(MainFuncDef mainFuncDef) {
        if (mainFuncDef == null) return null;
        IrFunction function = new IrFunction();
        nowIrFunction = function;
        function.getNowRank(); //没有参数的从%1开始
        newSymbolTable();
        IrFunctionTy type = new IrFunctionTy();
        type.setFuncType(new IrIntegerTy());
        function.setType(type); // 设置functionType为Integer
        function.setName("@main");
        function.setIrBlock(visitFuncBlock(mainFuncDef.getBlock(), 0)); //将block存储起来
        returnFatherTable();
        return function;
    }

    private int visitFuncType(FuncType funcType) {
        if (funcType == null) return 0;
        TokenType.tokenType t = funcType.getToken().getType();
        if (t == TokenType.tokenType.INTTK) return 0;
        else if (t == TokenType.tokenType.CHARTK) return 1;
        else return 2;
    }

    private Value visitFuncFParams(FuncFParams funcFParams) {
        FuncValue fv = new FuncValue(nowTableId, 0);
        if (funcFParams == null || funcFParams.getFuncFParamArrayList().isEmpty()) {
            return fv;
        }
        for (FuncFParam f:funcFParams.getFuncFParamArrayList()) {
            fv.addArgument(visitFuncFParam(f));
        }
        fv.setParaNum(fv.getArguments().size());
        //返回一个存储函数参数信息的符号表的序号以及函数参数的个数的value对象
        return fv;
    }

    private IrArgument visitFuncFParam(FuncFParam funcFParam) {
        if (funcFParam == null) return null;
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
        } else {
            // 配置完成并实现错误处理后，在最后把symbol加到表中
            addSymbol(s);
        }
        IrArgument argument = new IrArgument();
        IrType t;
        if (s.getBtype() == 0) {
            t = new IrIntegerTy();
        } else {
            t = new IrCharTy();
        }
        if (funcFParam.isArray()) {
            IrPointerTy type = new IrPointerTy();
            type.setType(t);
            t = type;
        }
        argument.setType(t);
        argument.setName(s.getSymbolName());
        s.setIrValue(argument); //配置完成后将argument加到symbol中
        return argument;
    }

    // 由于处理逻辑不同，所以把不同的block分开
    private IrBasicBlock visitBlock(Block block, boolean isInForBlock, int funcType) {
        // 多种情况，循环块与非循环块，是否为void函数中的块
        if (block == null) {
            return new IrBasicBlock();
        }
        IrBasicBlock basicBlock = new IrBasicBlock();
        newSymbolTable();
        for (BlockItem b : block.getBlockItemArrayList()) {
            basicBlock.addAllInstruction(visitBlockItem(b, isInForBlock, funcType).getTempInstructions());
        }
        returnFatherTable();
        return basicBlock;
    }

    private IrBasicBlock visitFuncBlock(Block block, int funcType) {
        if (block == null) return null;
        IrBasicBlock basicBlock = new IrBasicBlock();
        ReturnStmt returnStmt = null;
        for (BlockItem b : block.getBlockItemArrayList()) {
            if (b instanceof ReturnStmt) { // returnStmt单独处理
                returnStmt = (ReturnStmt) b;
                // 错误处理
                if (funcType == 2) {
                    if (returnStmt.getExp() != null) {
                        int errorLine = returnStmt.getLine();
                        errorDealer.errorF(errorLine);
                    }
                }

                IrRet ret = new IrRet(); // 添加返回指令
                IrValue v = visitExp(returnStmt.getExp());
                basicBlock.addAllInstruction(v.getTempInstructions());
                if (v instanceof IrConstant) {
                    //如果exp是Number或Character,那一层层向上传，最后到达这里的一定是一个IrConstant型的，
                    // 所以如果分析的结果是IrConstant型，我们可以认为ret了一个常量
                    ret.setResult(((IrConstantVal) v).getVal());
                } else {
                    //如果不是IrConstant型，那必然经过了运算，我们去上一次分析的IrValue，其RegisterName即为记过存储的位置，传入ret即可
                    ret.setRegisterName(v.getRegisterName());
                }
                if (funcType == 0) {
                    ret.setType(new IrIntegerTy());
                } else if (funcType == 1) {
                    ret.setType(new IrCharTy());
                } else {
                    ret.setType(new IrVoidTy());
                }
                basicBlock.addInstruction(ret);

            } else if (b instanceof Decl) {
                basicBlock.addAllInstruction(visitDeclInFunc((Decl) b));
            } else if (b instanceof Stmt) {
                basicBlock.addAllInstruction(visitStmt((Stmt) b, false, funcType).getTempInstructions());
            }
        }
        // 错误处理
        if (funcType != 2 && returnStmt == null) { //不是void型函数且没有返回语句
            int errorLine = block.getEndLine();
            errorDealer.errorG(errorLine);
        }
        return basicBlock;
    }

    private IrValue visitBlockItem(BlockItem blockItem, boolean isInForBlock, int funcType) {
        if (blockItem == null) {
            return null;
        }
        if (blockItem instanceof Decl) {
            IrValue v = new IrValue();
            ArrayList<IrInstruction> instructions = visitDeclInFunc((Decl) blockItem);
            v.addAllTempInstruction(instructions);
            return v;
        } else {
            return visitStmt((Stmt) blockItem, isInForBlock, funcType);
        }
    }

    private IrValue visitStmt(Stmt stmt, boolean isInForBlock, int funcType) {
        if (stmt == null) {
            return new IrValue();
        }
        // 由于要对for有关block进行判断，来看是否出现m错，设置了isInForBlock变量，这个变量的相关逻辑比较绕，
        // 主要就是如果是在for的stmt是block型，则在visitBlock时就将这个属性设为true，之后这个属性便会层层下传，直到for的block分析结束
        // 还要对是否是在void函数中进行判断，如果是在void函数中，那if，for，以及return语句就需要注意进行报错，所以需要传递isInVoidFunc参数
        if (stmt instanceof IfStmt) {
            visitIf((IfStmt) stmt, isInForBlock, funcType); //TODO：If这次作业不要求，先不管
        } else if (stmt instanceof For) {
            visitFor((For) stmt, funcType);//TODO：for这次作业不要求，先不管
        } else if (stmt instanceof ReturnStmt) {
            return visitReturnStmt((ReturnStmt) stmt, funcType);
        } else if (stmt instanceof PrintfStmt) { //TODO：printf分割出的字符变量要放到全局，切记
            return visitPrintfStmt((PrintfStmt) stmt);
        } else if (stmt instanceof LValStmt) {
            return visitLValStmt((LValStmt) stmt);
        } else if (stmt.getbOrC() != null) { // 处理break和continue //TODO：break和continue这次作业不要求，先不管
            if (!isInForBlock) { //不是for循环中的break与continue，直接错误处理
                int errorLine = stmt.getbOrC().getLine();
                errorDealer.errorM(errorLine);
            }
        } else if (stmt.getB() != null) {
            return visitBlock(stmt.getB(), isInForBlock, funcType);
        } else if (stmt.getE() != null) {
            return visitExp(stmt.getE());
        }
        return new IrValue();
    }

    private void visitIf(IfStmt ifStmt, boolean isInForBlock, int funcType) {//TODO：这次作业不考虑
        if (ifStmt == null) return;
        visitCond(ifStmt.getC());
        visitStmt(ifStmt.getS1(), isInForBlock, funcType);
        if (ifStmt.getS2() != null) visitStmt(ifStmt.getS2(), isInForBlock, funcType);
    }

    private void visitFor(For f, int funcType) {//TODO：这次作业不考虑
        if (f == null) return;
        visitForStmt(f.getForStmt1());
        visitCond(f.getC());
        visitForStmt(f.getForStmt2());
        if (f.getS().getB() != null) visitBlock(f.getS().getB(), true, funcType);
        else visitStmt(f.getS(), false, funcType);
    }

    private IrValue visitReturnStmt(ReturnStmt returnStmt, int funcType) {
        if (returnStmt == null) return new IrValue();
        if (funcType == 2) { // 如果在void函数中，就报错
            if (returnStmt.getExp() != null) {
                int errorLine = returnStmt.getLine();
                errorDealer.errorF(errorLine);
                return new IrValue();
            }
        }
        //配置返回IrValue，配置ret指令并添加
        IrValue res = new IrValue();
        IrRet ret = new IrRet();
        if (returnStmt.getExp() != null) {
            IrValue v = visitExp(returnStmt.getExp());
            res.addAllTempInstruction(v.getTempInstructions());
            if (v instanceof IrConstant) {
                //如果exp是Number或Character,那一层层向上传，最后到达这里的一定是一个IrConstant型的，
                // 所以如果分析的结果是IrConstant型，我们可以认为ret了一个常量
                ret.setResult(((IrConstantVal) v).getVal());
            } else {
                //如果不是IrConstant型，那必然经过了运算，我们去上一次分析的IrValue，其RegisterName即为记过存储的位置，传入ret即可
                ret.setRegisterName(v.getRegisterName());
            }
            if (funcType == 0) { //配置ret的返回类型，与函数保持一致 //TODO：不知道这里要不要类型转换
                ret.setType(new IrIntegerTy());
            } else if (funcType == 1) {
                ret.setType(new IrCharTy());
            }
        } else {
            ret.setType(new IrVoidTy());
        }
        res.addTempInstruction(ret);
        return res;
    }

    private IrValue visitPrintfStmt(PrintfStmt printfStmt) { // 先处理错误，再visit里边的属性
        if (printfStmt == null) {
            return new IrValue();
        }
        //错误处理
        if (printfStmt.getFCharacterNumInString() != printfStmt.getExpArrayList().size()) {
            int errorLine = printfStmt.getLine();
            errorDealer.errorL(errorLine);
        }
        //TODO: printf相关实现
        for (Exp e : printfStmt.getExpArrayList()) {
            visitExp(e);
        }
        return new IrValue();
    }

    private IrValue visitLValStmt(LValStmt lValStmt) {
        if (lValStmt == null) return new IrValue();
        //先错误处理
        Token t = lValStmt.getlVal().getIdent();
        if (isConstSymbol(t.getString())) {
            int errorLine = t.getLine();
            errorDealer.errorH(errorLine);
        }
        //再处理LVal
        //TODO：对于assign型语句，即Lval = exp型的，先通过visitLVal获取左边的值，从返回的IrValue中取出regisiterName用于构建Instruction(注意数组类型)
        //TODO：再通过visitExp获取右边的值，从返回的IrValue中取出regisiterName用于构建Store Instruction
        //这里的操作肯定是Store，visitLVal产生op1，这里是要被存入的位置，需要在store类型中转指针，visitExp产生等号右边的值(常量或寄存器)
        IrValue v1 = visitLVal(lValStmt.getlVal());
        IrValue v2 = visitExp(lValStmt.getExp());
        IrInstruction i = new IrStore(); //TODO:IrStore的实现
        i.setOperand(v1, 0);
        i.setOperand(v2, 1);
        IrValue res = new IrValue();
        res.addAllTempInstruction(v2.getTempInstructions());
        res.addAllTempInstruction(v1.getTempInstructions());
        res.addTempInstruction(i);
        return res;
    }

    private void visitForStmt(ForStmt forStmt) { // TODO:本次作业不涉及，先不考虑
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

    //TODO:由只返回一个int的值改为返回一个IrValue，包含过程中分析得到的语句以及最后结果存储的位置，还有exp的类型（用IrType表示的）
    private IrValue visitExp(Exp exp) {
        if (exp == null) {
            return new IrValue();
        }
        return visitAddExp(exp.getAddExp());
    }

    private void visitCond(Cond cond) {
        if (cond == null) return;
        visitLOrExp(cond.getlOrExp());
    }

    private IrValue visitLVal(LVal lVal) { //TODO:可能要传参数，是否再等号左边
        if (lVal == null) return new IrValue();
        //先错误处理
        if (isUnDefinedSymbol(lVal.getIdent().getString())) {
            int errorLine = lVal.getIdent().getLine();
            errorDealer.errorC(errorLine);
            return new IrValue();
        }
        //根据Indent从symbol中获取寄存器名，分析exp，如果是数组的话需要通过exp的值输出get指令，寄存器也要换成新的
        IrValue v1 = visitExp(lVal.getExp());
        IrValue v2 = getSymbol(lVal.getIdent().getString()).getIrValue();
        if (lVal.getExp() == null) { // 没有exp说明是确定的变量名，直接返回符号表中存储的IrValue即可 //TODO:左值有可能是变量名
            //注意，如果是全局变量，我们需要新建一个IrValue后，生成load指令后返回
            if (v2 instanceof IrGlobalVariable) { //TODO：是全局变量并且在等号右边
                IrValue res = new IrValue();
                IrLoad i = new IrLoad();
                i.setOperand(v2, 0);
                i.setType(v2.getType());
                i.setRegisterName("%" + nowIrFunction.getNowRank());
                res.setRegisterName(i.getRegisterName());
                res.addTempInstruction(i);
                return res;
            }
            return v2;
        } else {
            //否则为数组元素，先实现一个getptr指令，再配置IrValue并返回
            //先通过v1得到数组元素的位次号，从IrConstantArray类型中取出对应的IrConstntVal作为res的IrValue
            int index = ((IrConstantVal) v1).getVal();
            IrValue res;
            if (v2 instanceof IrGlobalVariable) { // 如果是全局变量，取出其IrConstant型转成IrConstantArray型得到目标
                res = ((IrConstantArray) ((IrGlobalVariable) v2).getConstant()).getConstantVals().get(index);
            } else { // 局部变量直接取出其IrConstant型转成IrConstantArray型得到目标
                res = ((IrConstantArray) v2).getConstantVals().get(index);
            }
            res.addAllTempInstruction(v1.getTempInstructions());
            res.setRegisterName("%" + nowIrFunction.getNowRank());
            res.setType(v1.getType());
            IrInstruction i = new IrGetelementptr(); //TODO:Getelementptr的实现,注意它的左值是一个指针类型，还需要考虑load与指针的问题，这里返回的IrValue类型很有可能需要改为IrPointer
            res.addTempInstruction(i);
            //TODO:需要再产生一条load指令，将getptr的结果load出来
            return res;
        }
    }

    private IrValue visitPrimaryExp(PrimaryExp primaryExp) { //根据不同种类进行分析并返回得到的IrValue
        if (primaryExp == null) {
            return new IrValue();
        }
        if (primaryExp.getExp() != null) {
            return visitExp(primaryExp.getExp());
        } else if (primaryExp.getlVal() != null) {
            return visitLVal(primaryExp.getlVal());
        } else if (primaryExp.getNumber() != null) {
            return visitNumber(primaryExp.getNumber());
        } else if (primaryExp.getCharacter() != null) {
            return visitCharacter(primaryExp.getCharacter());
        }
        return new IrValue();
    }

    private IrValue visitNumber(Number number) { //构建IrValue并返回
        IrConstantVal res = new IrConstantVal(number.getToken().getNumber());
        res.setType(new IrIntegerTy());
        return res;
    }

    private IrValue visitCharacter(Character character) { //TODO：构建IrValue并返回
        IrConstantVal res = new IrConstantVal(character.getToken().getString().charAt(0));
        res.setType(new IrCharTy());
        return res;
    }

    private IrValue visitUnaryExp(UnaryExp unaryExp) {
        if (unaryExp == null) {
            return new IrValue();
        }
        if (unaryExp.getPrimaryExp() != null) {
            return visitPrimaryExp(unaryExp.getPrimaryExp());
        } else if (unaryExp.getUnaryExp() != null) {
            //分析unaryOp的种类来决定下一步的操作，如果是+或者!则不生成语句，如果是-生成一个与0相减的语句
            int judge = visitUnaryOp(unaryExp.getUnaryOp());
            IrValue v = visitUnaryExp(unaryExp.getUnaryExp());
            if (judge == 1) { //生成一条sub语句并实现value的更新,注意这里没有保存计算结果，可能会出现bug
                IrConstantVal zero = new IrConstantVal(0);
                zero.setType(v.getType()); //Type和后边的数保持一致
                IrBinaryOp i = new IrBinaryOp();
                i.setRegisterName("%" + nowIrFunction.getNowRank());
                i.setOperationTy(IrInstructionType.irIntructionType.Sub);
                i.setOperand(zero, 0);
                i.setOperand(v, 1);
                v.setRegisterName(i.getRegisterName()); //一定要更新返回IrValue的寄存器名
                v.addTempInstruction(i);
            }
            return v;
        } else if (unaryExp.getIdent() != null) {
            //TODO：这个部分生成函数调用相关语句,构架一个IrValue并返回        //TODO：对于getint和getchar要实现call语句
            Token t = unaryExp.getIdent();
            // 先处理错误c
            if (isUnDefinedSymbol(t.getString())) {
                int errorLine = t.getLine();
                errorDealer.errorC(errorLine);
            } else {
                Symbol s = getSymbol(t.getString());
                FuncValue sValue = (FuncValue) s.getValue();
                IrValue res = new IrValue();
                ArrayList<IrValue> paraList;
                //再处理错误d
                if (unaryExp.getFuncRParams() != null) {
                    paraList = visitFuncRParams(unaryExp.getFuncRParams());
                    if (sValue.getParaNum() != unaryExp.getFuncRParams().getExpArrayList().size()) {
                        int errorLine = t.getLine();
                        errorDealer.errorD(errorLine);
                    } else {//再处理错误e，依次与定义比较每一个参数的类型
                        SymbolTable symbolTable = symbolTables.get(sValue.getParaTableId());
                        int i = 0;
                        for (Symbol ss:symbolTable.getDirectory().values()) {
                            if (ss.judgeKindN() != paraList.get(i).judgeKindN()) {
                                int errorLine = t.getLine();
                                errorDealer.errorE(errorLine);
                                break;
                            }
                            i++;
                            if (i >= paraList.size())break;
                        }
                    }
                    for (int i = 0; i < paraList.size(); i++) {
                        res.addAllTempInstruction(paraList.get(i).getTempInstructions());
                    }
                }
                IrCall call = new IrCall(); //TODO:call指令的具体实现
                call.setType(s.getIrValue().getType());
                call.setRegisterName("%" + nowIrFunction.getNowRank());
                res.addTempInstruction(call);
                res.setRegisterName(call.getRegisterName());//一定要更新返回IrValue的寄存器名
                res.setType(call.getType());
                return res;
            }
        }
        return new IrValue();
    }

    private int visitUnaryOp(UnaryOp unaryOp) {
        if (unaryOp.getToken().getString().equals("-")) {
            return 1;
        } else {
            return 0;
        }
    }

    private ArrayList<IrValue> visitFuncRParams(FuncRParams funcRParams) {
        if (funcRParams == null) return null;
        ArrayList<IrValue> l = new ArrayList<>();
        for (Exp e: funcRParams.getExpArrayList()) {
            l.add(visitExp(e));
        }
        return l;
    }

    private IrValue visitMulExp(MulExp mulExp) {
        IrValue res = new IrValue();
        if (mulExp == null) {
            return res;
        }
        IrValue v1 = new IrValue(), v2;
        for (int i = 0; i < mulExp.getUnaryExpArrayList().size(); i++) { //TODO:注意constant类型的数
            UnaryExp u = mulExp.getUnaryExpArrayList().get(i);
            if(i == 0) { // 返回第一个unaryExp的类型即可
                v1 = visitUnaryExp(u);
                res.setType(v1.getType());
                res.addAllTempInstruction(v1.getTempInstructions());
            } else { //其他的也需要分析，可能会有错误出现
                v2 = visitUnaryExp(u);
                res.addAllTempInstruction(v2.getTempInstructions());
                //根据对应符号实现mul、div、 mod语句的生成并添加到res的tempInstructions中
                IrBinaryOp instruction = new IrBinaryOp();
                TokenType.tokenType t = mulExp.getSymbolList().get(i - 1).getType();
                if (t == TokenType.tokenType.MULT) {
                    instruction.setOperationTy(IrInstructionType.irIntructionType.Mul);
                } else if (t == TokenType.tokenType.DIV) {
                    instruction.setOperationTy(IrInstructionType.irIntructionType.Div);
                } else {
                    instruction.setOperationTy(IrInstructionType.irIntructionType.Mod);
                }
                instruction.setOperand(v1, 0);
                instruction.setOperand(v2, 1);
                instruction.setType(v1.getType());
                instruction.setRegisterName("%" + nowIrFunction.getNowRank());
                res.setRegisterName(instruction.getRegisterName());//一定要更新返回IrValue的寄存器名
                res.addTempInstruction(instruction);
                v1 = res;
            }
        }
        return res;
    }

    private IrValue visitAddExp(AddExp addExp) {
        IrValue res = new IrValue();
        if (addExp == null) {
            return res;
        }
        IrValue v1 = new IrValue(), v2;
        for (int i = 0; i < addExp.getMulExpArrayList().size(); i++) { //TODO:注意constant类型的数
            MulExp m = addExp.getMulExpArrayList().get(i);
            if(i == 0) { // 返回第一个unaryExp的类型即可
                v1 = visitMulExp(m);
                res.setType(v1.getType());
                res.addAllTempInstruction(v1.getTempInstructions());
            } else { //其他的也需要分析，可能会有错误出现
                v2 = visitMulExp(m);
                res.addAllTempInstruction(v2.getTempInstructions());
                //根据对应符号实现add, sub语句的生成并添加到res的tempInstructions中
                IrBinaryOp instruction = new IrBinaryOp();
                TokenType.tokenType t = addExp.getSymbolList().get(i - 1).getType();
                if (t == TokenType.tokenType.PLUS) {
                    instruction.setOperationTy(IrInstructionType.irIntructionType.Add);
                } else if (t == TokenType.tokenType.MINU) {
                    instruction.setOperationTy(IrInstructionType.irIntructionType.Sub);
                }
                instruction.setOperand(v1, 0);
                instruction.setOperand(v2, 1);
                instruction.setType(v1.getType());
                instruction.setRegisterName("%" + nowIrFunction.getNowRank());
                res.setRegisterName(instruction.getRegisterName());//一定要更新返回IrValue的寄存器名
                res.addTempInstruction(instruction);
                v1 = res;
            }
        }
        return res;
    }

    private IrValue visitConstExp(ConstExp constExp) {
        return visitAddExp(constExp.getAddExp());
    }

    //TODO： 下边所有的visit方法均与位运算相关，本次作业不涉及，大致实现应该与visitAddExp类似
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

    public void printSymbolTables() throws IOException {
        for(SymbolTable s:symbolTables.values()) s.printSymbolTable(outputfile);
    }

    private void printIrCode() throws IOException {
        ArrayList<String> code = irModule.output();
        for (String s : code) {
            outputfile.write(s);
        }
    }
}
