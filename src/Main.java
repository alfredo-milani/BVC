import control.GetOperations;

import java.io.IOException;

/**
 * Created by alfredo on 17/03/17.
 */
public class Main {

    public static void main(String[] a) throws IOException{

        GetOperations getOperations = new GetOperations(a);
        getOperations.performOp();

    }

}
