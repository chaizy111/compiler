import Analysis.frontend.Lexer;
import Analysis.frontend.Parser;
import Analysis.middle.SymbolVisitor;
import Analysis.middle.Visitor;
import Error.ErrorDealer;

import java.io.*;

public class Compiler {
    public static void main(String[] args) throws IOException {
        InputStream input = new BufferedInputStream(new FileInputStream("testfile.txt"));
//        FileWriter output = new FileWriter("lexer.txt");
//        FileWriter output = new FileWriter("symbol.txt");
        FileWriter output = new FileWriter("llvm_ir.txt");
        FileWriter errorOutput = new FileWriter("error.txt");

        ErrorDealer errorDealer = new ErrorDealer(errorOutput);
        Lexer lexer = new Lexer(input, output, errorDealer);
//        lexer.next();
//        lexer.printToken();
        Parser parser = new Parser(lexer, output, errorDealer);
        parser.parse();
        SymbolVisitor symbolVisitor = new SymbolVisitor(parser.getCompUnit(), output, errorDealer);
        symbolVisitor.visit();
        if (errorDealer.isNoError()) { //无错误处理
            Visitor visitor = new Visitor(parser.getCompUnit(), output);
            visitor.visit();
        }

        errorDealer.printError();

        input.close();
        output.close();
        errorOutput.close();
    }
}
