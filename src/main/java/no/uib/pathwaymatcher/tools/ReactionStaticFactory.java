package no.uib.pathwaymatcher.tools;

import no.uib.pathwaymatcher.model.Pathway;
import no.uib.pathwaymatcher.model.Reaction;

import java.util.TreeMap;

public class ReactionStaticFactory {
    private static TreeMap<String, Reaction> reactionSet = new TreeMap<>();

    /**
     * Gets Reaction instance that corresponds to the stId and displayName.
     * If the instance is requested for the first time, then a new instance is created.
     * If the instance is requested again it returns the instance created before.
     *
     * @param stId
     * @param displayName
     * @return
     */
    public static Reaction getInstance(String stId, String displayName) {
        if (!reactionSet.containsKey(stId)) {
            Reaction reaction = new Reaction(stId, displayName);
            reactionSet.put(stId, reaction);
        }
        return reactionSet.get(stId);
    }

    public static void clear(){
        reactionSet.clear();
    }
}
