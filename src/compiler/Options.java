package compiler;



import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Options {
    private List<File> sourceFiles = new ArrayList<>();
    public static Options parse(String[] args) {
        Options opts = new Options();
        opts.parseArgs(args);
        return opts;
    }

    private void parseArgs(String[] args) {

       for(String str : args){
           sourceFiles.add(new File(str));
       }
    }

    public List<File> sourceFiles() {
        return sourceFiles;
    }

    public String asmFileNameOf(File file) {
        String filename = file.getName();
        return filename.substring(0, filename.indexOf('.')) + ".S";
    }
}
