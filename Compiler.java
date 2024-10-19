import Analysis.Lexer;
import Analysis.Parser;
import Analysis.Visitor;
import Error.ErrorDealer;

import java.io.*;

public class Compiler {
    public static void main(String[] args) throws IOException {
        InputStream input = new BufferedInputStream(new FileInputStream("testfile.txt"));
//        FileWriter output = new FileWriter("lexer.txt");
        FileWriter output = new FileWriter("parser.txt");
        FileWriter errorOutput = new FileWriter("error.txt");

        ErrorDealer errorDealer = new ErrorDealer(errorOutput);
        Lexer lexer = new Lexer(input, output, errorDealer);
//        lexer.next();
//        lexer.printToken();
        Parser parser = new Parser(lexer, output, errorDealer);
        parser.parse();
        Visitor visitor = new Visitor(parser.getCompUnit(), output, errorDealer);
        visitor.visit();

        errorDealer.printError();

        input.close();
        output.close();
        errorOutput.close();
    }
}
