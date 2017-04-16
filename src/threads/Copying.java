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

    private static int numberOfThreads = 0;

    private final String currentBranch;
    private final String sourcePath;
    private final String destinationPath;
    private final String tmpPath;
    private File destinationFile;
    private File sourceFile;

    public Copying(String path1, String path2,
                   String path3, String currentBranch) {
        Copying.countAndPrint('a');

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
            case 'a':
                ++Copying.numberOfThreads;
                break;

            case 'd':
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
        Iterator<File> iteratorSF;
        // destinationFiles con il metodo sotto conterrà tutti e soli i files regolari
        // destinationFiles.removeIf(File::isDirectory);

        ArrayList<Thread> threads = new ArrayList<>();

        iteratorSF = sourceFiles.iterator();
        while (iteratorSF.hasNext()){
            File s = iteratorSF.next();
            if (s.isDirectory()) {
                // TODO: rendi creazione threads indipendente dall'operazione
                String newSource = s.getAbsolutePath();
                String newDir = FileUtility.getNewDir(newSource);
                char sep = FileUtility.getOSSeparator();
                if (sep == ' ') {
                    FileUtility.printToScreen("OS non riconoscibile. File: " + s);
                    continue;
                }
                String newDestination = this.destinationPath + sep + newDir;

                iteratorDF = destinationFiles.iterator();
                while (iteratorDF.hasNext()) {
                    File d = iteratorDF.next();
                    if (!d.isDirectory()) break;
                    if (d.getName().equals(s.getName())) {
                        iteratorSF.remove();
                        iteratorDF.remove();
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
            File tmpFile = new File(FileUtility.getOSSeparator() +
                    FileUtility.rmSep(this.tmpPath) +
                    FileUtility.getOSSeparator() +
                    FileUtility.rmSep(this.currentBranch));
            if (!tmpFile.exists()) {
                if (!tmpFile.mkdir())
                    FileUtility.printToScreen("Errore nel creare la cartella dove spostare i files obsoleti" +
                            "\tpath: " + tmpFile);
            }
            if (!d.exists()) {
                if (!d.mkdir())
                    FileUtility.printToScreen("Errore nel creare la cartella dove spostare i files obsoleti" +
                            "\tpath: " + d);
            }

            if (d.isFile()) {
                try {
                    FileUtils.moveFileToDirectory(
                            d, tmpFile, false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (d.isDirectory()) {
                try {
                    FileUtils.moveToDirectory(
                            d, tmpFile, false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        for (File s : sourceFiles) {
            if (s.isDirectory()) {
                try {
                    FileUtils.copyDirectoryToDirectory(
                            s,
                            this.destinationFile);
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

        Copying.countAndPrint('d');
    }

}