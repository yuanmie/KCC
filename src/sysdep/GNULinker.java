package sysdep;


import utils.CommandUtils;

import java.util.ArrayList;
import java.util.List;

public class GNULinker{
    // 32bit Linux dependent
    // #@@range/vars{
    static final private String LINKER = "/usr/bin/ld";
    static final private String DYNAMIC_LINKER      = "/lib/ld-linux.so.2";
    static final private String C_RUNTIME_INIT      = "/usr/lib32/crti.o";
    static final private String C_RUNTIME_START     = "/usr/lib32/crt1.o";
    static final private String C_RUNTIME_START_PIE = "/usr/lib32/Scrt1.o";
    static final private String C_RUNTIME_FINI      = "/usr/lib32/crtn.o";
    // #@@}

    // #@@range/generateExecutable{
    public static void generateExecutable(String destPath){
        List<String> cmd = new ArrayList<String>();
        cmd.add(LINKER);
        cmd.add("-m");
        cmd.add("-elf_i386");
        cmd.add("-dynamic-linker");
        cmd.add(DYNAMIC_LINKER);
        cmd.add(C_RUNTIME_START);
        cmd.add(C_RUNTIME_INIT);
        cmd.add("-L/usr/lib32");
        cmd.add("-lc");
        cmd.add("/usr/lib32/crtn.o");
        cmd.add("-o");
        cmd.add("a.out");
        cmd.add(destPath);
        CommandUtils.invoke(cmd);
    }

}
