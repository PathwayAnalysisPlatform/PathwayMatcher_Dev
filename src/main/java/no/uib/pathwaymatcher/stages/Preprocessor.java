package no.uib.pathwaymatcher.stages;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.Conf.InputPatterns;
import static no.uib.pathwaymatcher.Conf.*;
import static no.uib.pathwaymatcher.Conf.strMap;
import no.uib.pathwaymatcher.Conf.StrVars;
import no.uib.pathwaymatcher.PathwayMatcher;
import static no.uib.pathwaymatcher.PathwayMatcher.println;
import static no.uib.pathwaymatcher.PathwayMatcher.uniprotSet;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Preprocessor {

    private static FileWriter output;

    /* This Class should transform any type of file to the standard format of representing the Modified Proteins. */
    public static void standarizeFile() {

        Boolean parseResult = false;
        try {
            output = new FileWriter(Conf.strMap.get(Conf.StrVars.standardFilePath));
        } catch (IOException ex) {
            System.out.println("The output file standarized has a problem.");
            Logger.getLogger(Preprocessor.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }

        try {
            switch (strMap.get(StrVars.inputType)) {
                case InputType.maxQuantMatrix:
                    parseResult = parseFormat_maxQuantMatrix();
                    break;
                case InputType.peptideList:
                    parseResult = parseFormat_peptideList();
                    break;
                case InputType.peptideListAndSites:
                    parseResult = parseFormat_peptideListAndSites();
                    break;
                case InputType.peptideListAndModSites:
                    parseResult = parseFormat_peptideListAndModSites();
                    break;
                case InputType.uniprotList:
                    parseResult = parseFormat_uniprotList();
                    break;
                case InputType.uniprotListAndSites:
                    parseResult = parseFormat_uniprotListAndSites();
                    break;
                case InputType.uniprotListAndModSites:
                    parseResult = parseFormat_uniprotListAndModSites();
                    break;
                case InputType.rsidList:
                    parseResult = parseFormat_snpList();
            }
        } catch (java.text.ParseException e) {

        } catch (IOException e) {

        }
        try {
            output.close();
        } catch (IOException ex) {
            if (Conf.boolMap.get(Conf.BoolVars.verbose)) {
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
    public static String detectInputType() throws FileNotFoundException, IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(Conf.strMap.get(Conf.StrVars.input)));
            String firstLine = reader.readLine();
            if (firstLine.trim().startsWith(InputPatterns.maxQuantMatrix)) {
                setValue(Conf.BoolVars.inputHasPTMs, Boolean.TRUE);
                return InputType.maxQuantMatrix;
            } else if (firstLine.matches(InputPatterns.peptideList)) {
                return InputType.peptideList;
            } else if (firstLine.matches(InputPatterns.peptideListAndSites)) {
                setValue(Conf.BoolVars.inputHasPTMs, Boolean.TRUE);
                return InputType.peptideListAndSites;
            } else if (firstLine.matches(InputPatterns.peptideListAndModSites)) {
                setValue(Conf.BoolVars.inputHasPTMs, Boolean.TRUE);
                return InputType.peptideListAndModSites;
            } else if (firstLine.matches(InputPatterns.uniprotList)) {
                return InputType.uniprotList;
            } else if (firstLine.matches(InputPatterns.uniprotListAndSites)) {
                setValue(Conf.BoolVars.inputHasPTMs, Boolean.TRUE);
                return InputType.uniprotListAndSites;
            } else if (firstLine.matches(InputPatterns.uniprotListAndModSites)) {
                setValue(Conf.BoolVars.inputHasPTMs, Boolean.TRUE);
                return InputType.uniprotListAndModSites;
            } else if (firstLine.matches(InputPatterns.rsid)) {
                return InputType.rsidList;
            }
        } catch (FileNotFoundException ex) {
            System.out.println("The Input file specified was not found: " + Conf.strMap.get(Conf.StrVars.input));
            System.out.println("The starting location is: " + System.getProperty("user.dir"));
            //Logger.getLogger(PathwayMatcher.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        return InputType.unknown;
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
            reader = new BufferedReader(new FileReader(Conf.strMap.get(Conf.StrVars.input)));
            String line = reader.readLine();        //Read header line; the first row of the file

            while ((line = reader.readLine()) != null) {
                row++;
                try {
                    if (line.matches(InputPatterns.maxQuantMatrix)) {
                        printMaxQuantLine(line);            //Process line
                    } else {
                        if (Conf.boolMap.get(Conf.BoolVars.ignoreMisformatedRows)) {
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
            reader = new BufferedReader(new FileReader(Conf.strMap.get(Conf.StrVars.input)));
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                row++;
                try {
                    if (line.matches(InputPatterns.peptideList)) {
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

//        //Print all uniprot ids to the standarized file
//        for (String id : uniprotSet) {
//            output.write(id + ",\n");
//        }
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
            reader = new BufferedReader(new FileReader(Conf.strMap.get(Conf.StrVars.input)));
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                row++;
                try {
                    if (line.matches(InputPatterns.peptideListAndSites)) {
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
            reader = new BufferedReader(new FileReader(Conf.strMap.get(Conf.StrVars.input)));
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                row++;
                try {
                    if (line.matches(InputPatterns.peptideListAndModSites)) {
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
            reader = new BufferedReader(new FileReader(Conf.strMap.get(Conf.StrVars.input)));
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                row++;
                try {
                    if (line.matches(InputPatterns.uniprotList)) {
                        uniprotSet.add(line);
                        //output.write(line + ",\n"); //Process line
                    } else {
                        throw new ParseException("Row " + row + " with wrong format", 0);
                    }
                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                    parsedCorrectly = false;
                }
            }
            
            //        //Print all uniprot ids to the standarized file
    //        for (String id : uniprotSet) {
    //            output.write(id + ",\n");
    //        }
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
            reader = new BufferedReader(new FileReader(Conf.strMap.get(Conf.StrVars.input)));
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                row++;
                try {
                    if (line.matches(InputPatterns.uniprotListAndSites)) {
                        //Process line
                        String[] parts = line.split(",");
                        if (parts.length > 1) {
                            String[] sites = parts[1].split(";");
                            output.write(parts[0] + ",");
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
            reader = new BufferedReader(new FileReader(Conf.strMap.get(Conf.StrVars.input)));
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                row++;
                try {
                    if (line.matches(InputPatterns.uniprotListAndModSites)) {
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

    private static Boolean parseFormat_snpList() {
        Boolean parsedCorrectly = true;
        BufferedReader reader = null;
        try {
            int row = 1;
            reader = new BufferedReader(new FileReader(Conf.strMap.get(Conf.StrVars.input)));
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {    //Every line is a SNP
                row++;
                try {
                    if (line.matches(InputPatterns.rsid)) {
                        //Get the Gene name of the current SNP
                        List<String> geneList = new ArrayList();

                        //Get the possible Proteins
                        List<String> proteinList = new ArrayList();

                        //Add proteins to a set (to filter duplicates)
                        uniprotSet.addAll(proteinList);

                    } else {
                        throw new ParseException("Row " + row + " with wrong format", 0);
                    }
                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                    parsedCorrectly = false;
                    if (boolMap.get(BoolVars.ignoreMisformatedRows) == false) {
                        System.exit(3);
                    }
                }
            }

            //Optionally: Print all the possible proteins to a file
            //        //Print all uniprot ids to the standarized file
            //        for (String id : uniprotSet) {
            //            output.write(id + ",\n");
            //        }
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

    static void validateVepTables() {
        File vepDirectory = new File(strMap.get(StrVars.vepTablesPath));
        if (!vepDirectory.exists()) {
            PathwayMatcher.println("The vepTablesPath provided does not exist.");
            System.exit(1);
        } else {
            for (int chr = 1; chr <= 22; chr++) {
                if (!(new File(strMap.get(StrVars.vepTablesPath) + strMap.get(StrVars.vepTableName).replace("XX", chr+"")).exists())) {
                    PathwayMatcher.println("The vep table for chromosome " + chr + " was not found. Expected: " + strMap.get(StrVars.vepTablesPath) + strMap.get(StrVars.vepTableName).replace("XX", chr+""));
                    System.exit(1);
                }
            }
        }
    }
}
