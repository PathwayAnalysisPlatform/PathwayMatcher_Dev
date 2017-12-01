/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uib.pathwaymatcher.model;

import no.uib.pathwaymatcher.Conf;

import static no.uib.pathwaymatcher.Conf.boolMap;
import static no.uib.pathwaymatcher.Conf.strMap;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Pathway {
    private String stId;
    private String displayName;
    private Pathway topLevelPathway;
    
    public Pathway(String id, String name) {
        this.stId = id;
        this.displayName = name;
        topLevelPathway = this;
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
    public String toString(){
        StringBuilder result = new StringBuilder();
        if(boolMap.get(Conf.BoolVars.showTopLevelPathways)){
            if(this.topLevelPathway == this){
                result.append(this.stId + strMap.get(Conf.StrVars.colSep) + this.displayName);
            }
            else{
                result.append(this.topLevelPathway.toString());
            }
            result.append(strMap.get(Conf.StrVars.colSep));
        }
        return this.stId + "," + this.displayName;
    }
}
