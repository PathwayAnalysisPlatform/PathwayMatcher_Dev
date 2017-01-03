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
 *
 * @author Optimus Franck
 */
public class ReactomeProtein {
    String stId;
    String id;
    String displayName;
    List<Integer> sites;   //Known phosphosites in Reactome.
    List<String> siteNames;   //Known phosphosites names in Reactome.

    public ReactomeProtein() {
        stId = "";
        id = "";
        displayName = "";
        sites = new ArrayList<Integer>();
        siteNames = new ArrayList<String>();
    }    
}
