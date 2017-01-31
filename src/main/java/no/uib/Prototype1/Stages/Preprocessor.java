package no.UiB.Prototype1.Stages;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.UiB.Prototype1.Configuration;
import static no.UiB.Prototype1.Prototype1.println;
import static no.UiB.Prototype1.Prototype1.uniprotSet;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Preprocessor {

    private static FileWriter output;

    /* This Class should transform any type of file to the standard format of representing the Modified Proteins. */
    public static void standarizeFile() {

        Configuration.InputType t = Configuration.InputType.unknown;
        try {
            println("Detecting input type...");
            t = detectInputType();
            println("Input type detected: " + t.toString());
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }

        Boolean parseResult = false;
        try {
            output = new FileWriter(Configuration.standarizedFile);
        } catch (IOException ex) {
            System.out.println("The output file standarized has a problem.");
            Logger.getLogger(Preprocessor.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            switch (t) {
                case maxQuantMatrix:
                    parseResult = parseFormat_maxQuantMatrix();
                    break;
                case peptideList:
                    parseResult = parseFormat_peptideList();
                    break;
                case peptideListAndSites:
                    parseResult = parseFormat_peptideListAndSites();
                    break;
                case peptideListAndModSites:
                    parseResult = parseFormat_peptideListAndModSites();
                    break;
                case uniprotList:
                    parseResult = parseFormat_uniprotList();
                    break;
                case uniprotListAndSites:
                    parseResult = parseFormat_uniprotListAndSites();
                    break;
                case uniprotListAndModSites:
                    parseResult = parseFormat_uniprotListAndModSites();
                    break;
            }
        } catch (java.text.ParseException e) {

        } catch (IOException e) {

        }
        try {
            output.close();
        } catch (IOException ex) {
            if (Configuration.verboseConsole) {
                System.out.println("\nThe output file standarized has a problem.");
            }
            Logger.getLogger(Preprocessor.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (parseResult) {
            System.out.println("\nFile parsed correctly!");
        } else {
            System.out.println("\nThe format of the file is incorrect.");
        }

    }

    //Detect the type of input
    public static Configuration.InputType detectInputType() throws FileNotFoundException, IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(Configuration.inputListFile));
            String firstLine = reader.readLine();
            if (firstLine.trim().startsWith("Protein")) {
                return Configuration.InputType.maxQuantMatrix;
            } else if (firstLine.matches("^[ARNDBCEQZGHILKMFPSTWYV]+$")) {
                return Configuration.InputType.peptideList;
            } else if (firstLine.matches("^[ARNDBCEQZGHILKMFPSTWYV]+,(\\d+;)*\\d*$")) {
                return Configuration.InputType.peptideListAndSites;
            } else if (firstLine.matches("^[ARNDBCEQZGHILKMFPSTWYV]+,(\\d{5}:\\d+;)*(\\d{5}:\\d+)?$")) {
                return Configuration.InputType.peptideListAndModSites;
            } else if (firstLine.matches("^\\w\\d{5}$")) {
                return Configuration.InputType.uniprotList;
            } else if (firstLine.matches("^\\w\\d{5},(\\d+;)*\\d*$")) {
                return Configuration.InputType.uniprotListAndSites;
            } else if (firstLine.matches("^\\w\\d{5},(\\d{5}:\\d+;)*\\d{5}:\\d*$")) {
                return Configuration.InputType.peptideListAndModSites;
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        return Configuration.InputType.unknown;
    }

    /**
     * ********** The parser methods transform the original input file to the
     * standard Modified proteins standard file. **************
     */
    //If all the file was converted properly they return true.
    //If the file does not follow the format they return false. 
    public static Boolean parseFormat_maxQuantMatrix() throws java.text.ParseException, IOException {
        Boolean parsedCorrectly = true;
        BufferedReader reader = null;

        try {
            int row = 1;
            reader = new BufferedReader(new FileReader(Configuration.inputListFile));
            String line = reader.readLine();        //Read header line; the first row of the file

            while ((line = reader.readLine()) != null) {
                row++;
                try {
                    if (line.matches("^(\\w{6}(-\\d+)?;)*\\w{6}(-\\d+)?\\t(\\d+;)*\\d+\\t.*$")) {
                        printMaxQuantLine(line);            //Process line
                    } else {
                        if (Configuration.ignoreMisformatedRows) {
                            printMaxQuantLine(line);                            //Process line
                        }
                        throw new ParseException("Row " + row + " with wrong format", 0);
                    }
                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                    parsedCorrectly = false;
                }
            }
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
            }
        }
        return parsedCorrectly;
    }

    private static void printMaxQuantLine(String line) throws IOException {
        String[] sections = line.split("\t");
        String[] ids = sections[0].split(";");
        String[] sites = sections[1].split(";");

        for (int P = 0; P < ids.length; P++) {
            output.write(ids[P] + ",00000:" + sites[P] + "\n");
        }
    }

    public static Boolean parseFormat_peptideList() throws java.text.ParseException, IOException {
        //Note: In this function the duplicate protein identifiers are removed by adding the whole input list to a set.

        println("Loading peptide mapper...");
        compomics.utilities.PeptideMapping.initializePeptideMapper();
        println("Loading peptide mapper complete.");

        Boolean parsedCorrectly = true;
        BufferedReader reader = null;
        try {
            int row = 1;
            reader = new BufferedReader(new FileReader(Configuration.inputListFile));
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                row++;
                try {
                    if (line.matches("^[ARNDBCEQZGHILKMFPSTWYV]+$")) {
                        //Process line
                        for (String id : compomics.utilities.PeptideMapping.getPeptideMapping(line)) {
                            uniprotSet.add(id);
                        }
                    } else {
                        throw new ParseException("Row " + row + " with wrong format", 0);
                    }
                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                    parsedCorrectly = false;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
            }
        }

        //Print all uniprot ids to the standarized file
        for (String id : uniprotSet) {
            output.write(id + ",\n");
        }
        return parsedCorrectly;
    }

    public static Boolean parseFormat_peptideListAndSites() throws java.text.ParseException {
        //Note: In this function, the duplicate protein identifiers are NOT removed, since every row might show a protein with a different modified version.
        println("Loading peptide mapper...");
        compomics.utilities.PeptideMapping.initializePeptideMapper();
        println("Loading peptide mapper complete.");

        Boolean parsedCorrectly = true;
        BufferedReader reader = null;
        try {
            int row = 1;
            reader = new BufferedReader(new FileReader(Configuration.inputListFile));
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                row++;
                try {
                    if (line.matches("^[ARNDBCEQZGHILKMFPSTWYV]+,(\\d+;)*\\d*$")) {
                        //Process line
                        String[] parts = line.split(",");
                        String[] sites = parts[1].split(";");
                        for (String id : compomics.utilities.PeptideMapping.getPeptideMapping(parts[0])) {
                            output.write(id + ",");
                            for (int S = 0; S < sites.length; S++) {
                                if (S > 0) {
                                    output.write(";");
                                }
                                output.write("00000:" + sites[S]);
                            }
                            output.write("\n");
                        }
                    } else {
                        throw new ParseException("Row " + row + " with wrong format", 0);
                    }
                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                    parsedCorrectly = false;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
            }
        }

        return parsedCorrectly;
    }

    public static Boolean parseFormat_peptideListAndModSites() throws java.text.ParseException {
        //Note: In this function, the duplicate protein identifiers are NOT removed, since every row might show a protein with a different modified version.

        println("Loading peptide mapper...");
        compomics.utilities.PeptideMapping.initializePeptideMapper();
        println("Loading peptide mapper complete.");

        Boolean parsedCorrectly = true;
        BufferedReader reader = null;
        try {
            int row = 1;
            reader = new BufferedReader(new FileReader(Configuration.inputListFile));
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                row++;
                try {
                    if (line.matches("^[ARNDBCEQZGHILKMFPSTWYV]+,(\\d{5}:\\d+;)*(\\d{5}:\\d+)?$")) {
                        //Process line
                        String[] parts = line.split(",");
                        for (String id : compomics.utilities.PeptideMapping.getPeptideMapping(parts[0])) {
                            output.write(id + "," + parts[1] + "\n");
                        }
                    } else {
                        throw new ParseException("Row " + row + " with wrong format", 0);
                    }
                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                    parsedCorrectly = false;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        return parsedCorrectly;
    }

    public static Boolean parseFormat_uniprotList() throws java.text.ParseException {
        Boolean parsedCorrectly = true;
        BufferedReader reader = null;
        try {
            int row = 1;
            reader = new BufferedReader(new FileReader(Configuration.inputListFile));
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                row++;
                try {
                    if (line.matches("^(\\w{6}(-\\d+)?;)*\\w{6}(-\\d+)?\\t(\\d+;)*\\d+\\t.*$")) {
                        //Process line
                    } else {
                        throw new ParseException("Row " + row + " with wrong format", 0);
                    }
                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                    parsedCorrectly = false;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        return parsedCorrectly;
    }

    public static Boolean parseFormat_uniprotListAndSites() throws java.text.ParseException {
        Boolean parsedCorrectly = true;
        BufferedReader reader = null;
        try {
            int row = 1;
            reader = new BufferedReader(new FileReader(Configuration.inputListFile));
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                row++;
                try {
                    if (line.matches("^(\\w{6}(-\\d+)?;)*\\w{6}(-\\d+)?\\t(\\d+;)*\\d+\\t.*$")) {
                        //Process line
                    } else {
                        throw new ParseException("Row " + row + " with wrong format", 0);
                    }
                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                    parsedCorrectly = false;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        return parsedCorrectly;
    }

    public static Boolean parseFormat_uniprotListAndModSites() throws java.text.ParseException {
        Boolean parsedCorrectly = true;
        BufferedReader reader = null;
        try {
            int row = 1;
            reader = new BufferedReader(new FileReader(Configuration.inputListFile));
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                row++;
                try {
                    if (line.matches("^(\\w{6}(-\\d+)?;)*\\w{6}(-\\d+)?\\t(\\d+;)*\\d+\\t.*$")) {
                        //Process line
                    } else {
                        throw new ParseException("Row " + row + " with wrong format", 0);
                    }
                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                    parsedCorrectly = false;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        return parsedCorrectly;
    }
}
