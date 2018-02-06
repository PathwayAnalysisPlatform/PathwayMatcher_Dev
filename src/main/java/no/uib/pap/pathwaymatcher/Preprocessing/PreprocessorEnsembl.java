package no.uib.pap.pathwaymatcher.Preprocessing;

import no.uib.pap.model.Error;
import no.uib.pap.model.Proteoform;
import no.uib.pap.pathwaymatcher.Conf;
import no.uib.pap.pathwaymatcher.db.ConnectionNeo4j;
import no.uib.pap.pathwaymatcher.db.ReactomeQueries;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;

import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;

import static no.uib.pap.model.Error.sendError;
import static no.uib.pap.model.Warning.*;
import static no.uib.pap.pathwaymatcher.Conf.strMap;
import static no.uib.pap.pathwaymatcher.PathwayMatcher.logger;
import static no.uib.pap.pathwaymatcher.util.InputPatterns.matches_Protein_Ensembl;

public class PreprocessorEnsembl extends Preprocessor {

    public TreeSet<Proteoform> process(List<String> input) throws ParseException {

        logger.log(Level.INFO, "\nPreprocessing input file...");
        HashSet<String> ensemblSet = new HashSet<>();
        HashMap<String, HashSet<String>> ensemblMapping = new HashMap<>();
        TreeSet<Proteoform> entities = new TreeSet<>();

        int row = 1;
        for (String line : input) {
            line = line.trim();
            row++;
            if (matches_Protein_Ensembl(line)) {
                ensemblSet.add(line);
            } else {
                if (line.isEmpty()) sendWarning(EMPTY_ROW, row);
                else sendWarning(INVALID_ROW, row);
            }
        }

        // Get all the mapping from ensembl to uniprot from Reactome
        ensemblMapping = getAllUniprotAccessionToEnsemblMapping();

        // Convert the ensembl ids to uniprot accessions
        logger.log(Level.FINE, "Converting Ensembl ids to UniProt accessions");
        int cont = 0;
        int percentage = 0;
        for (String ensemblId : ensemblSet) {
            if (ensemblMapping.containsKey(ensemblId)) {
                for (String uniprotAccession : ensemblMapping.get(ensemblId)) {
                    entities.add(new Proteoform(uniprotAccession));
                }
            }
            cont++;
            int newPercentage = cont * 100 / ensemblSet.size();
            if (newPercentage - percentage >= Conf.intMap.get(Conf.IntVars.percentageStep)) {
                percentage = newPercentage;
                logger.log(Level.FINE, percentage + "% ");
            }
        }
        if (percentage == 100) {
            logger.log(Level.FINE, "");
        } else {
            logger.log(Level.FINE, "100%");
        }

        return entities;
    }

    private static HashMap<String, HashSet<String>> getAllUniprotAccessionToEnsemblMapping() {

        HashMap<String, HashSet<String>> mapping = new HashMap<>();
        try {
            Session session = ConnectionNeo4j.driver.session();

            String query = ReactomeQueries.getAllUniprotAccessionToEnsembl;
            StatementResult queryResult;

            queryResult = session.run(query);

            while (queryResult.hasNext()) {
                Record record = queryResult.next();
                String ensemblId = record.get("ensemblId").asString();
                String uniprotAccession = record.get("uniprotAccession").asString();
                mapping.putIfAbsent(ensemblId, new HashSet<>());
                mapping.get(ensemblId).add(uniprotAccession);
            }

            session.close();
        } catch (org.neo4j.driver.v1.exceptions.ClientException e) {
            logger.log(Level.SEVERE, " Unable to connect to \"" + strMap.get(Conf.StrVars.host.toString()) + "\", ensure the database is running and that there is a working network connection to it.");
            sendError(Error.COULD_NOT_CONNECT_TO_NEO4j);
        }
        return mapping;
    }

    private static HashSet<String> getUniprotAccessionByEnsembl(String ensemblId) {

        HashSet<String> uniprotAccessionsResult = new HashSet<>();
        try {
            Session session = ConnectionNeo4j.driver.session();

            String query = ReactomeQueries.getUniprotAccessionByEnsembl;
            StatementResult queryResult;

            queryResult = session.run(query, Values.parameters("id", ensemblId));

            while (queryResult.hasNext()) {
                Record record = queryResult.next();
                uniprotAccessionsResult.add(record.get("uniprotAccession").asString());
            }

            session.close();
        } catch (org.neo4j.driver.v1.exceptions.ClientException e) {
            logger.log(Level.SEVERE, " Unable to connect to \"" + strMap.get(Conf.StrVars.host.toString()) + "\", ensure the database is running and that there is a working network connection to it.");
            sendError(Error.COULD_NOT_CONNECT_TO_NEO4j);
        }
        return uniprotAccessionsResult;
    }
}
