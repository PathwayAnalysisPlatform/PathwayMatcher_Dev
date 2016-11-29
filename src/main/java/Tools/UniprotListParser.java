package Tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class UniprotListParser {

    public static void main(String args[]) throws IOException {
        
        BufferedReader br = new BufferedReader(new FileReader("C:/Users/Optimus Franck/Documents/PhD UiB/Projects/MappingPhosporilation/uniprot-reviewed.list"));
        FileWriter resultFile = new FileWriter("listFileUniprot.csv");
        int cont = 0;
        String prev = "";
        
        for (String line; (line = br.readLine()) != null && cont < 20000000; cont++) {
            String[] parts = line.split("\t");
            if (parts[0].equals(prev)){
                //System.out.println(parts[0] + " es igual a " + prev);
                continue;
            }
            System.out.println(parts[0]);
            prev = parts[0];
            resultFile.write(parts[0] + "\n");
        }
        
        br.close();
        resultFile.close();
    }
}
