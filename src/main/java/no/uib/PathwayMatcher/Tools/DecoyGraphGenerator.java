
package no.uib.PathwayMatcher.Tools;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Luis Francisco Hernández Sánchez
 */
public class DecoyGraphGenerator {
    public static void main(String[] args) throws IOException {
        //FileWriter output = new FileWriter("./src/main/resources/out/DecoyGraph.json");
        FileWriter output = new FileWriter("C:/Users/Francisco/Documents/PhD UiB/Projects/PathwayAdventure/public/resources/DecoyGraph.json");
        output.write("{\n\"nodes\": [\n");
        
        int numNodes = 10000;
        //Print the nodes here
        for(int I = 1; I <= numNodes; I++)
        {
            if(I > 1)
                output.write(",");
            output.write("{\"id\": \"Node" + I + "\", \"group\": " + I%10 + "}");
        }
        output.write("],\n  \"links\": [");
        //Print the edges here
        for(int I = 1; I <= numNodes; I++)
        {
            if(I > 1)
                output.write(",");
            output.write("{\"source\": \"Node" + I + "\", \"target\": \"Node" + ((int)(Math.random() * numNodes-1)+1) + "\", \"value\": " + 10 + "}");
        }
        output.write("]\n}\n");
        
        output.close();
    }
}
