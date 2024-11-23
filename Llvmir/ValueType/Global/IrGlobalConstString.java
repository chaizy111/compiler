package Llvmir.ValueType.Global;

import Llvmir.IrNode;
import Llvmir.IrValue;

import java.util.ArrayList;

public class IrGlobalConstString extends IrValue implements IrNode {
    private String s;
    private int length;

    public IrGlobalConstString() {
        super();
        s = "wrong";
    }

    public void setS(String s) { // 注意 LLVM IR 中需要对 \n 和 \0 等进行转义
        String old1 = "\n";
        String old2 = " ";
        String new1 = "\\0A";
        String new2 = "\\00";
        String r1 = s.replace(old1, new1);
        String r2 = r1.replace(old2, new2);
        this.s = r2;
    }

    public String getS() {
        return s;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    //@.str = private unnamed_addr constant [4 x i8] c" - \00", align 1
    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        String s1 = this.getRegisterName() + " = private unnamed_addr constant [" + length + " x i8 c\"" + s + "\", align 1\n";
        res.add(s1);
        return res;
    }
}
