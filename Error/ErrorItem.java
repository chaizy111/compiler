package Error;

public class ErrorItem {
    private char kind;
    private int line;

    public ErrorItem(char kind, int line) {
        this.kind = kind;
        this.line = line;
    }

    public char getKind() {
        return kind;
    }

    public int getLine() {
        return line;
    }
}
