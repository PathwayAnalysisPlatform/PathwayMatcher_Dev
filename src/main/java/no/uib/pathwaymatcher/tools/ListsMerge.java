package no.uib.pathwaymatcher.tools;

import java.io.*;
import java.util.TreeSet;

public class ListsMerge {

    /**
     * The program receives a list of files to combine into one sorted file. The duplicate rows are discarded.
     *
     * @param args The first argument is the name of the output file. The rest of the arguments ins the name of the files
     *             to merge. At least it requires three arguments to work. Otherwise there is nothing to merge.
     */
    public static void main(String args[]) {
        if (args.length <= 2) {
            return;
        }

        try {
            FileWriter output = new FileWriter(args[0]);

            TreeSet<String> peptideSet = new TreeSet<>();

            for (int I = 0; I < args.length - 1; I++) {
                try {
                    FileReader fileReader = new FileReader(args[I + 1]);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        peptideSet.add(line);
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("It was not possibe to read from: " + args[I + 1]);
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("Error reading from: " + args[I + 1]);
                    e.printStackTrace();
                }
            }

            for (String peptide : peptideSet) {     //Send all non-reduntant peptides to the output
                try {
                    output.write(peptide + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            output.close();

        } catch (IOException e) {
            System.out.println("It was not possible to write to the specified output file: " + args[0]);
        }


    }

}
