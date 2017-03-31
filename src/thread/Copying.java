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
    private final String tmpPath;

    public Copying(String path1, String path2, String path3) {
        this.sourcePath = path1;
        this.destinationPath = path2;
        this.tmpPath = path3;
    }

    @Override
    public void run() {
        if (this.sourcePath == null ||
                this.destinationPath == null) return;

        File[] sF; File[] dF;
        File fs = FileUtility.getFile(sourcePath);
        File fd = FileUtility.getFile(destinationPath);
        if (fs == null)
            throw new RuntimeException("Wrong path: " + sourcePath);
        else if (fd == null)
            throw new RuntimeException("Wrong path: " + destinationPath);
        sF = FileUtility.dirFirst(fs.listFiles());
        dF = FileUtility.dirFirst(fd.listFiles());

        ArrayList<File> sourceFiles = new ArrayList<>(Arrays.asList(sF));
        ArrayList<File> destinationFiles = new ArrayList<>(Arrays.asList(dF));

        Thread thread = new Thread(new Copying());

        for (File s : sourceFiles) {
            if (s.isDirectory()) {
                thread.start();
            } else if (s.isFile()) {
                boolean copyFile = true;
                for (File d : FileUtils.listFiles(
                        new File(this.destinationPath),
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
                                    FileUtils.moveFileToDirectory(d, new File(this.tmpPath), false);
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
                            new File(this.tmpPath),
                            false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
