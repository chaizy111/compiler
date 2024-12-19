package Analysis.middle;

import Analysis.middle.Llvmir.IrModule;
import Analysis.middle.Llvmir.IrValue;
import Analysis.middle.Llvmir.Type.*;
import Analysis.middle.Llvmir.ValueType.Constant.IrConstant;
import Analysis.middle.Llvmir.ValueType.Constant.IrConstantArray;
import Analysis.middle.Llvmir.ValueType.Constant.IrConstantVal;
import Analysis.middle.Llvmir.ValueType.Function.CntUtils;
import Analysis.middle.Llvmir.ValueType.Function.IrArgument;
import Analysis.middle.Llvmir.ValueType.Function.IrFunction;
import Analysis.middle.Llvmir.ValueType.Global.IrGlobalConstString;
import Analysis.middle.Llvmir.ValueType.Instruction.*;
import Analysis.middle.Llvmir.ValueType.IrBasicBlock;
import Analysis.middle.Llvmir.ValueType.Global.IrGlobalVariable;
import Analysis.middle.Llvmir.ValueType.Instruction.IrLabel;
import Analysis.middle.Symbol.Symbol;
import Analysis.middle.Symbol.SymbolTable;
import Analysis.middle.Symbol.Value.ArrayValue;
import Analysis.middle.Symbol.Value.FuncValue;
import Analysis.middle.Symbol.Value.Value;
import Analysis.frontend.Token.Token;
import Analysis.frontend.Token.TokenType;
import Analysis.middle.Symbol.Value.VarValue;
import Analysis.frontend.Tree.*;
import Error.ErrorDealer;
import Analysis.frontend.Tree.Character;
import Analysis.frontend.Tree.Number;

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
    private CntUtils cntUtils;
    //先在visitor全局实现一个label分配器，用以给label命名，label命名采用 %label.n的形式

    public Visitor(CompUnit compUnit, FileWriter outputfile, ErrorDealer errorDealer) {
        this.compUnit = compUnit;
        this.outputfile = outputfile;
        this.errorDealer = errorDealer;
        this.symbolTables = new LinkedHashMap<>();
        this.nowTableId = 0;
        this.maxTableId = 0;
        this.irModule = new IrModule();
        this.nowIrFunction = new IrFunction();
        this.cntUtils = new CntUtils();
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
            res.add(visitConstDef(c, constDecl.getbType()));
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

    private IrGlobalVariable visitConstDef(ConstDef constDef, TokenType.tokenType bType) {
        if (constDef == null) return null;
        // 配置symbol
        Symbol s = new Symbol(nowTableId, constDef.getIdent().getString());
        if(constDef.getConstExp() == null) s.setType(0);
        else {
            IrValue v = visitConstExp(constDef.getConstExp());
            if (v instanceof IrConstantVal) {
                s.setArraySize(((IrConstantVal) v).getVal());
            } else {
                s.setArraySize(constDef.getConstExp().getResult());
            }
            s.setType(1);
        }
        if (bType.equals(TokenType.tokenType.INTTK)) s.setBtype(0);
        else s.setBtype(1);
        s.setConst(true);
        s.setValue(visitConstInitVal(constDef.getConstInitVal()));

        //配置IrGlobalVariable
        IrGlobalVariable irGlobalVariable = new IrGlobalVariable("@" + s.getSymbolName());
        irGlobalVariable.setConst(true);
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
                constant.setType(t);
                irGlobalVariable.setConstant(constant);
            } else { //数组型
                ArrayList<Integer> temp = ((ArrayValue) s.getValue()).getArray(s.getArraySize());
                ArrayList<IrConstantVal> arrays = new ArrayList<>();
                for (Integer i:temp) {
                    IrConstantVal c = new IrConstantVal(i);
                    c.setType(t);
                    arrays.add(c);
                }
                IrArrayTy nt = new IrArrayTy();
                nt.setArrayType(t);
                nt.setArraySize(s.getArraySize());
                irGlobalVariable.setOutputType(nt);
                IrConstant constant = new IrConstantArray(arrays);
                constant.setType(t);
                irGlobalVariable.setConstant(constant);
            }
        } else { //没有定义就全定义为0
            if (s.getArraySize() == 0) { // 非数组型
                irGlobalVariable.setConstant(null);
            } else { //数组型
                irGlobalVariable.setConstant(null);
                IrArrayTy nt = new IrArrayTy();
                nt.setArrayType(t);
                nt.setArraySize(s.getArraySize());
                irGlobalVariable.setOutputType(nt);
            }
        }
        irGlobalVariable.setRegisterName("@" + s.getSymbolName());
        irGlobalVariable.setConst(true); //区分const型与正常variable
        s.setIrValue(irGlobalVariable); // 配置完成后将irGlobalVariable加到symbol中

        //错误处理
        if(isDuplicateSymbol(s.getSymbolName())) {
            int errorLine = constDef.getIdent().getLine();
            errorDealer.errorB(errorLine);
        } else {
            // 配置完成并实现错误处理后，在最后把symbol加到表中
            addSymbol(s);
        }
        return irGlobalVariable;
    }

    private ArrayList<IrInstruction> visitConstDefInFunc(ConstDef constDef, TokenType.tokenType bType) {
        //这里返回类型不是void原因是有可能会在InitVal的定义中出现计算语句，所以要返回一个instruction的list
        ArrayList<IrInstruction> instructions = new ArrayList<>();
        if (constDef == null) return null;
        // 配置symbol
        Symbol s = new Symbol(nowTableId, constDef.getIdent().getString());
        if(constDef.getConstExp() == null) s.setType(0);
        else {
            IrValue v = visitConstExp(constDef.getConstExp());
            if (v instanceof IrConstantVal) {
                s.setArraySize(((IrConstantVal) v).getVal());
            } else {
                s.setArraySize(constDef.getConstExp().getResult());
            }
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
        IrPointerTy pointerTy = new IrPointerTy();
        pointerTy.setType(t);
        IrArrayTy nt = new IrArrayTy(); //用来配置AllocaType
        if (s.getArraySize() != 0) {
            nt.setArrayType(t);
            nt.setArraySize(s.getArraySize());
            t = nt;
        }

        IrAlloca irAlloca = new IrAlloca(); //先分配内存，将内存分配指令alloca填到list中，并将这个内存分配指令作为该符号的IrValue
        irAlloca.setConst(true);
        irAlloca.setAllocaType(t);
        irAlloca.setType(pointerTy); //irAlloca得到指针型的数据，所以其真正类型只有一个，即指针，t与nt都是用来输出的
        irAlloca.setRegisterName("%" + nowIrFunction.getNowRank());
        instructions.add(irAlloca);

        if (constDef.getConstInitVal() != null) { //可能没有等号和等号右边的部分！！！
            IrValue v = visitConstInitValInFunction(constDef.getConstInitVal());
            instructions.addAll(v.getTempInstructions()); // 分析右边部分，将产生的代码放到list中
            if (s.getArraySize() != 0) { //数组类型，多条IrStore与getelementptr指令组合
                ArrayList<IrValue> irValues = v.getTempValues();
                irAlloca.setTempValues(irValues); //数组型要在符号表中存的irValue进行配置，将诸irValue存起来备用
                ArrayList<IrConstantVal> constantVals = new ArrayList<>();
                for (int i = 0; i < irValues.size(); i++) {
                    IrGetelementptr irGetelementptr = new IrGetelementptr(); //先配置getelementptr指令
                    irGetelementptr.setExc(String.valueOf(i));

                    irGetelementptr.setType(irAlloca.getType()); //实际返回的type就是与alloca同样的指针type
                    IrType temp = irAlloca.getAllocaType();
                    irGetelementptr.setOutputType(temp);

                    irGetelementptr.setOperand(irAlloca, 0);
                    irGetelementptr.setRegisterName("%" + nowIrFunction.getNowRank());
                    instructions.add(irGetelementptr);
                    IrStore irStore = new IrStore(); //数组def时紧跟在getptr指令后边的就是一条store指令
                    irStore.setOperand(irGetelementptr, 0);
                    irStore.setOperand(irValues.get(i), 1); //返回的irValues的第i个，即exp的第i个
                    instructions.add(irStore);

                    constantVals.add(new IrConstantVal(((IrConstantVal) irValues.get(i)).getVal()));
                }
                for (int i = irValues.size(); i < s.getArraySize(); i++) { //不够填0
                    IrGetelementptr irGetelementptr = new IrGetelementptr(); //先配置getelementptr指令
                    irGetelementptr.setExc(String.valueOf(i));

                    irGetelementptr.setType(irAlloca.getType()); //实际返回的type就是与alloca同样的指针type
                    IrType temp = irAlloca.getAllocaType();
                    irGetelementptr.setOutputType(temp);

                    irGetelementptr.setOperand(irAlloca, 0);
                    irGetelementptr.setRegisterName("%" + nowIrFunction.getNowRank());
                    instructions.add(irGetelementptr);
                    IrStore irStore = new IrStore(); //数组def时紧跟在getptr指令后边的就是一条store指令
                    irStore.setOperand(irGetelementptr, 0);
                    irStore.setOperand(new IrConstantVal(0), 1); //返回的irValues的第i个，即exp的第i个
                    instructions.add(irStore);
                }
                irAlloca.setConstant(new IrConstantArray(constantVals));
            } else { //val类型，一条IrStore
                IrConstant constant = new IrConstantVal(((IrConstantVal) v).getVal());
                constant.setType(t);
                irAlloca.setConstant(constant);

                IrStore irStore = new IrStore();
                irStore.setOperand(irAlloca, 0);
                irStore.setOperand(v, 1);
                instructions.add(irStore);
            }
        }

        //错误处理
        if(isDuplicateSymbol(s.getSymbolName())) {
            int errorLine = constDef.getIdent().getLine();
            errorDealer.errorB(errorLine);
        } else {
            // 配置完成并实现错误处理后，在最后把symbol加到表中
            s.setIrValue(irAlloca);
            addSymbol(s);
        }
        return instructions;
    }

    //这里的分析先将等号右边作为一个整体传入Value，然后value保存在左边符号的符号表内，之后再对右边进行分析
    private Value visitConstInitVal(ConstInitVal constInitVal) {
        if (constInitVal.getConstExp() != null) { //只有1个exp
            IrValue v = visitConstExp(constInitVal.getConstExp());
            VarValue value = new VarValue();
            if (v instanceof IrConstantVal) {
                value.setItem(((IrConstantVal) v).getVal());
            } else {
                value.setItem(constInitVal.getConstExp().getResult());
            }
            return value;
        } else if (!constInitVal.getConstExpArrayList().isEmpty()) { // 多个Exp
            ArrayValue value = new ArrayValue();
            for (ConstExp c: constInitVal.getConstExpArrayList()) {
                IrValue v = visitConstExp(c);
                if (v instanceof IrConstantVal) {
                    value.addItem(((IrConstantVal) v).getVal());
                } else {
                    value.addItem(c.getResult());
                }
            }
            return value;
        } else { //String型
            ArrayValue value = new ArrayValue();
            if (constInitVal.getStringConst() == null) return value; // 如果为空直接return
            String s = constInitVal.getStringConst().getString();
            for (java.lang.Character c: s.substring(1, s.length() - 1).toCharArray()) {
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
            IrValue res = new IrValue();
            String s = constInitVal.getStringConst().getString();
            for (java.lang.Character c: s.substring(1, s.length() - 1).toCharArray()) {
                IrConstantVal i = new IrConstantVal(c);
                i.setType(new IrCharTy());
                res.addTempValue(i);
            }
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
            IrValue v = visitConstExp(valDef.getConstExp());
            if (v instanceof IrConstantVal) {
                s.setArraySize(((IrConstantVal) v).getVal());
            } else {
                s.setArraySize(valDef.getConstExp().getResult());
            }
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
                constant.setType(t);
                irGlobalVariable.setConstant(constant);
            } else { //数组型
                ArrayList<Integer> temp = ((ArrayValue) s.getValue()).getArray(s.getArraySize());
                ArrayList<IrConstantVal> arrays = new ArrayList<>();
                for (Integer i:temp) {
                    IrConstantVal c = new IrConstantVal(i);
                    c.setType(t);
                    arrays.add(c);
                }
                IrArrayTy nt = new IrArrayTy();
                nt.setArrayType(t);
                nt.setArraySize(s.getArraySize());
                irGlobalVariable.setOutputType(nt);
                IrConstant constant = new IrConstantArray(arrays);
                constant.setType(t);
                irGlobalVariable.setConstant(constant);
            }
        } else { //没有定义就全定义为0
            if (s.getArraySize() == 0) { // 非数组型
                irGlobalVariable.setConstant(null);
            } else { //数组型
                irGlobalVariable.setConstant(null);
                IrArrayTy nt = new IrArrayTy();
                nt.setArrayType(t);
                nt.setArraySize(s.getArraySize());
                irGlobalVariable.setOutputType(nt);
            }
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
            IrValue v = visitConstExp(valDef.getConstExp());
            instructions.addAll(v.getTempInstructions());
            if (v instanceof IrConstantVal) {
                s.setArraySize(((IrConstantVal) v).getVal());
            } else {
                s.setArraySize(valDef.getConstExp().getResult());
            }
            s.setType(1);
        }
        if (bType.equals(TokenType.tokenType.INTTK)) s.setBtype(0);
        else s.setBtype(1);

        IrType t; // 判断该符号的种类
        if (s.getBtype() == 0) {
            t = new IrIntegerTy();
        } else {
            t = new IrCharTy();
        }
        IrPointerTy pointerTy = new IrPointerTy();
        pointerTy.setType(t);
        IrArrayTy nt = new IrArrayTy(); //用来配置AllocaType
        if (s.getArraySize() != 0) {
            nt.setArrayType(t);
            nt.setArraySize(s.getArraySize());
            t = nt;
        }

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
                    irGetelementptr.setExc(String.valueOf(i));

                    irGetelementptr.setType(irAlloca.getType()); //实际返回的type就是与alloca同样的指针type
                    IrType temp = irAlloca.getAllocaType();
                    irGetelementptr.setOutputType(temp);

                    irGetelementptr.setOperand(irAlloca, 0);
                    irGetelementptr.setRegisterName("%" + nowIrFunction.getNowRank());
                    instructions.add(irGetelementptr);
                    IrStore irStore = new IrStore(); //数组def时紧跟在getptr指令后边的就是一条store指令
                    irStore.setOperand(irGetelementptr, 0);
                    irStore.setOperand(irValues.get(i), 1); //返回的irValues的第i个，即exp的第i个
                    instructions.add(irStore);
                }
                for (int i = irValues.size(); i < s.getArraySize(); i++) { //不够填0
                    IrGetelementptr irGetelementptr = new IrGetelementptr(); //先配置getelementptr指令
                    irGetelementptr.setExc(String.valueOf(i));

                    irGetelementptr.setType(irAlloca.getType()); //实际返回的type就是与alloca同样的指针type
                    IrType temp = irAlloca.getAllocaType();
                    irGetelementptr.setOutputType(temp);

                    irGetelementptr.setOperand(irAlloca, 0);
                    irGetelementptr.setRegisterName("%" + nowIrFunction.getNowRank());
                    instructions.add(irGetelementptr);
                    IrStore irStore = new IrStore(); //数组def时紧跟在getptr指令后边的就是一条store指令
                    irStore.setOperand(irGetelementptr, 0);
                    irStore.setOperand(new IrConstantVal(0), 1);
                    instructions.add(irStore);
                }
            } else { //val类型，一条IrStore
                IrStore irStore = new IrStore(); //这里也存在类型转换
                IrValue v1 = irAlloca;
                IrValue v2 = v;
                if (((IrPointerTy) v1.getType()).getType().getClass() != v2.getType().getClass()) {
                    if (v2.getType().getClass() == IrIntegerTy.class && !(v2 instanceof IrConstant)) { //int转char，用trunc
                        IrTrunc trunc = new IrTrunc();
                        trunc.setOperand(new IrValue(v2), 0);
                        trunc.setRegisterName("%" + nowIrFunction.getNowRank());
                        trunc.setType(new IrCharTy());
                        instructions.add(trunc);
                        v2= trunc;
                    } else if (v2.getType().getClass() == IrCharTy.class && !(v2 instanceof IrConstant)) { //char转int，用zext
                        IrZext z = new IrZext();
                        z.setOperand(new IrValue(v2), 0);
                        z.setRegisterName("%" + nowIrFunction.getNowRank());
                        z.setType(new IrIntegerTy());
                        instructions.add(z);
                        v2 = z;
                    }
                }
                irStore.setOperand(v1, 0);
                irStore.setOperand(v2, 1);
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
        if (initVal.getExp() != null) { //只有1个exp
            IrValue v = visitExp(initVal.getExp(), false);
            VarValue value = new VarValue();
            if (v instanceof IrConstantVal) {
                value.setItem(((IrConstantVal) v).getVal());
            } else {
                value.setItem(initVal.getExp().getAddExp().getResult());
            }
            return value;
        } else if (!initVal.getExpArrayList().isEmpty()) { // 多个Exp
            ArrayValue value = new ArrayValue();
            for (Exp c: initVal.getExpArrayList()) {
                IrValue v = visitExp(c, false);
                if (v instanceof IrConstantVal) {
                    value.addItem(((IrConstantVal) v).getVal());
                } else {
                    value.addItem(c.getAddExp().getResult());
                }
            }
            return value;
        }  else { //String型
            ArrayValue value = new ArrayValue();
            String s = initVal.getStringConst().getString();
            for (java.lang.Character c: s.substring(1, s.length() - 1).toCharArray()) {
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
            IrValue res = new IrValue();
            String s = initVal.getStringConst().getString();
            for (java.lang.Character c: s.substring(1, s.length() - 1).toCharArray()) {
                IrConstantVal i = new IrConstantVal(c);
                i.setType(new IrCharTy());
                res.addTempValue(i);
            }
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
        s.setIrValue(function); //提前配置function

        if(isDuplicateSymbol(s.getSymbolName())) {
            int errorLine = funcDef.getIdent().getLine();
            errorDealer.errorB(errorLine);
        } else {
            addSymbol(s);
        } //防止递归，一定要在进入新func的block前加进去

        // 再新建一个symbolTable，
        // 注意函数的block与普通block的不同，函数的block需要在上一层建立，因为需要将参数进行存储，普通block在visitBlock函数中建立即可
        newSymbolTable(); // 进入新符号表的同时配置IrFunction
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
        //为传入的参数分配内存，使用alloca，store语句导入
        for (int i = 0; i < temp.size(); i++) {
            IrAlloca irAlloca = new IrAlloca(); //先分配内存
            irAlloca.setAllocaType(temp.get(i).getType());
            IrPointerTy pointerTy = new IrPointerTy();
            pointerTy.setType(temp.get(i).getType());
            irAlloca.setType(pointerTy); //irAlloca得到指针型的数据，所以其真正类型只有一个，即指针，t与nt都是用来输出的
            irAlloca.setRegisterName("%" + nowIrFunction.getNowRank());
            function.addTempInstruction(irAlloca);
            Symbol sy = getSymbol(temp.get(i).getName());
            IrStore irStore = new IrStore();
            irStore.setOperand(irAlloca, 0);
            irStore.setOperand(sy.getIrValue(), 1);
            function.addTempInstruction(irStore);
            sy.setIrValue(irAlloca); //store后将sy的IrValue改为IrAlloca
        }
        function.setIrBlock(visitFuncBlock(funcDef.getBlock(), bType));
        //function.setRegisterName("-1");
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

        //错误处理
        if(isDuplicateSymbol(s.getSymbolName())) {
            int errorLine = funcFParam.getIdent().getLine();
            errorDealer.errorB(errorLine);
        } else {
            // 配置完成并实现错误处理后，在最后把symbol加到表中
            addSymbol(s);
        }

        return argument;
    }

    // 由于处理逻辑不同，所以把不同的block分开
    private IrBasicBlock visitBlock(Block block, boolean isInForBlock, int funcType, IrLabel endLabel, IrLabel continueLabel) {
        // 多种情况，循环块与非循环块，是否为void函数中的块
        if (block == null) {
            return new IrBasicBlock();
        }
        IrBasicBlock basicBlock = new IrBasicBlock();
        newSymbolTable();
        for (BlockItem b : block.getBlockItemArrayList()) {
            basicBlock.addAllTempInstruction(visitBlockItem(b, isInForBlock, funcType, endLabel, continueLabel).getTempInstructions());
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
                IrLabel label = new IrLabel(cntUtils.getCount());
                IrGotoBr g = new IrGotoBr(label);
                basicBlock.addTempInstruction(g);
                basicBlock.addTempInstruction(label);

                IrRet ret = new IrRet(); // 添加返回指令 //注意返回时可能的类型转换
                IrValue v = visitExp(returnStmt.getExp(), false);//根据文法，这里肯定不是左值
                basicBlock.addAllInstruction(v.getTempInstructions());
                if (funcType == 0) {
                    ret.setType(new IrIntegerTy());
                } else if (funcType == 1) {
                    ret.setType(new IrCharTy());
                } else {
                    ret.setType(new IrVoidTy());
                }
                if (v instanceof IrConstantVal) {
                    //如果exp是Number或Character,那一层层向上传，最后到达这里的一定是一个IrConstant型的，
                    // 所以如果分析的结果是IrConstant型，我们可以认为ret了一个常量
                    ret.setResult(((IrConstantVal) v).getVal());
                } else {
                    //如果不是IrConstant型，那必然经过了运算，我们去上一次分析的IrValue，其RegisterName即为记过存储的位置，传入ret即可
                    //在此处有类型转换
                    ret.setRegisterName(v.getRegisterName());
                    if (v.getType() instanceof IrCharTy && ret.getType() instanceof IrIntegerTy) {
                        IrZext z = new IrZext();
                        z.setOperand(new IrValue(v), 0);
                        z.setRegisterName("%" + nowIrFunction.getNowRank());
                        z.setType(new IrIntegerTy());
                        basicBlock.addInstruction(z);
                        ret.setRegisterName(z.getRegisterName());
                    } else if (v.getType() instanceof IrIntegerTy && ret.getType() instanceof IrCharTy) {
                        IrTrunc t = new IrTrunc();
                        t.setOperand(new IrValue(v), 0);
                        t.setRegisterName("%" + nowIrFunction.getNowRank());
                        t.setType(new IrCharTy());
                        basicBlock.addInstruction(t);
                        ret.setRegisterName(t.getRegisterName());
                    }
                }
                basicBlock.addInstruction(ret);

                //ret块后的第一个块
                IrLabel label1 = new IrLabel(cntUtils.getCount());
                basicBlock.addTempInstruction(label1);

            } else if (b instanceof Decl) {
                basicBlock.addAllInstruction(visitDeclInFunc((Decl) b));
            } else if (b instanceof Stmt) {
                basicBlock.addAllInstruction(visitStmt((Stmt) b, false, funcType, null, null).getTempInstructions());
            }
        }
        // 错误处理
        if (funcType != 2 && returnStmt == null) { //不是void型函数且没有返回语句
            int errorLine = block.getEndLine();
            errorDealer.errorG(errorLine);
        }
        if (funcType == 2 && returnStmt == null) { //是void型函数且没有返回语句
            IrLabel label = new IrLabel(cntUtils.getCount());
            IrGotoBr g = new IrGotoBr(label);
            basicBlock.addTempInstruction(g);
            basicBlock.addTempInstruction(label);

            IrRet ret = new IrRet();
            ret.setType(new IrVoidTy());
            basicBlock.addInstruction(ret);

            //ret块后的第一个块
            IrLabel label1 = new IrLabel(cntUtils.getCount());
            basicBlock.addTempInstruction(label1);
        }
        return basicBlock;
    }

    private IrValue visitBlockItem(BlockItem blockItem, boolean isInForBlock, int funcType, IrLabel endLabel , IrLabel continueLabel) {
        if (blockItem == null) {
            return null;
        }
        if (blockItem instanceof Decl) {
            IrValue value = new IrValue();
            ArrayList<IrInstruction> instructions = visitDeclInFunc((Decl) blockItem);
            value.addAllTempInstruction(instructions);
            return value;
        } else {
            return visitStmt((Stmt) blockItem, isInForBlock, funcType, endLabel, continueLabel);
        }
    }

    private IrValue visitStmt(Stmt stmt, boolean isInForBlock, int funcType, IrLabel endLabel, IrLabel continueLabel) {
        if (stmt == null) {
            return new IrValue();
        }
        // 由于要对for有关block进行判断，来看是否出现m错，设置了isInForBlock变量，这个变量的相关逻辑比较绕，
        // 主要就是如果是在for的stmt是block型，则在visitBlock时就将这个属性设为true，之后这个属性便会层层下传，直到for的block分析结束
        // 还要对是否是在void函数中进行判断，如果是在void函数中，那if，for，以及return语句就需要注意进行报错，所以需要传递isInVoidFunc参数
        if (stmt instanceof IfStmt) {
            return visitIf((IfStmt) stmt, isInForBlock, funcType, endLabel, continueLabel);
        } else if (stmt instanceof For) {
            return visitFor((For) stmt, funcType);
        } else if (stmt instanceof ReturnStmt) {
            return visitReturnStmt((ReturnStmt) stmt, funcType);
        } else if (stmt instanceof PrintfStmt) {
            return visitPrintfStmt((PrintfStmt) stmt);
        } else if (stmt instanceof LValStmt) {
            return visitLValStmt((LValStmt) stmt);
        } else if (stmt.getbOrC() != null) { // 处理break和continue
            if (!isInForBlock) { //不是for循环中的break与continue，直接错误处理
                int errorLine = stmt.getbOrC().getLine();
                errorDealer.errorM(errorLine);
            } else {
                IrValue res = new IrValue();
                if (stmt.getbOrC().getType() == TokenType.tokenType.BREAKTK) { //这里要把break和continue放到一个新块中
                    IrLabel label = new IrLabel(cntUtils.getCount());
                    IrGotoBr g = new IrGotoBr(label);
                    res.addTempInstruction(g);
                    res.addTempInstruction(label);
                    IrGotoBr g1 = new IrGotoBr(endLabel);
                    res.addTempInstruction(g1);
                    IrLabel label1 = new IrLabel(cntUtils.getCount());
                    res.addTempInstruction(label1);
                } else {
                    IrLabel label = new IrLabel(cntUtils.getCount());
                    IrGotoBr g = new IrGotoBr(label);
                    res.addTempInstruction(g);
                    res.addTempInstruction(label);
                    IrGotoBr g1 = new IrGotoBr(continueLabel);
                    res.addTempInstruction(g1);
                    IrLabel label1 = new IrLabel(cntUtils.getCount());
                    res.addTempInstruction(label1);
                }
                return res;
            }
        } else if (stmt.getB() != null) {
            return visitBlock(stmt.getB(), isInForBlock, funcType, endLabel, continueLabel);
        } else if (stmt.getE() != null) {
            //if (stmt.getE().getAddExp().isUseful()) { //这里判断exp是否有用，防止出现在一行有 1+0;这种语句出现，这种语句出现就直接忽略
                //判断的逻辑就是看是不是函数，如果不是函数就直接忽略(暂时不用，优化的时候可以用)
                return visitExp(stmt.getE(), false); //根据文法，这里肯定不是左值
            //}
        }
        return new IrValue();
    }

    private IrValue visitIf(IfStmt ifStmt, boolean isInForBlock, int funcType, IrLabel endLabel1, IrLabel continueLabel) {
        //TODO:实际应该返回一个ArrayList<IrBlock>,但这里先返回一个 IrValue
        IrValue res = new IrValue();
        if (ifStmt == null) return res;
        //先生成stmt，else，end位置的label
        IrLabel ifLabel = new IrLabel(cntUtils.getCount());
        IrLabel elseLabel;
        IrLabel endLabel = new IrLabel(cntUtils.getCount());
        if (ifStmt.getS2() == null) {  //无else
            //分析cond
            IrLabel v1 = visitCond(ifStmt.getC(), ifLabel, endLabel);
            //先实现一条gotobr指令，确保跳到cond
            IrGotoBr g1 = new IrGotoBr(v1);
            res.addTempInstruction(g1);
            res.addAllTempInstruction(v1.getTempInstructions());
            // 加if的Label，后边跟的就是stmt1中的语句
            res.addTempInstruction(ifLabel);
            IrValue v2 = visitStmt(ifStmt.getS1(), isInForBlock, funcType, endLabel1, continueLabel);
            res.addAllTempInstruction(v2.getTempInstructions());
            //在if的stmt执行完之后要加一条goto指令跳到endLabel处
            IrGotoBr g = new IrGotoBr(endLabel);
            res.addTempInstruction(g);
            //最后再把endLabel输出
            res.addTempInstruction(endLabel);
        } else { // 有else
            elseLabel = new IrLabel(cntUtils.getCount());
            //分析cond,这里的endLabel是else的label
            IrLabel v1 = visitCond(ifStmt.getC(), ifLabel, elseLabel);
            //先实现一条gotobr指令，确保跳到cond
            IrGotoBr g1 = new IrGotoBr(v1);
            res.addTempInstruction(g1);
            res.addAllTempInstruction(v1.getTempInstructions());
            // 加if的Label，后边跟的就是stmt1中的语句
            res.addTempInstruction(ifLabel);
            IrValue v2 = visitStmt(ifStmt.getS1(), isInForBlock, funcType, endLabel1, continueLabel);
            res.addAllTempInstruction(v2.getTempInstructions());
            //在if的stmt执行完之后要加一条gotobr指令跳到endLabel处
            IrGotoBr g = new IrGotoBr(endLabel);
            res.addTempInstruction(g);
            //然后输出else的label，后边跟stmt2中的语句
            res.addTempInstruction(elseLabel);
            IrValue v3 = visitStmt(ifStmt.getS2(), isInForBlock, funcType, endLabel1, continueLabel);
            res.addAllTempInstruction(v3.getTempInstructions());
            //else的stmt执行完也要加一句gotobr跳到endLabel处
            res.addTempInstruction(g);
            //最后再把endLabel输出
            res.addTempInstruction(endLabel);
        }
        return res;
    }

    private IrValue visitFor(For f, int funcType) { //逻辑类似
        //TODO:实际应该返回一个ArrayList<IrBlock>,但这里先返回一个 IrValue
        IrValue res = new IrValue();
        if (f == null) return res;
        //三个标签，第一个标签为for大括号内的语句，第二个为循环语句，即第二个forStmt，第三个为结束部分
        IrLabel mainLabel = new IrLabel(cntUtils.getCount());
        IrLabel continueLabel = new IrLabel(cntUtils.getCount());
        IrLabel endLabel = new IrLabel(cntUtils.getCount());
        //对于forStmt1，不需要Label，在for之前生成一次即可
        IrValue v1 = visitForStmt(f.getForStmt1());
        res.addAllTempInstruction(v1.getTempInstructions());
        //先分析cond，得到cond的label
        IrLabel condLabel = visitCond(f.getC(), mainLabel, endLabel);
        //先实现一条gotobr指令，确保跳到cond
        IrGotoBr g = new IrGotoBr(condLabel);
        res.addTempInstruction(g);
        //然后输出condlabel部分的所有语句
        res.addAllTempInstruction(condLabel.getTempInstructions());
        //输出完cond后输出mainLabel，开始这部分的输出,注意，这部分涉及break与continue
        res.addTempInstruction(mainLabel);
        IrValue v2;
        if (f.getS().getB() != null) v2 = visitBlock(f.getS().getB(), true, funcType, endLabel, continueLabel);
        else v2 = visitStmt(f.getS(), false, funcType, endLabel, continueLabel);
        res.addAllTempInstruction(v2.getTempInstructions());
        //main跳到continueLabel
        IrGotoBr g1 = new IrGotoBr(continueLabel);
        res.addTempInstruction(g1);
        //mainLabel部分输出完成后输出continueLabel
        res.addTempInstruction(continueLabel);
        IrValue v3 = visitForStmt(f.getForStmt2());
        res.addAllTempInstruction(v3.getTempInstructions());
        //continueLabel部分跳到condLabel部分，在后边添加一条gotobr
        IrGotoBr g3 = new IrGotoBr(condLabel);
        res.addTempInstruction(g3);
        //最后输出一个endLabel的名字即可
        res.addTempInstruction(endLabel);

        return res;
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
        IrValue res = new IrValue(); //注意返回时可能的类型转换
        //ret是基本块,这是建立ret基本快的几步
        IrLabel label = new IrLabel(cntUtils.getCount());
        IrGotoBr g = new IrGotoBr(label);
        res.addTempInstruction(g);
        res.addTempInstruction(label);

        IrRet ret = new IrRet();
        if (returnStmt.getExp() != null) {
            IrValue v = visitExp(returnStmt.getExp(), false);
            res.addAllTempInstruction(v.getTempInstructions());
            if (funcType == 0) { //配置ret的返回类型，与函数保持一致
                ret.setType(new IrIntegerTy());
            } else if (funcType == 1) {
                ret.setType(new IrCharTy());
            }
            if (v instanceof IrConstantVal) {
                //如果exp是Number或Character,那一层层向上传，最后到达这里的一定是一个IrConstant型的，
                // 所以如果分析的结果是IrConstant型，我们可以认为ret了一个常量
                ret.setResult(((IrConstantVal) v).getVal());
            } else {
                //如果不是IrConstant型，那必然经过了运算，我们去上一次分析的IrValue，其RegisterName即为记过存储的位置，传入ret即可
                ret.setRegisterName(v.getRegisterName());
                if (v.getType() instanceof IrCharTy && ret.getType() instanceof IrIntegerTy) {
                    IrZext z = new IrZext();
                    z.setOperand(new IrValue(v), 0);
                    z.setRegisterName("%" + nowIrFunction.getNowRank());
                    z.setType(new IrIntegerTy());
                    res.addTempInstruction(z);
                    ret.setRegisterName(z.getRegisterName());
                } else if (v.getType() instanceof IrIntegerTy && ret.getType() instanceof IrCharTy) {
                    IrTrunc t = new IrTrunc();
                    t.setOperand(new IrValue(v), 0);
                    t.setRegisterName("%" + nowIrFunction.getNowRank());
                    t.setType(new IrCharTy());
                    res.addTempInstruction(t);
                    ret.setRegisterName(t.getRegisterName());
                }
            }
        } else {
            ret.setType(new IrVoidTy());
        }
        res.addTempInstruction(ret);

        //ret块后的第一个块
        IrLabel label1 = new IrLabel(cntUtils.getCount());
        res.addTempInstruction(label1);

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
                    if (old.charAt(rpos + 1) == 'c') { //存kind
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
            } else { //是%d%c 注意：putch putint前需要类型转换
                IrCall call2 = new IrCall(); //配置putint putch的call，其名字需要根据kind[i]判断，参数数为1，参数从exp[k]获取
                IrValue v = exps.get(k);
                if (exps.get(k).getType() instanceof IrCharTy) { //char类型赋给%d%c,要用zext做类型转换，并更改exp的类型
                    if (v instanceof IrConstantVal) { //如果是常量，不用类型转换，直接改其type即可
                        v.setType(new IrIntegerTy());
                    } else {
                        IrZext z = new IrZext();
                        z.setOperand(new IrValue(exps.get(k)), 0);
                        z.setRegisterName("%" + nowIrFunction.getNowRank());
                        z.setType(new IrIntegerTy());
                        res.addTempInstruction(z);
                        v = z;
                    }
                }
                if (kind.get(i).equals("%d")) {
                    call2.setFuncName("@putint");
                } else {
                    call2.setFuncName("@putch");
                }
                call2.setType(new IrVoidTy());
                call2.setParaNum(1);
                call2.setOperand(v, 0);
                k++;
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
        if (lValStmt.isGetChar()) { //处理getChar与getInt类型函数调用 //注意可能的类型转换，类型转换的指令根据等号左边的类型判断
            IrCall call = new IrCall();
            call.setRegisterName("%" + nowIrFunction.getNowRank()); //不能再if外边设，编号会出问题
            call.setParaNum(0);
            call.setFuncName("@getchar");
            call.setType(new IrIntegerTy());
            res.setRegisterName(call.getRegisterName());
            res.addTempInstruction(call);//call完要store，注意类型转换
            IrValue v1 = visitLVal(lValStmt.getlVal(), true);
            res.addAllTempInstruction(v1.getTempInstructions());
            IrValue v2 = call;
            IrStore store = new IrStore();
            if (((IrPointerTy) v1.getType()).getType().getClass() == IrCharTy.class) {
                //如果v1Pointer类型内部存储的type与call得到的不一致， 则需要类型转换，生成一个新的IrValue传给store
                IrTrunc trunc = new IrTrunc();
                trunc.setOperand(new IrValue(call), 0);
                trunc.setRegisterName("%" + nowIrFunction.getNowRank());
                trunc.setType(new IrCharTy());
                res.addTempInstruction(trunc);
                v2= trunc;
            }
            store.setOperand(v1, 0);
            store.setOperand(v2, 1);
            res.addTempInstruction(store);
        } else if (lValStmt.isGetInt()) {
            IrCall call = new IrCall();
            call.setRegisterName("%" + nowIrFunction.getNowRank());
            call.setParaNum(0);
            call.setFuncName("@getint");
            call.setType(new IrIntegerTy());
            res.setRegisterName(call.getRegisterName());
            res.addTempInstruction(call);//call完要store，注意类型转换
            IrValue v1 = visitLVal(lValStmt.getlVal(), true);
            res.addAllTempInstruction(v1.getTempInstructions());
            IrValue v2 = call;
            IrStore store = new IrStore();
            if (((IrPointerTy) v1.getType()).getType().getClass() == IrCharTy.class) {
                //如果v1Pointer类型内部存储的type与call得到的不一致， 则需要类型转换，生成一个新的IrValue传给store
                IrTrunc trunc = new IrTrunc();
                trunc.setOperand(new IrValue(call), 0);
                trunc.setRegisterName("%" + nowIrFunction.getNowRank());
                trunc.setType(new IrCharTy());
                res.addTempInstruction(trunc);
                v2= trunc;
            }
            store.setOperand(v1, 0);
            store.setOperand(v2, 1);
            res.addTempInstruction(store);
        } else { //注意这里可能出现的类型转换
            //对于assign型语句，即Lval = exp型的，先通过visitLVal获取左边的值，从返回的IrValue中取出regisiterName用于构建Instruction
            //再通过visitExp获取右边的值，返回的IrValue用于构建Store Instruction
            //这里的操作肯定是Store，visitLVal产生op1，这里是要被存入的位置，我们需要保证op1一定是指针类型，visitExp产生等号右边的值(常量或寄存器)
            IrValue v2 = visitExp(lValStmt.getExp(), false); //注意顺序，先右后左
            IrValue v1 = visitLVal(lValStmt.getlVal(), true);
            res.addAllTempInstruction(v2.getTempInstructions());
            res.addAllTempInstruction(v1.getTempInstructions());
            if (((IrPointerTy) v1.getType()).getType().getClass() != v2.getType().getClass()) {
                if (v2.getType().getClass() == IrIntegerTy.class && !(v2 instanceof IrConstantVal)) { //int转char，用trunc
                    IrTrunc trunc = new IrTrunc();
                    trunc.setOperand(new IrValue(v2), 0);
                    trunc.setRegisterName("%" + nowIrFunction.getNowRank());
                    trunc.setType(new IrCharTy());
                    res.addTempInstruction(trunc);
                    v2= trunc;
                } else if (v2.getType().getClass() == IrCharTy.class && !(v2 instanceof IrConstantVal)) { //char转int，用zext
                    IrZext z = new IrZext();
                    z.setOperand(new IrValue(v2), 0);
                    z.setRegisterName("%" + nowIrFunction.getNowRank());
                    z.setType(new IrIntegerTy());
                    res.addTempInstruction(z);
                    v2 = z;
                }
            }
            IrInstruction i = new IrStore();
            i.setOperand(v1, 0);
            i.setOperand(v2, 1);
            res.addTempInstruction(i);
        }
        return res;
    }

    private IrValue visitForStmt(ForStmt forStmt) {
        //过程与visitLvalStmt的第三种情况，即LVal = exp型一致
        IrValue res = new IrValue();
        if (forStmt == null) return res;
        //先错误处理
        Token t = forStmt.getlVal().getIdent();
        if (isConstSymbol(t.getString())) {
            int errorLine = t.getLine();
            errorDealer.errorH(errorLine);
        }
        //再处理LVal与exp
        //对于assign型语句，即Lval = exp型的，先通过visitLVal获取左边的值，从返回的IrValue中取出regisiterName用于构建Instruction
        //再通过visitExp获取右边的值，返回的IrValue用于构建Store Instruction
        //这里的操作肯定是Store，visitLVal产生op1，这里是要被存入的位置，我们需要保证op1一定是指针类型，visitExp产生等号右边的值(常量或寄存器)
        IrValue v2 = visitExp(forStmt.getExp(), false); //注意顺序，先右后左
        IrValue v1 = visitLVal(forStmt.getlVal(), true);
        res.addAllTempInstruction(v2.getTempInstructions());
        res.addAllTempInstruction(v1.getTempInstructions());
        if (((IrPointerTy) v1.getType()).getType().getClass() != v2.getType().getClass()) {
            if (v2.getType().getClass() == IrIntegerTy.class && !(v2 instanceof IrConstantVal)) { //int转char，用trunc
                IrTrunc trunc = new IrTrunc();
                trunc.setOperand(new IrValue(v2), 0);
                trunc.setRegisterName("%" + nowIrFunction.getNowRank());
                trunc.setType(new IrCharTy());
                res.addTempInstruction(trunc);
                v2= trunc;
            } else if (v2.getType().getClass() == IrCharTy.class && !(v2 instanceof IrConstantVal)) { //char转int，用zext
                IrZext z = new IrZext();
                z.setOperand(new IrValue(v2), 0);
                z.setRegisterName("%" + nowIrFunction.getNowRank());
                z.setType(new IrIntegerTy());
                res.addTempInstruction(z);
                v2 = z;
            }
        }
        IrInstruction i = new IrStore();
        i.setOperand(v1, 0);
        i.setOperand(v2, 1);
        res.addTempInstruction(i);
        return res;
    }

    //由只返回一个int的值改为返回一个IrValue，包含过程中分析得到的语句以及最后结果存储的位置（寄存器名），还有exp的类型（用IrType表示的）
    private IrValue visitExp(Exp exp, boolean isLvalue) {
        if (exp == null) {
            return new IrValue();
        }
        return visitAddExp(exp.getAddExp(), isLvalue);
    }

    private IrLabel visitCond(Cond cond, IrLabel ifLabel, IrLabel endLabel) {
        if (cond == null) {
            IrLabel condLabel = new IrLabel(cntUtils.getCount());
            condLabel.addTempInstruction(condLabel);
            IrGotoBr g = new IrGotoBr(ifLabel);
            condLabel.addTempInstruction(g);
            return condLabel;
        }
        return visitLOrExp(cond.getlOrExp(), ifLabel, endLabel);
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

                i.setType(s.getIrValue().getType()); //getelemetptr的返回类型一定是一个指针，具体类型和符号表中的所存储的IrValue的类型保持一致
                IrArrayTy arrayTy = new IrArrayTy(); //既然是数组，就输出type一定是数组型
                arrayTy.setArrayType(((IrPointerTy) s.getIrValue().getType()).getType()); //数组的内部的类型与指针内部的类型保持一致
                arrayTy.setArraySize(s.getArraySize());
                i.setOutputType(arrayTy); //这里outputtype与i返回的type是一致的

                i.setOperand(v2, 0);
                i.setRegisterName("%" + nowIrFunction.getNowRank());
                res.setType(i.getType());
                res.setRegisterName(i.getRegisterName());
                res.addTempInstruction(i);
                return res;
            }
            if (v2.isConst()) {
                if (v2 instanceof IrGlobalVariable) {
                    if (v2.getConstant() != null) { //说明可以取到常量，直接用于计算
                        return v2.getConstant();
                    }
                } else {
                    return v2.getConstant();
                }
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
            String index;
            int num = -1;
            IrValue res = new IrValue();
            res.addAllTempInstruction(v1.getTempInstructions());
            if (v1 instanceof IrConstantVal) {
                index = String.valueOf(((IrConstantVal) v1).getVal());
                num = Integer.parseInt(index);
            } else {
                index = v1.getRegisterName();
                if (v1.getType().getClass() != IrIntegerTy.class) {
                    IrZext z = new IrZext();
                    z.setOperand(new IrValue(v1), 0);
                    z.setRegisterName("%" + nowIrFunction.getNowRank());
                    z.setType(new IrIntegerTy());
                    res.addTempInstruction(z);
                    index = z.getRegisterName();
                }
            }

            if (num != -1 && !isLvalue) { //不是左值才能直接用
                if (v2 instanceof IrGlobalVariable) {
                    if (v2.getConstant() != null) { //说明可以取到常量，直接用于计算
                        res = ((IrConstantArray) v2.getConstant()).getConstantVals().get(num);
                        return res;
                    }
                } else if (v2.isConst()) {
                    return ((IrConstantArray) v2.getConstant()).getConstantVals().get(num);
                }
            }

            IrGetelementptr i = new IrGetelementptr(); //配置Getelementptr
            i.setExc(index);
            i.setOperand(v2, 0);

            i.setType(v2.getType()); //getelemetptr的返回类型一定是一个指针，具体类型和符号表中的所存储的IrValue的类型保持一致
            if (s.getArraySize() == 0) { //这里需要特判，因为函数中的数组size为0，我们使用getelemetptr只能将outputtype设为指针的内置型
                //特判发现需要再load,我们需要把这个里边的指针load出来做返回值
                IrLoad load = new IrLoad();
                load.setOperand(v2, 0);
                load.setType(((IrPointerTy)v2.getType()).getType());
                load.setRegisterName("%" + nowIrFunction.getNowRank());
                res.addTempInstruction(load);
                i.setOperand(load, 0);
                i.setType(((IrPointerTy)v2.getType()).getType());
                i.setOutputType(((IrPointerTy) i.getType()).getType()); //outputtype为i32/i8
            }else { //正常有定义的数组
                IrArrayTy arrayTy = new IrArrayTy(); //既然是数组，就输出type一定是数组型
                if (v2.getType() instanceof IrArrayTy) { //常量数组，其符号表中存的IrValue的type即为数组内元素的type
                   arrayTy = ((IrArrayTy) v2.getType());
                } else {
                    arrayTy.setArrayType(((IrPointerTy) v2.getType()).getType()); //数组的内部的类型与指针内部的类型保持一致
                    arrayTy.setArraySize(s.getArraySize());
                }
                i.setOutputType(arrayTy); //这里outputtype与i返回的type是一致的
            }

            i.setRegisterName("%" + nowIrFunction.getNowRank());
            res.setType(i.getType());
            res.setRegisterName(i.getRegisterName());
            res.addTempInstruction(i);
            //如果不是左值需要再产生一条load指令，将getptr的结果load出来
            if (!isLvalue) {
                IrLoad l = new IrLoad();
                l.setOperand(i, 0);
                if (i.getType()  instanceof IrPointerTy) {
                    l.setType(((IrPointerTy) i.getType()).getType()); //load的type与i指令的type保持一致
                } else {
                    l.setType(i.getType());
                }
                l.setRegisterName("%" + nowIrFunction.getNowRank());
                res.setType(l.getType());
                res.addTempInstruction(l);
                res.setRegisterName(l.getRegisterName());//最后res的寄存器名与load保持一致
            }
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
        IrConstantVal res = new IrConstantVal(character.getToken().getString().charAt(1));
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
            //分析unaryOp的种类来决定下一步的操作，如果是+，如果是-生成一个与0相减的语句, 如果是!,则需要比较其与0的大小
            int judge = visitUnaryOp(unaryExp.getUnaryOp());
            IrValue v = visitUnaryExp(unaryExp.getUnaryExp(), isLvalue);
            if (judge == 1) { //生成一条sub语句并实现value的更新,注意这里没有保存计算结果，可能会出现bug
                if (v instanceof IrConstantVal) { //如果是常量，直接计算
                    ((IrConstantVal) v).setVal(-((IrConstantVal) v).getVal());
                } else {
                    //不是常量，可能需要类型转换 //todo
                    if (v.getType().getClass() != IrIntegerTy.class) { //所有非int类型一律转成int类型
                        IrZext z = new IrZext();
                        z.setOperand(new IrValue(v), 0);
                        z.setRegisterName("%" + nowIrFunction.getNowRank());
                        z.setType(new IrIntegerTy());
                        v.addTempInstruction(z);
                        v.setType(z.getType());
                        v.setRegisterName(z.getRegisterName());
                    }
                    IrConstantVal zero = new IrConstantVal(0);
                    zero.setType(v.getType()); //Type和后边的数保持一致
                    IrBinaryOp i = new IrBinaryOp();
                    i.setRegisterName("%" + nowIrFunction.getNowRank());
                    i.setOperationTy(IrInstructionType.irIntructionType.Sub);
                    i.setOperand(zero, 0);
                    i.setOperand(new IrValue(v), 1);
                    v.setRegisterName(i.getRegisterName()); //一定要更新返回IrValue的寄存器名
                    v.addTempInstruction(i);
                }
            } else if (judge == 2) { //判断逻辑：如果是常量，直接看其是否为0，构造后返回，如果不是常量，实现一个icmp语句并返回
                if (v instanceof IrConstantVal) { //如果是常量，直接计算
                    IrConstantVal res = new IrConstantVal(((IrConstantVal) v).getVal() == 0 ? 1: 0);
                    res.setType(new IrBooleanTy());
                    res.addAllTempInstruction(v.getTempInstructions());
                    return res;
                } else {
                    IrIcmp cmp = new IrIcmp("eq", nowIrFunction.getNowRank(), new IrBooleanTy());
                    cmp.setOperand(v, 0);
                    cmp.setOperand(new IrConstantVal(0), 1);
                    cmp.addAllTempInstruction(v.getTempInstructions());
                    cmp.addTempInstruction(new IrIcmp(cmp));
                    return cmp;
                }
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
                call.setParaNum(paraList.size());
                call.setFuncName("@" + t.getString());
                LinkedList<IrArgument> alist = ((IrFunction) s.getIrValue()).getArguments();
                for (int i = 0; i < paraList.size(); i++) { // 配置参数
                    IrValue temp = paraList.get(i);
                    if (alist.get(i).getType().getClass() != temp.getType().getClass() && !(alist.get(i).getType() instanceof IrPointerTy || temp.getType() instanceof IrPointerTy)) {
                        if (temp instanceof IrConstantVal) {
                            temp.setType(temp.getType() instanceof IrIntegerTy ? new IrCharTy() : new IrIntegerTy());
                        } else if (temp.getType() instanceof IrCharTy) {
                            IrZext z = new IrZext();
                            z.setOperand(temp, 0);
                            z.setRegisterName("%" + nowIrFunction.getNowRank());
                            z.setType(new IrIntegerTy());
                            res.addTempInstruction(z);
                            temp.setType(z.getType());
                            temp.setRegisterName(z.getRegisterName());
                        } else if (temp.getType() instanceof IrIntegerTy) {
                            IrTrunc trunc = new IrTrunc();
                            trunc.setOperand(temp, 0);
                            trunc.setRegisterName("%" + nowIrFunction.getNowRank());
                            trunc.setType(new IrCharTy());
                            res.addTempInstruction(trunc);
                            temp.setType(trunc.getType());
                            temp.setRegisterName(trunc.getRegisterName());
                        }
                    }
                    call.setOperand(temp, i);
                }
                if (!(call.getType() instanceof IrVoidTy)) { //void型的不分配寄存器
                    call.setRegisterName("%" + nowIrFunction.getNowRank());
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
        } else if (unaryOp.getToken().getString().equals("!")) {
            return 2;
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

    private IrValue visitMulExp(MulExp mulExp, boolean isLvalue) { //注意运算时可能产生的类型转换//TODO:boolean型的类型转换
        IrValue res = new IrValue();
        if (mulExp == null) {
            return res;
        }
        IrValue v1 = new IrValue(), v2;
        for (int i = 0; i < mulExp.getUnaryExpArrayList().size(); i++) { //注意constant类型的数
            UnaryExp u = mulExp.getUnaryExpArrayList().get(i);
            if(i == 0) { // 返回第一个unaryExp的类型即可
                v1 = visitUnaryExp(u, isLvalue);
                if (mulExp.getUnaryExpArrayList().size() == 1) return v1; //只有一个操作数，直接返回即可
                res.addAllTempInstruction(v1.getTempInstructions());
                if (v1.getType().getClass() != IrIntegerTy.class && !(v1 instanceof IrConstantVal)) { //所有char类型一律转成int类型
                    IrZext z = new IrZext();
                    z.setOperand(new IrValue(v1), 0);
                    z.setRegisterName("%" + nowIrFunction.getNowRank());
                    z.setType(new IrIntegerTy());
                    res.addTempInstruction(z);
                    v1 = z;
                }
                res.setType(v1.getType());
            } else { //其他的也需要分析，可能会有错误出现
                v2 = visitUnaryExp(u, isLvalue);
                res.addAllTempInstruction(v2.getTempInstructions());
                if (v2.getType().getClass() != IrIntegerTy.class && !(v2 instanceof IrConstantVal)) { //所有char类型一律转成int类型
                    IrZext z = new IrZext();
                    z.setOperand(new IrValue(v2), 0);
                    z.setRegisterName("%" + nowIrFunction.getNowRank());
                    z.setType(new IrIntegerTy());
                    res.addTempInstruction(z);
                    v2 = z;
                }

                if ((v1 instanceof IrConstantVal) && (v2 instanceof IrConstantVal)) { //如果都是constant型，直接计算
                    TokenType.tokenType t = mulExp.getSymbolList().get(i - 1).getType();
                    if (t == TokenType.tokenType.MULT) {
                        v1 = new IrConstantVal(((IrConstantVal) v1).getVal() * ((IrConstantVal) v2).getVal());
                    } else if (t == TokenType.tokenType.DIV) {
                        v1 = new IrConstantVal(((IrConstantVal) v1).getVal() / ((IrConstantVal) v2).getVal());
                    } else if (t == TokenType.tokenType.MOD) {
                        v1 = new IrConstantVal(((IrConstantVal) v1).getVal() % ((IrConstantVal) v2).getVal());
                    }
                    v1.setType(new IrIntegerTy());
                    res = v1; //这里会导致res的种类确定为IrConstVal
                } else {
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
                    v1 = new IrValue(res); //深拷贝
                    res = new IrValue(v1); //拷贝回来，更改res的class为IrValue
                }
            }
        }
        return res;
    }

    private IrValue visitAddExp(AddExp addExp, boolean isLvalue) { //注意运算时可能的类型转换
        IrValue res = new IrValue();
        if (addExp == null) {
            return res;
        }
        IrValue v1 = new IrValue(), v2;
        for (int i = 0; i < addExp.getMulExpArrayList().size(); i++) { //注意constant类型的数
            MulExp m = addExp.getMulExpArrayList().get(i);
            if(i == 0) { // 返回第一个mulExp的类型即可
                v1 = visitMulExp(m, isLvalue);
                if (addExp.getMulExpArrayList().size() == 1) return v1; //只有一个mulExp，直接返回v1
                res.addAllTempInstruction(v1.getTempInstructions());
                if (v1.getType().getClass() != IrIntegerTy.class && !(v1 instanceof IrConstantVal)) { //所有char类型一律转成int类型
                    IrZext z = new IrZext();
                    z.setOperand(new IrValue(v1), 0);
                    z.setRegisterName("%" + nowIrFunction.getNowRank());
                    z.setType(new IrIntegerTy());
                    res.addTempInstruction(z);
                    v1 = z;
                }
                res.setType(v1.getType());
            } else { //其他的也需要分析，可能会有错误出现
                v2 = visitMulExp(m, isLvalue);
                res.addAllTempInstruction(v2.getTempInstructions());
                if (v2.getType().getClass() != IrIntegerTy.class && !(v2 instanceof IrConstantVal)) { //所有char类型一律转成int类型
                    IrZext z = new IrZext();
                    z.setOperand(new IrValue(v2), 0);
                    z.setRegisterName("%" + nowIrFunction.getNowRank());
                    z.setType(new IrIntegerTy());
                    res.addTempInstruction(z);
                    v2 = z;
                }

                if ((v1 instanceof IrConstantVal) && (v2 instanceof IrConstantVal)) { //如果都是constant型，直接计算
                    TokenType.tokenType t = addExp.getSymbolList().get(i - 1).getType();
                    if (t == TokenType.tokenType.PLUS) {
                        v1 = new IrConstantVal(((IrConstantVal) v1).getVal() + ((IrConstantVal) v2).getVal());
                    } else if (t == TokenType.tokenType.MINU) {
                        v1 = new IrConstantVal(((IrConstantVal) v1).getVal() - ((IrConstantVal) v2).getVal());
                    }
                    v1.setType(new IrIntegerTy());
                    res = v1;
                } else {
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
                    v1 = new IrValue(res); //这里一定要用深拷贝
                    res = new IrValue(v1); //拷贝回来，更改res的class为IrValue
                }
            }
        }
        return res;
    }

    private IrValue visitConstExp(ConstExp constExp) {
        return visitAddExp(constExp.getAddExp(), false); // 根据文法肯定不是左值
    }

    private IrValue visitRelExp(RelExp relExp) {
        IrValue res = new IrValue();
        if (relExp == null) {
            return res;
        }
        IrValue v1 = new IrValue(), v2;
        for (int i = 0; i < relExp.getAddExpArrayList().size(); i++) {
            AddExp a = relExp.getAddExpArrayList().get(i);
            if (i == 0) {
                v1 = visitAddExp(a, false);
                if (relExp.getAddExpArrayList().size() == 1) return v1;
                res.addAllTempInstruction(v1.getTempInstructions());
                if (v1.getType().getClass() != IrIntegerTy.class && !(v1 instanceof IrConstantVal)) { //所有char类型一律转成int类型
                    IrZext z = new IrZext();
                    z.setOperand(new IrValue(v1), 0);
                    z.setRegisterName("%" + nowIrFunction.getNowRank());
                    z.setType(new IrIntegerTy());
                    res.addTempInstruction(z);
                    v1 = z;
                }
                res.setType(v1.getType());
            } else {
                v2 = visitAddExp(a, false);
                res.addAllTempInstruction(v2.getTempInstructions());
                if (v2.getType().getClass() != IrIntegerTy.class && !(v2 instanceof IrConstantVal)) { //所有char类型一律转成int类型
                    IrZext z = new IrZext();
                    z.setOperand(new IrValue(v2), 0);
                    z.setRegisterName("%" + nowIrFunction.getNowRank());
                    z.setType(new IrIntegerTy());
                    res.addTempInstruction(z);
                    v2 = z;
                }
                if (v1.getType().getClass() != IrIntegerTy.class && !(v1 instanceof IrConstantVal)) { //如果v1经过计算是i1型的，需要转成int进行计算
                    IrZext z = new IrZext();
                    z.setOperand(new IrValue(v1), 0);
                    z.setRegisterName("%" + nowIrFunction.getNowRank());
                    z.setType(new IrIntegerTy());
                    res.addTempInstruction(z);
                    v1 = new IrValue(z); //不确定这里用不用深拷贝，这里先用深拷贝
                }
                if ((v1 instanceof IrConstantVal) && (v2 instanceof IrConstantVal)) {
                    //如果都是constant型，直接计算,注意这里的判断条件,因为visitaddexp可能返回IrConstant型但实际并不是IrConstant型
                    TokenType.tokenType t = relExp.getSymbolList().get(i - 1).getType();
                    if (t == TokenType.tokenType.LSS) {
                        v1 = new IrConstantVal(((IrConstantVal) v1).getVal() < ((IrConstantVal) v2).getVal() ? 1 : 0);
                    } else if (t == TokenType.tokenType.LEQ) {
                        v1 = new IrConstantVal(((IrConstantVal) v1).getVal() <= ((IrConstantVal) v2).getVal() ? 1 : 0);
                    } else if (t == TokenType.tokenType.GRE) {
                        v1 = new IrConstantVal(((IrConstantVal) v1).getVal() > ((IrConstantVal) v2).getVal() ? 1 : 0);
                    } else if (t == TokenType.tokenType.GEQ) {
                        v1 = new IrConstantVal(((IrConstantVal) v1).getVal() >= ((IrConstantVal) v2).getVal() ? 1 : 0);
                    }
                    v1.setType(new IrBooleanTy());
                    res = v1;
                } else {
                    //根据对应符号实现比较语句的生成并添加到res的tempInstructions中
                    IrIcmp icmp = null;
                    TokenType.tokenType t = relExp.getSymbolList().get(i - 1).getType();
                    if (t == TokenType.tokenType.LSS) {
                        icmp = new IrIcmp("slt", nowIrFunction.getNowRank(), new IrBooleanTy());
                    } else if (t == TokenType.tokenType.LEQ) {
                        icmp = new IrIcmp("sle", nowIrFunction.getNowRank(), new IrBooleanTy());
                    } else if (t == TokenType.tokenType.GRE) {
                        icmp = new IrIcmp("sgt", nowIrFunction.getNowRank(), new IrBooleanTy());
                    } else if (t == TokenType.tokenType.GEQ) {
                        icmp = new IrIcmp("sge", nowIrFunction.getNowRank(), new IrBooleanTy());
                    }
                    icmp.setOperand(v1, 0);
                    icmp.setOperand(v2, 1);
                    res.setRegisterName(icmp.getRegisterName());//一定要更新返回IrValue的寄存器名
                    res.setType(icmp.getType());
                    res.addTempInstruction(icmp);
                    v1 = new IrValue(res); //这里一定要用深拷贝
                    res = new IrValue(v1); //拷贝回来，更改res的class为IrValue
                }
            }
        }
        return res;
    }

    private IrValue visitEqExp(EqExp eqExp) {
        IrValue res = new IrValue();
        if (eqExp == null) {
            return res;
        }
        IrValue v1 = new IrValue(), v2;
        for (int i = 0; i < eqExp.getRelExpArrayList().size(); i++) {
            RelExp r = eqExp.getRelExpArrayList().get(i);
            if (i == 0) {
                v1 = visitRelExp(r);
                if (eqExp.getRelExpArrayList().size() == 1) return v1;
                res.addAllTempInstruction(v1.getTempInstructions());
                if (v1.getType().getClass() != IrIntegerTy.class && !(v1 instanceof IrConstantVal)) { //所有char类型一律转成int类型
                    IrZext z = new IrZext();
                    z.setOperand(new IrValue(v1), 0);
                    z.setRegisterName("%" + nowIrFunction.getNowRank());
                    z.setType(new IrIntegerTy());
                    res.addTempInstruction(z);
                    v1 = z;
                }
                res.setType(v1.getType());
            } else {
                v2 = visitRelExp(r);
                res.addAllTempInstruction(v2.getTempInstructions());
                if (v2.getType().getClass() != IrIntegerTy.class && !(v2 instanceof IrConstantVal)) { //所有char类型一律转成int类型
                    IrZext z = new IrZext();
                    z.setOperand(new IrValue(v2), 0);
                    z.setRegisterName("%" + nowIrFunction.getNowRank());
                    z.setType(new IrIntegerTy());
                    res.addTempInstruction(z);
                    v2 = z;
                }
                if (v1.getType().getClass() != IrIntegerTy.class && !(v1 instanceof IrConstantVal)) { //如果v1经过计算是i1型的，需要转成int进行计算
                    IrZext z = new IrZext();
                    z.setOperand(new IrValue(v1), 0);
                    z.setRegisterName("%" + nowIrFunction.getNowRank());
                    z.setType(new IrIntegerTy());
                    res.addTempInstruction(z);
                    v1 = new IrValue(z); //不确定这里用不用深拷贝，这里先用深拷贝
                }
                if ((v1 instanceof IrConstantVal) && (v2 instanceof IrConstantVal)) { //如果都是constant型，直接计算
                    TokenType.tokenType t = eqExp.getSymbolList().get(i - 1).getType();
                    if (t == TokenType.tokenType.EQL) {
                        v1 = new IrConstantVal(((IrConstantVal) v1).getVal() == ((IrConstantVal) v2).getVal() ? 1 : 0);
                    } else if (t == TokenType.tokenType.NEQ) {
                        v1 = new IrConstantVal(((IrConstantVal) v1).getVal() != ((IrConstantVal) v2).getVal() ? 1 : 0);
                    }
                    v1.setType(new IrBooleanTy());
                    res = v1;
                } else {
                    IrIcmp icmp = null;
                    TokenType.tokenType t = eqExp.getSymbolList().get(i - 1).getType();
                    if (t == TokenType.tokenType.EQL) {
                        icmp = new IrIcmp("eq", nowIrFunction.getNowRank(), new IrBooleanTy());
                    } else if (t == TokenType.tokenType.NEQ) {
                        icmp = new IrIcmp("ne", nowIrFunction.getNowRank(), new IrBooleanTy());
                    }
                    icmp.setOperand(v1, 0);
                    icmp.setOperand(v2, 1);
                    res.setRegisterName(icmp.getRegisterName());//一定要更新返回IrValue的寄存器名
                    res.setType(icmp.getType());
                    res.addTempInstruction(icmp);
                    v1 = new IrValue(res); //这里一定要用深拷贝
                    res = new IrValue(v1); //拷贝回来，更改res的class为IrValue
                }
            }
        }
        return res;
    }

    private IrLabel visitLAndExp(LAndExp lAndExp, IrLabel ifLabel, IrLabel nextLabel) { //返回IrLabel，意为下一个LAndExp基本块的起始块号
        //观察文法，在这里就要生成Br语句了，如果判断不成立，就要跳到nextLabel处，nextLabel作为参数传进方法，如果全部EqExp判断成立，就跳到IfLabel处
        //注意短路求值的实现
        //为右边提前创好基本块
        IrLabel res = new IrLabel();
        if (lAndExp == null) {
            return res;
        }
        IrValue v1;
        for (int i = 0; i < lAndExp.getEqExpArrayList().size(); i++) {
            EqExp e = lAndExp.getEqExpArrayList().get(i);
            v1 = visitEqExp(e);
            res.addAllTempInstruction(v1.getTempInstructions());
            //两种情况，没有&&符号和有&&符号
            if (lAndExp.getEqExpArrayList().size() == 1) {
                //没有符号，等价于判断e返回的寄存器中存储的值是否为0，并返回
                res = new IrLabel(cntUtils.getCount());
                res.addAllTempInstruction(v1.getTempInstructions()); //res换成了一个新的res，重新传一遍v1的语句
                if (v1 instanceof IrConstantVal) { //如果是常数，看是否为0，不为0，就跳到传进来的IfLabel中；如果为0，就跳到nextLabel处
                    IrGotoBr g;
                    if (((IrConstantVal) v1).getVal() == 0) {
                        g = new IrGotoBr(nextLabel);
                    } else {
                        g = new IrGotoBr(ifLabel);
                    }
                    res.addTempInstruction(g);
                } else { //否则取出v1寄存器并与0 构造icmp语句,返回一个新的IrLabel
//                    if (v1.getType().getClass() != IrIntegerTy.class) { //如果v1经过计算是i1型的，需要转成int进行计算
//                        IrZext z = new IrZext();
//                        z.setOperand(v1, 0);
//                        z.setRegisterName("%" + nowIrFunction.getNowRank());
//                        z.setType(new IrIntegerTy());
//                        res.addTempInstruction(z);
//                        v1 = new IrValue(z); //不确定这里用不用深拷贝，这里先用深拷贝
//                    }
                    //不需要类型转换，与常数比ty和当前运算数一致即可
                    IrIcmp cmp = new IrIcmp("ne", nowIrFunction.getNowRank(), new IrBooleanTy());
                    cmp.setOperand(v1, 0);
                    cmp.setOperand(new IrConstantVal(0), 1);
                    IrBr b = new IrBr(ifLabel, nextLabel);
                    b.setOperand(cmp, 0);
                    res.addTempInstruction(cmp);
                    res.addTempInstruction(b);
                }
                return res;
            } else { //有多个eqExp,除第一个eqExp，每个eqExp都放到一个新的Label中,每次都取eqExp构建icmp
                // TODO：按理说这里的Label部分就算一个新的basicBlock了，这一点在后边优化的时候有用，这里先不考虑
                IrLabel l = new IrLabel(cntUtils.getCount());
                if (v1 instanceof IrConstantVal) { //如果v1是常数
                    if (((IrConstantVal) v1).getVal() == 0) {
                        ((IrConstantVal) v1).setVal(0);
                    } else {
                        ((IrConstantVal) v1).setVal(1);
                    }
                    v1.setType(new IrBooleanTy());
                }

//                if (v1.getType().getClass() != IrIntegerTy.class && !(v1 instanceof IrConstantVal)) { //如果v1经过计算是i1型的，需要转成int进行计算
//                    IrZext z = new IrZext();
//                    z.setOperand(v1, 0);
//                    z.setRegisterName("%" + nowIrFunction.getNowRank());
//                    z.setType(new IrIntegerTy());
//                    res.addTempInstruction(z);
//                    v1 = new IrValue(z); //不确定这里用不用深拷贝，这里先用深拷贝
//                }
                //不需要类型转换，与常数比ty和当前运算数一致即可
                IrIcmp cmp = new IrIcmp("eq", nowIrFunction.getNowRank(), new IrBooleanTy()); //注意和前边的区别
                cmp.setOperand(v1, 0);
                cmp.setOperand(new IrConstantVal(0), 1);
                IrBr b = new IrBr(nextLabel, l); //为0则整体不成立，跳到整体外下一个Label中（函数参数），否则跳入当前lAndExp中的下一个Label中
                b.setOperand(cmp, 0);
                res.addTempInstruction(cmp);
                res.addTempInstruction(b);
                //声明lAndExp中的下一个Label
                res.addTempInstruction(l);
                if ( i == lAndExp.getEqExpArrayList().size() - 1) { //如果全成立，跳到ifLabel
                    res.addTempInstruction(new IrGotoBr(ifLabel));
                }
            }
        }
        return res;
    }

    private IrLabel visitLOrExp(LOrExp lOrExp, IrLabel ifLabel, IrLabel endLabel) { //返回一个IrLabel，表示Cond的起始标签
        //传两个Label，一个是有一个成立就要跳到的if的blcok的Label，另一个是全都不成立跳到If外的Label
        IrLabel res = new IrLabel(cntUtils.getCount());
        if (lOrExp == null) {
            return res;
        }
        res.addTempInstruction(res); //先把最开始的Label输出
        IrLabel finishLabel = new IrLabel(cntUtils.getCount());
        for (int i = 0; i < lOrExp.getlAndExpArrayList().size(); i++) { //注意这里finishLabel的实现，可能有点问题，需要再盘盘逻辑
            LAndExp la = lOrExp.getlAndExpArrayList().get(i);
            IrLabel l = visitLAndExp(la, ifLabel, finishLabel);
            res.addAllTempInstruction(l.getTempInstructions());
            res.addTempInstruction(finishLabel);
            finishLabel = new IrLabel(cntUtils.getCount());
        }
        if (lOrExp.getlAndExpArrayList().isEmpty()) { //是空的，就跳到if块内
            IrGotoBr g = new IrGotoBr(ifLabel);
            res.addTempInstruction(g);
        } else { //走了所有的lAndExp都是false，跳到endLabel中
            IrGotoBr g = new IrGotoBr(endLabel);
            res.addTempInstruction(g);
        }
        return res;
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
