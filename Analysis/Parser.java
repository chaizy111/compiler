package Analysis;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import Analysis.Token.Token;
import Analysis.Token.TokenType;
import Error.ErrorDealer;
import Tree.*;
import Tree.Character;
import Tree.Number;

public class Parser {
    private Lexer lexer;
    private FileWriter outputfile;
    private int preIndex;
    private Token token;
    private Token preRead;
    private CompUnit compUnit;
    private ArrayList<Token> list;
    private ErrorDealer error;

    public Parser(Lexer lexer, FileWriter outputfile, ErrorDealer e) {
        this.lexer = lexer;
        this.outputfile = outputfile;
        this.preIndex = 0;
        this.token = null;
        this.preRead = null;
        this.compUnit = null;
        this.list = null;
        this.error = e;
    }

    public void parse() throws IOException{
        lexer.next();
        this.list = lexer.getList();
        nextToken();
        nextToken();
        compUnit = parseCompUnit();
    }

    private void nextToken() throws IOException {
        // 词法分析在这里输出
        if (token != null && token.getType() != TokenType.tokenType.END) // 一定要在前边输出，这样才符合题目的要求
            outputfile.write(token.getType() + " " + token.getString() + "\n");
        token = preRead;
        preRead = list.get(preIndex);
        preIndex++;
        if (preIndex == list.size()) preIndex--; // 这里对index的处理是为了防止index越界，这样可以保证即使没有新的preRead，我们依然能进行nextToken()
    }
    //TODO ：2：增加或更改判断条件，防止多输出或少输出，多写一些is方法，让判断逻辑独立出来
    private CompUnit parseCompUnit() throws IOException {
        // CompUnit → {Decl} {FuncDef} MainFuncDef
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        CompUnit c = new CompUnit();
        while (true) {
            if(match(token, TokenType.tokenType.CONSTTK)) {
                c.addDeclArrayList(parseConstDecl());
            } else {
                if(match(preRead, TokenType.tokenType.MAINTK)) {
                    c.setMainFuncDef(parseMainFuncDef());
                    break;
                }
                if(isFuncDef()) {
                    c.addFuncDefArrayList(parseFuncDef());
                } else {
                    c.addDeclArrayList(parseVarDecl());
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
        c.setbType(token.getType());
        if(match(token, TokenType.tokenType.INTTK) || match(token, TokenType.tokenType.CHARTK)) {
            c.addConstDefList(parseConstDef());
        }
        while (match(token, TokenType.tokenType.COMMA)) { //实现了跳过逗号
            c.addConstDefList(parseConstDef());
        }
        if(!match(token, TokenType.tokenType.SEMICN)) // 错误处理
            error.errorI(list.get(list.indexOf(token) - 1).getLine());
        c.print(outputfile);
        return c;
    }

    private ValDecl parseVarDecl() throws IOException {
        // VarDecl → BType VarDef { ',' VarDef } ';'
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        ValDecl v = new ValDecl();
        if (match(token, TokenType.tokenType.INTTK) || match(token, TokenType.tokenType.CHARTK))
            v.setbType(list.get(list.indexOf(token) - 1).getType());
        while (match(token, TokenType.tokenType.IDENFR)) {
            v.addVarDefList(parseValDef());
            match(token, TokenType.tokenType.COMMA);
        }
        if(!match(token, TokenType.tokenType.SEMICN)) // 错误处理
            error.errorI(list.get(list.indexOf(token) - 1).getLine());
        v.print(outputfile);
        return v;
    }

    private ConstDef parseConstDef() throws IOException {
        // ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        ConstDef c = new ConstDef();
        match(token, TokenType.tokenType.IDENFR);
        if(match(token, TokenType.tokenType.LBRACK)) {
            c.setConstExp(parseConstExp());
            if(!match(token, TokenType.tokenType.RBRACK)) // 错误处理
                error.errorK(list.get(list.indexOf(token) - 1).getLine());
        }
        match(token, TokenType.tokenType.ASSIGN);
        c.setConstInitVal(parseConstInitVal());
        c.print(outputfile);
        return c;
    }

    private ValDef parseValDef() throws IOException {
        // VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        ValDef v = new ValDef();
        match(token, TokenType.tokenType.IDENFR);
        if(match(token, TokenType.tokenType.LBRACK)) {
            v.setConstExp(parseConstExp());
            if(!match(token, TokenType.tokenType.RBRACK)) // 错误处理
                error.errorK(list.get(list.indexOf(token) - 1).getLine());
        }
        if(match(token, TokenType.tokenType.ASSIGN)) {
            v.setInitVal(parseInitVal());
        }
        v.print(outputfile);
        return v;
    }

    public InitVal parseInitVal() throws IOException {
        //InitVal → Exp | '{' [ Exp { ',' Exp } ] '}' | StringConst
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        InitVal i = new InitVal();
        if(match(token, TokenType.tokenType.LBRACE)) {
            do{
                i.addExpArrayList(parseExp());
            } while (match(token, TokenType.tokenType.COMMA));
            match(token, TokenType.tokenType.RBRACE);
            i.print(outputfile);
        } else if (match(token, TokenType.tokenType.STRCON)) {
            i.setToken(list.get(list.indexOf(token) - 1));
            i.print(outputfile);
        } else {
            i.setExp(parseExp());
            i.print(outputfile);
        }
        return i;
    }

    private FuncDef parseFuncDef() throws IOException {
        // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        FuncDef f = new FuncDef();
        f.setFuncType(parseFuncType());
        if(match(token, TokenType.tokenType.IDENFR)) {
            f.setIdent(list.get(list.indexOf(token) - 1));
        }
        if(match(token, TokenType.tokenType.LPARENT)) {
            f.setFuncFParams(parseFuncFParams());
            if(!match(token, TokenType.tokenType.RPARENT)) // 错误处理
                error.errorJ(list.get(list.indexOf(token) - 1).getLine());
        }
        if(isBlock()) f.setBlock(parseBlock());
        f.print(outputfile);
        return f;
    }

    private MainFuncDef parseMainFuncDef() throws IOException {
        // MainFuncDef → 'int' 'main' '(' ')' Block
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        MainFuncDef m = new MainFuncDef();
        match(token, TokenType.tokenType.MAINTK);
        match(token, TokenType.tokenType.LPARENT);
        if(!match(token, TokenType.tokenType.RPARENT)) // 错误处理
            error.errorJ(list.get(list.indexOf(token) - 1).getLine());
        if(isBlock()) m.setBlock(parseBlock());
        m.print(outputfile);
        return m;
    }

    private ConstInitVal parseConstInitVal() throws IOException {
        // ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        ConstInitVal c = new ConstInitVal();
        if(match(token, TokenType.tokenType.LBRACE)) {
            if(isExp()) { // 防止出现为空的情况
                do {
                    c.addConstExpArrayList(parseConstExp());
                } while (match(token, TokenType.tokenType.COMMA));
            }
            match(token, TokenType.tokenType.RBRACE);
        } else if(match(token, TokenType.tokenType.STRCON)) {
            c.setString(token.getString());
        } else {
            c.addConstExpArrayList(parseConstExp());
        }
        c.print(outputfile);
        return c;
    }

    private FuncFParam parseFuncFParam() throws IOException {
        // FuncFParam → BType Ident ['[' ']']
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        FuncFParam f = new FuncFParam();
        if(match(token, TokenType.tokenType.CHARTK) || match(token, TokenType.tokenType.INTTK)) {
            f.setbType(list.get(list.indexOf(token) - 1).getType());
        }
        if(match(token, TokenType.tokenType.IDENFR)) {
            f.setIdent(list.get(list.indexOf(token) - 1));
        }
        if(match(token, TokenType.tokenType.LBRACK)) {
            f.setIsArray(true);
            if(!match(token, TokenType.tokenType.RBRACK)) // 错误处理
                error.errorK(list.get(list.indexOf(token) - 1).getLine());
        }
        f.print(outputfile);
        return f;
    }

    private Block parseBlock() throws IOException {
        // Block → '{' { BlockItem } '}'
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        Block b = new Block();
        if(match(token, TokenType.tokenType.LBRACE)) {
            while (!match(token, TokenType.tokenType.RBRACE))
                b.addBlockItemArrayList(parseBlockItem());
            b.print(outputfile);
        }
        return b;
    }

    private BlockItem parseBlockItem() throws IOException {
        // BlockItem → Decl | Stmt
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        if(match(token, TokenType.tokenType.CONSTTK)) {
            return parseConstDecl();
        } else if(isBtype()){
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
            return parseFor();
        } else if (match(token, TokenType.tokenType.BREAKTK) || match(token, TokenType.tokenType.CONTINUETK)) {
            if(!match(token, TokenType.tokenType.SEMICN)) // 错误处理
                error.errorI(list.get(list.indexOf(token) - 1).getLine());
            Stmt s = new Stmt();
            s.print(outputfile);
            return s;
        } else if (match(token, TokenType.tokenType.RETURNTK)) {
            return parseReturnStmt();
        } else if (match(token, TokenType.tokenType.PRINTFTK)) {
            return parsePrintStmt();
        }  else if (isBlock()) {
            Stmt s = new Stmt();
            s.setB(parseBlock());
            s.print(outputfile);
            return s;
        } else if (isLValStmt()) {
            return parseLValStmt(1);
        }else {
            Stmt s = new Stmt();
            if(isExp()) {
                s.setE(parseExp());
            }
            if(!match(token, TokenType.tokenType.SEMICN)) // 错误处理
                error.errorI(list.get(list.indexOf(token) - 1).getLine());
            s.print(outputfile);
            return s;
        }
    }

    private IfStmt parseIfStmt() throws IOException {
        // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        IfStmt i = new IfStmt();
        match(token, TokenType.tokenType.LPARENT);
        i.setC(parseCond());
        if(!match(token, TokenType.tokenType.RPARENT)) // 错误处理
            error.errorJ(list.get(list.indexOf(token) - 1).getLine());
        i.setS1(parseStmt());
        if(match(token, TokenType.tokenType.ELSETK)) i.setS2(parseStmt());
        i.print(outputfile);
        return i;
    }

    private For parseFor() throws IOException { //TODO 修改逻辑
        // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        For f = new For();
        match(token, TokenType.tokenType.LPARENT);
        if(!match(token, TokenType.tokenType.SEMICN)) {
            f.setL1(parseLValStmt(2));
            match(token, TokenType.tokenType.SEMICN);
        }
        if(!match(token, TokenType.tokenType.SEMICN)) {
            f.setC(parseCond());
            match(token, TokenType.tokenType.SEMICN);
        }
        if(!match(token, TokenType.tokenType.RPARENT)) {
            f.setL2(parseLValStmt(2));
            match(token, TokenType.tokenType.RPARENT);
        }
        f.setS(parseStmt());
        f.print(outputfile);
        return f;
    }

    private ReturnStmt parseReturnStmt() throws IOException {
        // 'return' [Exp] ';'
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        ReturnStmt r = new ReturnStmt();
        if (isExp()) r.setExp(parseExp());
        if(!match(token, TokenType.tokenType.SEMICN)) // 错误处理
            error.errorI(list.get(list.indexOf(token) - 1).getLine());
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
            p.addExpArrayLsit(parseExp());
        }
        if(!match(token, TokenType.tokenType.RPARENT)) // 错误处理
            error.errorJ(list.get(list.indexOf(token) - 1).getLine());
        if(!match(token, TokenType.tokenType.SEMICN)) // 错误处理
            error.errorI(list.get(list.indexOf(token) - 1).getLine());
        p.print(outputfile);
        return p;
    }

    private LValStmt parseLValStmt(int kind) throws IOException { //TODO 两种类型杂糅，需要分来
        //LVal '=' Exp ';'
        //| LVal '=' 'getint''('')'';'
        //| LVal '=' 'getchar''('')'';'
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        LValStmt l = new LValStmt();
        l.setlVal(parseLVal());
        match(token, TokenType.tokenType.ASSIGN);
        if(match(token, TokenType.tokenType.GETINTTK)) {
            l.setIsGetInt(true);
            match(token, TokenType.tokenType.LPARENT);
            if(!match(token, TokenType.tokenType.RPARENT)) // 错误处理
                error.errorJ(list.get(list.indexOf(token) - 1).getLine());
        } else if (match(token, TokenType.tokenType.GETCHARTK)) {
            l.setIsGetChar(true);
            match(token, TokenType.tokenType.LPARENT);
            if(!match(token, TokenType.tokenType.RPARENT)) // 错误处理
                error.errorJ(list.get(list.indexOf(token) - 1).getLine());
        } else {
            l.setExp(parseExp());
        }
        if(kind == 2) {
            l.print1(outputfile);
        }
        if(kind != 2 && !match(token, TokenType.tokenType.SEMICN)) // 错误处理
            error.errorI(list.get(list.indexOf(token) - 1).getLine());
        if(kind == 1) {
            l.print(outputfile);
        }
        return l;
    }

    private LVal parseLVal() throws IOException {
        // LVal → Ident ['[' Exp ']']
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        LVal l = new LVal();
        l.setIdent(token.getString());
        match(token, TokenType.tokenType.IDENFR);
        if(match(token, TokenType.tokenType.LBRACK)) {
            l.setExp(parseExp());
            if(!match(token, TokenType.tokenType.RBRACK)) // 错误处理
                error.errorK(list.get(list.indexOf(token) - 1).getLine());
        }
        l.print(outputfile);
        return l;
    }

    private ConstExp parseConstExp() throws IOException {
        // ConstExp → AddExp
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        ConstExp c = new ConstExp();
        c.setAddExp(parseAddExp());
        c.print(outputfile);
        return c;
    }

    private Exp parseExp() throws IOException {
        // Exp → AddExp
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        Exp e= new Exp();
        e.setAddExp(parseAddExp());
        e.print(outputfile);
        return e;
    }

    private AddExp parseAddExp() throws IOException { // TODO 实现符号的存储，便于后边的语义分析
        // AddExp → MulExp | AddExp ('+' | '−') MulExp
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        AddExp a = new AddExp();
        do {
            a.addMulExpArrayList(parseMulExp());
            a.print(outputfile);
        } while (match(token, TokenType.tokenType.PLUS) || match(token, TokenType.tokenType.MINU));
        return a;
    }

    private MulExp parseMulExp() throws IOException { // TODO 实现符号的存储，便于后边的语义分析
        //  MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        MulExp m = new MulExp();
        do {
           m.unaryExpArrayList.add(parseUnaryExp());
           m.print(outputfile);
        } while (match(token, TokenType.tokenType.MULT)
                || match(token, TokenType.tokenType.DIV)
                || match(token, TokenType.tokenType.MOD));
        return m;
    }

    private UnaryExp parseUnaryExp() throws IOException { // TODO 实现符号的存储，便于后边的语义分析
        // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        UnaryExp u = new UnaryExp();
        if(isUnaryExpKind2()) { // 注意一定要用预读匹配，否则会出现PrimaryExp与Ident()冲突的问题
            match(token, TokenType.tokenType.IDENFR);
            u.setIdent(list.get(list.indexOf(token) - 1));
            if(match(token, TokenType.tokenType.LPARENT)) {
                if (isFuncRParams()) u.setFuncRParams(parseFuncRParams());
                if(!match(token, TokenType.tokenType.RPARENT)) // 错误处理
                    error.errorJ(list.get(list.indexOf(token) - 1).getLine());
            }
        } else if (isUnaryOp()) {
            u.setUnaryOp(parseUnaryOp());
            u.setUnaryExp(parseUnaryExp());
        } else {
            u.setPrimaryExp(parsePrimaryExp());
        }
        u.print(outputfile);
        return u;
    }

    private PrimaryExp parsePrimaryExp() throws IOException {
        // PrimaryExp → '(' Exp ')' | LVal | Number | Character
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        PrimaryExp p = new PrimaryExp();
        if(match(token, TokenType.tokenType.LPARENT)) {
            p.setExp(parseExp());
            if(!match(token, TokenType.tokenType.RPARENT)) // 错误处理
                error.errorJ(list.get(list.indexOf(token) - 1).getLine());
        } else if(match(token, TokenType.tokenType.IDENFR)) {
            p.setlVal(parseLVal());
        } else if (isNumber()) {
            p.setNumber(parseNumber());
        } else if (isCharacter()) {
            p.setChar1(parseCharacter());
        }
        p.print(outputfile);
        return p;
    }

    private Cond parseCond() throws IOException {
        // Cond → LOrExp
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        Cond c = new Cond();
        c.setlOrExp(parseLOrExp());
        c.print(outputfile);
        return c;
    }

    private LOrExp parseLOrExp() throws IOException {
        //  LOrExp → LAndExp | LOrExp '||' LAndExp
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        LOrExp l = new LOrExp();
        do{
            l.addLAndExpArrayList(parseLAndExp());
            l.print(outputfile);
        } while (match(token, TokenType.tokenType.OR));
        return l;
    }

    private LAndExp parseLAndExp() throws IOException {
        // LAndExp → EqExp | LAndExp '&&' EqExp
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        LAndExp l = new LAndExp();
        do{
            l.addEqExpArrayList(parseEqExp());
            l.print(outputfile);
        } while (match(token, TokenType.tokenType.AND));
        return l;
    }

    private EqExp parseEqExp() throws IOException { // TODO 实现符号的存储，便于后边的语义分析
        // EqExp → RelExp | EqExp ('==' | '!=') RelExp
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        EqExp e = new EqExp();
        do{
            e.addRelExpArrayList(parseRelExp());
            e.print(outputfile);
        } while (match(token, TokenType.tokenType.EQL) || match(token, TokenType.tokenType.NEQ));
        return e;
    }

    private RelExp parseRelExp() throws IOException { // TODO 实现符号的存储，便于后边的语义分析
        // RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        RelExp r = new RelExp();
        do{
           r.addAddExpArrayList(parseAddExp());
           r.print(outputfile);
        } while (match(token, TokenType.tokenType.LSS) || match(token, TokenType.tokenType.LEQ)
                || match(token, TokenType.tokenType.GRE) || match(token, TokenType.tokenType.GEQ));
        return r;
    }

    public Character parseCharacter() throws IOException {
        // Character → CharConst
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        Character c = new Character();
        if (match(token, TokenType.tokenType.CHRCON)) {
            c.setToken(list.get(list.indexOf(token) - 1));
            c.print(outputfile);
        }
        return c;
    }

    public ForStmt parseForStmt() throws IOException { //TODO
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        ForStmt f = new ForStmt();
        f.print(outputfile);
        return f;
    }

    public FuncFParams parseFuncFParams() throws IOException {
        //FuncFParams → FuncFParam { ',' FuncFParam }
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        FuncFParams f = new FuncFParams();
        ArrayList<FuncFParam> list = new ArrayList<>();
        if(isFuncParam()) {
            // 有无参数的情况
            do{
                list.add(parseFuncFParam());
            } while (match(token, TokenType.tokenType.COMMA));
        }
        f.setFuncFParamArrayList(list);
        if(!list.isEmpty())f.print(outputfile);
        return f;
    }

    public FuncRParams parseFuncRParams() throws IOException {
        // FuncRParams → Exp { ',' Exp }
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        FuncRParams f = new FuncRParams();
        ArrayList<Exp> list = new ArrayList<>();
        do{
            list.add(parseExp());
        } while (match(token, TokenType.tokenType.COMMA));
        f.setExpArrayList(list);
        if(!list.isEmpty())f.print(outputfile);
        return f;
    }

    public FuncType parseFuncType() throws IOException {
        // FuncType → 'void' | 'int' | 'char'
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        FuncType f = new FuncType();
        if(match(token, TokenType.tokenType.VOIDTK)
                || match(token, TokenType.tokenType.INTTK)
                || match(token, TokenType.tokenType.CHARTK)) {
            f.setToken(list.get(list.indexOf(token) - 1));
            f.print(outputfile);
        }
        return f;
    }

    public Number parseNumber() throws IOException {
        //  IntConst
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        Number n = new Number();
        if (match(token, TokenType.tokenType.INTCON)) {
            n.setToken(list.get(list.indexOf(token) - 1));
            n.print(outputfile);
        }
        return n;
    }

    public UnaryOp parseUnaryOp() throws IOException {
        // UnaryOp → '+' | '−' | '!'
        if(match(token, TokenType.tokenType.END)) return null; //读到结束就返回空值
        UnaryOp u = new UnaryOp();
        if(match(token, TokenType.tokenType.PLUS)
                || match(token, TokenType.tokenType.MINU)
                || match(token, TokenType.tokenType.NOT)) {
            u.setToken(list.get(list.indexOf(token) - 1));
            u.print(outputfile);
        }
        return u;
    }

    private boolean isBtype() {
        return token.getType() == TokenType.tokenType.INTTK || token.getType() == TokenType.tokenType.CHARTK;
    }

    private boolean isFuncDef() { // 通过FuncType, Ident, ( 三个来判断
        int index = list.indexOf(token);
        return isFuncType()
                && list.get(index + 1).getType() == TokenType.tokenType.IDENFR
                && list.get(index + 2).getType() == TokenType.tokenType.LPARENT;
    }

    private boolean isFuncParam() {
        int index = list.indexOf(token);
        return list.get(index + 1).getType() != TokenType.tokenType.RPARENT
                && list.get(index + 1).getType() != TokenType.tokenType.LBRACE;
    }

    private boolean isFuncType() {
        return token.getType() == TokenType.tokenType.VOIDTK
                || token.getType() == TokenType.tokenType.INTTK
                || token.getType() == TokenType.tokenType.CHARTK;
    }

    private boolean isBlock() {
        return token.getType() == TokenType.tokenType.LBRACE;
    }

    private boolean isLValStmt() { // 要把当前行数记下来，相同行数四个Token以内有assign就是LValStmt\
        int index = list.indexOf(token), line = token.getLine();
        for(;index - preIndex < 5 && index < list.size(); index++) {
            Token t = list.get(index);
            if(t.getLine() != line) return false;
            if(t.getType().equals(TokenType.tokenType.ASSIGN)) return true;
        }
        return false;
    }

    private boolean isExp() {
        return token.getType() == TokenType.tokenType.LPARENT
                || token.getType() == TokenType.tokenType.IDENFR
                || isNumber()
                || isCharacter()
                || isUnaryOp();
    }

    private boolean isNumber() { // 当前token是INT类型就是Number
        return token.getType() == TokenType.tokenType.INTCON;
    }

    private boolean isCharacter() { // 当前token是CHAR类型就是Char
        return token.getType() == TokenType.tokenType.CHRCON;
    }

    private boolean isUnaryOp() {
        return token.getType() == TokenType.tokenType.PLUS
                || token.getType() == TokenType.tokenType.MINU
                || token.getType() == TokenType.tokenType.NOT;
    }

    private boolean isFuncRParams() {
        return isExp();
    }

    private boolean isUnaryExpKind2() {
        int index = list.indexOf(token);
        return token.getType() == TokenType.tokenType.IDENFR
                && list.get(index + 1).getType() == TokenType.tokenType.LPARENT;
    }

    private boolean match(Token actual, TokenType.tokenType aim) throws IOException {
        if (actual.getType() != aim) {
            return false;
        } else {
            nextToken();
            return true;
        }
    }
}
