package no.UiB.Prototype1.Stages;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import no.UiB.Prototype1.Configuration;

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
            t = detectInputType();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }

        Boolean parseResult = false;
        System.out.println(t.toString());

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
        if (parseResult) {
            System.out.println("File parsed correctly!");
        } else {
            System.out.println("The format of the file is incorrect.");
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
        output = new FileWriter(Configuration.standarizedFile);
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
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        output.close();
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

    public static Boolean parseFormat_peptideList() throws java.text.ParseException {
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

    public static Boolean parseFormat_peptideListAndSites() throws java.text.ParseException {
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

    public static Boolean parseFormat_peptideListAndModSites() throws java.text.ParseException {
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
