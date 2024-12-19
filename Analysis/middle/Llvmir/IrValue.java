package Analysis.middle.Llvmir;

import Analysis.middle.Llvmir.Type.*;
import Analysis.middle.Llvmir.ValueType.Constant.IrConstant;
import Analysis.middle.Llvmir.ValueType.Instruction.IrInstruction;

import java.util.ArrayList;

public class IrValue {
    private IrType type;
    private String name;
    private boolean isConst;
//    private LinkedList<IrUse> useList;
//    private LinkedList<IrUser> userList;
    private String registerName;
    private ArrayList<IrInstruction> tempInstructions;
    // 用于visit addExp后的返回，无奈之举，addExp的分析结果返回只能是个IrValue，而且我们还需要IrInstruction

    private ArrayList<IrValue> tempValues;
    //用于initVal类型语句在basicBlock中传递values
    private IrConstant constant;

    public IrValue() {
        tempInstructions = new ArrayList<>();
        tempValues = new ArrayList<>();
        isConst = false;
    }

    public IrValue(IrValue v) {
        this.type = v.getType();
        this.name = v.getName();
        this.tempValues = v.tempValues;
        this.registerName = v.getRegisterName();
        this.tempInstructions = v.getTempInstructions();
        this.isConst = v.isConst;
    }

    public void setConstant(IrConstant constant) {
        this.constant = constant;
    }

    public IrConstant getConstant() {
        return constant;
    }

    public void setRegisterName(String registerName) {
        this.registerName = registerName;
    }

    public void setType(IrType type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setConst(boolean aConst) { //TODO: 在定义的时候要注意配置这个属性，用于错误判断，对于代码生成并无作用
        isConst = aConst;
    }

    public boolean isConst() {
        return isConst;
    }

    public IrType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getRegisterName() {
        return registerName;
    }

    public void addTempValue(IrValue irValue) {
        tempValues.add(irValue);
    }

    public void setTempValues(ArrayList<IrValue> tempValues) {
        this.tempValues = tempValues;
    }

    public ArrayList<IrValue> getTempValues() {
        return tempValues;
    }

    public void addTempInstruction(IrInstruction instruction) {
        tempInstructions.add(instruction);
    }

    public void addAllTempInstruction(ArrayList<IrInstruction> instructions) {
        tempInstructions.addAll(instructions);
    }

    public ArrayList<IrInstruction> getTempInstructions() {
        return tempInstructions;
    }


//   int type;// 0 -> var, 1 -> array, 2 -> func
//   int btype;// 0 -> int, 1 -> char, 2-> void
    public int judgeKindN() { // 用于符号表中错误的判断
        if (isConst && (type instanceof IrArrayTy) && (((IrArrayTy) type).getArrayType() instanceof IrCharTy)) return 3;
        else if (isConst && (type instanceof IrArrayTy) && (((IrArrayTy) type).getArrayType() instanceof IrIntegerTy)) return 2;
        else if (!isConst && (type instanceof IrArrayTy) && (((IrArrayTy) type).getArrayType() instanceof IrCharTy)) return 3;
        else if (!isConst && (type instanceof IrArrayTy) && (((IrArrayTy) type).getArrayType() instanceof IrIntegerTy)) return 2;
        else if (!isConst && (type instanceof IrFunctionTy) && (((IrFunctionTy) type).getFuncType() instanceof IrVoidTy)) return 4;
        else return 0;
    }

    //protected:
    //    TypePtr _type;
    //    std::string _name;
    //
    //    UseList _useList;
    //    UseList _userList;
    //
    //  private:
    //    ValueType _valueType;
}
