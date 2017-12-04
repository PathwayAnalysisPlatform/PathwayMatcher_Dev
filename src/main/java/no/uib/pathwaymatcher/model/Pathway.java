/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uib.pathwaymatcher.model;

import no.uib.pathwaymatcher.Conf;

import java.nio.file.Path;

import static no.uib.pathwaymatcher.Conf.boolMap;
import static no.uib.pathwaymatcher.Conf.strMap;

/**
 * @author Luis Francisco Hernández Sánchez
 */
public class Pathway implements Comparable<Pathway> {
    private String stId;
    private String displayName;
    private Pathway topLevelPathway;

    /**
     * Create a new instance of a top level pathway. Sets the topLevelPathway to itself.
     *
     * @param id
     * @param name
     */
    public Pathway(String id, String name) {
        this.stId = id;
        this.displayName = name;
        topLevelPathway = this;
    }

    /**
     * Creates a new instance of a regular pathway. Sets the topLevelPathway to the pathway sent as parameter.
     *
     * @param stId
     * @param displayName
     * @param topLevelPathway
     */
    public Pathway(String stId, String displayName, Pathway topLevelPathway) {
        this.stId = stId;
        this.displayName = displayName;
        this.topLevelPathway = topLevelPathway;
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

    public Pathway getTopLevelPathway() {
        return topLevelPathway;
    }

    public void setTopLevelPathway(Pathway topLevelPathway) {
        this.topLevelPathway = topLevelPathway;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || !(obj instanceof Pathway)) return false;

        Pathway that = (Pathway) obj;

        return this.topLevelPathway.displayName.equals(that.topLevelPathway.displayName)
                && this.stId.equals(that.stId)
                && this.displayName.equals(that.displayName);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        if (boolMap.get(Conf.BoolVars.showTopLevelPathways)) {
            if (this.topLevelPathway == this) {
                result.append(this.stId + strMap.get(Conf.StrVars.colSep) + this.displayName);
            } else {
                result.append(this.topLevelPathway.toString());
            }
            result.append(strMap.get(Conf.StrVars.colSep));
        }
        return this.stId + "," + this.displayName;
    }

    @Override
    public int compareTo(Pathway that) {

        if (this.equals(that)) return 0;

        // First sort by TopLevelPathway displayName
        if (!this.topLevelPathway.displayName.equals(that.topLevelPathway.displayName)) {
            return this.topLevelPathway.displayName.compareTo(that.topLevelPathway.displayName);
        }

        // Second by displayName
        if (!this.displayName.equals(that.displayName)) {
            return this.displayName.compareTo(that.displayName);
        }

        // Third by stId
        if (!this.stId.equals(that.stId)) {
            return this.stId.compareTo(that.stId);
        }

        assert this.equals(that) : "Check consistency with equals";

        return 0;
    }
}
