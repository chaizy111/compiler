package Analysis.Symbol;

import javax.imageio.IIOException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class SymbolTable {
    private int id; 		// 当前符号表的id。
    private int fatherId; 	// 外层符号表的id。
    private LinkedHashMap<String, Symbol> directory;

    public SymbolTable(int id, int fatherId){
        this.id = id;
        this.fatherId = fatherId;
        this.directory = new LinkedHashMap<>();
    }

    public int getId() {
        return id;
    }

    public int getFatherId() {
        return fatherId;
    }

    public HashMap<String, Symbol> getDirectory() {
        return directory;
    }

    public void printSymbolTable(FileWriter outputfile) throws IOException {
        for (Symbol s: directory.values()) {
            outputfile.write(id + " " + s.getSymbolName() + " " + s.judgeKind() + "\n");
        }
    }
}
