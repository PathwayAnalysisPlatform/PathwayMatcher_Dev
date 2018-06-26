package no.uib.pap.pathwaymatcher.dsd.model.paths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Spliterator;
import java.util.stream.Collectors;
import no.uib.pap.pathwaymatcher.dsd.model.Path;

/**
 * Simple model for a path.
 *
 * @author Marc Vaudel
 */
public class SimplePath implements Path {

    /**
     * Array of vertices indexes traversed by this path.
     */
    private final int[] path;
    /**
     * Total weight of the path.
     */
    private final double weight;

    /**
     * Constructor.
     *
     * @param path Array of vertices indexes traversed by this path
     * @param weight Total weight of the path
     */
    public SimplePath(int[] path, double weight) {

        this.path = path;
        this.weight = weight;

    }

    @Override
    public int[] getPath() {
        return path;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public int getStart() {

        return path[0];

    }

    @Override
    public int getEnd() {

        return path[path.length - 1];

    }

    @Override
    public int length() {

        return path.length;

    }

    @Override
    public boolean contains(int i) {

        return Arrays.stream(path)
                .anyMatch(j -> i == j);

    }

}
