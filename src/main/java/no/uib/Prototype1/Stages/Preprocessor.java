package no.UiB.Prototype1.Stages;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Preprocessor {

    /* This Class should transform any type of file to the standard format of representing the Modified Proteins. */
    //Detect the type of input
    public static int detectInputType(String inputPath) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(inputPath));
            String firstLine = reader.readLine();
            if (firstLine.trim().startsWith("Protein")) {
                return 1;
            } else if (firstLine.matches("^\\w\\d{5},(\\d+;)*\\d+$")) {
                return 2;
            } else if (firstLine.matches("^[A-Z]+,(\\d+;)*\\d+$")) {
                return 3;
            } else if (firstLine.matches("^\\w\\d{5},(\\d{5}:\\d+;)*\\d{5}:\\d+$")) {
                return 4;
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
        return 0;
    }

    /**
     * ********** The parser methods transform the original input file to the
     * standard Modified proteins standard file. **************
     */
    //If all the file was converted properly they return true.
    //If the file does not follow the format they return false. 
    public static Boolean parseFormat1_MaxQuant(String inputPath) throws java.text.ParseException {
        Boolean parsedCorrectly = true;
        BufferedReader reader = null;
        try {
            int row = 1;
            reader = new BufferedReader(new FileReader(inputPath));
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

    public static Boolean parseFormat2_UniprotsWithSites(String inputPath) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static Boolean parseFormat3_PeptidesWithSites(String inputPath) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
