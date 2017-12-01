package no.uib.pathwaymatcher.model;

import no.uib.pathwaymatcher.Conf;

import static no.uib.pathwaymatcher.Conf.strMap;

public class Reaction {
    private String stId;
    private String displayName;

    public Reaction(String stId, String displayName) {
        this.stId = stId;
        this.displayName = displayName;
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

    @Override
    public String toString() {
        return this.stId + strMap.get(Conf.StrVars.colSep) + this.displayName;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
