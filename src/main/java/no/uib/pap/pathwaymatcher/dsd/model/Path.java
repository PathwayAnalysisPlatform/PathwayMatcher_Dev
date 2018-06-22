package no.uib.pap.pathwaymatcher.dsd.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Simple model for a path.
 *
 * @author Marc Vaudel
 */
public class Path {
    
    /**
     * Array of vertices indexes traversed by this path.
     */
    public final int[] path;
    /**
     * HashSet of vertices indexes traversed by this path.
     */
    public final HashSet<Integer> pathSet;
    /**
     * Total weight of the path.
     */
    public final double weight;
    
    /**
     * Constructor.
     * 
     * @param path Array of vertices indexes traversed by this path
     * @param weight Total weight of the path
     */
    public Path(int[] path, double weight) {
        
        this.path = path;
        this.weight = weight;
        
        this.pathSet = Arrays.stream(path)
                .boxed()
                .collect(Collectors.toCollection(HashSet::new));
        
    }
    
    /**
     * Returns the start index of the path.
     * 
     * @return the start index of the path
     */
    public int getStart() {
        
        return path[0];
    
    }
    
    /**
     * Returns the end index of the path.
     * 
     * @return the end index of the path
     */
    public int getEnd() {
        
        return path[path.length - 1];
        
    }
    
    /**
     * Returns the number of vertices in the path.
     * 
     * @return the number of vertices in the path
     */
    public int length() {
        
        return path.length;
    
    }
    
    /**
     * Returns a boolean indicating whether the given index corresponds to a vertex in the path.
     * 
     * @param i the index of the vertex
     * 
     * @return a boolean indicating whether the given index corresponds to a vertex in the path
     */
    public boolean contains(int i) {
        
        return pathSet.contains(i);
        
    }
    
    /**
     * Returns the array of vertices indexes in this path as a string.
     * 
     * @return the array of vertices indexes in this path as a string
     */
    public String getPathToString() {
        
        return Arrays.stream(path)
                .mapToObj(i -> Integer.toString(i))
                .collect(Collectors.joining("-", "[", "]"));
        
    }
    
    /**
     * Concatenates two paths.
     * 
     * @param path1 the first path
     * @param path2 the second path
     * 
     * @return a path corresponding to the concatenation of path1 and path2
     */
    public static Path concat(Path path1, Path path2) {
        
        int[] newPath = Arrays.copyOf(path1.path, path1.length() + path2.length()-1);
        System.arraycopy(path2.path, 1, newPath, path1.length(), path2.length()-1);
        
        double newWeight = path1.weight + path2.weight;
        
        return new Path(newPath, newWeight);
        
    }

}
