package Analysis.middle.Llvmir.ValueType.Instruction;

import Analysis.middle.Llvmir.Type.IrArrayTy;
import Analysis.middle.Llvmir.Type.IrPointerTy;
import Analysis.middle.Llvmir.Type.IrType;

import java.util.ArrayList;

public class IrGetelementptr extends IrInstruction{
    private IrType outputType; // output时专门使用的type
    private String exc; //数组的偏移量，默认为0

    public IrGetelementptr() {
        super();
        exc = "0";
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
        s.append(this.getRegisterName());
        s.append(" = getelementptr inbounds ");
        s.append(outputType.output().get(0));
        s.append(", ");
        IrPointerTy t = new IrPointerTy();
        t.setType(outputType);
        s.append(t.output().get(0));
        s.append(" ");
        s.append(this.getOperand(0).getRegisterName());
        if (outputType instanceof IrArrayTy) s.append(", i32 0"); //函数中的没有第一个i32 0
        s.append(", i32 ");
        s.append(exc);
        s.append("\n");
        res.add(s.toString());
        return res;
    }
}
