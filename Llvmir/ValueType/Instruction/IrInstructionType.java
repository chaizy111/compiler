package Llvmir.ValueType.Instruction;

public class IrInstructionType {
    public enum irIntructionType{
        /* Binary */
        /* Arithmetic Binary */
        Add,// +
        Sub,// -
        Mul,// *
        Div,// /
        Mod,// %
        /* Logic Binary */
        Lt, // <
        Le, // <=
        Ge, // >=
        Gt, // >
        Eq, // ==
        Ne, // !=
        And,// &
        Or, // |
        Not, // ! ONLY ONE PARAM
        Beq, // IrBeq branch if ==
        Bne, // IrBne branch if !=
        Blt, // IrBlt branch if less than <
        Ble, // IrBle branch if less or equal <=
        Bgt, // IrBgt branch if greater than >
        Bge, // IrBge branch if greater or equal >=
        Goto, // IrGoto
        /* Terminator */
        Br,
        Call,
        Ret,
        /* mem op */
        Alloca,
        Load,
        Store,
        GEP, // Get Element Ptr
        Zext,
        Phi,//用于 mem2reg
        /* label */
        Label,
    }
}
