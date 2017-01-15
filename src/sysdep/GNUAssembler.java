package sysdep;



import utils.CommandUtils;

import java.util.ArrayList;
import java.util.List;

public class GNUAssembler{

    GNUAssembler() {

    }

    // #@@range/assemble{
    public static void assemble(String srcPath, String destPath
                            ){
        List<String> cmd = new ArrayList<String>();
        cmd.add("as");
        cmd.add("--32");
        cmd.add("-o");
        cmd.add(destPath);
        cmd.add(srcPath);
        CommandUtils.invoke(cmd);
    }
    // #@@}
}
