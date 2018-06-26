package no.uib.pap.pathwaymatcher.dsd.model.paths;

import java.util.Arrays;
import no.uib.pap.pathwaymatcher.dsd.model.Path;

/**
 * Path based on other paths.
 *
 * @author Marc Vaudel
 */
public class RecursivePath implements Path {

    /**
     * The beginning of the path.
     */
    private final Path subPath;
    /**
     * The total path weight.
     */
    private final double totalWeight;
    /**
     * The last vertex.
     */
    private final int lastVertex;

    /**
     * Constructor.
     *
     * @param subPath the beginning of the path
     * @param newVertex the new vertex index
     * @param newWeight the new weight
     */
    public RecursivePath(Path subPath, int newVertex, double newWeight) {

        this.subPath = subPath;
        this.lastVertex = newVertex;
        this.totalWeight = subPath.getWeight() + newWeight;

    }

    @Override
    public int[] getPath() {

        int[] subPathAsArray = subPath.getPath();
        int[] newPath = Arrays.copyOf(subPathAsArray, subPathAsArray.length + 1);
        newPath[subPathAsArray.length] = lastVertex;
        return newPath;

    }

    @Override
    public double getWeight() {

        return totalWeight;

    }

    @Override
    public int getStart() {

        return subPath.getStart();

    }

    @Override
    public int getEnd() {
        return lastVertex;
    }

    @Override
    public int length() {

        return subPath.length() + 1;

    }

    @Override
    public boolean contains(int i) {

        return i == lastVertex || subPath.contains(i);

    }

}
