package no.uib.pap.pathwaymatcher.tools;

import java.util.Collection;
import java.util.TreeMap;

import no.uib.pap.model.Pathway;

/**
 * Keeps a list of known pathways to avoid duplications.
 */
public class PathwayStaticFactory {

    /**
     * The list of known pathways. Maps from pathway stable identifiers to pathway instances.
     */
    private static TreeMap<String, Pathway> pathwayMap;

    /**
     * Initializes the pathway list data structure
     */
    public static void initialize(){
        pathwayMap = new TreeMap<>();
    }

    /**
     * Gets Pathway instance that corresponds to the stId and displayName.
     * If the instance is requested for the first time, then a new instance is created for it and its top level pathway.
     * If the instance is requested again it returns the instance created before.
     *
     * @param stId
     * @param displayName
     * @return
     */
    public static Pathway getInstance(String stId, String displayName) {
        if (!pathwayMap.containsKey(stId)) {
            pathwayMap.put(stId, new Pathway(stId, displayName));
        }
        return pathwayMap.get(stId);
    }

    public static void clear() {
        pathwayMap.clear();
    }

    public static Collection<Pathway> getPathwaySet() {
        return pathwayMap.values();
    }
}
