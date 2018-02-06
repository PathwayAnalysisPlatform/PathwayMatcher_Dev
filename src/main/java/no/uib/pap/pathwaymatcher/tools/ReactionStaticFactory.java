package no.uib.pap.pathwaymatcher.tools;

import no.uib.pap.model.Pathway;
import no.uib.pap.model.Reaction;

import java.util.TreeMap;

/**
 * Keeps a list of known reactions to avoid duplications.
 */
public class ReactionStaticFactory {

    /**
     * The list of known reactions. Maps from reaction stable identifiers to reaction instances.
     */
    private static TreeMap<String, Reaction> reactionSet;

    /**
     * Initializes the pathway list data structure
     */
    public static void initialize(){
        reactionSet = new TreeMap<>();
    }

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
