package Llvmir.ValueType.Instruction;

import Llvmir.IrValue;
import Llvmir.Type.IrArrayTy;
import Llvmir.Type.IrVoidTy;

import java.util.ArrayList;

public class IrCall extends IrInstruction{
    private int paraNum;
    private String funcName;
    private String paraForPutStr;

    public IrCall() {
        super();
    }

    public void setParaNum(int paraNum) {
        this.paraNum = paraNum;
    }

    public void setFuncName(String funcName) {
        this.funcName = funcName;
    }

    public void setParaForPutStr(String paraForPutStr) {
        this.paraForPutStr = paraForPutStr;
    }

    // call void @bar()
    // %6 = call i32 @foo(i32 %4, i32 %5)
    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        StringBuilder s = new StringBuilder();
        if (funcName.equals("@putstr")) {
            s.append("call void @putstr(");
            s.append(paraForPutStr);
        } else {
            if (this.getType() instanceof IrVoidTy) { // void 型， 没有返回值，不需要用到registerName
                s.append("call void");
            } else { //有返回值，要写成“%i = call”型
                s.append(this.getRegisterName());
                s.append(" = call ");
                s.append(this.getType().output().get(0));
            }
            s.append(" ");
            s.append(funcName);
            s.append("(");
            for (int i = 0; i < paraNum; i++) { //参数的输出
                IrValue v = this.getOperand(i);
                s.append(v.getType().output().get(0));
                s.append(" ");
                s.append(v.getRegisterName());
                if (i + 1 != paraNum) {
                    s.append(", ");
                }
            }
        }
        s.append(")\n");
        res.add(s.toString());
        return res;
    }
}
