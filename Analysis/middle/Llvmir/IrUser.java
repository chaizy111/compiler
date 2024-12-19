package Analysis.middle.Llvmir;

import Analysis.middle.Llvmir.ValueType.Constant.IrConstant;

import java.util.LinkedList;

public class IrUser extends IrValue{
    private LinkedList<IrValue> operands;

    public IrUser() {
        super();
        operands = new LinkedList<>();
    }

    public void addOprands(IrValue irValue) {
        operands.add(irValue);
    }

    public void setOperand(IrValue value, int index) {
        IrValue v = value instanceof IrConstant ? value : new IrValue(value);
        if (index >= operands.size()) {
            operands.add(v);
        } else {
            operands.set(index, v);
        }
    }

    /* 根据给定index找到指定操作数 */
    public IrValue getOperand(int index) {
        return operands.get(index);
    }
}
