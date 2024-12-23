package Analysis.middle.Llvmir.ValueType.Global;

import Analysis.middle.Llvmir.IrNode;
import Analysis.middle.Llvmir.IrValue;

import java.util.ArrayList;

public class IrGlobalConstString extends IrValue implements IrNode {
    private String  s = "wrong";
    private int length;

    public IrGlobalConstString(String s) {
        super();
        setS(s);
        setLength();
    }

    public void setS(String s) { // 注意 LLVM IR 中需要对 \n 和 \0 等进行转义
        String old1 = "\\n";
        String new1 = "\\0A";
        String r1 = s.replace(old1, new1);
        this.s = r1 + "\\00";
    }

    public String getS() {
        return s;
    }

    public void setLength() {
        String old1 = "\\0A";
        String new1 = "n";
        String old2 = "\\00";
        String new2 = " ";
        String t1 = s;
        this.length = t1.replace(old1, new1).replace(old2, new2).length();
    }

    public int getLength() {
        return length;
    }

    //@.str = private unnamed_addr constant [4 x i8] c" - \00", align 1
    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        String s1 = "@.str" + this.getRegisterName() + " = private unnamed_addr constant [" + length + " x i8] c\"" + s + "\"\n";
        res.add(s1);
        return res;
    }
}
