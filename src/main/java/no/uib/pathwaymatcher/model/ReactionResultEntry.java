package no.uib.pathwaymatcher.model;

import no.uib.pathwaymatcher.Conf;

/**
 * @author Luis Francisco Hernández Sánchez
 */
public class ReactionResultEntry {

    public String stId;
    public String name;
    public no.uib.pathwaymatcher.model.Pathway pathway;
    public no.uib.pathwaymatcher.model.Pathway topLevelPathway;

    public ReactionResultEntry(String id, String name, String pathwayStId, String pathwayName) {
        this.stId = id;
        this.name = name;
        this.pathway = new Pathway(pathwayStId, pathwayName);
    }

    public ReactionResultEntry(String id, String name, String pathwayStId, String pathwayName, String tlpStId, String tlpName) {
        this.stId = id;
        this.name = name;
        this.pathway = new Pathway(pathwayStId, pathwayName);
        this.topLevelPathway = new Pathway(tlpStId, tlpName);
    }

    public ReactionResultEntry() {
        this.stId = "";
        this.name = "";
    }

    @Override
    public String toString() {
        return this.stId + "," + this.name;
    }

    public String printEntry() {
        return this.pathway.stId + Conf.strMap.get(Conf.StrVars.colSep)
                + this.pathway.displayName + Conf.strMap.get(Conf.StrVars.colSep)
                + this.stId + Conf.strMap.get(Conf.StrVars.colSep)
                + this.name;
    }

    public String printEntry(Boolean showTopLevelPathway) {
        if (showTopLevelPathway) {
            return this.topLevelPathway.stId + Conf.strMap.get(Conf.StrVars.colSep) 
                    + this.topLevelPathway.displayName + Conf.strMap.get(Conf.StrVars.colSep) 
                    + this.pathway.stId + Conf.strMap.get(Conf.StrVars.colSep) 
                    + this.pathway.displayName + Conf.strMap.get(Conf.StrVars.colSep)
                    + this.stId + Conf.strMap.get(Conf.StrVars.colSep)
                    + this.name;
        } else {
            return this.pathway.stId + Conf.strMap.get(Conf.StrVars.colSep)
                    + this.pathway.displayName + Conf.strMap.get(Conf.StrVars.colSep)
                    + this.stId + Conf.strMap.get(Conf.StrVars.colSep)
                    + this.name;
        }
    }
}
