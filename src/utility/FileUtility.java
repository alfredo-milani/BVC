package utility;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

/**
 * Created by alfredo on 31/03/17.
 */
public class FileUtility {

    public static boolean confirmOperation(String destinationPath, String sourcePath) {
        Scanner reader = new Scanner(System.in);

        FileUtility.printToScreen(String.format(
                Constants.confirm,
                destinationPath,
                sourcePath
        ));
        FileUtility.printToScreen("(S/s -> Sì  -  N/n -> No)");
        String c = reader.next();

        return c.equals("S") || c.equals("s");
    }

    public static File getFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        return file;
    }

    public static void printToScreen(String string) {
        synchronized (System.out) {
            System.out.println(string);
        }
    }

    public static File[] dirFirst(File[] dir) {
        for (int i = 0, j = dir.length - 1; i < j; ++i) {
            if (!dir[i].isDirectory()) {
                for (; i < j; --j) {
                    if (dir[j].isDirectory()) {
                        File tmpFile = dir[j];
                        dir[j] = dir[i];
                        dir[i] = tmpFile;
                        break;
                    }
                }
            }
        }

        return dir;
    }

    public static String rmSep(String str) {
        if (str.length() == 0) return "";

        int i = 0, j = str.length();
        if (str.charAt(i) == '/' ||
                str.charAt(i) == '\"') ++i;
        if (str.charAt(j - 1) == '/' ||
                str.charAt(j - 1) == '\"') --j;

        return str.substring(i, j);
    }

    public static ArrayList<File> dropFiles(ArrayList<File> dir) {
        int len = dir.size();
        for (int i = 0; i < len; ++i)
            if (!dir.get(i).isDirectory()) dir.remove(i);

        return dir;
    }

    public static void printFiles(Collection<File> fileCollection) {
        FileUtility.printToScreen("\n");
        for (File f : fileCollection)
            System.out.println("file: " + f);
    }

    public static String getNewDir(String currentPath) {
        String w[] = null;

        if (Constants.osCurrent.equals(Constants.osLinux))
            w = currentPath.split("/");
        else if (Constants.osCurrent.equals(Constants.osWindows))
            w = currentPath.split("\"");

        if (w != null)
            return w[w.length - 1];

        throw new RuntimeException(Constants.osDetectFailed);
    }

    public static char getOSSeparator() {
        switch (Constants.osCurrent) {
            case "Linux":
                return '/';
            case "Windows 10":
                return '\'';
            default:
                return ' ';
        }
    }

    public static void printFiles(File[] files) {
        for (File f : files)
            System.out.println("file: " + f);
    }

    public boolean canRead(File file) throws RuntimeException{
        if (!file.canRead()) {
            throw new RuntimeException(String.format(
                    Constants.readDenied, file.getPath()
            ));
        }

        return true;
    }

    public boolean canWrite(File file) throws RuntimeException{
        if (!file.canWrite()) {
            throw new RuntimeException(String.format(
                    Constants.writeDenied, file.getParent()
            ));
        }

        return true;
    }

    public void strcturalCorr(File source, File destination) {
        Collection<File> filesDir = FileUtils.listFilesAndDirs(
                source, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE
        );
        FileUtility.printToScreen("\nDirectory Sorgente:\r");
        for (File f : filesDir)
            FileUtility.printToScreen(f.getAbsolutePath());

        filesDir = FileUtils.listFilesAndDirs(
                destination, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE
        );
        FileUtility.printToScreen("\n\nDirectory di Destinazione:\r");
        for (File f : filesDir)
            FileUtility.printToScreen(f.getAbsolutePath());
    }
}
