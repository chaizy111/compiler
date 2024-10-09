package Analysis.Token;

public class Token {
    private TokenType.tokenType type;
    private String string;
    private int line;


    public Token(TokenType.tokenType t, String s, int l) {
        this.type = t;
        this.string = s;
        this.line = l;
    }

    public TokenType.tokenType getType() {
        return type;
    }

    public String getString() {
        return string;
    }

    public int getLine() {
        return line;
    }

    public int getNumber() {
        return Integer.parseInt(string);
    }
}
