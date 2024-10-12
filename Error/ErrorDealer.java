package Error;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ErrorDealer {
    private FileWriter file;
    private HashMap<Integer, ArrayList<ErrorItem>> list;
    private int maxLine;

    public ErrorDealer(FileWriter errorFile) {
        this.file = errorFile;
        this.list = new HashMap<>();
        this.maxLine = 0;
    }

    public void setMaxLine(int maxLine) {
        this.maxLine = maxLine;
    }

    public void errorA(int n) {
        ArrayList<ErrorItem> l = list.getOrDefault(n, new ArrayList<>());
        l.add(new ErrorItem('a', n));
        list.put(n, l);
    }

    public void errorI(int n) {
        ArrayList<ErrorItem> l = list.getOrDefault(n, new ArrayList<>());
        l.add(new ErrorItem('i', n));
        list.put(n, l);
    }

    public void errorJ(int n) {
        ArrayList<ErrorItem> l = list.getOrDefault(n, new ArrayList<>());
        l.add(new ErrorItem('j', n));
        list.put(n, l);
    }

    public void errorK(int n) {
        ArrayList<ErrorItem> l = list.getOrDefault(n, new ArrayList<>());
        l.add(new ErrorItem('k', n));
        list.put(n, l);
    }

    public void printError() throws IOException {
        for(int i = 0; i <= maxLine; i++) {
            if(list.containsKey(i)) {
                for (ErrorItem e:list.get(i)) {
                    file.write(e.getLine() + " " + e.getKind() + "\n");
                }
            }
        }
    }
}
