package Analysis.middle.Symbol.Value;

import Analysis.middle.Llvmir.ValueType.Function.IrArgument;

import java.util.LinkedList;

public class FuncValue extends Value {
    private int paraTableId;
    private int paraNum;
    private LinkedList<IrArgument> arguments;

    public FuncValue(int paraTableId, int paraNum) {
        this.paraTableId = paraTableId;
        this.paraNum = paraNum;
        this.arguments = new LinkedList<>();
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

    public LinkedList<IrArgument> getArguments() {
        return arguments;
    }
}
