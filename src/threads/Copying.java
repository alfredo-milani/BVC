package threads;

import org.apache.commons.io.FileUtils;
import utility.FileUtility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by alfredo on 31/03/17.
 */
public class Copying implements Runnable {

    private final String sourcePath;
    private final String destinationPath;
    private File destinationFile;
    private File sourceFile;
    private final File tmpFile;

    public Copying(String path1, String path2, File file) {
        this.sourcePath = path1;
        this.destinationPath = path2;
        this.tmpFile = file;
        this.destinationFile =
                FileUtility.getFile(this.destinationPath);
        this.sourceFile =
                FileUtility.getFile(this.sourcePath);
    }

    @Override
    public void run() {
        File[] sF; File[] dF;
        if (this.sourceFile == null) throw new RuntimeException("Path errato: " + sourcePath);
        else if (this.destinationFile == null) {
            this.destinationFile = new File(destinationPath);
            if (!this.destinationFile.mkdir())
                throw new RuntimeException("Impossibile creare la cartella: " + destinationPath);
        }
        sF = FileUtility.dirFirst(this.sourceFile.listFiles());
        dF = FileUtility.dirFirst(this.destinationFile.listFiles());

        ArrayList<File> sourceFiles = new ArrayList<>(Arrays.asList(sF));
        ArrayList<File> destinationFiles = new ArrayList<>(Arrays.asList(dF));
        // NOTA: aggiungere elementi nella lista, dopo aver inizializzato
        //       l'iterator prova il lancio di una eccezione
        Iterator<File> iteratorDF;
        // destinationFiles con il metodo sotto conterrà tutti e soli i files regolari
        // destinationFiles.removeIf(File::isDirectory);

        ArrayList<Thread> threads = new ArrayList<>();

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


                Thread thread = new Thread(new Copying(
                        newSource,
                        newDestination,
                        this.tmpFile)
                );
                threads.add(thread);
                thread.start();

                iteratorDF = destinationFiles.iterator();
                while (iteratorDF.hasNext()) {
                    File d = iteratorDF.next();
                    if (!d.isDirectory()) break;
                    if (d.getName().equals(s.getName())) {
                        iteratorDF.remove();
                        break;
                    }
                }
            } else if (s.isFile()) {
                // Se nessuno dei nomi di files in source ha un matching
                //  nella directory destination, il file viene copiato
                boolean copyToDest = true;
                iteratorDF = destinationFiles.iterator();
                while (iteratorDF.hasNext()) {
                    File d = iteratorDF.next();
                    if (s.getName().equals(this.destinationFile.getName())) {
                        boolean sameContent = false;
                        try {
                            sameContent = FileUtils.contentEquals(s, d);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (!sameContent) {
                            if (s.lastModified() > d.lastModified()) {
                                try {
                                    FileUtils.moveFileToDirectory(
                                            d,
                                            this.tmpFile,
                                            false);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                iteratorDF.remove();
                                break;
                            } else {
                                System.out.println(String.format("File '%s' modificato: %d\n" +
                                        "File '%s' modificato: %d\n\n" +
                                        "Feature da completare", s.getName(), s.lastModified(),
                                        d.getName(), d.lastModified()));
                            }
                        }
                        copyToDest = false;
                        iteratorDF.remove();
                        break;
                    }
                }

                if (copyToDest) {
                    try {
                        FileUtils.moveFileToDirectory(
                                s,
                                this.destinationFile,
                                false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                FileUtility.printToScreen(String.format("Il file: %s ha un formato sconosciuto", s));
            }
        }

        // COPIA FILES ELIMINATI MANTENENDO LA GERARCHIA DI DIRECTORY
        //
        // Files e cartelle rimanste nella lista destinationFiles sono
        //  sono spostati nella directory temporanea per essere eliminati
        // Fino a quando non confronto tutti i files contenuti in source,
        //  non posso spostare i file in destination altrimenti rischierei
        //  di copiare inutilmente un file
        for (File d : destinationFiles) {
            if (d.isFile()) {
                try {
                    FileUtils.moveFileToDirectory(
                            d,
                            this.tmpFile,
                            false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (d.isDirectory()) {
                try {
                    FileUtils.moveToDirectory(
                            d,
                            this.tmpFile,
                            false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        for (Thread thread : threads) {
            if (thread != null) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
