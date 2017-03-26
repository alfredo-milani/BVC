import control.GetOperations;

/**
 * Created by alfredo on 17/03/17.
 */
public class Main {

    public static void main(String[] a) {

        GetOperations getOperations = new GetOperations(a);
        getOperations.performOp();

    }


    private static void print(String msg) {
        System.out.println(msg);
    }

    private static void printArray(String[] a) {
        int i = 0;
        for (String s : a) {
            print("Elemento[" + i + "]: " + s);
            ++i;
        }
    }

}
