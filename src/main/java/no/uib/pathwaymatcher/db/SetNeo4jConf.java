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

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
class SetNeo4jConf {

    /**
     * Uncomments variables in the configuration file of neo4j
     * The variables are specified
     * @param args Receives the file name(including path), variable name and value 
     */
    public static void main(String args[]) {

        System.out.println("The number of arguments received is: " + args.length);
        if(args.length < 2){
            System.out.println("Missing some arguments: <fileName> <variable> <value>");
        }

        if(args.length == 2){
            System.out.println("Modifying the configuration of Neo4j...");
            uncommentVariable(args[0], args[1]);
        }
    }

    public static void commentVariable(String fileName, String variable){
        try {
            //Read the file until it finds the desired variable
            StringBuffer inputBuffer = new StringBuffer();
            LineIterator it = FileUtils.lineIterator(new File(fileName), "UTF-8");

            while (it.hasNext()) {
                String line = it.nextLine();
                if (line.startsWith(variable)) {
                    System.out.println("Line commented: " + line);
                    inputBuffer.append("#");
                }
                inputBuffer.append(line);
                inputBuffer.append('\n');
            }
            String inputStr = inputBuffer.toString();

            LineIterator.closeQuietly(it);

            // Second: Write all lines to the new file
            FileOutputStream fileOut = new FileOutputStream(fileName);
            fileOut.write(inputStr.getBytes());
            fileOut.close();

        } catch (IOException ex) {
            Logger.getLogger(SetNeo4jConf.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
    Common locations: /etc/neo4j/neo4j.conf or /var/lib/neo4j/conf/neo4j.conf
     */
    public static void uncommentVariable(String fileName, String variable){
        try {
            //Read the file until it finds the desired variable
            StringBuffer inputBuffer = new StringBuffer();
            LineIterator it = FileUtils.lineIterator(new File(fileName), "UTF-8");

            while (it.hasNext()) {
                String line = it.nextLine();
                if (line.contains(variable)) {
                    line = line.replace("#", "");
                    System.out.println("Line uncommented: " + line);
                }
                inputBuffer.append(line);
                inputBuffer.append('\n');
            }
            String inputStr = inputBuffer.toString();

            LineIterator.closeQuietly(it);

            // Second: Write all lines to the new file
            FileOutputStream fileOut = new FileOutputStream(fileName);
            fileOut.write(inputStr.getBytes());
            fileOut.close();

        } catch (IOException ex) {
            Logger.getLogger(SetNeo4jConf.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
