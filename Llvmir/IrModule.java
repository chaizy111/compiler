package Llvmir;

import Llvmir.ValueType.IrFunction;
import Llvmir.ValueType.IrGlobalVariable;

import java.util.ArrayList;

public class IrModule implements IrNode{
    private ArrayList<IrGlobalVariable> irGlobalVariables;
    private ArrayList<IrFunction> irFunctions;

    public IrModule() {
        irGlobalVariables = new ArrayList<>();
        irFunctions = new ArrayList<>();
    }

    public void addGlobalVariable(ArrayList<IrGlobalVariable> globalVariable) {
        irGlobalVariables.addAll(globalVariable);
    }

    public void addFunction(IrFunction function) {
        irFunctions.add(function);
    }

    @Override
    public ArrayList<String> output() {
        ArrayList<String> out = new ArrayList<>();
        String s = "declare i32 @getint()\n" +
                "declare i32 @getchar()\n" +
                "declare void @putint(i32)\n" +
                "declare void @putch(i32)\n" +
                "declare void @putstr(i8*)\n\n";
        out.add(s);
        for (IrGlobalVariable globalVariable : irGlobalVariables) {
            ArrayList<String> res = globalVariable.output();
            if (res != null && !res.isEmpty()) {
                out.addAll(res);
            }
        }
        for (IrFunction function : irFunctions) {
            ArrayList<String> res = function.output();
            if (res != null && !res.isEmpty()) {
                out.addAll(res);
            }
        }
        return out;
    }
}
