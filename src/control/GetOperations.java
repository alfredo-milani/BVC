package control;

import threads.Copying;
import utility.Constants;
import utility.FileUtility;

import java.io.File;
import java.io.IOException;


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
        switch (this.arg.length) {
            case 0:
                throw new RuntimeException(Constants.defaultMsg);

            case 1:
                throw new RuntimeException(Constants.notImplemented);

            case 2:
                throw new RuntimeException(Constants.notImplemented);

            case 3:
                this.ops[0] = PathType.Source.toString();
                this.ops[1] = this.arg[0];
                if ((this.sourceFile = FileUtility.getFile(this.ops[1])) == null)
                    throw new RuntimeException(
                            String.format(Constants.wrongPath, this.ops[1])
                    );
                boolean operation = false;
                for (int i = 1, j = 2; i <= this.arg.length - 1; ++i) {
                    if (this.arg[i].equals("-u")) {
                        this.ops[j] = Operations.Update.toString();
                        this.ops[++j] = this.arg[++i];
                        operation = true;
                    } else if (this.arg[i].equals("-diff")) {
                        this.ops[j] = Operations.Differences.toString();
                        this.ops[++j] = this.arg[++i];
                        operation = true;
                    }
                }
                if (!operation)
                    throw new RuntimeException(Constants.missingOp);
                else if ((this.destinationFile = FileUtility.getFile(this.ops[3])) == null)
                    throw new RuntimeException(
                            String.format(Constants.wrongPath, this.ops[3])
                    );
                break;

            case 4:
                throw new RuntimeException(Constants.notImplemented);

            default:
                throw new RuntimeException(Constants.badRequest);
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
            FileUtility.printToScreen("Errore nel creare la cartella dove spostare i files obsoleti");

        return tmpFile;
    }

    public void performOp() {
        if (this.ops.length == 0)
            throw new RuntimeException("Operazioni non definite!");

        /*
        if (!FileUtility.confirmOperation(this.absoluteDestinationPath, this.absoluteSourcePath))
            return;
            */

        Runnable runnableCopy = new Copying(
                this.absoluteSourcePath,
                this.absoluteDestinationPath,
                this.tmpFile
        );
        Thread copyingThread = new Thread(runnableCopy);
        copyingThread.start();

        try {
            copyingThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        FileUtility.printToScreen("\n\nOperazione completata con successo");


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

}
