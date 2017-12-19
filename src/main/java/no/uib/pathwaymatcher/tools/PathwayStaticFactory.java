package no.uib.pathwaymatcher.tools;

import no.uib.pathwaymatcher.model.Pathway;

import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

public class PathwayStaticFactory {

    private static TreeMap<String, Pathway> pathwayMap = new TreeMap<>();

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
