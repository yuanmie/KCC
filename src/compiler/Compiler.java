package compiler;

import ast.AST;
import parser.Parser;

import java.io.File;
import java.util.List;


public class Compiler {
    static final public String ProgramName = "KCC";
    static final public String Version = "0.1";

    public static void main(String[] args){
        new Compiler().commandMain(args);
    }

    public void commandMain(String[] args){
        Options opts = parseOptions(args);
        List<File> srcs = opts.sourceFiles();
        build(srcs, opts);
    }

    private void build(List<File> srcs, Options opts) {
        for(File file : srcs){
            String destPath = opts.asmFileNameOf(file);
            compile(file, destPath, opts);
        }
    }

    public void compile(File file, String destPath, Options opt){
        AST ast = Parser.parseFile(file);

    }
    private Options parseOptions(String[] args) {
        return Options.parse(args);
    }
}
