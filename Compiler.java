import Analysis.Lexer;
import Error.Error;

import java.io.*;

public class Compiler {
    public static void main(String[] args) throws IOException {
        InputStream input = new BufferedInputStream(new FileInputStream("testfile.txt"));
        FileWriter lexerOutput = new FileWriter("lexer.txt");
        FileWriter errorOutput = new FileWriter("error.txt");

        Error error = new Error(errorOutput);
        Lexer lexer = new Lexer(input, lexerOutput, error);
        lexer.next();

        input.close();
        lexerOutput.close();
        errorOutput.close();
    }
}
