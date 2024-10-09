import Analysis.Lexer;
import Analysis.Parser;
import Error.ErrorDealer;

import java.io.*;

public class Compiler {
    public static void main(String[] args) throws IOException {
        InputStream input = new BufferedInputStream(new FileInputStream("testfile.txt"));
        FileWriter output = new FileWriter("parser.txt");
//        FileWriter output1 = new FileWriter("lexer.txt");
        FileWriter errorOutput = new FileWriter("error.txt");

        ErrorDealer errorDealer = new ErrorDealer(errorOutput);
        Lexer lexer = new Lexer(input, output, errorDealer);
        Parser parser = new Parser(lexer, output, errorDealer);
        parser.parse();

        input.close();
        output.close();
//        output1.close();
        errorOutput.close();
    }
}
