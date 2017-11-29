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
package no.uib.pathwaymatcher.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class ColumnExtractor {

    // Run this file: java -cp PathwayMatcher-1.0/PathwayMatcher-1.0.jar no.uib.pathwaymatcher.tools.ColumnExtractor ../../ERC/zBMI3-autosome.result
    // Parameters to run snpList in Netbeans: -i ../ERC/zBMI3-autosome.result
    public static void main(String args[]) {

        Options options = new Options();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        Option inputOp = new Option("i", "input", true, "File to parse");
        inputOp.setRequired(true);
        options.addOption(inputOp);

        Option outputOp = new Option("o", "output", true, "Output file path and name");
        outputOp.setRequired(false);
        options.addOption(outputOp);

        Option columnOp = new Option("c", "column", true, "Index of the desired column. Count starting with 0,1,2...");
        columnOp.setRequired(false);
        options.addOption(columnOp);

        Option separatorOp = new Option("s", "separator", true, "The string used as field separator. For example: \"\\t\", \" \", \",\"");
        separatorOp.setRequired(false);
        options.addOption(separatorOp);

        Option patternOp = new Option("p", "pattern", true, "The regular expression pattern that the rows must follow");
        patternOp.setRequired(false);
        options.addOption(patternOp);

        Option pValueColumnOp = new Option("pvc", "pValueColumn", true, "p-value column index. Count starting with 0,1,2...");
        pValueColumnOp.setRequired(false);
        options.addOption(pValueColumnOp);

        Option pValueThresholdOp = new Option("pvt", "pValueThreshold", true, "p-value threshold. Only rows with this value or more will be sent to the output");
        pValueThresholdOp.setRequired(false);
        options.addOption(pValueThresholdOp);

        //Verify command line options
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
            return;
        }

        //Set all default values
        String input = "./input.csv";
        String output = "./output.csv";
        int column = 1;
        String separator = " ";
        String pattern = "^rs\\d*";
        int pvalueColumn = 20;
        double threshold = 0.001;
        LinkedHashSet<String> rsIdList = new LinkedHashSet<String>();

        // Set received values
        input = cmd.getOptionValue("input");
        if (cmd.hasOption("output")) {
            output = cmd.getOptionValue("output");
        }
        if (cmd.hasOption("column")) {
            column = Integer.valueOf(cmd.getOptionValue("column"));
        }
        if (cmd.hasOption("separator")) {
            switch (cmd.getOptionValue("separator")) {
                case "coma":
                case ",":
                    separator = ",";
                    break;
                case "/t":
                case "tab":
                    separator = "\t";
                    break;
                case " ":
                case "space":
                    separator = " ";
                    break;
            }
        }

        if (cmd.hasOption("pattern")) {
            pattern = cmd.getOptionValue("pattern");
        }

        if (cmd.hasOption("pValueColumn")) {
            if (!cmd.hasOption("pValueThreshold")) {
                System.out.println("If column for p-value is specified, then the threshold value must be given too.");
                formatter.printHelp("utility-name", options);
                System.exit(1);
            }
            pvalueColumn = Integer.valueOf(cmd.getOptionValue("pValueColumn"));
            threshold = Double.valueOf(cmd.getOptionValue("pValueThreshold"));
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(input));
            FileWriter fw = new FileWriter(output);

            String[] fields = br.readLine().split(separator); //Read the header row of the file
            for (String line; (line = br.readLine()) != null;) {//For each line of the file
                fields = line.split(separator);
                if (column >= fields.length) {
                    System.out.println("The column specified does not exist.");
                    System.exit(1);
                }
                if (fields[column].matches(pattern)) {
                    if (cmd.hasOption("pValueColumn") && cmd.hasOption("pValueThreshold")) {
                        if (!fields[pvalueColumn].matches("-?\\d+(\\.\\d+)?")) {
                            continue;
                        }
                        if (Double.valueOf(fields[pvalueColumn]) <= threshold) {
                            continue;
                        }
                    }
                    rsIdList.add(fields[column]);
                }
            }

            for (String rsId : rsIdList) {
                fw.write(rsId + "\n");
            }

            fw.close();
            br.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ColumnExtractor.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        } catch (IOException ex) {
            Logger.getLogger(ColumnExtractor.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }
}
