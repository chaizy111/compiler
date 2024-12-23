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

    public void errorB(int n) {
        ArrayList<ErrorItem> l = list.getOrDefault(n, new ArrayList<>());
        l.add(new ErrorItem('b', n));
        list.put(n, l);
    }

    public void errorC(int n) {
        ArrayList<ErrorItem> l = list.getOrDefault(n, new ArrayList<>());
        l.add(new ErrorItem('c', n));
        list.put(n, l);
    }

    public void errorD(int n) {
        ArrayList<ErrorItem> l = list.getOrDefault(n, new ArrayList<>());
        l.add(new ErrorItem('d', n));
        list.put(n, l);
    }

    public void errorE(int n) {
        ArrayList<ErrorItem> l = list.getOrDefault(n, new ArrayList<>());
        l.add(new ErrorItem('e', n));
        list.put(n, l);
    }

    public void errorF(int n) {
        ArrayList<ErrorItem> l = list.getOrDefault(n, new ArrayList<>());
        l.add(new ErrorItem('f', n));
        list.put(n, l);
    }

    public void errorG(int n) {
        ArrayList<ErrorItem> l = list.getOrDefault(n, new ArrayList<>());
        l.add(new ErrorItem('g', n));
        list.put(n, l);
    }

    public void errorH(int n) {
        ArrayList<ErrorItem> l = list.getOrDefault(n, new ArrayList<>());
        l.add(new ErrorItem('h', n));
        list.put(n, l);
    }

    public void errorL(int n) {
        ArrayList<ErrorItem> l = list.getOrDefault(n, new ArrayList<>());
        l.add(new ErrorItem('l', n));
        list.put(n, l);
    }

    public void errorM(int n) {
        ArrayList<ErrorItem> l = list.getOrDefault(n, new ArrayList<>());
        l.add(new ErrorItem('m', n));
        list.put(n, l);
    }

    public boolean isNoError() {
        for (ArrayList<ErrorItem> l:list.values()) {
            if (!l.isEmpty()) return false;
        }
        return true;
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
