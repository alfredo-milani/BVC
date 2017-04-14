package utility;

/**
 * Created by alfredo on 17/03/17.
 */
public interface Constants {
    /**
     * SINTASSI: "./BVC /path/../source [-param | -param /path/../destination]"
     */


    /**
     * STRINGS
     */
    String notImplemented = "Non ancora implementato";
    String badRequest = "Richiesta incoprensibile";
    String defaultMsg = "\n" +
            "***\t" +
            "Sintassi: \"./BVC /path/../source [-param | -param /path/../destination]\"" +
            "\t***" +
            "\n";
    String wrongPath = "Path: \"%s\" non corretto";
    String readDenied = "Accesso in lettura non consentito per il path: %s";
    String writeDenied = "Accesso in scrittura non consentito per il path: %s";
    String osDetectFailed = "Impossibile determinare il tipo di OS per la determinazione della sintassi dei path dei files";
    String confirm = "Sicuro di vole aggiornare il contenuto di \"%s\" con quello di \"%s\" ?";
    String missingOp = "Operazione mancante";



    /**
     * OS TYPE
     */
    String osLinux = "Linux";
    String osWindows = "Windows 10";
    String osCurrent = System.getProperty("os.name");


    /**
     * PATHS
     */
    String defaultTmpPathWindows = "R:\"DeletedFilesBVC_";
    String defautlTmpPathLinux = "/home/" +
            System.getProperty("user.name") +
            "/Scaricati/shm/DeletedFilesBVC_";
}
