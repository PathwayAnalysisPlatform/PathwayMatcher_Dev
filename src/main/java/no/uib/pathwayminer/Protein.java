package no.uib.pathwayminer;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author Optimus Franck
 */
public class Protein {
    String id;      //Uniprot Id. This is not unique across protein instances.
    List<ReactomeProtein> reactomeProteinList;    
    HashSet<Integer> knownSites;        //Known sites in reactome
    HashSet<Integer> requestedSites;    //Sites in the file
    int status;          //Case respect found sites.

    public Protein(String id, List<ReactomeProtein> rp, HashSet<Integer> knownSites, HashSet<Integer> requestedSites, int c) {
        this.id = id;
        this.reactomeProteinList = rp;
        this.knownSites = knownSites;
        this.requestedSites = requestedSites;
        this.status = c;
    }

    public Protein() {
        this.id = "";
        this.reactomeProteinList = new ArrayList<ReactomeProtein>();
        this.knownSites = new HashSet();
        this.requestedSites = new HashSet();
        this.status = 5;
    }
}
