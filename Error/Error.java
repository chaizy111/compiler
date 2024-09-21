package Error;

import java.io.FileWriter;
import java.io.IOException;

public class Error {
    private FileWriter file;
    public Error(FileWriter errorFile) {
        this.file = errorFile;
    }

    public void errorA(int n) {
        try {
            file.write(n + " " + "a" + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
