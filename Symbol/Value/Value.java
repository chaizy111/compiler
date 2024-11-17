package Symbol.Value;

import Tree.ConstInitVal;
import Tree.InitVal;

public class Value {
    private ConstInitVal constInitVal;
    private InitVal initVal;

    public Value() {
        this.constInitVal = null;
        this.initVal = null;
    }

    public void setConstInitVal(ConstInitVal constInitVal) {
        this.constInitVal = constInitVal;
    }

    public void setInitVal(InitVal initVal) {
        this.initVal = initVal;
    }
}
