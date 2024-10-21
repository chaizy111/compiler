package Analysis.Symbol;

import Analysis.Symbol.Value.Value;

public class Symbol {
    private int tableId;
    private String symbolName;
    private int type;// 0 -> var, 1 -> array, 2 -> func
    private int btype;// 0 -> int, 1 -> char, 2-> void
    private boolean isConst;
    private Value value;

    public Symbol(int tableId, String symbolName){
        this.tableId = tableId;
        this.symbolName = symbolName;
        this.type = -1;
        this.btype = -1;
        this.isConst = false;
        this.value = null;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setBtype(int btype) {
        this.btype = btype;
    }

    public void setConst(boolean aConst) {
        isConst = aConst;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public String getSymbolName() {
        return symbolName;
    }

    public int getType() {
        return type;
    }

    public int getBtype() {
        return btype;
    }

    public boolean isConst() {
        return isConst;
    }

    public Value getValue() {
        return value;
    }

    public String judgeKind() {
        if(isConst && (type == 0) && (btype == 1)) return "ConstChar";
        else if (isConst && (type == 0) && (btype == 0)) return "ConstInt";
        else if (isConst && (type == 1) && (btype == 1)) return "ConstCharArray";
        else if (isConst && (type == 1) && (btype == 0)) return "ConstIntArray";
        else if (!isConst && (type == 0) && (btype == 1)) return "Char";
        else if (!isConst && (type == 0) && (btype == 0)) return "Int";
        else if (!isConst && (type == 1) && (btype == 1)) return "CharArray";
        else if (!isConst && (type == 1) && (btype == 0)) return "IntArray";
        else if (!isConst && (type == 2) && (btype == 2)) return "VoidFunc";
        else if (!isConst && (type == 2) && (btype == 1)) return "CharFunc";
        else if (!isConst && (type == 2) && (btype == 0)) return "IntFunc";
        else return null;
    }
}
