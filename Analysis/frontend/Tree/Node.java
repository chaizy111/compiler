package Analysis.frontend.Tree;

import java.io.FileWriter;
import java.io.IOException;

public abstract class Node {
    abstract void print(FileWriter output) throws IOException;
}

