package Llvmir;

import Llvmir.Type.IrType;

import java.util.ArrayList;
import java.util.LinkedList;

public class IrValue { // TODO: IrValue,IrUse,IrUser是与代码优化相关的，代码生成是可以先不做
    private IrType type;
    private String name;
    private LinkedList<IrUse> useList;
    private LinkedList<IrUser> userList;

    //protected:
    //    TypePtr _type;
    //    std::string _name;
    //
    //    UseList _useList;
    //    UseList _userList;
    //
    //  private:
    //    ValueType _valueType;
}
