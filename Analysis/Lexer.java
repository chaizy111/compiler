package Analysis;

import Analysis.Token.Token;
import Analysis.Token.TokenType;
import Error.ErrorDealer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class Lexer {
    private InputStream file;
    private FileWriter outputfile;
    private HashMap<String, TokenType.tokenType> reversedWord;
    private TokenType.tokenType currentToken;
    private Character currentChar;
    private int currentLine;
    private boolean isEnd;
    private ErrorDealer errorDealer;
    private ArrayList<Token> list;

    public Lexer(InputStream file, FileWriter outputfile, ErrorDealer e) {
        this.file = file;
        this.outputfile = outputfile;
        this.errorDealer = e;
        this.currentToken = null;
        this.currentChar = 0;
        this.currentLine = 0;
        this.isEnd = false;
        this.list = new ArrayList<>();
        initReservedWord();
    }

    private void initReservedWord() {
        reversedWord = new HashMap<>();
        reversedWord.put("main", TokenType.tokenType.MAINTK);
        reversedWord.put("const", TokenType.tokenType.CONSTTK);
        reversedWord.put("int", TokenType.tokenType.INTTK);
        reversedWord.put("char", TokenType.tokenType.CHARTK);
        reversedWord.put("break", TokenType.tokenType.BREAKTK);
        reversedWord.put("continue", TokenType.tokenType.CONTINUETK);
        reversedWord.put("if", TokenType.tokenType.IFTK);
        reversedWord.put("else", TokenType.tokenType.ELSETK);
        reversedWord.put("for", TokenType.tokenType.FORTK);
        reversedWord.put("getint", TokenType.tokenType.GETINTTK);
        reversedWord.put("getchar", TokenType.tokenType.GETCHARTK);
        reversedWord.put("printf", TokenType.tokenType.PRINTFTK);
        reversedWord.put("return", TokenType.tokenType.RETURNTK);
        reversedWord.put("void", TokenType.tokenType.VOIDTK);
    }

    public TokenType.tokenType getCurrentToken() {
        return currentToken;
    }

    public int getCurrentLine(){
        return currentLine;
    }

    public ArrayList<Token> getList() {
        return list;
    }

    public boolean isCurrentCharDigit() {
        return currentChar >= '0' && currentChar <= '9';
    }

    public boolean isCurrentCharAlpha() {
        return (currentChar >= 'A' && currentChar <= 'Z') ||
                (currentChar >= 'a' && currentChar <= 'z');
    }

    public void getchar() throws IOException{
        file.mark(10000); // everytime you read a char ,you must mark the position first
        int c = file.read();
        if (c == -1) isEnd = true;
        currentChar = (char) c;
    }

    public void passSpace() throws IOException{
        while(true) {
            getchar();
            if (currentChar == '\n') currentLine++;
            if (currentChar == ' ' || currentChar == '\n' || currentChar == '\t') continue;
            break;
        }
    }

    public void next() throws IOException{
        passSpace();
        if (isEnd) {
            currentToken = TokenType.tokenType.END;
            Token t = new Token(currentToken, "", currentLine);
            if (list.get(list.size() - 1).getType() != TokenType.tokenType.END) list.add(t);
            return; // If we reach the end of program, then stop
        }
        currentToken = null;
        String s = currentChar.toString();
        if (isCurrentCharAlpha() || currentChar == '_') { // Judge Indent and reversed word
            while (true) {
                getchar();
                if(isCurrentCharAlpha() || currentChar == '_' || isCurrentCharDigit()) {
                    s = s.concat(currentChar.toString());
                } else {
                    file.reset();
                    currentToken = reversedWord.getOrDefault(s, TokenType.tokenType.IDENFR);
                    break;
                }
            }
        } else if (isCurrentCharDigit()) { // Judge IntConst
            while (true) {
                getchar();
                if (isCurrentCharDigit()) {
                    s = s.concat(currentChar.toString());
                } else {
                    file.reset();
                    currentToken = TokenType.tokenType.INTCON;
                    break;
                }
            }
        } else if (currentChar == '\"') { // Judge StringConst
            currentToken = TokenType.tokenType.STRCON;
            while (true) {
                getchar();
                if (currentChar == '\\') { // don't care what is behind the black flash
                    s = s.concat(currentChar.toString());
                    getchar();
                    s = s.concat(currentChar.toString());
                } else if (currentChar == '\"') { // meet the other double quotation mark, then stop
                    s = s.concat(currentChar.toString());
                    break;
                } else {
                    s = s.concat(currentChar.toString());
                }
            }
        } else if (currentChar == '\'') { // Judge CharConst
            currentToken = TokenType.tokenType.CHRCON;
            while (true) {
                getchar();
                if (currentChar == '\\') { // don't care what is behind the black flash
                    s = s.concat(currentChar.toString());
                    getchar();
                    s = s.concat(currentChar.toString());
                } else if (currentChar == '\'') { // meet the other single quotation mark, then stop
                    s = s.concat(currentChar.toString());
                    break;
                } else {
                    s = s.concat(currentChar.toString());
                }
            }
        } else if (currentChar == '!') { // Judge ! and !=
            getchar();
            if (currentChar == '=') {
                s = s.concat(currentChar.toString());
                currentToken = TokenType.tokenType.NEQ;
            } else {
                file.reset();
                currentToken = TokenType.tokenType.NOT;
            }
        } else if (currentChar == '&') { // Judge &&
            getchar();
            currentToken = TokenType.tokenType.AND;
            if (currentChar == '&') {
                s = s.concat(currentChar.toString());
            } else {
                s = s.concat("&"); //correct the wrong place
                file.reset();
                errorDealer.errorA(currentLine + 1);
            }
        } else if (currentChar == '|') { // Judge ||
            getchar();
            currentToken = TokenType.tokenType.OR;
            if (currentChar == '|') {
                s = s.concat(currentChar.toString());
            } else {
                s = s.concat("|"); //correct the wrong place;
                file.reset();
                errorDealer.errorA(currentLine + 1);
            }
        } else if (currentChar == '+') { // Judge +
            currentToken = TokenType.tokenType.PLUS;
        } else if (currentChar == '-') { // Judge -
            currentToken = TokenType.tokenType.MINU;
        } else if (currentChar == '*') { // Judge *
            currentToken = TokenType.tokenType.MULT;
        } else if (currentChar == '/') { // Judge / and annotation(// or /*)
            getchar();
            if (currentChar == '/') {
                while (true) {
                    getchar();
                    if (currentChar == '\n') {
                        currentLine++;
                        break;
                    }
                    if(isEnd) break; // if encounters the end, then we must end the loop
                }
            } else if (currentChar == '*') {
                while (true) {
                    getchar();
                    if (currentChar == '*') {
                        getchar();
                        if (currentChar == '/') {
                            break;
                        }
                    }
                    if (currentChar == '\n') {
                        currentLine++;
                    }
                }
            } else {
                currentToken = TokenType.tokenType.DIV;
                file.reset();
            }
        } else if (currentChar == '%') { // Judge %
            currentToken = TokenType.tokenType.MOD;
        } else if (currentChar == '<') { // Judge < and <=
            getchar();
            if (currentChar == '=') {
                s = s.concat(currentChar.toString());
                currentToken = TokenType.tokenType.LEQ;
            } else {
                file.reset();
                currentToken = TokenType.tokenType.LSS;
            }
        } else if (currentChar == '>') { // Judge > and >=
            getchar();
            if (currentChar == '=') {
                s = s.concat(currentChar.toString());
                currentToken = TokenType.tokenType.GEQ;
            } else {
                file.reset();
                currentToken = TokenType.tokenType.GRE;
            }
        } else if (currentChar == '=') { // Judge = and ==
            getchar();
            if (currentChar == '=') {
                s = s.concat(currentChar.toString());
                currentToken = TokenType.tokenType.EQL;
            } else {
                file.reset();
                currentToken = TokenType.tokenType.ASSIGN;
            }
        } else if (currentChar == ';') { // Judge ;
            currentToken = TokenType.tokenType.SEMICN;
        } else if (currentChar == ',') { // Judge ,
            currentToken = TokenType.tokenType.COMMA;
        } else if (currentChar == '(') { // Judge (
            currentToken = TokenType.tokenType.LPARENT;
        } else if (currentChar == ')') { // Judge )
            currentToken = TokenType.tokenType.RPARENT;
        } else if (currentChar == '[') { // Judge [
            currentToken = TokenType.tokenType.LBRACK;
        } else if (currentChar == ']') { // Judge ]
            currentToken = TokenType.tokenType.RBRACK;
        } else if (currentChar == '{') { // Judge {
            currentToken = TokenType.tokenType.LBRACE;
        } else if (currentChar == '}') { // Judge }
            currentToken = TokenType.tokenType.RBRACE;
        }
        // write token and string into result file
        if (currentToken != null) {
//            outputfile.write(currentToken + " " + s + "\n");
            Token t = new Token(currentToken, s, currentLine);
            list.add(t);
        }
        next();
    }
}
