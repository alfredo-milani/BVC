import control.GetOperations;

import java.io.IOException;

/**
 * Created by alfredo on 17/03/17.
 */
public class Main {

    public static void main(String[] a) throws IOException{


        GetOperations getOperations = new GetOperations(a);
        getOperations.performOp();



/*
        ArrayList<String> s = new ArrayList<>();s.add("dio"); s.add("cane"); s.add("madonna");
        Iterator<String> f = s.iterator();


        System.out.println("len: " + s.size());
        while (f.hasNext()) {
            String g = f.next();
            if (g.length() <= 4)
                f.remove();
        }

        f = s.iterator();
        while (f.hasNext()) {
            System.out.println(f.next());
        }

        System.out.println("len: " + s.size());
        */


    }

}
