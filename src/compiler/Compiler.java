package compiler;

import asm.Type;
import ast.AST;

import ir.IR;
import parser.Parser;
import sysdep.AssemblyCode;
import sysdep.CodeGenerator;
import sysdep.GNUAssembler;
import sysdep.GNULinker;
import type.TypeTable;

import java.io.*;
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
            String linkPath = assemble(destPath);
            link(linkPath);
        }
    }

    private void link(String linkPath) {
        GNULinker.generateExecutable(linkPath);
    }

    private String assemble(String srcPath) {
        String destPath = srcPath.substring(0, srcPath.indexOf('.')) + ".o";
        GNUAssembler.assemble(srcPath, destPath);
        return destPath;
    }

    public void compile(File file, String destPath, Options opts){
        AST ast = Parser.parseFile(file);
        TypeTable types = TypeTable.ilp32();
        AST sem = semanticAnalyze(ast, types, opts);
        IR ir = new IRGenerator(types).generate(sem);
        AssemblyCode asm = generateAssembly(ir, opts);
        System.out.println(asm.toSource());
        writeFile(destPath, asm.toSource());
    }

    private void writeFile(String path, String str) {
        if (path.equals("-")) {
            System.out.print(str);
            return;
        }
        try {
            BufferedWriter f = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(path)));
            try {
                f.write(str);
            }
            finally {
                f.close();
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private AssemblyCode generateAssembly(IR ir, Options opts) {
        return new CodeGenerator(Type.INT32).generate(ir);
    }

    private AST semanticAnalyze(AST ast, TypeTable types, Options opts) {
        new LocalResolver().resolve(ast);
        new TypeResolver(types).resolve(ast);
        types.semanticCheck();
        new DereferenceChecker(types).check(ast);
        new TypeChecker(types).check(ast);
        return ast;
    }

    private Options parseOptions(String[] args) {
        return Options.parse(args);
    }
}
