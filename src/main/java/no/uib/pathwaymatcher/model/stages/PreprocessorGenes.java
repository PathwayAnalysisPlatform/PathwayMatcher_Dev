package no.uib.pathwaymatcher.model.stages;

import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.db.ConnectionNeo4j;
import no.uib.pathwaymatcher.db.ReactomeQueries;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static no.uib.pathwaymatcher.Conf.boolMap;
import static no.uib.pathwaymatcher.Conf.strMap;
import static no.uib.pathwaymatcher.PathwayMatcher.print;
import static no.uib.pathwaymatcher.PathwayMatcher.println;
import static no.uib.pathwaymatcher.util.InputPatterns.matches_Gene;

public class PreprocessorGenes extends Preprocessor {

    @Override
    public Set<String> process(List<String> input) throws ParseException {

        Set<String> entities = new HashSet<>();
        Boolean parsedCorrectly = true;
        HashSet<String> geneSet = new HashSet<>();
        HashMap<String, HashSet<String>> geneMapping = new HashMap<>();

        try {
            int row = 1;
            LineIterator it = FileUtils.lineIterator(new File(Conf.strMap.get(Conf.StrVars.input)), "UTF-8");
            String line;
            while (it.hasNext()) {
                line = it.nextLine();
                row++;
                try {
                    if (matches_Gene(line)) {
                        geneSet.add(line);
                    } else {
                        if (boolMap.get(Conf.BoolVars.ignoreMisformatedRows)) {
                            System.out.println("Ignoring invalid row: " + row);
                        } else {
                            throw new ParseException("Row " + row + " with wrong format", 0);
                        }
                    }
                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                    parsedCorrectly = false;
                    System.exit(0);
                }
            }
            LineIterator.closeQuietly(it);

            // Get all the mapping from ensembl to uniprot from Reactome
            geneMapping = getAllUniprotAccessionToGeneNameMapping();

            // Convert the ensembl ids to uniprot accessions
            println("Converting Gene Names to UniProt accessions");
            int cont = 0;
            int percentage = 0;
            for (String geneName : geneSet) {
                if (geneMapping.containsKey(geneName)) {
                    for (String uniprotAccession : geneMapping.get(geneName)) {
                        entities.add(uniprotAccession);
                    }
                }
                cont++;
                int newPercentage = cont * 100 / geneSet.size();
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
            println(" Unable to connect to \"" + strMap.get(Conf.StrVars.host.toString()) + "\", ensure the database is running and that there is a working network connection to it.");
            System.exit(1);
        }
        return mapping;
    }
}
