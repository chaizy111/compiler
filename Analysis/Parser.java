package Analysis;

import java.io.FileWriter;
import java.io.IOException;

public class Parser {
    private Lexer lexer;
    private FileWriter outputfile;
    private TokenType.tokenType token;
    private TokenType.tokenType preRead;
    private CompUnit compUnit;

    public Parser(Lexer lexer, FileWriter outputfile) {
        this.lexer = lexer;
        this.outputfile = outputfile;
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

    private ConstDecl parseConstDecl(){
        ConstDecl c = new ConstDecl();

        return c;
    }

    private ValDecl parseVarDecl(){
        ValDecl v = new ValDecl();

        return v;
    }
}
