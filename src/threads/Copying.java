package threads;

import org.apache.commons.io.FileUtils;
import utility.FileUtility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by alfredo on 31/03/17.
 */
public class Copying implements Runnable {

    private static int numberOfThreads = 0;

    private final String currentBranch;
    private final String sourcePath;
    private final String destinationPath;
    private final String tmpPath;
    private File destinationFile;
    private File sourceFile;

    public Copying(String path1, String path2,
                   String path3, String currentBranch) {
        Copying.countAndPrint('+');

        this.currentBranch = currentBranch;
        this.sourcePath = path1;
        this.destinationPath = path2;
        this.tmpPath = path3;
        this.destinationFile =
                FileUtility.getFile(this.destinationPath);
        this.sourceFile =
                FileUtility.getFile(this.sourcePath);
    }

    private synchronized static void countAndPrint(char operation) {
        switch (operation) {
            case '+':
                ++Copying.numberOfThreads;
                break;

            case '-':
                --Copying.numberOfThreads;
                break;
        }

        System.out.print(
                "\r " + Copying.numberOfThreads +
                        "  threads created"
        );
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
            boolean copyToDest = true;
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
                        this.tmpPath,
                        FileUtility.rmSep(this.currentBranch) +
                                FileUtility.getOSSeparator() +
                                newDir +
                                FileUtility.getOSSeparator())
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
                iteratorDF = destinationFiles.iterator();
                while (iteratorDF.hasNext()) {
                    File d = iteratorDF.next();
                    if (d.isDirectory()) continue;
                    if (s.getName().equals(d.getName())) {
                        boolean sameContent = false;
                        try {
                            sameContent = FileUtils.contentEquals(s, d);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (!sameContent) {
                            if (s.lastModified() > d.lastModified()) {
                                try {
                                    FileUtils.copyFileToDirectory(d,
                                            new File(this.tmpPath),
                                            true);
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
                        FileUtils.copyFileToDirectory(
                                s,
                                this.destinationFile,
                                true);
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
            Path path = Paths.get(FileUtility.getOSSeparator() +
                    FileUtility.rmSep(this.tmpPath) +
                    FileUtility.getOSSeparator() +
                    FileUtility.rmSep(this.currentBranch));
            if (!Files.exists(path)) {
                try {
                    Files.createDirectories(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (d.isFile()) {
                try {
                    FileUtils.moveFileToDirectory(
                            d, path.toFile(), false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (d.isDirectory()) {
                try {
                    FileUtils.moveToDirectory(
                            d, path.toFile(), false);
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

        Copying.countAndPrint('-');
    }

}