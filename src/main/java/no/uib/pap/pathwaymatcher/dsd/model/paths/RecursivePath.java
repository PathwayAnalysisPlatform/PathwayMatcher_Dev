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
     * Boolean indicating whether a vertex has been traversed by this path.
     */
    private boolean[] traversedVertices;
    /**
     * The layer of recursion.
     */
    private final int layer;

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
        this.layer = subPath.getLayer() + 1;

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

        if (traversedVertices == null) {

            traversedVertices = getTraversedVertices();

        }

        return i >= traversedVertices.length ? false : traversedVertices[i];

    }

    @Override
    public boolean[] getTraversedVertices() {

        if (traversedVertices != null) {

            return traversedVertices;

        }

        boolean[] subPathTraversedVertices = subPath.getTraversedVertices();

        int newLength = subPathTraversedVertices.length > lastVertex+1 ? subPathTraversedVertices.length : lastVertex+1;

        boolean[] result = Arrays.copyOf(subPathTraversedVertices, newLength);
        result[lastVertex] = true;

        return result;

    }

    /**
     * Clears the values in cache.
     */
    public void clearCache() {

        traversedVertices = null;

    }

    @Override
    public int getLayer() {
        return layer;
    }

}
