package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

abstract public class CommandUtils {
    static public void invoke(List<String> cmdArgs){
        try {
            String[] cmd = cmdArgs.toArray(new String[] {});
            System.out.println(Arrays.toString(cmd));
            Process proc = Runtime.getRuntime().exec(cmd);
            proc.waitFor();
            passThrough(proc.getInputStream());
            passThrough(proc.getErrorStream());
            if (proc.exitValue() != 0) {
                System.err.println(cmd[0] + " failed."
                        + " (status " + proc.exitValue() + ")");
            }
        }
        catch (InterruptedException ex) {
            System.err.println("external command interrupted: "
                    + cmdArgs.get(0) + ": " + ex.getMessage());
        }
        catch (IOException ex) {
            System.err.println(
                    "IO error in external command: " + ex.getMessage());
        }
    }
    

    static private void passThrough(InputStream s) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(s));
        String line;
        while ((line = r.readLine()) != null) {
            System.err.println(line);
        }
    }
}
