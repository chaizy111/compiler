package Analysis.middle.Llvmir.ValueType.Instruction;

import Analysis.middle.Llvmir.IrValue;
import Analysis.middle.Llvmir.Type.IrArrayTy;
import Analysis.middle.Llvmir.Type.IrPointerTy;
import Analysis.middle.Llvmir.Type.IrType;
import Analysis.middle.Llvmir.ValueType.Global.IrGlobalVariable;

import java.util.ArrayList;

public class IrGetelementptr extends IrInstruction{
    private IrType outputType; // output时专门使用的type
    private String exc = "0"; //数组的偏移量，默认为0

    public IrGetelementptr() {
        super();
    }

    public IrGetelementptr(String exc, IrType outputType, IrType type, IrValue operand0, int rname) {
        this.exc = exc;
        this.outputType = outputType;
        this.setType(type);
        this.setOperand(operand0, 0);
        this.setRegisterName(String.valueOf(rname));
    }

    public void setExc(String exc) {
        this.exc = exc;
    }

    public void setOutputType(IrType outputType) {
        this.outputType = outputType;
    }

    //%2 = getelementptr inbounds [3 x i32], [3 x i32]* %1, i32 0, i32 0
    //统一用上方的偏移形式，指针后边跟的i32一定是0，第二个i32根据exc偏移量来写，偏移量为0既代表数组第一个元素的地址，又表示数组的地址
    @Override
    public ArrayList<String> output() {
        ArrayList<String> res = new ArrayList<>();
        StringBuilder s = new StringBuilder();
        s.append("%r.");
        s.append(this.getRegisterName());
        s.append(" = getelementptr inbounds ");
        s.append(outputType.output().get(0));
        s.append(", ");
        IrPointerTy t = new IrPointerTy();
        t.setType(outputType);
        s.append(t.output().get(0));
        if (this.getOperand(0).getRegisterName().charAt(0) < '0' || this.getOperand(0).getRegisterName().charAt(0) > '9') {
            //因为可以取全局变量，所以前边的符号不一样
            s.append("@");
        } else {
            s.append("%r.");
        }
        s.append(this.getOperand(0).getRegisterName());
        if (outputType instanceof IrArrayTy) s.append(", i32 0"); //函数中的没有第一个i32 0
        s.append(", i32 ");
        s.append(exc);
        s.append("\n");
        res.add(s.toString());
        return res;
    }
}
