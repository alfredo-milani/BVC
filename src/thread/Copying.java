package thread;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import utility.FileUtility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by alfredo on 31/03/17.
 */
public class Copying implements Runnable {

    private final String sourcePath;
    private final String destinationPath;
    private final File tmpFile;

    public Copying(String path1, String path2, File file) {
        this.sourcePath = path1;
        this.destinationPath = path2;
        this.tmpFile = file;
    }

    @Override
    public void run() {
        if (this.sourcePath == null ||
                this.destinationPath == null) return;

        File[] sF; File[] dF;
        File fs = FileUtility.getFile(sourcePath);
        File fd = FileUtility.getFile(destinationPath);
        if (fs == null)
            throw new RuntimeException("Path errato: " + sourcePath);
        else if (fd == null) {
            fd = new File(destinationPath);
            if (!fd.mkdir())
                throw new RuntimeException("Impossibile creare la cartella: " + destinationPath);
        }
        sF = FileUtility.dirFirst(fs.listFiles());
        dF = FileUtility.dirFirst(fd.listFiles());

        ArrayList<File> sourceFiles = new ArrayList<>(Arrays.asList(sF));
        ArrayList<File> destinationFiles = new ArrayList<>(Arrays.asList(dF));

        Thread thread = null;

        /**
         * RISOLVI MODIFICHE CONCORRENTI
         */
        for (File s : sourceFiles) {
            if (s.isDirectory()) {
                String newSource = s.getAbsolutePath();
                String newDir = FileUtility.getNewDir(newSource);
                char sep = FileUtility.getOSSeparator();
                if (sep == ' ') {
                    FileUtility.printToScreen("OS non riconoscibile. File: " + s);
                    continue;
                }
                String newDestination = this.destinationPath + sep + newDir;

                thread = new Thread(new Copying(
                        newSource,
                        newDestination,
                        this.tmpFile)
                );
                thread.start();

                boolean delDir = false;
                for (File d : destinationFiles) {
                    if (!d.isDirectory()) break;
                    if (d.getName().equals(s.getName()))
                        sourceFiles.remove(s);
                }
            } else if (s.isFile()) {
                boolean copyFile = true;
                for (File d : FileUtils.listFiles(
                        FileUtility.getFile(this.destinationPath),
                        TrueFileFilter.INSTANCE,
                        FalseFileFilter.INSTANCE)) {
                    // Se nella cartella destinazione c'è un file
                    // con lo stesso nome della cartella sorgente
                    if (s.getName().equals(d.getName())) {
                        boolean sameContent = false;
                        try {
                            sameContent = FileUtils.contentEquals(s, d);
                        } catch (IOException e) {
                            e.printStackTrace();
                            FileUtility.printToScreen("Errore nell'analizzare il file: " + d);
                        }
                        if (!sameContent) {
                            if (s.lastModified() > d.lastModified()) {
                                try {
                                    FileUtils.moveFileToDirectory(d, this.tmpFile, false);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                copyFile = true;
                            } else {
                                // chiedere all'utente se aggiornare comunque il file oppure no
                                copyFile = false;
                                FileUtility.printToScreen("Il file \"" + d.getName() + "\" risulta essere stato modificato dopo del file \"" + s.getName() + "\"");
                            }
                        }
                        destinationFiles.remove(d);
                        break;
                    }
                }

                if (copyFile) {
                    try {
                        FileUtils.copyFileToDirectory(s, new File(this.destinationPath));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                FileUtility.printToScreen(String.format("Il file: %s ha un formato sconosciuto", s));
            }
        }

        for (File d : destinationFiles) {
            if (d.isFile())
                try {
                    FileUtils.moveFileToDirectory(
                            d,
                            this.tmpFile,
                            false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        sourceFiles = FileUtility.dropFiles(
                sourceFiles
        );
        for (File d : sourceFiles) {
            if (!d.isDirectory()) break;
            try {
                FileUtils.moveFileToDirectory(
                        d,
                        this.tmpFile,
                        true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
