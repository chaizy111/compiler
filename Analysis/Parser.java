package Analysis;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import Error.ErrorDealer;

public class Parser {
    private Lexer lexer;
    private FileWriter outputfile;
    private TokenType.tokenType token;
    private TokenType.tokenType preRead;
    private CompUnit compUnit;
    private ErrorDealer error;

    public Parser(Lexer lexer, FileWriter outputfile, ErrorDealer e) {
        this.lexer = lexer;
        this.outputfile = outputfile;
        this.token = null;
        this.preRead = null;
        this.compUnit = null;
        this.error = e;
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

    private CompUnit parseCompUnit() throws IOException{ // CompUnit → {Decl} {FuncDef} MainFuncDef
        CompUnit c = new CompUnit();
        while (true) {
            if(match(token, TokenType.tokenType.CONSTTK)) {
                c.declArrayList.add(parseConstDecl());
            } else {
                TokenType.tokenType t = token; // 保存btype类型
                if(match(preRead, TokenType.tokenType.MAINTK)) {
                    c.mainFuncDef = parseMainFuncDef();
                    break;
                }
                match(preRead, TokenType.tokenType.IDENFR); // 剩下两个的预读都是Indent类型
                if(match(preRead, TokenType.tokenType.LPARENT)) { //再次查看预读的字符，如果是(就是funcdef
                    c.funcDefArrayList.add(parseFuncDef());
                } else {
                    c.declArrayList.add(parseVarDecl());
                }
            }
        }
        c.print(outputfile);
        return c;
    }

    private ConstDecl parseConstDecl() throws IOException{ //  ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
        ConstDecl c = new ConstDecl();
        c.bType = token;
        nextToken();
        c.constDefList.add(parseConstDef());
        while (match(token, TokenType.tokenType.COMMA)) { //实现了跳过逗号
            c.constDefList.add(parseConstDef());
        }
        int nowLine = lexer.getCurrentLine(); // 错误处理
        if(!match(token, TokenType.tokenType.SEMICN)) error.errorI(nowLine + 1);
        c.print(outputfile);
        return c;
    }

    private ValDecl parseVarDecl() throws IOException{ // VarDecl → BType VarDef { ',' VarDef } ';'
        ValDecl v = new ValDecl();
        if (match(token, TokenType.tokenType.INTTK) || match(token, TokenType.tokenType.CHARTK))
            v.bType = token;
        while (match(token, TokenType.tokenType.IDENFR)) {
            v.varDefList.add(parseValDef());
            match(token, TokenType.tokenType.COMMA);
        }
        int nowLine = lexer.getCurrentLine(); // 错误处理
        if(!match(token, TokenType.tokenType.SEMICN)) error.errorI(nowLine + 1);
        v.print(outputfile);
        return v;
    }

    private ConstDef parseConstDef() throws IOException{ // ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal
        ConstDef c = new ConstDef();
        match(token, TokenType.tokenType.IDENFR);
        match(token, TokenType.tokenType.LBRACK);
        c.constExp = parseConstExp();
        int nowLine = lexer.getCurrentLine(); // 错误处理
        if(!match(token, TokenType.tokenType.RBRACK)) error.errorK(nowLine + 1);
        match(token, TokenType.tokenType.EQL);
        c.constInitVal = parseConstInitVal();
        c.print(outputfile);
        return c;
    }

    private ValDef parseValDef() throws IOException{ // VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
        ValDef v = new ValDef();
        match(token, TokenType.tokenType.IDENFR);
        if(match(token, TokenType.tokenType.LBRACK)) {
            v.constExp = parseConstExp();
            int nowLine = lexer.getCurrentLine(); // 错误处理
            if (!match(token, TokenType.tokenType.RBRACK)) error.errorK(nowLine + 1);
        }
        if(match(token, TokenType.tokenType.EQL)) {
            v.constInitVal = parseConstInitVal();
        }
        v.print(outputfile);
        return v;
    }

    private FuncDef parseFuncDef() throws IOException{ //  FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        FuncDef f = new FuncDef();
        match(token, TokenType.tokenType.LPARENT);
        ArrayList<FuncParam> list = new ArrayList<>();
        if(!match(preRead, TokenType.tokenType.RPARENT) && !match(preRead, TokenType.tokenType.LBRACE)) {
            // 有无参数的情况
            do{
                list.add(parseFuncParam());
            } while (match(token, TokenType.tokenType.COMMA));
        }
        f.funcParamList = list;
        int nowLine = lexer.getCurrentLine(); // 错误处理
        if(!match(token, TokenType.tokenType.RPARENT)) error.errorJ(nowLine + 1);
        f.block = parseBlock();
        f.print(outputfile);
        return f;
    }

    private MainFuncDef parseMainFuncDef() throws IOException{ //  MainFuncDef → 'int' 'main' '(' ')' Block
        MainFuncDef m = new MainFuncDef();
        match(token, TokenType.tokenType.MAINTK);
        match(token, TokenType.tokenType.LPARENT);
        int nowLine = lexer.getCurrentLine(); // 错误处理
        if(!match(token, TokenType.tokenType.RPARENT)) error.errorJ(nowLine + 1);
        m.block = parseBlock();
        m.print(outputfile);
        return  m;
    }

    private ConstInitVal parseConstInitVal() throws IOException{ // ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst
        ConstInitVal c = new ConstInitVal();
        if(match(token, TokenType.tokenType.LBRACE)) {
            do {
                c.constExpArrayList.add(parseConstExp());
            } while (match(token, TokenType.tokenType.COMMA));
            match(token, TokenType.tokenType.RBRACE);
        } else if(match(token, TokenType.tokenType.STRCON)) {
            c.isString = true;
        }
        c.print(outputfile);
        return c;
    }

    private FuncParam parseFuncParam() throws IOException{
        FuncParam f = new FuncParam();
        if(match(token, TokenType.tokenType.CHARTK) || match(token, TokenType.tokenType.INTTK)) {
            f.bType = token;
        }
        match(token, TokenType.tokenType.IDENFR);
        if(match(token, TokenType.tokenType.LBRACK)) {
            f.isArray = true;
            int nowLine = lexer.getCurrentLine(); // 错误处理
            if(!match(token, TokenType.tokenType.RPARENT)) error.errorK(nowLine + 1);
        }
        f.print(outputfile);
        return f;
    }

    private Block parseBlock() throws IOException{
        Block b = new Block();
        b.print(outputfile);
        return b;
    }

    private ConstExp parseConstExp() throws IOException{
        ConstExp c = new ConstExp();
        c.addExp = parseAddExp();
        c.print(outputfile);
        return c;
    }


    private AddExp parseAddExp() throws IOException{
        AddExp a = new AddExp();
        do {
            a.mulExpArrayList.add(parseMulExp());
        } while (match(token, TokenType.tokenType.PLUS) || match(token, TokenType.tokenType.MINU));
        a.print(outputfile);
        return a;
    }

    public MulExp parseMulExp() throws IOException{
        MulExp m = new MulExp();
        do {
           m.unaryExpArrayList.add(parseUnaryExp());
        } while (match(token, TokenType.tokenType.MULT)
                || match(token, TokenType.tokenType.DIV)
                || match(token, TokenType.tokenType.MOD));
        m.print(outputfile);
        return m;
    }

    private UnaryExp parseUnaryExp() throws IOException{
        UnaryExp u = new UnaryExp();
        u.print(outputfile);
        return u;
    }

    private boolean match(TokenType.tokenType actual, TokenType.tokenType aim) throws IOException{
        if (actual != aim) {
            return false;
        } else {
            nextToken();
            return true;
        }
    }
}
