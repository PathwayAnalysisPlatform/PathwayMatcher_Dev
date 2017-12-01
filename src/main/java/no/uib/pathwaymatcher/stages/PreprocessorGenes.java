package no.uib.pathwaymatcher.stages;

import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.db.ConnectionNeo4j;
import no.uib.pathwaymatcher.db.ReactomeQueries;
import no.uib.pathwaymatcher.model.Proteoform;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;

import static no.uib.pathwaymatcher.Conf.strMap;
import static no.uib.pathwaymatcher.PathwayMatcher.logger;
import static no.uib.pathwaymatcher.model.Error.COULD_NOT_CONNECT_TO_NEO4j;
import static no.uib.pathwaymatcher.model.Error.sendError;
import static no.uib.pathwaymatcher.model.Warning.INVALID_ROW;
import static no.uib.pathwaymatcher.util.InputPatterns.matches_Gene;

public class PreprocessorGenes extends Preprocessor {

    @Override
    public TreeSet<Proteoform> process(List<String> input) throws ParseException {

        logger.log(Level.INFO, "\nPreprocessing input file...");

        TreeSet<Proteoform> entities = new TreeSet<>();
        HashSet<String> geneSet = new HashSet<>();
        HashMap<String, HashSet<String>> geneMapping = new HashMap<>();

        try {
            int row = 1;
            LineIterator it = FileUtils.lineIterator(new File(Conf.strMap.get(Conf.StrVars.input)), "UTF-8");
            String line;
            while (it.hasNext()) {
                line = it.nextLine();
                row++;
                if (matches_Gene(line)) {
                    geneSet.add(line);
                } else {
                    logger.log(Level.WARNING, "Row " + row + " with wrong format", INVALID_ROW.getCode());
                }
            }
            LineIterator.closeQuietly(it);

            // Get all the mapping from ensembl to uniprot from Reactome
            geneMapping = getAllUniprotAccessionToGeneNameMapping();

            // Convert the ensembl ids to uniprot accessions
            logger.log(Level.FINE, "Converting Gene Names to UniProt accessions");
            int cont = 0;
            int percentage = 0;
            for (String geneName : geneSet) {
                if (geneMapping.containsKey(geneName)) {
                    for (String uniprotAccession : geneMapping.get(geneName)) {
                        entities.add(new Proteoform(uniprotAccession));
                    }
                }
                cont++;
                int newPercentage = cont * 100 / geneSet.size();
                if (newPercentage - percentage >= Conf.intMap.get(Conf.IntVars.percentageStep)) {
                    percentage = newPercentage;
                    logger.log(Level.FINER, percentage + "% ");
                }
            }
            if (percentage == 100) {
                logger.log(Level.FINER, "");
            } else {
                logger.log(Level.FINER, "100%");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Cannot find the input file specified.");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Cannot read the input file specified.");
            System.exit(1);
        }
        return entities;
    }

    private static HashMap<String, HashSet<String>> getAllUniprotAccessionToGeneNameMapping() {

        HashMap<String, HashSet<String>> mapping = new HashMap<>();
        try {
            Session session = ConnectionNeo4j.driver.session();

            String query = ReactomeQueries.getAllUniprotAccessionToGeneName;
            StatementResult queryResult;

            queryResult = session.run(query);

            while (queryResult.hasNext()) {
                Record record = queryResult.next();
                String ensemblId = record.get("gene").asString();
                String uniprotAccession = record.get("uniprotAccaession").asString();
                mapping.putIfAbsent(ensemblId, new HashSet<>());
                mapping.get(ensemblId).add(uniprotAccession);
            }

            session.close();
        } catch (org.neo4j.driver.v1.exceptions.ClientException e) {
            logger.log(Level.SEVERE, " Unable to connect to \"" + strMap.get(Conf.StrVars.host.toString()) + "\", ensure the database is running and that there is a working network connection to it.");
            sendError(COULD_NOT_CONNECT_TO_NEO4j);
        }
        return mapping;
    }
}
