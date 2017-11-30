package no.uib.pathwaymatcher.model.stages;

import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.db.ConnectionNeo4j;
import no.uib.pathwaymatcher.db.ReactomeQueries;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;

import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static no.uib.pathwaymatcher.Conf.boolMap;
import static no.uib.pathwaymatcher.Conf.strMap;
import static no.uib.pathwaymatcher.PathwayMatcher.*;
import static no.uib.pathwaymatcher.model.Error.INVALID_ROW;
import static no.uib.pathwaymatcher.util.InputPatterns.matches_Protein_Ensembl;

public class PreprocessorEnsembl extends Preprocessor {

    public Set<String> process(List<String> input) throws ParseException {

        HashSet<String> ensemblSet = new HashSet<>();
        HashMap<String, HashSet<String>> ensemblMapping = new HashMap<>();
        Set<String> entities = new HashSet<>();

        int row = 1;
        for (String line : input) {
            line = line.trim();
            row++;
            if (matches_Protein_Ensembl(line)) {
                ensemblSet.add(line);
            } else if (boolMap.get(Conf.BoolVars.ignoreMisformatedRows)) {
                System.out.println("Ignoring invalid row " + row + ": " + line);
            } else {
                throw new ParseException("Row " + row + " with wrong format", INVALID_ROW.getCode());
            }
        }

        // Get all the mapping from ensembl to uniprot from Reactome
        ensemblMapping = getAllUniprotAccessionToEnsemblMapping();

        // Convert the ensembl ids to uniprot accessions
        println("Converting Ensembl ids to UniProt accessions");
        int cont = 0;
        int percentage = 0;
        for (String ensemblId : ensemblSet) {
            if (ensemblMapping.containsKey(ensemblId)) {
                for (String uniprotAccession : ensemblMapping.get(ensemblId)) {
                    entities.add(uniprotAccession);
                }
            }
            cont++;
            int newPercentage = cont * 100 / ensemblSet.size();
            if (newPercentage - percentage >= Conf.intMap.get(Conf.IntVars.percentageStep)) {
                percentage = newPercentage;
                print(percentage + "% ");
            }
        }
        if (percentage == 100) {
            println("");
        } else {
            println("100%");
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
            println(" Unable to connect to \"" + strMap.get(Conf.StrVars.host.toString()) + "\", ensure the database is running and that there is a working network connection to it.");
            System.exit(1);
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
            println(" Unable to connect to \"" + strMap.get(Conf.StrVars.host.toString()) + "\", ensure the database is running and that there is a working network connection to it.");
            System.exit(1);
        }
        return uniprotAccessionsResult;
    }
}
