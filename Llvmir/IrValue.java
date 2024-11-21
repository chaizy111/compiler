package Llvmir;

import Llvmir.Type.IrType;

import java.util.ArrayList;
import java.util.LinkedList;

public class IrValue {
    private IrType type;
    private String name;
//    private LinkedList<IrUse> useList;
//    private LinkedList<IrUser> userList;
    private String registerName;

    public void setRegisterName(String registerName) {
        this.registerName = registerName;
    }

    public void setType(IrType type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IrType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getRegisterName() {
        return registerName;
    }

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
