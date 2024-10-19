package Analysis.Symbol;

import Analysis.Token.TokenType;

public class Symbol {
    private int tableId;
    private String symbolName;
    private int type;// 0 -> var, 1 -> array, 2 -> func
    private int btype;// 0 -> int, 1 -> char
    private boolean isConst;

    public Symbol(int tableId, String symbolName){
        this.tableId = tableId;
        this.symbolName = symbolName;
        this.type = -1;
        this.btype = -1;
        this.isConst = false;
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

    //TODO
    public String judgeKind() {
        return null;
    }
}
