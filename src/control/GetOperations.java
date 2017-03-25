package control;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import utility.Constants;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Scanner;

import static utility.Constants.confirm;


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
        try {
            this.absoluteSourcePath =
                    this.sourceFile.getCanonicalPath();
            this.absoluteDestinationPath =
                    this.destinationFile.getCanonicalPath();
        } catch (IOException e) {
            this.absoluteSourcePath =
                    this.sourceFile.getAbsolutePath();
            this.absoluteDestinationPath =
                    this.destinationFile.getAbsolutePath();
            e.printStackTrace();
        }
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
        String k = Operations.Destination.toString();
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

        /*
        if (!this.confirmOperation())
            return;
            */

        // consideriamo una sola funzione per ora:
        // quella di aggiornare la destinazione
        Collection<File> sourceFile = FileUtils.listFilesAndDirs(
                this.sourceFile, TrueFileFilter.INSTANCE, FalseFileFilter.INSTANCE
        );
        Collection<File> destinationFile = FileUtils.listFilesAndDirs(
                this.sourceFile, TrueFileFilter.INSTANCE, FalseFileFilter.INSTANCE
        );

        this.printToScreen(this.absoluteSourcePath);
        this.printFiles(sourceFile);
        sourceFile.remove(
          new File(this.relativeCurrentSorcePath)
        );
        destinationFile.remove(
          new File(this.relativeCurrentDestinationPath)
        );
        this.printFiles(sourceFile);


        // collection.stream().anyMatch(x -> x == f);


        for (File s : sourceFile) {
            for (File d : destinationFile) {
                //System.out.println("s: " + s + "\td: " + d);

                if (!s.isDirectory() && !d.isDirectory()) {
                    try {

                        // boolean e = FileUtils.contentEquals(s, d);
                        boolean e = FileUtils.contentEquals(new File("/home/alfredo/Scaricati/shm/prova1/dir1/file1"),
                                new File("/home/alfredo/Scaricati/shm/prova2/dir1/file1"));
                        //if (e)
                            //System.out.println("madonna");
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

                    /*
                    System.out.println("s: " + dateFormattedSource);
                    System.out.println("s: " + dateFormattedDestination);
                    */
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

    private void strcturalCorr(File source, File destination) {
        Collection<File> filesDir = FileUtils.listFilesAndDirs(
                source, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE
        );
        this.printToScreen("\nDirectory Sorgente:\r");
        for (File f : filesDir)
            this.printToScreen(f.getAbsolutePath());

        filesDir = FileUtils.listFilesAndDirs(
                destination, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE
        );
        this.printToScreen("\n\nDirectory di Destinazione:\r");
        for (File f : filesDir)
            this.printToScreen(f.getAbsolutePath());
    }

    private boolean confirmOperation() {
        Scanner reader = new Scanner(System.in);

        this.printToScreen(String.format(confirm, this.absoluteDestinationPath, this.absoluteSourcePath));
        this.printToScreen("(S/s -> Sì  -  N/n -> No)");
        String c = reader.next();

        return c.equals("S") || c.equals("s");
    }

    private void printToScreen(String string) {
        System.out.println(string);
    }

    private void printFiles(Collection<File> fileCollection) {
        this.printToScreen("\n");
        for (File f : fileCollection)
            System.out.println("file: " + f);
    }

    private void printFiles(File[] files) {
        for (File f : files)
            System.out.println("file: " + f);
    }

}
