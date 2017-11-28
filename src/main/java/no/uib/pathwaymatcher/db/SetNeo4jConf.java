/*
 * Copyright 2017 Luis Francisco Hernández Sánchez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.uib.pathwaymatcher.db;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.pathwaymatcher.Conf;
import org.apache.commons.cli.Options;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class SetNeo4jConf {

    /**
     * 
     * @param args Receives the file name(including path), variable name and value 
     */
    public static void main(String args[]) {
        System.out.println("Modifying the configuration of Neo4j...");
        System.out.println("The number of arguments received is: " + args.length);
        System.out.print("[");
        for(String arg : args){
            System.out.print(arg + ", ");
        }
        System.out.println("]");
        if (args.length == 2) {
            // First: Read all lines to memory
            String fileName = args[0];
            String variable = args[1];
            try {
                //Read the file until it finds the desired variablenew FileReader("/var/lib/neo4j/conf/neo4j.conf")
                BufferedReader file = new BufferedReader(new FileReader(fileName));
                StringBuffer inputBuffer = new StringBuffer();
                String line;
                
                while ((line = file.readLine()) != null) {
                    if (line.contains(variable)) {
                        line = line.replace("#", "");
                        System.out.println("Line uncommented: " + line);
                    }
                    inputBuffer.append(line);
                    inputBuffer.append('\n');
                }
                String inputStr = inputBuffer.toString();
                file.close();

                // Second: Write all lines to the new file
                FileOutputStream fileOut = new FileOutputStream(fileName);
                fileOut.write(inputStr.getBytes());
                fileOut.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SetNeo4jConf.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(SetNeo4jConf.class.getName()).log(Level.SEVERE, null, ex);
            }
            ;
        }
    }
}
