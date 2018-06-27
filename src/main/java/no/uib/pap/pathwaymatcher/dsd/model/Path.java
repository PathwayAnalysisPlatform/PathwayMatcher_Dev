package no.uib.pap.pathwaymatcher.dsd.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import no.uib.pap.pathwaymatcher.dsd.model.paths.SimplePath;

/**
 * Interface for a path.
 *
 * @author Marc Vaudel
 */
public interface Path {

    /**
     * Returns the path as int array.
     *
     * @return the path as int array
     */
    public int[] getPath();

    /**
     * Returns the weight;
     *
     * @return the weight
     */
    public double getWeight();

    /**
     * Returns the start index of the path.
     *
     * @return the start index of the path
     */
    public int getStart();

    /**
     * Returns the end index of the path.
     *
     * @return the end index of the path
     */
    public int getEnd();

    /**
     * Returns the number of vertices in the path.
     *
     * @return the number of vertices in the path
     */
    public int length();

    /**
     * Returns a boolean indicating whether the given index corresponds to a
     * vertex in the path.
     *
     * @param i the index of the vertex
     *
     * @return a boolean indicating whether the given index corresponds to a
     * vertex in the path
     */
    public boolean contains(int i);
    
    /**
     * Returns an array indicating whether a vertex is traversed by this path.
     * 
     * @return an array indicating whether a vertex is traversed by this path
     */
    public boolean[] getTraversedVertices();

    /**
     * Returns the array of vertices indexes in this path as a string.
     *
     * @param path the path as int array
     * 
     * @return the array of vertices indexes in this path as a string
     */
    public static String getPathToString(int[] path) {
        
        return Arrays.stream(path)
                .mapToObj(i -> Integer.toString(i))
                .collect(Collectors.joining("-", "[", "]"));
        
    }

    /**
     * Parses a path from a char array.
     *
     * @param pathAsArray the path as char array
     *
     * @return the array of vertices
     */
    public static int[] parsePathFromString(char[] pathAsArray) {

        char sep = '-';
        int lastSeparator = 0;
        ArrayList<Integer> path = new ArrayList<>(4);

        for (int i = 1; i < pathAsArray.length - 1; i++) {

            if (pathAsArray[i] == sep) {

                String subString = new String(Arrays.copyOfRange(pathAsArray, lastSeparator + 1, i));

                int vertex = Integer.parseInt(subString);

                lastSeparator = i;

                path.add(vertex);

            }
        }

        String subString = new String(Arrays.copyOfRange(pathAsArray, lastSeparator + 1, pathAsArray.length - 1));
        int vertex = Integer.parseInt(subString);
        path.add(vertex);

        return path.stream()
                .mapToInt(a -> a)
                .toArray();
    }

}
