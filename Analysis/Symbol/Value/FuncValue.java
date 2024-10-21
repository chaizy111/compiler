package Analysis.Symbol.Value;

public class FuncValue extends Value {
    int paraTableId;
    int paraNum;

    public FuncValue(int paraTableId, int paraNum) {
        this.paraTableId = paraTableId;
        this.paraNum = paraNum;
    }

    public int getParaTableId() {
        return paraTableId;
    }

    public int getParaNum() {
        return paraNum;
    }
}
