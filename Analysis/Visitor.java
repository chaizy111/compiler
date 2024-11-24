package Analysis;

import Llvmir.IrModule;
import Llvmir.IrValue;
import Llvmir.Type.*;
import Llvmir.ValueType.Constant.IrConstant;
import Llvmir.ValueType.Constant.IrConstantArray;
import Llvmir.ValueType.Constant.IrConstantVal;
import Llvmir.ValueType.Function.IrArgument;
import Llvmir.ValueType.Function.IrFunction;
import Llvmir.ValueType.Global.IrGlobalConstString;
import Llvmir.ValueType.Instruction.*;
import Llvmir.ValueType.IrBasicBlock;
import Llvmir.ValueType.Global.IrGlobalVariable;
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
    //TODO：5.debug时要注意的一个点，返回的IrValue寄存器名的设置不要有遗漏
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
            //实验中所有中括号中的表达式全部为常量表达式，所以可以为constDef设置一个计算方法，直接得到其结果后存储起来，同理后边的Initval
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
        irGlobalVariable.setOutputType(t);
        IrPointerTy pt = new IrPointerTy();
        pt.setType(t);
        irGlobalVariable.setType(pt); //全局变量的type也是指针
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
                irGlobalVariable.setOutputType(nt);
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
        IrPointerTy pointerTy = new IrPointerTy();
        pointerTy.setType(t);

        IrAlloca irAlloca = new IrAlloca(); //先分配内存，将内存分配指令alloca填到list中，并将这个内存分配指令作为该符号的IrValue
        irAlloca.setAllocaType(t);
        irAlloca.setType(pointerTy); //irAlloca得到指针型的数据，所以其真正类型只有一个，即指针，t与nt都是用来输出的
        irAlloca.setRegisterName("%" + nowIrFunction.getNowRank());
        instructions.add(irAlloca);

        if (valDef.getInitVal() != null) { //valDef可能没有等号和等号右边的部分！！！
            IrValue v = visitInitValInFunction(valDef.getInitVal());
            instructions.addAll(v.getTempInstructions()); // 分析右边部分，将产生的代码放到list中
            //与constVarDef的区别，varDef不能直接使用，必须输出store语句进行store存储
            if (s.getArraySize() != 0) { //数组类型，多条IrStore与getelementptr指令组合
                ArrayList<IrValue> irValues = v.getTempValues();
                irAlloca.setTempValues(irValues); //数组型要在符号表中存的irValue进行配置，将诸irValue存起来备用
                for (int i = 0; i < irValues.size(); i++) {
                    IrGetelementptr irGetelementptr = new IrGetelementptr(); //先配置getelementptr指令
                    irGetelementptr.setExc(i);
                    irGetelementptr.setType(irAlloca.getType()); //实际返回的type就是与alloca同样的指针type
                    IrType temp = irAlloca.getAllocaType();
                    //irGetelement在output使用的type需要特判，如果是Array与pointer的type，我们需要拿出其内部存储的type作为该指令的type
                    if (temp instanceof IrArrayTy) {
                        irGetelementptr.setOutputType(((IrArrayTy) temp).getArrayType());
                    } else if (temp instanceof IrPointerTy) {
                        irGetelementptr.setOutputType(((IrPointerTy) temp).getType());
                    } else {
                        irGetelementptr.setOutputType(temp); //这里type与alloca的一致
                    }
                    irGetelementptr.setOperand(irAlloca, 0);
                    irGetelementptr.setRegisterName("%" + nowIrFunction.getNowRank());
                    instructions.add(irGetelementptr);
                    IrStore irStore = new IrStore(); //数组def时紧跟在getptr指令后边的就是一条store指令
                    irStore.setOperand(irGetelementptr, 0);
                    irStore.setOperand(irValues.get(i), 1); //返回的irValues的第i个，即exp的第i个
                    instructions.add(irStore);
                }
            } else { //val类型，一条IrStore
                IrStore irStore = new IrStore();
                irStore.setOperand(irAlloca, 0);
                irStore.setOperand(v, 1);
                instructions.add(irStore);
            }
        }
        //错误处理
        if(isDuplicateSymbol(s.getSymbolName())) {
            int errorLine = valDef.getIdent().getLine();
            errorDealer.errorB(errorLine);
        } else {
            // 配置完成并实现错误处理后，在最后把symbol加到表中
            s.setIrValue(irAlloca);
            addSymbol(s);
        }
        return instructions;
    }

    //与constInitVal逻辑一致
    private Value visitInitVal(InitVal initVal) {
        // TODO：已经实现了对于Initval的存储，现在需要优化代码实现，现在的逻辑有些冗余
        if (initVal.getExp() != null) { // 只有一个Exp
            visitExp(initVal.getExp(), false);
            VarValue value = new VarValue();
            value.setItem(initVal.getExp().getAddExp().getResult());
            return value;
        } else if (!initVal.getExpArrayList().isEmpty()) { // 多个Exp
            ArrayValue value = new ArrayValue();
            for (Exp e: initVal.getExpArrayList()) {
                visitExp(e, false);
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
            return visitExp(initVal.getExp(), false);
        } else if (!initVal.getExpArrayList().isEmpty()) { // 多个Exp
            IrValue res = new IrValue();
            for (Exp e: initVal.getExpArrayList()) {
                IrValue t = visitExp(e, false);
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
        for(int i = 0; i < temp.size(); i++) {
            IrArgument a = temp.get(i);
            a.setRank(function.getNowRank());
            a.setRegisterName("%" + a.getRank());
        }
        function.setArguments(temp);
        // 分析后边的block，需要将函数是否为void类型传入函数以供后续判断
        nowIrFunction.getNowRank();//这里是给函数入口留一个寄存器号
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
//        if (funcFParam.isArray()) {
//            IrPointerTy type = new IrPointerTy();
//            type.setType(t);
//            t = type;
//        }
        IrPointerTy ty = new IrPointerTy();
        ty.setType(t);
        argument.setType(ty);
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
            basicBlock.addAllTempInstruction(visitBlockItem(b, isInForBlock, funcType).getTempInstructions());
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
                IrValue v = visitExp(returnStmt.getExp(), false);//根据文法，这里肯定不是左值
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
        } else if (stmt instanceof PrintfStmt) {
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
            //if (stmt.getE().getAddExp().isUseful()) { //这里判断exp是否有用，防止出现在一行有 1+0;这种语句出现，这种语句出现就直接忽略
                //判断的逻辑就是看是不是函数，如果不是函数就直接忽略(暂时不用，优化的时候可以用)
                return visitExp(stmt.getE(), false); //根据文法，这里肯定不是左值
            //}
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
            IrValue v = visitExp(returnStmt.getExp(), false);
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
        LinkedList<IrGlobalConstString> str = new LinkedList<>(); //按顺序存储被%d，%c分割出来的常量字符串
        LinkedList<String> kind = new LinkedList<>(); //存储出现顺序
        LinkedList<IrValue> exps = new LinkedList<>(); //存储一众exp分析的结果
        int length = printfStmt.getStringConst().getString().length();
        String old = printfStmt.getStringConst().getString().substring(1, length - 1); //去头尾的引号
        int lpos = 0;
        int rpos = 0;
        for (;rpos < old.length(); rpos++) { //分割字符串，填str与kind
            if (old.charAt(rpos) == '%') {
                if (rpos + 1 < old.length() && (old.charAt(rpos + 1) == 'c' || old.charAt(rpos + 1) == 'd')) {
                    //构造IrGlobalConstStr，存str
                    String temp = old.substring(lpos, rpos);
                    if (!temp.isEmpty()) { //防止在开头截出空的
                        IrGlobalConstString constString = new IrGlobalConstString();
                        constString.setS(temp);
                        constString.setLength();
                        if (irModule.getIrGlobalConstStrings().isEmpty()) {
                            constString.setRegisterName("@.str");
                        } else {
                            constString.setRegisterName("@.str." + irModule.getIrGlobalConstStrings().size());
                        }
                        str.add(constString);
                        kind.add("str");
                        irModule.addGlobalConstStr(constString); // constStr是需要输出的全局常量，要加到irModule的全局常量字符串表中用来输出
                    }
                    if (old.charAt(rpos) == 'c') { //存kind
                        kind.add("%c");
                    } else {
                        kind.add("%d");
                    }
                    //移lpos， rpos
                    rpos = rpos + 1;
                    lpos = rpos + 1;
                }
            }
        }
        if (lpos < old.length()) { //如果以字符串结尾，不要忘了再截最后一次字符串
            String temp = old.substring(lpos, rpos);
            IrGlobalConstString constString = new IrGlobalConstString();
            constString.setS(temp);
            constString.setLength();
            if (irModule.getIrGlobalConstStrings().isEmpty()) {
                constString.setRegisterName("@.str");
            } else {
                constString.setRegisterName("@.str." + irModule.getIrGlobalConstStrings().size());
            }
            str.add(constString);
            kind.add("str");
            irModule.addGlobalConstStr(constString); // constStr是需要输出的全局常量，要加到irModule的全局常量字符串表中用来输出
        }
        IrValue res = new IrValue();
        for (Exp e : printfStmt.getExpArrayList()) {
            IrValue v = visitExp(e, false);// 根据文法，这里肯定不是左值
            exps.add(v);
            res.addAllTempInstruction(v.getTempInstructions());
        }
        for (int i = 0, j = 0, k = 0; i < kind.size(); i++) { // j 是str的index，k是exps的index
            if (kind.get(i).equals("str")) { //是str
                IrCall call1 = new IrCall();
                call1.setFuncName("@putstr");
                String s = "i8* getelementptr inbounds ([" +
                        str.get(j).getLength() +
                        " x i8], [" +
                        str.get(j).getLength() +
                        " x i8]* " +
                        str.get(j).getRegisterName() +
                        ", i64 0, i64 0)";
                call1.setParaForPutStr(s);
                call1.setType(new IrVoidTy());
                res.addTempInstruction(call1);
                j++;
            } else { //是%d%c
                IrCall call2 = new IrCall(); //配置putint putch的call，其名字需要根据kind[i]判断，参数数为1，参数从exp[k]获取
                if (kind.get(i).equals("%d")) {
                    call2.setFuncName("@putint");
                } else {
                    call2.setFuncName("@putch");
                }
                call2.setType(new IrVoidTy());
                call2.setParaNum(1);
                call2.setOperand(exps.get(k++), 0);
                res.addTempInstruction(call2);
            }
        }
        return res;
    }

    private IrValue visitLValStmt(LValStmt lValStmt) {
        if (lValStmt == null) return new IrValue();
        //先错误处理
        Token t = lValStmt.getlVal().getIdent();
        if (isConstSymbol(t.getString())) {
            int errorLine = t.getLine();
            errorDealer.errorH(errorLine);
        }
        IrValue res = new IrValue();
        if (lValStmt.isGetChar()) { //处理getChar与getInt类型函数调用 //TODO:注意可能的类型转换
            IrCall call = new IrCall();
            call.setRegisterName("%" + nowIrFunction.getNowRank()); //不能再if外边设，编号会出问题
            call.setParaNum(0);
            call.setFuncName("@getchar");
            call.setType(new IrIntegerTy());
            res.setRegisterName(call.getRegisterName());
            res.addTempInstruction(call);
        } else if (lValStmt.isGetInt()) {
            IrCall call = new IrCall();
            call.setRegisterName("%" + nowIrFunction.getNowRank());
            call.setParaNum(0);
            call.setFuncName("@getint");
            call.setType(new IrIntegerTy());
            res.setRegisterName(call.getRegisterName());
            res.addTempInstruction(call);
        } else {
            //对于assign型语句，即Lval = exp型的，先通过visitLVal获取左边的值，从返回的IrValue中取出regisiterName用于构建Instruction
            //再通过visitExp获取右边的值，返回的IrValue用于构建Store Instruction
            //这里的操作肯定是Store，visitLVal产生op1，这里是要被存入的位置，我们需要保证op1一定是指针类型，visitExp产生等号右边的值(常量或寄存器)
            IrValue v1 = visitLVal(lValStmt.getlVal(), true);
            IrValue v2 = visitExp(lValStmt.getExp(), false);
            IrInstruction i = new IrStore();
            i.setOperand(v1, 0);
            i.setOperand(v2, 1);
            res.addAllTempInstruction(v2.getTempInstructions());
            res.addAllTempInstruction(v1.getTempInstructions());
            res.addTempInstruction(i);
        }
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
        visitLVal(forStmt.getlVal(), false);
        visitExp(forStmt.getExp(), false);
    }

    //由只返回一个int的值改为返回一个IrValue，包含过程中分析得到的语句以及最后结果存储的位置（寄存器名），还有exp的类型（用IrType表示的）
    private IrValue visitExp(Exp exp, boolean isLvalue) {
        if (exp == null) {
            return new IrValue();
        }
        return visitAddExp(exp.getAddExp(), isLvalue);
    }

    private void visitCond(Cond cond) {
        if (cond == null) return;
        visitLOrExp(cond.getlOrExp());
    }

    private IrValue visitLVal(LVal lVal, boolean isLvalue) {
        //传递isLvalue，即是否为左值，如果不是左值则不需要load，这个会导致所有分析过程沿途的方法都要传isLvalue属性，这是必需的
        if (lVal == null) return new IrValue();
        //先错误处理
        if (isUnDefinedSymbol(lVal.getIdent().getString())) {
            int errorLine = lVal.getIdent().getLine();
            errorDealer.errorC(errorLine);
            return new IrValue();
        }
        //根据Indent从symbol中获取寄存器名，分析exp，如果是数组的话需要通过exp的值输出get指令，寄存器也要换成新的
        IrValue v1 = visitExp(lVal.getExp(), false);
        IrValue v2 = getSymbol(lVal.getIdent().getString()).getIrValue();
        Symbol s = getSymbol(lVal.getIdent().getString());
        if (lVal.getExp() == null) { // 没有exp说明是确定的变量名，通过load构造一个新的IrValue后返回
            //如果lVal没有exp，也有可能是直接传了一个数组，这里需要先处理这种情况
            if (s.getArraySize() > 0) { //是数组类型的，我们需要getelementptr，返回一个指针型
                IrValue res = new IrValue();
                IrGetelementptr i = new IrGetelementptr(); //配置Getelementptr
                i.setExc(0);
                IrType temp = v2.getType();//此时temp一定是IrArrayTy，构造成IrPointerTy给i即可
                IrPointerTy t = new IrPointerTy();
                t.setType(((IrArrayTy) temp).getArrayType());
                i.setType(t);
                i.setOutputType(t); //这里outputtype与i返回的type是一致的
                i.setOperand(v2, 0);
                i.setRegisterName("%" + nowIrFunction.getNowRank());
                res.setType(i.getType());
                res.setRegisterName(i.getRegisterName());
                res.addTempInstruction(i);
                return res;
            }
            //注意，如果是全局变量，我们需要新建一个IrValue后，生成load指令后返回
            if (!isLvalue) { //左值不要load，从其被alloca或在全局被分配的指针中load值，构造IrValue返回
                IrValue res = new IrValue();
                IrLoad l = new IrLoad();
                l.setOperand(v2, 0); //给load分配的操作数应该是指针类型的
                l.setType(((IrPointerTy) v2.getType()).getType());
                l.setRegisterName("%" + nowIrFunction.getNowRank());
                res.setType(l.getType());
                res.setRegisterName(l.getRegisterName());
                res.addTempInstruction(l);
                return res;
            }
            return v2; //是左值直接返回
        } else { //数组元素不管是不是左值都要load
            //否则为数组元素，先实现一个getptr指令，再load，再配置IrValue并返回
            //先通过v1得到数组元素的位次号，从IrConstantArray类型中取出对应的IrConstntVal作为res的IrValue
            int index = ((IrConstantVal) v1).getVal();
            IrValue res = new IrValue();
            if (v2 instanceof IrGlobalVariable) { // 如果是全局变量，取出其IrConstant型转成IrConstantArray型得到目标
                res = ((IrConstantArray) ((IrGlobalVariable) v2).getConstant()).getConstantVals().get(index);
            } else if (v2 instanceof IrConstantArray) { //如果是常量
                res = ((IrConstantArray) v2).getConstantVals().get(index);
            } else if (!v2.getTempValues().isEmpty()){ // 局部变量取第i个tempValue得到目标,如果取不到，说明是函数内的
                res = v2.getTempValues().get(index);
            }
            res.addAllTempInstruction(v1.getTempInstructions());
            res.setType(v1.getType());
            IrGetelementptr i = new IrGetelementptr(); //配置Getelementptr
            i.setExc(index);
            IrPointerTy pointerTy = new IrPointerTy();
            IrType temp = v2.getType();
            //irGetelement的type需要特判，如果是Array与pointer的type，我们需要拿出其内部存储的type作为该指令的type
            if (temp instanceof IrArrayTy) {
                pointerTy.setType(((IrArrayTy) temp).getArrayType());
                i.setType(pointerTy);
            } else if (temp instanceof IrPointerTy) {
                pointerTy.setType(((IrPointerTy) temp).getType());
                i.setType(pointerTy);
            } else {
                pointerTy.setType(v2.getType());
                i.setType(pointerTy); //这里type与alloca的一致
            }
            i.setOutputType(i.getType()); //这里outputtype与i返回的type是一致的
            i.setOperand(v2, 0);
            i.setRegisterName("%" + nowIrFunction.getNowRank());
            res.addTempInstruction(i);
            //需要再产生一条load指令，将getptr的结果load出来
            IrLoad l = new IrLoad();
            l.setOperand(i, 0);
            l.setType(v1.getType()); //load的type与返回value的type保持一致
            l.setRegisterName("%" + nowIrFunction.getNowRank());
            res.addTempInstruction(l);
            res.setRegisterName(l.getRegisterName());//最后res的寄存器名与load保持一致
            return res;
        }
    }

    private IrValue visitPrimaryExp(PrimaryExp primaryExp, boolean isLvalue) { //根据不同种类进行分析并返回得到的IrValue
        if (primaryExp == null) {
            return new IrValue();
        }
        if (primaryExp.getExp() != null) {
            return visitExp(primaryExp.getExp(), isLvalue);
        } else if (primaryExp.getlVal() != null) {
            return visitLVal(primaryExp.getlVal(), isLvalue);
        } else if (primaryExp.getNumber() != null) {
            return visitNumber(primaryExp.getNumber());
        } else if (primaryExp.getCharacter() != null) {
            return visitCharacter(primaryExp.getCharacter());
        }
        return new IrValue();
    }

    private IrValue visitNumber(Number number) { //构建IrValue并返回
        IrConstantVal res = new IrConstantVal(number.getToken().getNumber());
        res.setType(new IrIntegerTy());//type为整型
        return res;
    }

    private IrValue visitCharacter(Character character) { //构建IrValue并返回
        IrConstantVal res = new IrConstantVal(character.getToken().getString().charAt(0));
        res.setType(new IrCharTy());//type为字符型
        return res;
    }

    private IrValue visitUnaryExp(UnaryExp unaryExp, boolean isLvalue) {
        if (unaryExp == null) {
            return new IrValue();
        }
        if (unaryExp.getPrimaryExp() != null) {
            return visitPrimaryExp(unaryExp.getPrimaryExp(), isLvalue);
        } else if (unaryExp.getUnaryExp() != null) {
            //分析unaryOp的种类来决定下一步的操作，如果是+或者!则不生成语句，如果是-生成一个与0相减的语句
            int judge = visitUnaryOp(unaryExp.getUnaryOp());
            IrValue v = visitUnaryExp(unaryExp.getUnaryExp(), isLvalue);
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
            //这个部分生成函数调用相关语句,构架一个IrValue并返回
            Token t = unaryExp.getIdent();
            IrValue res = new IrValue();
            ArrayList<IrValue> paraList = new ArrayList<>();
            // 先处理错误c
            if (isUnDefinedSymbol(t.getString())) {
                int errorLine = t.getLine();
                errorDealer.errorC(errorLine);
            } else {
                Symbol s = getSymbol(t.getString());
                FuncValue sValue = (FuncValue) s.getValue();
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
                IrCall call = new IrCall(); //完成call指令并填到value的指令集合中
                call.setType(((IrFunctionTy) s.getIrValue().getType()).getFuncType());
                call.setRegisterName("%" + nowIrFunction.getNowRank());
                call.setParaNum(paraList.size());
                call.setFuncName("@" + t.getString());
                for (int i = 0; i < paraList.size(); i++) { // 配置参数
                    call.setOperand(paraList.get(i), i);
                }
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
            l.add(visitExp(e, false)); //根据文法，这里的exp肯定不是左值
        }
        return l;
    }

    private IrValue visitMulExp(MulExp mulExp, boolean isLvalue) {
        IrValue res = new IrValue();
        if (mulExp == null) {
            return res;
        }
        IrValue v1 = new IrValue(), v2;
        for (int i = 0; i < mulExp.getUnaryExpArrayList().size(); i++) { //TODO:注意constant类型的数
            UnaryExp u = mulExp.getUnaryExpArrayList().get(i);
            if(i == 0) { // 返回第一个unaryExp的类型即可
                v1 = visitUnaryExp(u, isLvalue);
                if (mulExp.getUnaryExpArrayList().size() == 1) return v1; //只有一个操作数，直接返回即可
                res.setType(v1.getType());
                res.addAllTempInstruction(v1.getTempInstructions());
            } else { //其他的也需要分析，可能会有错误出现
                v2 = visitUnaryExp(u, isLvalue);
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

    private IrValue visitAddExp(AddExp addExp, boolean isLvalue) {
        IrValue res = new IrValue();
        if (addExp == null) {
            return res;
        }
        IrValue v1 = new IrValue(), v2;
        for (int i = 0; i < addExp.getMulExpArrayList().size(); i++) { //TODO:注意constant类型的数
            MulExp m = addExp.getMulExpArrayList().get(i);
            if(i == 0) { // 返回第一个mulExp的类型即可
                v1 = visitMulExp(m, isLvalue);
                if (addExp.getMulExpArrayList().size() == 1) return v1; //只有一个mulExp，直接返回v1
                res.setType(v1.getType());
                res.addAllTempInstruction(v1.getTempInstructions());
            } else { //其他的也需要分析，可能会有错误出现
                v2 = visitMulExp(m, isLvalue);
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
        return visitAddExp(constExp.getAddExp(), false); // 根据文法肯定不是左值
    }

    //TODO： 下边所有的visit方法均与位运算相关，本次作业不涉及，大致实现应该与visitAddExp类似
    private void visitRelExp(RelExp relExp) {
        for (AddExp a: relExp.getAddExpArrayList()) {
            visitAddExp(a, false);
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
