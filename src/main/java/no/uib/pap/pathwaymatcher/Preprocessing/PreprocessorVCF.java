package no.uib.pap.pathwaymatcher.Preprocessing;

import static no.uib.pap.model.Warning.EMPTY_ROW;
import static no.uib.pap.model.Warning.INVALID_ROW;
import static no.uib.pap.model.Warning.sendWarning;
import static no.uib.pap.pathwaymatcher.Matching.InputPatterns.matches_Vcf_Record;
import static no.uib.pap.pathwaymatcher.PathwayMatcher.logger;

import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;

import no.uib.pap.model.Proteoform;
import no.uib.pap.model.Snp;

public class PreprocessorVCF extends PreprocessorVariants {

    @Override
    public TreeSet<Proteoform> process(List<String> input) throws ParseException {

        logger.log(Level.INFO, "\nPreprocessing input file...");
        TreeSet<Proteoform> entities = new TreeSet<>();
        HashSet<Snp> snpSet = new HashSet<>();

        int row = 0;

        // Create set of snps
        for (String line : input) {
            row++;
            line = line.trim();

            if (!line.startsWith("#")) {
                continue;
            }

            if (matches_Vcf_Record(line)) {
                Snp snp = getSnpFromVcf(line);
                snpSet.add(snp);
            } else {
                if (line.isEmpty()) sendWarning(EMPTY_ROW, row);
                else sendWarning(INVALID_ROW, row);
            }
        }
        return entities;
    }

    /*
    This method expects the line to be validated already
     */
    private static Snp getSnpFromVcf(String line) {
        String[] fields = line.split(" ");
        Integer chr = Integer.valueOf(fields[0]);
        Long bp = Long.valueOf(fields[1]);
        String rsid = fields[2];

        if (rsid.equals(".")) {
            return new Snp(chr, bp);
        } else {
            return new Snp(chr, bp, rsid);
        }
    }
}
