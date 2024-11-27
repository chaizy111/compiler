package Llvmir.ValueType.Global;

import Llvmir.IrNode;
import Llvmir.IrValue;
import Llvmir.Type.IrArrayTy;
import Llvmir.Type.IrPointerTy;
import Llvmir.Type.IrType;
import Llvmir.ValueType.Constant.IrConstant;

import java.util.ArrayList;

public class IrGlobalVariable extends IrValue implements IrNode {
    private IrConstant constant; // 初始化的值
    private IrType outputType;
    private boolean isConst;

    public IrGlobalVariable(String name) {
        super();
        this.setName(name);
    }

    public void setOutputType(IrType outputType) {
        this.outputType = outputType;
    }

    public void setConstant(IrConstant constant) {
        this.constant = constant;
    }

    @Override
    public void setConst(boolean aConst) {
        isConst = aConst;
    }

    public boolean isConst() {
        return isConst;
    }

    public IrConstant getConstant() {
        return constant;
    }

    @Override
    public ArrayList<String> output() {
        //@a = dso_local global i32 97
        //@b = dso_local global i8 5
        //@a = dso_local global [10 x i32] [i32 1, i32 2, i32 3, i32 0, i32 0, i32 0, i32 0, i32 0, i32 0, i32 0]
        //@b = dso_local global [20 x i32] zeroinitializer
        //@c = dso_local global [8 x i8] [i8 102, i8 111, i8 111, i8 98, i8 97, i8 114, i8 0, i8 0]
        ArrayList<String> res = new ArrayList<>();
        if (isConst) {
            if (outputType instanceof IrArrayTy) { // 是数组型全局常量（变量）
                String s;
                if(constant != null) {//被赋初值
                    s = this.getName() + " = constant " + outputType.output().get(0) + constant.output().get(0) + "\n";
                } else {//未被赋初值(赋0)
                    s = this.getName() + " = constant " + outputType.output().get(0) + " zeroinitializer" + "\n";
                }
                res.add(s);
            } else {//不是数组型全局常量（变量）
                String s;
                if(constant != null) {//被赋初值
                    s = this.getName() + " = constant " + outputType.output().get(0) +
                            " " + constant.output().get(0) + "\n";
                } else {//未被赋初值(赋0)
                    s = this.getName() + " = constant " + outputType.output().get(0) +
                            " 0" + "\n";
                }
                res.add(s);
            }
        } else {
            if (outputType instanceof IrArrayTy) { // 是数组型全局常量（变量）
                String s;
                if(constant != null) {//被赋初值
                    s = this.getName() + " = dso_local global " + outputType.output().get(0) + constant.output().get(0) + "\n";
                } else {//未被赋初值(赋0)
                    s = this.getName() + " = dso_local global " + outputType.output().get(0) + " zeroinitializer" + "\n";
                }
                res.add(s);
            } else {//不是数组型全局常量（变量）
                String s;
                if(constant != null) {//被赋初值
                    s = this.getName() + " = dso_local global " + outputType.output().get(0) +
                        " " + constant.output().get(0) + "\n";
                } else {//未被赋初值(赋0)
                    s = this.getName() + " = dso_local global " + outputType.output().get(0) +
                        " 0" + "\n";
                }
               res.add(s);
            }
        }
        return res;
    }
}
