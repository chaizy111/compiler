package Analysis;

import java.io.IOException;

public class Parser {
    private Lexer lexer;
    private TokenType.tokenType token;
    private TokenType.tokenType preRead;
    private CompUnit compUnit;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.token = null;
        this.preRead = null;
        this.compUnit = null;
    }

    public void parse() throws IOException{
        nextToken();
        compUnit = parseCompUnit();
    }

    private void nextToken() throws IOException {
        token = preRead;
        lexer.next();
        preRead = lexer.getCurrentToken();
    }

    private CompUnit parseCompUnit(){
        CompUnit c = new CompUnit();


        return c;
    }
}
