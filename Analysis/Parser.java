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
        nextToken();
        compUnit = parseCompUnit();
    }

    private void nextToken() throws IOException {
        token = preRead;
        lexer.next();
        preRead = lexer.getCurrentToken();
    }

    private CompUnit parseCompUnit() throws IOException {
        // CompUnit → {Decl} {FuncDef} MainFuncDef
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
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

    private ConstDecl parseConstDecl() throws IOException {
        // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
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

    private ValDecl parseVarDecl() throws IOException {
        // VarDecl → BType VarDef { ',' VarDef } ';'
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
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

    private ConstDef parseConstDef() throws IOException {
        // ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        ConstDef c = new ConstDef();
        match(token, TokenType.tokenType.IDENFR);
        match(token, TokenType.tokenType.LBRACK);
        c.constExp = parseConstExp();
        int nowLine = lexer.getCurrentLine(); // 错误处理
        if(!match(token, TokenType.tokenType.RBRACK)) error.errorK(nowLine + 1);
        match(token, TokenType.tokenType.ASSIGN);
        c.constInitVal = parseConstInitVal();
        c.print(outputfile);
        return c;
    }

    private ValDef parseValDef() throws IOException {
        // VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        ValDef v = new ValDef();
        match(token, TokenType.tokenType.IDENFR);
        if(match(token, TokenType.tokenType.LBRACK)) {
            v.constExp = parseConstExp();
            int nowLine = lexer.getCurrentLine(); // 错误处理
            if (!match(token, TokenType.tokenType.RBRACK)) error.errorK(nowLine + 1);
        }
        if(match(token, TokenType.tokenType.ASSIGN)) {
            v.constInitVal = parseConstInitVal();
        }
        v.print(outputfile);
        return v;
    }

    private FuncDef parseFuncDef() throws IOException {
        // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
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
        if(match(token, TokenType.tokenType.LBRACE)) f.block = parseBlock();
        f.print(outputfile);
        return f;
    }

    private MainFuncDef parseMainFuncDef() throws IOException {
        // MainFuncDef → 'int' 'main' '(' ')' Block
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        MainFuncDef m = new MainFuncDef();
        match(token, TokenType.tokenType.MAINTK);
        match(token, TokenType.tokenType.LPARENT);
        int nowLine = lexer.getCurrentLine(); // 错误处理
        if(!match(token, TokenType.tokenType.RPARENT)) error.errorJ(nowLine + 1);
        if(match(token, TokenType.tokenType.LBRACE)) m.block = parseBlock();
        m.print(outputfile);
        return m;
    }

    private ConstInitVal parseConstInitVal() throws IOException {
        // ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
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

    private FuncParam parseFuncParam() throws IOException {
        // FuncFParam → BType Ident ['[' ']']
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        FuncParam f = new FuncParam();
        if(match(token, TokenType.tokenType.CHARTK) || match(token, TokenType.tokenType.INTTK)) {
            f.bType = token;
        }
        match(token, TokenType.tokenType.IDENFR);
        if(match(token, TokenType.tokenType.LBRACK)) {
            f.isArray = true;
            int nowLine = lexer.getCurrentLine(); // 错误处理
            if(!match(token, TokenType.tokenType.RBRACK)) error.errorK(nowLine + 1);
        }
        f.print(outputfile);
        return f;
    }

    private Block parseBlock() throws IOException {
        // Block → '{' { BlockItem } '}'
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        Block b = new Block();
        while (!match(token, TokenType.tokenType.RBRACE))
            b.blockItemArrayList.add(parseBlockItem());
        b.print(outputfile);
        return b;
    }

    private BlockItem parseBlockItem() throws IOException {
        // BlockItem → Decl | Stmt
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        if(match(token, TokenType.tokenType.CONSTTK)) {
            return parseConstDecl();
        } else if(match(token, TokenType.tokenType.INTTK) || match(token, TokenType.tokenType.CHARTK)){
            return parseVarDecl();
        } else {
            return parseStmt();
        }
    }

    private Stmt parseStmt() throws IOException {
        //Stmt → LVal '=' Exp ';'
        //| [Exp] ';' //有无Exp两种情况
        //| Block
        //| 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
        //| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
        //| 'break' ';' | 'continue' ';'
        //| 'return' [Exp] ';'
        //| LVal '=' 'getint''('')'';'
        //| LVal '=' 'getchar''('')'';'
        //| 'printf''('StringConst {','Exp}')'';'
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        if (match(token, TokenType.tokenType.IFTK)) {
            return parseIfStmt();
        } else if (match(token, TokenType.tokenType.FORTK)) {
            return parseForStmt();
        } else if (match(token, TokenType.tokenType.BREAKTK) || match(token, TokenType.tokenType.CONTINUETK)) {
            int nowLine = lexer.getCurrentLine(); // 错误处理
            if(!match(token, TokenType.tokenType.SEMICN)) error.errorI(nowLine + 1);
            Stmt s = new Stmt();
            s.print(outputfile);
            return s;
        } else if (match(token, TokenType.tokenType.RETURNTK)) {
            return parseReturnStmt();
        } else if (match(token, TokenType.tokenType.PRINTFTK)) {
            return parsePrintStmt();
        } else if (match(token, TokenType.tokenType.IDENFR)) {
            return parseLValStmt(1);
        } else if (match(token, TokenType.tokenType.LBRACE)) {
            Stmt s = new Stmt();
            s.b = parseBlock();
            s.print(outputfile);
            return s;
        } else {
            Stmt s = new Stmt();
            s.e = parseExp();
            int nowLine = lexer.getCurrentLine(); // 错误处理
            if(!match(token, TokenType.tokenType.SEMICN)) error.errorI(nowLine + 1);
            s.print(outputfile);
            return s;
        }
    }

    private IfStmt parseIfStmt() throws IOException {
        // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        IfStmt i = new IfStmt();
        match(token, TokenType.tokenType.LPARENT);
        i.c = parseCond();
        int nowLine = lexer.getCurrentLine(); // 错误处理
        if(!match(token, TokenType.tokenType.RPARENT)) error.errorJ(nowLine + 1);
        i.s1 = parseStmt();
        if(match(token, TokenType.tokenType.ELSETK)) i.s2 = parseStmt();
        i.print(outputfile);
        return i;
    }
    
    private ForStmt parseForStmt() throws IOException {
        // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        ForStmt f = new ForStmt();
        match(token, TokenType.tokenType.LPARENT);
        if(!match(token, TokenType.tokenType.SEMICN)) {
            f.l1 = parseLValStmt(2);
            match(token, TokenType.tokenType.SEMICN);
        }
        if(!match(token, TokenType.tokenType.SEMICN)) {
            f.c = parseCond();
            match(token, TokenType.tokenType.SEMICN);
        }
        if(!match(token, TokenType.tokenType.RPARENT)) {
            f.l2 = parseLValStmt(2);
            match(token, TokenType.tokenType.RPARENT);
        }
        f.s = parseStmt();
        f.print(outputfile);
        return f;
    }

    private ReturnStmt parseReturnStmt() throws IOException {
        // 'return' [Exp] ';'
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        ReturnStmt r = new ReturnStmt();
        r.exp = parseExp();
        int nowLine = lexer.getCurrentLine(); // 错误处理
        if(!match(token, TokenType.tokenType.SEMICN)) error.errorI(nowLine + 1);
        r.print(outputfile);
        return r;
    }

    private PrintfStmt parsePrintStmt() throws IOException {
        // 'printf''('StringConst {','Exp}')'';'
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        PrintfStmt p = new PrintfStmt();
        match(token, TokenType.tokenType.LPARENT);
        match(token, TokenType.tokenType.STRCON);
        while (match(token, TokenType.tokenType.COMMA)) {
            p.expArrayList.add(parseExp());
        }
        int nowLine = lexer.getCurrentLine(); // 错误处理
        if(!match(token, TokenType.tokenType.RPARENT)) error.errorJ(nowLine + 1);
        if(!match(token, TokenType.tokenType.SEMICN)) error.errorI(nowLine + 1);
        p.print(outputfile);
        return p;
    }

    private LValStmt parseLValStmt(int kind) throws IOException {
        //LVal '=' Exp ';'
        //| LVal '=' 'getint''('')'';'
        //| LVal '=' 'getchar''('')'';'
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        LValStmt l = new LValStmt();
        l.lVal = parseLVal();
        match(token, TokenType.tokenType.ASSIGN);
        if(match(token, TokenType.tokenType.GETINTTK)) {
            l.isGetInt = true;
            match(token, TokenType.tokenType.LPARENT);
            int nowLine = lexer.getCurrentLine(); // 错误处理
            if(!match(token, TokenType.tokenType.RPARENT)) error.errorJ(nowLine + 1);
        } else if (match(token, TokenType.tokenType.GETCHARTK)) {
            l.isGetChar = true;
            match(token, TokenType.tokenType.LPARENT);
            int nowLine = lexer.getCurrentLine(); // 错误处理
            if(!match(token, TokenType.tokenType.RPARENT)) error.errorJ(nowLine + 1);
        } else {
            l.exp = parseExp();
        }
        int nowLine = lexer.getCurrentLine(); // 错误处理
        if(!match(token, TokenType.tokenType.SEMICN)) error.errorI(nowLine + 1);
        if(kind == 1) {
            l.print(outputfile);
        } else if(kind == 2) {
            l.print1(outputfile);
        }
        return l;
    }

    private LVal parseLVal() throws IOException {
        // LVal → Ident ['[' Exp ']']
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        LVal l = new LVal();
        if(match(token, TokenType.tokenType.LBRACK)) {
            l.exp = parseExp();
            int nowLine = lexer.getCurrentLine(); // 错误处理
            if(!match(token, TokenType.tokenType.RBRACK)) error.errorK(nowLine + 1);
        }
        l.print(outputfile);
        return l;
    }

    private ConstExp parseConstExp() throws IOException {
        // ConstExp → AddExp
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        ConstExp c = new ConstExp();
        c.addExp = parseAddExp();
        c.print(outputfile);
        return c;
    }

    private Exp parseExp() throws IOException {
        // Exp → AddExp
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        Exp e= new Exp();
        e.addExp = parseAddExp();
        e.print(outputfile);
        return e;
    }

    private AddExp parseAddExp() throws IOException {
        // AddExp → MulExp | AddExp ('+' | '−') MulExp
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        AddExp a = new AddExp();
        do {
            a.mulExpArrayList.add(parseMulExp());
        } while (match(token, TokenType.tokenType.PLUS) || match(token, TokenType.tokenType.MINU));
        a.print(outputfile);
        return a;
    }

    private MulExp parseMulExp() throws IOException {
        //  MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        MulExp m = new MulExp();
        do {
           m.unaryExpArrayList.add(parseUnaryExp());
        } while (match(token, TokenType.tokenType.MULT)
                || match(token, TokenType.tokenType.DIV)
                || match(token, TokenType.tokenType.MOD));
        m.print(outputfile);
        return m;
    }

    private UnaryExp parseUnaryExp() throws IOException {
        // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        UnaryExp u = new UnaryExp();
        if(match(token, TokenType.tokenType.IDENFR)) {
            ArrayList<FuncParam> list = new ArrayList<>();
            if(!match(preRead, TokenType.tokenType.RPARENT)) {
                // 有无参数的情况
                do{
                    list.add(parseFuncParam());
                } while (match(token, TokenType.tokenType.COMMA));
            }
            u.funcParamArrayList = list;
            int nowLine = lexer.getCurrentLine(); // 错误处理
            if(!match(token, TokenType.tokenType.RPARENT)) error.errorJ(nowLine + 1);
        } else if (match(token, TokenType.tokenType.PLUS)
                    || match(token, TokenType.tokenType.MINU)
                    || match(token, TokenType.tokenType.NOT)) {
            u.unaryExp = parseUnaryExp();
        } else {
            u.primaryExp = parsePrimaryExp();
        }
        u.print(outputfile);
        return u;
    }

    private PrimaryExp parsePrimaryExp() throws IOException {
        // PrimaryExp → '(' Exp ')' | LVal | Number | Character
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        PrimaryExp p = new PrimaryExp();
        if(match(token, TokenType.tokenType.LPARENT)) {
            p.exp = parseExp();
            int nowLine = lexer.getCurrentLine(); // 错误处理
            if(!match(token, TokenType.tokenType.RPARENT)) error.errorJ(nowLine + 1);
        } else if(match(token, TokenType.tokenType.IDENFR)) {
            p.lVal = parseLVal();
        } else if (match(token, TokenType.tokenType.INTCON)) {
            p.isNumber = true;
        } else if (match(token, TokenType.tokenType.CHRCON)) {
            p.isChar = true;
        }
        p.print(outputfile);
        return p;
    }

    private Cond parseCond() throws IOException {
        // Cond → LOrExp
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        Cond c = new Cond();
        c.lOrExp = parseLOrExp();
        c.print(outputfile);
        return c;
    }

    private LOrExp parseLOrExp() throws IOException {
        //  LOrExp → LAndExp | LOrExp '||' LAndExp
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        LOrExp l = new LOrExp();
        do{
            l.lAndExpArrayList.add(parseLAndExp());
        } while (match(token, TokenType.tokenType.OR));
        l.print(outputfile);
        return l;
    }

    private LAndExp parseLAndExp() throws IOException {
        // LAndExp → EqExp | LAndExp '&&' EqExp
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        LAndExp l = new LAndExp();
        do{
            l.eqExpArrayList.add(parseEqExp());
        } while (match(token, TokenType.tokenType.AND));
        l.print(outputfile);
        return l;
    }

    private EqExp parseEqExp() throws IOException {
        // EqExp → RelExp | EqExp ('==' | '!=') RelExp
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        EqExp e = new EqExp();
        do{
            e.relExpArrayList.add(parseRelExp());
        } while (match(token, TokenType.tokenType.EQL) || match(token, TokenType.tokenType.NEQ));
        e.print(outputfile);
        return e;
    }

    private RelExp parseRelExp() throws IOException {
        // RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        RelExp r = new RelExp();
        do{
            r.addExpArrayList.add(parseAddExp());
        } while (match(token, TokenType.tokenType.LSS) || match(token, TokenType.tokenType.LEQ)
                || match(token, TokenType.tokenType.GRE) || match(token, TokenType.tokenType.GEQ));
        r.print(outputfile);
        return r;
    }

    private boolean match(TokenType.tokenType actual, TokenType.tokenType aim) throws IOException {
        if (actual != aim) {
            return false;
        } else {
            nextToken();
            return true;
        }
    }
}
