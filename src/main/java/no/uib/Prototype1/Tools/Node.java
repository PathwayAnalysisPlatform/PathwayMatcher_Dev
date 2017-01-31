/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uib.Prototype1.Tools;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import no.uib.Prototype1.Configuration;
import no.uib.Prototype1.db.ConnectionNeo4j;
import static org.neo4j.driver.v1.GraphDatabase.driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Node {

    String id; //Uniprot Id
    int group;
    public Set<String> complexNeighbors;
    public Set<String> reactionNeighbors;

    public Node(String i, int g) throws IOException {
        this.id = i;
        this.group = g;

        if (Configuration.complexNeighbors) {
            this.complexNeighbors = new HashSet<String>(16);
            getComplexNeighbors(this.id);
        }
        if (Configuration.reactionNeighbors) {
            this.reactionNeighbors = new HashSet<String>(16);
            getReactonNeighbors(this.id);
        }
    }
    
    public Node(String i, int g, boolean isNeighbor) {
        this.id = i;
        this.group = g;
    }

    @Override
    public String toString() {
        switch(Configuration.outputGraphFileType){
            case json:
                return "{\"id\": \"" + this.id + "\", \"group\": " + this.group + "}";
            case graphviz:
                return this.id + " : " + this.group;
            case sif:
                return this.id + " : " + this.group;
        }
        return "";
    }

    private void getComplexNeighbors(String id) throws IOException //Relations at protein reference entity level, not considering subcellular location
    {
        Session session = ConnectionNeo4j.driver.session();                                     //Connect to Reactome
        String query = "";

        switch (Configuration.unitType) {
            case ewas: //TODO
                query = "MATCH (re:ReferenceEntity{identifier:{id}})<-[:referenceEntity]-(p:EntityWithAccessionedSequence)<-[:hasComponent|hasMember|hasCandidate|repeatedUnit*]-(c:Complex)-[:hasComponent|hasMember|hasCandidate|repeatedUnit*]->(nE:EntityWithAccessionedSequence)-[:referenceEntity]->(nP:ReferenceEntity)\n"
                        + "RETURN DISTINCT re.identifier as source, p.stId, p.displayName, c.displayName, nE.stId, nE.displayName, nP.identifier as target";
                break;
            case uniprot:
                query = "MATCH (re:ReferenceEntity{identifier:{id}})<-[:referenceEntity]-(p:EntityWithAccessionedSequence)<-[:hasComponent|hasMember|hasCandidate|repeatedUnit*]-(c:Complex)-[:hasComponent|hasMember|hasCandidate|repeatedUnit*]->(nE:EntityWithAccessionedSequence)-[:referenceEntity]->(nP:ReferenceEntity)\n"
                        + "RETURN DISTINCT re.identifier as source, p.stId, p.displayName, c.displayName, nE.stId, nE.displayName, nP.identifier as target";
                break;
        }
        StatementResult queryResult = session.run(query, Values.parameters("id", id));

        while (queryResult.hasNext()) {
            Record record = queryResult.next();
            this.complexNeighbors.add(record.get("target").asString());
        }

        session.close();
    }

    public void getReactonNeighbors(String id) throws IOException //Relations at protein reference entity level, not considering subcellular location
    {
        Session session = ConnectionNeo4j.driver.session();                                     //Connect to Reactome
        String query = "";

        switch (Configuration.unitType) {
            case ewas: //TODO
                query = "MATCH (re:ReferenceEntity{identifier:{id}})<-[:referenceEntity]-(p:EntityWithAccessionedSequence)<-[:hasComponent|hasMember|hasCandidate|repeatedUnit|input|output|catalystActivity|regulatedBy|physicalEntity|regulator*]-(rle:ReactionLikeEvent)-[:hasComponent|hasMember|hasCandidate|repeatedUnit|input|output|catalystActivity|regulatedBy|physicalEntity|regulator*]->(nE:EntityWithAccessionedSequence)-[:referenceEntity]->(nP:ReferenceEntity)\n"
                        + "RETURN DISTINCT re.identifier  as source, p.stId, p.displayName, rle.stId, rle.displayName, nE.stId, nE.displayName, nP.identifier as target";
                break;
            case uniprot:
                query = "MATCH (re:ReferenceEntity{identifier:{id}})<-[:referenceEntity]-(p:EntityWithAccessionedSequence)<-[:hasComponent|hasMember|hasCandidate|repeatedUnit|input|output|catalystActivity|regulatedBy|physicalEntity|regulator*]-(rle:ReactionLikeEvent)-[:hasComponent|hasMember|hasCandidate|repeatedUnit|input|output|catalystActivity|regulatedBy|physicalEntity|regulator*]->(nE:EntityWithAccessionedSequence)-[:referenceEntity]->(nP:ReferenceEntity)\n"
                        + "RETURN DISTINCT re.identifier  as source, p.stId, p.displayName, rle.stId, rle.displayName, nE.stId, nE.displayName, nP.identifier as target";
                break;
        }
        StatementResult queryResult = session.run(query, Values.parameters("id", id));

        while (queryResult.hasNext()) {
            Record record = queryResult.next();
            this.reactionNeighbors.add(record.get("target").asString());
        }

        session.close();
    }
}
