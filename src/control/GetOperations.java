package control;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import utility.Constants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;


/**
 * Created by alfredo on 17/03/17.
 */
public class GetOperations {

    private final String[] arg;
    private String[] ops;
    private final File sourceFile;
    private final File destinationFile;
    private String absoluteSourcePath;
    private String absoluteDestinationPath;
    private final String relativeCurrentSourcePath;
    private final String relativeCurrentDestinationPath;
    private File relativeCurrentSourceFile;
    private File relativeCurrentDestinationFile;
    private File absoluteCurrentSourceFile;
    private File absoluteCurrentDestinationFile;
    private final String pathTmp;
    private final File tmpFile;

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
        this.relativeCurrentSourcePath =
                this.getRootDir(this.absoluteSourcePath);
        this.relativeCurrentDestinationPath =
                this.getRootDir(this.absoluteDestinationPath);
        this.relativeCurrentSourceFile = new File(this.relativeCurrentSourcePath);
        this.relativeCurrentDestinationFile = new File(this.relativeCurrentDestinationPath);
        this.absoluteCurrentSourceFile = new File(this.absoluteSourcePath);
        this.absoluteCurrentDestinationFile = new File(this.absoluteDestinationPath);
        this.pathTmp = this.getDefaultTmpPath();
        this.tmpFile = this.getTmpFile(this.pathTmp);
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
            return "./" + w[w.length - 1];

        throw new RuntimeException(Constants.osDetectFailed);
    }

    private File getTmpFile(String path) {
        int i = 0;
        File tmpFile;

        do {
            ++i;
            tmpFile = new File(path + i);
        } while (tmpFile.exists());

        if (!tmpFile.mkdir())
            this.printToScreen("Errore nel creare la cartella dove spostare i files obsoleti");

        return tmpFile;
    }

    public void performOp() {
        if (this.ops.length == 0)
            throw new RuntimeException("Operazioni non definite!");

        /*
        if (!this.confirmOperation())
            return;
            */

        try {
            this.copyingFiles();
        } catch (IOException e){
            e.printStackTrace();
            this.printToScreen("Errore IO");
        }

        // consideriamo una sola funzione per ora:
        // quella di aggiornare la destinazione


        /*
         * FileUtils.listFilesAndDir
         * true - true  --> file e dir in modo ricorsivo
         * false - true --> solo dir in modo ricorsivo
         * false - false --> n'cazzo
         * true - false  --> solo file non in modo ricorsivo
         *
         * FileUtils.listFiles
         * true - true  --> files in modo ricorsivo
         * true - false --> files non in modo ricorsivo
         * false - false --> n'cazzo
         * false - true  --> n'cazzo
         */



    }

    private void copyingFiles() throws IOException {
        File[] sF = this.sourceFile.listFiles();
        File[] dF = this.destinationFile.listFiles();
        if (sF == null || dF == null)    return;
        ArrayList<File> sourceFiles = new ArrayList<>(Arrays.asList(sF));
        ArrayList<File> destinationFiles = new ArrayList<>(Arrays.asList(dF));

        for (File s : sourceFiles) {
            if (s.isFile()) {
                boolean copyFile = true;
                for (File d : destinationFiles) {
                    // Se nella cartella destinazione c'è un file
                    // con lo stesso nome della cartella sorgente
                    if (s.getName().equals(d.getName())) {
                        if (!FileUtils.contentEquals(s, d)) {
                            if (s.lastModified() > d.lastModified()) {
                                FileUtils.moveFileToDirectory(d, this.tmpFile, false);
                                copyFile = true;
                            } else {
                                // chiedere all'utente se aggiornare comunque il file oppure no
                                copyFile = false;
                                this.printToScreen("Il file \"" + d.getName() + "\" risulta essere stato modificato dopo del file \"" + s.getName() + "\"");
                            }
                        }
                        destinationFiles.remove(d);
                        break;
                    }
                }

                if (copyFile) {
                    try {
                        FileUtils.copyFileToDirectory(s, this.absoluteCurrentDestinationFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (s.isDirectory()) {

            }
        }

        for (File d : destinationFiles) {
            if (d.isFile())
                FileUtils.moveFileToDirectory(d, this.tmpFile, false);
        }
    }

    private void opOnFiles() {

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

        this.printToScreen(String.format(Constants.confirm, this.absoluteDestinationPath, this.absoluteSourcePath));
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
