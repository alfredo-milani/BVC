package control;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import utility.Constants;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;


/**
 * Created by alfredo on 17/03/17.
 */
public class GetOperations {

    private final String[] arg;
    private String[] ops;
    private File sourceFile;
    private File destinationFile;
    private String absoluteSourcePath;
    private String absoluteDestinationPath;
    private String relativeCurrentSorcePath;
    private String relativeCurrentDestinationPath;
    private String pathTmp;

    public GetOperations(String[] arg) {
        this.arg = arg;
        this.ops = new String[10]; // 5 argomenti
        this.ops = this.getOpType();
        this.sourceFile = checker(this.ops[1]);
        this.destinationFile = checker(this.ops[3]);
        this.absoluteSourcePath =
                this.sourceFile.getAbsolutePath();
        this.absoluteDestinationPath =
                this.destinationFile.getAbsolutePath();
        this.relativeCurrentSorcePath =
                this.getRootDir(this.absoluteSourcePath);
        this.relativeCurrentDestinationPath =
                this.getRootDir(this.absoluteDestinationPath);
        this.pathTmp = this.getDefaultTmpPath();
    }


    /**
     * Operazioni possibili descritte dalla classe Operations (enum)
     *
     * @return array di stringhe del tipo: [tipoOperazione (enum); valore (path, o simili)]
     */
    private String[] getOpType() {
        if (this.arg.length == 0)
            throw new RuntimeException(Constants.defaultMsg);

        int lenArg = this.arg.length;
        String k = Operations.Destionation.toString();
        Operations l = Enum.valueOf(Operations.class, "Update");
        if (l == Operations.Update)
            System.out.println("prova enum");

        this.ops[0] = Operations.Source.toString();
        this.ops[1] = this.arg[0];
        for (int i = 1, j = 2; i <= lenArg - 1; ++i) {
            if (this.arg[i].equals("-u")) {
                this.ops[j] = Operations.Update.toString();
                this.ops[++j] = this.arg[++i];
            } else if (this.arg[i].equals("-diff")) {
                this.ops[j] = Operations.Differences.toString();
                this.ops[++j] = this.arg[++i];
            }
        }

        return this.ops;
    }

    private String getDefaultTmpPath() {
        if (Constants.osCurrent.equals(Constants.osLinux))
            return Constants.defautlTmpPathLinux;
        else if (Constants.osCurrent.equals(Constants.osWindows))
            return Constants.defaultTmpPathWindows;

        return null;
    }

    private String getRootDir(String path) {
        String w[] = null;

        if (Constants.osCurrent.equals(Constants.osLinux))
            w = path.split("/");
        else if (Constants.osCurrent.equals(Constants.osWindows))
            w = path.split("\"");

        if (w != null)
            return w[w.length - 1];

        throw new RuntimeException(Constants.osDetectFailed);
    }

    public void performOp() {
        if (this.ops.length == 0)
            throw new RuntimeException("Operazioni non definite!");

        // consideriamo una sola funzione per ora:
        // quella di aggiornare la destinazione
        File[] sourceFile = this.sourceFile.listFiles();
        File[] destinationFile = this.destinationFile.listFiles();

        System.out.println("source; " + this.sourceFile);
        Collection<File> filesS = FileUtils.listFiles(this.sourceFile, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        Collection<File> fileS2 = FileUtils.listFilesAndDirs(this.sourceFile, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        for (File f : filesS)
            System.out.println("S1: " + f);
        for (File f2 : fileS2) {
            System.out.println("S2: " + f2);
            if (!f2.isDirectory())
                fileS2.remove(f2);
        }

        for (File s : sourceFile) {
            for (File d : destinationFile) {
                System.out.println("s: " + s + "\td: " + d);

                if (!s.isDirectory() && !d.isDirectory()) {
                    try {

                        // boolean e = FileUtils.contentEquals(s, d);
                        boolean e = FileUtils.contentEquals(new File("/home/alfredo/Scaricati/shm/prova1/dir1/file1"),
                                new File("/home/alfredo/Scaricati/shm/prova2/dir1/file1"));
                        if (e)
                            System.out.println("madonna");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }

                if (s.compareTo(d) == 0) {
                    Date dateSource = new Date(s.lastModified());
                    Date dateDestination = new Date(d.lastModified());

                    DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
                    String dateFormattedSource = formatter.format(dateSource);
                    String dateFormattedDestination = formatter.format(dateDestination);

                    System.out.println("s: " + dateFormattedSource);
                    System.out.println("s: " + dateFormattedDestination);
                }
            }
        }
    }

    private File checker(String path) {
        File file = new File(path);
        if (!file.exists()) {
            throw new RuntimeException(String.format(
                    Constants.wrongPath, path
            ));
        }

        return file;
    }

    private boolean canRead(File file) throws RuntimeException{
        if (!file.canRead()) {
            throw new RuntimeException(String.format(
                    Constants.readDenied, file.getPath()
            ));
        }

        return true;
    }

    private boolean canWrite(File file) throws RuntimeException{
        if (!file.canWrite()) {
            throw new RuntimeException(String.format(
                    Constants.writeDenied, file.getParent()
            ));
        }

        return true;
    }

}
