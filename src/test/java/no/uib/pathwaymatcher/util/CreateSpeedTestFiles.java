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
}
