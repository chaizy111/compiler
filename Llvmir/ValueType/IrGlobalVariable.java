package Llvmir.ValueType;

import Llvmir.IrNode;
import Llvmir.IrValue;
import Llvmir.ValueType.Constant.IrConstant;

import java.util.ArrayList;

public class IrGlobalVariable extends IrValue implements IrNode {
    private boolean isArray; //标记是否为数组；
    private int arraySize;
    private IrConstant constant; // 初始化的值

    public IrGlobalVariable(String name) {
        this.setName(name);
    }

    public void setIsArray(boolean array) {
        isArray = array;
    }

    public void setArraySize(int arraySize) {
        this.arraySize = arraySize;
    }

    public void setConstant(IrConstant constant) {
        this.constant = constant;
    }

    @Override
    public ArrayList<String> output() {
        //@a = dso_local global i32 97
        //@b = dso_local global i8 5
        //@a = dso_local global [10 x i32] [i32 1, i32 2, i32 3, i32 0, i32 0, i32 0, i32 0, i32 0, i32 0, i32 0]
        //@b = dso_local global [20 x i32] zeroinitializer
        //@c = dso_local global [8 x i8] [i8 102, i8 111, i8 111, i8 98, i8 97, i8 114, i8 0, i8 0]
        ArrayList<String> res = new ArrayList<>();
        if (isArray) { // 是数组型全局常量（变量）
            String s;
            if(constant != null) {//被赋初值
                s = this.getName() + " = dso_local global " + "[" +
                        arraySize + " x " + this.getType().output().get(0) +
                        "] " + constant.output().get(0) + "\n";
            } else {//未被赋初值(赋0)
                s = this.getName() + " = dso_local global " + "[" +
                        arraySize + " x " + this.getType().output().get(0) + "] zeroinitializer" + "\n";
            }
            res.add(s);
        } else {//不是数组型全局常量（变量）
            String s;
            if(constant != null) {//被赋初值
                s = this.getName() + " = dso_local global " + this.getType().output().get(0) +
                        " " + constant.output().get(0) + "\n";
            } else {//未被赋初值(赋0)
                s = this.getName() + " = dso_local global " + this.getType().output().get(0) +
                        " 0" + "\n";
            }
            res.add(s);
        }
        return res;
    }
}
