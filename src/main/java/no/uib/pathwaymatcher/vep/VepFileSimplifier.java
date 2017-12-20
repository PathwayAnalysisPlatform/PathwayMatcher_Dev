package no.uib.pathwaymatcher.vep;

import com.google.common.collect.Multimap;
import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.Preprocessing.PreprocessorSnps;
import no.uib.pathwaymatcher.db.ConnectionNeo4j;
import no.uib.pathwaymatcher.db.ReactomeQueries;
import no.uib.pathwaymatcher.model.Snp;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;

import static no.uib.pathwaymatcher.PathwayMatcher.logger;
import static no.uib.pathwaymatcher.Preprocessing.PreprocessorVariants.getSNPAndSwissProtFromVep;
import static no.uib.pathwaymatcher.model.Error.ERROR_READING_VEP_TABLES;
import static no.uib.pathwaymatcher.model.Error.sendError;

/**
 * TODO
 */
public class VepFileSimplifier {

    public static void main(String args[]) {
        VepFileSimplifier simplifier = new VepFileSimplifier();
        simplifier.simplyTables();
    }

    private void simplyTables() {

        // Set paths to files with the default configuration
        String vepPath = "vep/";

        // Read list with all Reactome proteins
        TreeSet<String> allProteins = new TreeSet<>();

        Conf.setDefaultValues();
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");
        ConnectionNeo4j.session = ConnectionNeo4j.driver.session();

        String query = ReactomeQueries.getAllProteinsWithIsoforms;
        StatementResult queryResult = ConnectionNeo4j.session.run(query);
        while (queryResult.hasNext()) {
            Record r = queryResult.next();
            allProteins.add(r.get("Identifiers").asString());
        }

        // Traverse all vep tables to keep only the rows related to the proteins in Reactome
        for (int chr = 1; chr <= 22; chr++) {
            logger.log(Level.INFO, "Scanning vepTable for chromosome " + chr);
            try {

                PreprocessorSnps preprocessorSnps = new PreprocessorSnps();
                BufferedReader br = preprocessorSnps.getBufferedReader("vep/" + chr + ".gz");

                FileOutputStream outputStream = new FileOutputStream("src/main/resources/vep/" + "simpleXX.gz".replace("XX", chr + ""));
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);

                gzipOutputStream.write(br.readLine().getBytes()); //Read header line
                gzipOutputStream.write("\n".getBytes());
                for (String line; (line = br.readLine()) != null; ) {

                    Multimap<Snp, String> snpMap = getSNPAndSwissProtFromVep(line);

                    for (String swissprot : snpMap.values()) {
                        if (!swissprot.equals("NA")) {
                            if (allProteins.contains(swissprot)) {
                                // send to simplified file
                                gzipOutputStream.write(line.getBytes());
                                gzipOutputStream.write("\n".getBytes());
                                break;
                            }
                        }
                    }

                }
                gzipOutputStream.finish();
                gzipOutputStream.close();

            } catch (
                    IOException ex)

            {
                sendError(ERROR_READING_VEP_TABLES, chr);
            }
        }
    }
}
