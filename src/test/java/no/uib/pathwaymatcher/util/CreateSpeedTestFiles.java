package no.uib.pathwaymatcher.util;

import no.uib.pathwaymatcher.Conf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static no.uib.pathwaymatcher.util.ConstantHolder.*;

public class CreateSpeedTestFiles {

    public static void main(String args[]) throws IOException {
//        createFiles_geneticVariants();
        createFiles_proteinLists();
    }

    public static void createFiles(Conf.InputTypeEnum inputType, List<String> allLines) throws IOException {

        FileWriter[] files = new FileWriter[SIZES.length];

        for (int F = 0; F < SIZES.length; F++) {
            files[F] = new FileWriter(INPUT_PATH + inputType + "_" + String.format("%08d", SIZES[F]) + ".txt");
        }

        int row = 0;
        for (String line : allLines) {
            row++;
            for (int S = SIZES.length - 1; S >= 0; S--) {
                if (SIZES[S] < row) {
                    continue;
                }
                files[S].write(line + "\n");
            }
        }

        for (int F = 0; F < SIZES.length; F++) {
            files[F].close();
        }
    }

    private static void createFiles_geneticVariants() throws IOException {
        // Read all the Genetic Variants sample
        FileWriter[] files = new FileWriter[snpSizes.length];

        for (int F = 0; F < snpSizes.length; F++) {
            files[F] = new FileWriter(PATH + "rsidList_" + String.format("%08d", snpSizes[F]) + ".txt");
        }

        try (BufferedReader br = new BufferedReader(new FileReader(INPUT_PATH + "snpList005.csv"))) {
            for (int R = 1; R <= snpSizes[snpSizes.length - 1]; R++) {
                String line = br.readLine();
                for (int F = snpSizes.length - 1; F >= 0; F--) {
                    if (R > snpSizes[F]) {
                        break;
                    }
                    files[F].write(line + "\n");
                }
            }
        }

        for (int F = 0; F < snpSizes.length; F++) {
            files[F].close();
        }
    }

    public static List<String> getFileAsList(String fileName) throws IOException {
        List<String> lines = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(fileName), 8192);
        String line;
        while ((line = br.readLine()) != null) {
            lines.add(line);
        }
        br.close();
        return lines;
    }

    public static List<String> getFileAsList(String fileName, int numberOfLines) throws IOException {
        int cont = 0;
        List<String> lines = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(fileName), 8192);
        String line;
        while ((line = br.readLine()) != null) {
            lines.add(line);
            cont++;
            if(cont >= numberOfLines)
                break;;
        }
        br.close();
        return lines;
    }

    public static void createFiles_proteinLists() throws IOException {
        List<String> lines = new ArrayList<>();
        FileWriter[] files_uniprotList = new FileWriter[SIZES.length];
        FileWriter[] files_uniprotListAndSites = new FileWriter[SIZES.length];
        FileWriter[] files_uniprotListAndModSites = new FileWriter[SIZES.length];

        for (int F = 0; F < SIZES.length; F++) {
            files_uniprotList[F] = new FileWriter(PATH + "uniprotList_" + String.format("%05d", SIZES[F]) + ".txt");
            files_uniprotListAndSites[F] = new FileWriter(PATH + "uniprotListAndSites_" + String.format("%05d", SIZES[F]) + ".txt");
            files_uniprotListAndModSites[F] = new FileWriter(PATH + "uniprotListAndModSites_" + String.format("%05d", SIZES[F]) + ".txt");
        }

        // Read proteoform big file
        try (BufferedReader br = new BufferedReader(new FileReader(PATH + "reactomeAllProteoforms.txt"))) {
            String line;
            int row = 0;
            while ((line = br.readLine()) != null) {
                row++;
                for (int S = SIZES.length - 1; S >= 0; S--) {
                    if (SIZES[S] < row) {
                        continue;
                    }
                    String[] parts = line.split(";");
                    files_uniprotList[S].write(parts[0]);
                    files_uniprotListAndSites[S].write(parts[0] + ";");
                    files_uniprotListAndModSites[S].write(line);

                    if (parts.length > 1) {
                        String[] modifications = parts[1].split(",");
                        for (int M = 0; M < modifications.length; M++) {
                            if (M > 0) {
                                files_uniprotListAndSites[S].write(",");
                            }
                            files_uniprotListAndSites[S].write(modifications[M].split(":")[1]);
                        }
                    }

                    files_uniprotList[S].write("\n");
                    files_uniprotListAndSites[S].write("\n");
                    files_uniprotListAndModSites[S].write("\n");
                }
            }
        }

        for (int F = 0; F < SIZES.length; F++) {
            files_uniprotList[F].close();
            files_uniprotListAndSites[F].close();
            files_uniprotListAndModSites[F].close();
        }
    }

    private static Pair<String, MapList<String, Long>> getProteoformCustom(String line) {

        StringBuilder protein = new StringBuilder();
        StringBuilder coordinate = null;
        StringBuilder mod = null;
        MapList<String, Long> ptms = new MapList<>();

        // Get the identifier
        // Read until end of line or semicolon
        int pos = 0;
        char c = line.charAt(pos);
        while (c != ';') {
            protein.append(c);
            pos++;
            if (pos == line.length())
                break;
            c = line.charAt(pos);
        }
        pos++;
        if (protein.length() == 0) {
            throw new RuntimeException("Problem parsing line " + line);
        }

        // Get ptms one by one
        //While there are characters

        while (pos < line.length()) {
            coordinate = new StringBuilder();
            mod = new StringBuilder();
            //Read a ptm
            c = line.charAt(pos);
            while (c != ':') {
                mod.append(c);
                pos++;
                c = line.charAt(pos);
            }
            pos++;
            c = line.charAt(pos);
            while (c != ',') {
                coordinate.append(c);
                pos++;
                if (pos == line.length())
                    break;
                c = line.charAt(pos);
            }
            ptms.add(mod.toString(), (coordinate.toString().toLowerCase().equals("null") ? null : Long.valueOf(coordinate.toString())));
            pos++;
        }

        return new Pair<>(protein.toString(), ptms);
    }
}
