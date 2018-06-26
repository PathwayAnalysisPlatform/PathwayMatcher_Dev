package no.uib.pap.pathwaymatcher.dsd.model.paths;

import java.util.Arrays;
import no.uib.pap.pathwaymatcher.dsd.model.Path;

/**
 * Path made of two paths.
 *
 * @author Marc Vaudel
 */
public class DoublePath implements Path {
    
    /**
     * The start of the path.
     */
    private final Path pathStart;
    /**
     * The end of the path
     */
    private final Path pathEnd;
    /**
     * Constructor.
     * 
     * @param pathStart the start of the path
     * @param pathEnd the end of the path
     */
    public DoublePath(Path pathStart, Path pathEnd) {
        
        this.pathStart = pathStart;
        this.pathEnd = pathEnd;
        
    }

    @Override
    public int[] getPath() {
        
        int[] path1 = pathStart.getPath();
        int[] path2 = pathEnd.getPath();
        
        int[] newPath = Arrays.copyOf(path1, path1.length + path2.length);
        System.arraycopy(path2, 0, newPath, path1.length, path2.length);
        
        return newPath;
    }

    @Override
    public double getWeight() {
        return pathStart.getWeight() + pathEnd.getWeight();
    }

    @Override
    public int getStart() {
        return pathStart.getStart();
    }

    @Override
    public int getEnd() {
        return pathEnd.getEnd();
    }

    @Override
    public int length() {
        return pathStart.length() + pathEnd.length();
    }

    @Override
    public boolean contains(int i) {
        return pathStart.contains(i) || pathEnd.contains(i);
    }

}
