package Symbol.Value;

import Llvmir.ValueType.Function.IrArgument;

import java.util.ArrayList;

public class FuncValue extends Value {
    int paraTableId;
    int paraNum;
    ArrayList<IrArgument> arguments;

    public FuncValue(int paraTableId, int paraNum) {
        this.paraTableId = paraTableId;
        this.paraNum = paraNum;
        this.arguments = new ArrayList<>();
    }

    public int getParaTableId() {
        return paraTableId;
    }

    public int getParaNum() {
        return paraNum;
    }

    public void setParaNum(int paraNum) {
        this.paraNum = paraNum;
    }

    public void addArgument(IrArgument argument) {
        arguments.add(argument);
    }

    public ArrayList<IrArgument> getArguments() {
        return arguments;
    }
}
