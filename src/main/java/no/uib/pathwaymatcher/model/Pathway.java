/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uib.pathwaymatcher.model;

import no.uib.pathwaymatcher.Conf;
import sun.reflect.generics.tree.Tree;

import java.nio.file.Path;
import java.util.TreeSet;

import static no.uib.pathwaymatcher.Conf.boolMap;
import static no.uib.pathwaymatcher.Conf.strMap;

/**
 * @author Luis Francisco Hernández Sánchez
 */
public class Pathway implements Comparable<Pathway> {
    private String stId;
    private String displayName;
    private TreeSet<Pathway> topLevelPathwaySet;

    /**
     * Create a new instance of a top level pathway. Sets the topLevelPathway to itself.
     *
     * @param id
     * @param name
     */
    public Pathway(String id, String name) {
        this.stId = id;
        this.displayName = name;
        topLevelPathwaySet = new TreeSet<>();
    }

    public String getStId() {
        return stId;
    }

    public void setStId(String stId) {
        this.stId = stId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public TreeSet<Pathway> getTopLevelPathwaySet() {
        return topLevelPathwaySet;
    }

    public void setTopLevelPathwaySet(TreeSet<Pathway> topLevelPathway) {
        this.topLevelPathwaySet = topLevelPathway;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || !(obj instanceof Pathway)) return false;

        Pathway that = (Pathway) obj;

        return this.stId.equals(that.stId) && this.displayName.equals(that.displayName);
    }

    @Override
    public String toString() {
        return this.stId + "," + this.displayName;
    }

    @Override
    public int compareTo(Pathway that) {

        if (this.equals(that)) return 0;

        // First by displayName
        if (!this.displayName.equals(that.displayName)) {
            return this.displayName.compareTo(that.displayName);
        }

        // Second by stId
        if (!this.stId.equals(that.stId)) {
            return this.stId.compareTo(that.stId);
        }

        assert this.equals(that) : "Check consistency with equals";

        return 0;
    }
}
