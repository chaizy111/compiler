package Error;

import java.io.FileWriter;
import java.io.IOException;

public class ErrorDealer {
    private FileWriter file;
    public ErrorDealer(FileWriter errorFile) {
        this.file = errorFile;
    }

    public void errorA(int n) {
        try {
            file.write(n + " " + "a" + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void errorI(int n) {
        try {
            file.write(n + " " + "i" + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void errorJ(int n) {
        try {
            file.write(n + " " + "j" + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void errorK(int n) {
        try {
            file.write(n + " " + "k" + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
