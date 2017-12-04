package no.uib.pathwaymatcher.tools;

import no.uib.pathwaymatcher.model.Pathway;

import java.util.TreeMap;

public class PathwayStaticFactory {
    private static TreeMap<String, Pathway> pathwaySet = new TreeMap<>();

    /**
     * Gets Pathway instance that corresponds to the stId and displayName.
     * If the instance is requested for the first time, then a new instance is created for it and its top level pathway.
     * If the instance is requested again it returns the instance created before.
     * @param stId
     * @param displayName
     * @param tlpStId
     * @param tlpDisplayName
     * @return
     */
    public static Pathway getInstance(String stId, String displayName, String tlpStId, String tlpDisplayName){
        if(!pathwaySet.containsKey(stId)){
            if(!pathwaySet.containsKey(tlpStId)){
                pathwaySet.put(tlpStId, new Pathway(tlpStId, tlpDisplayName));
            }
            if(!stId.equals(tlpStId)){
                Pathway tlp = pathwaySet.get(tlpStId);
                Pathway pathway = new Pathway(stId, displayName, tlp);
                pathwaySet.put(stId, pathway);
            }
        }
        return pathwaySet.get(stId);
    }

    public static void clear(){
        pathwaySet.clear();
    }
}
