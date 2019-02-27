package no.uib.pap.pathwaymatcher.tools;

import com.google.common.io.Files;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public class FormatConverter {

    static String outputPath = "";

    /**
     * Converts one file of proteins with post-translational modifications to the SIMPLE proteoform format for PathwayMatcher.
     *
     * Example input file:
     * Accession	Site in Protein
     * A0FGR8	758 and 761
     * A1L390	611 or 612 or 614
     * O00139	61 or 75 or 78
     * O00264	57
     * O00299	145 or 146 or 155 or 156 or 163
     *
     * Example result file "toMatchAsSuperset.tsv":
     * A0FGR8;00000:758,00000:761
     * O00264;00000:57
     *
     * Example result file "toMatchAsSubset.tsv":
     * A1L390;00000:611,00000:612,00000:614
     * O00139;00000:61,00000:75,00000:78
     * O00299;00000:145,00000:146,00000:155,00000:156,00000:163
     *
     * <p>It will add MOD:00000 as the type for each modification.</p>
     * <p>For each proteoform, if the ptms are separated by 'or', then the matching type should be subset.
     * If the proteoform is separateb by 'and' then superset matching type is used.</p>
     *
     * @param args [0] input path+file [1] output path
     */
    public static void main(String args[]) {
        convertToSimpleFormat(args);
    }

    private static void convertToSimpleFormat(String[] args) {
        FileWriter outputSuperset = null;
        FileWriter outputSubset = null;
        if (args.length <= 0) {
            System.out.println("Need to specify the input file as argument.");
            System.exit(1);
        }
        if(args.length <= 1){
            System.out.println("Need to specify the outputPath.");
            System.exit(2);
        }
        if(args.length >= 2){
            outputPath = args[1];
        }
        try {
            List<String> lines = Files.readLines(new File(args[0]), Charset.defaultCharset());
            outputSuperset = new FileWriter(outputPath + "toMatchAsSuperset.tsv");
            outputSubset = new FileWriter(outputPath + "toMatchAsSubset.tsv");

            int R = 0;
            for (String line : lines) {
                if (R == 0) {   // Skip the header line
                    R++;
                    continue;
                }
                if(line.contains("or")){
                    outputSubset.write(line.replace("\t", ";00000:").replace(" or ", ",00000:") + "\n");
                } else{
                    outputSuperset.write(line.replace("\t", ";00000:").replace(" and ", ",00000:") + "\n");
                }
            }
            outputSuperset.close();
            outputSubset.close();
        } catch (IOException e) {
            System.out.println("Could not read file: " + args[0]);
            System.exit(2);
        }
    }
}
