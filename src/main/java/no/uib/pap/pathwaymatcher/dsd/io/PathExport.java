package no.uib.pap.pathwaymatcher.dsd.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;
import no.uib.pap.pathwaymatcher.dsd.model.Graph;
import no.uib.pap.pathwaymatcher.dsd.model.Path;
import no.uib.pap.pathwaymatcher.dsd.model.Vertex;

/**
 * This class exports paths.
 *
 * @author Marc Vaudel
 */
public class PathExport {

    /**
     * Encoding, cf the second rule.
     */
    public static final String encoding = "UTF-8";
/**
 * The separator to use for the different columns.
 */
    public static final String separator = " ";
/**
 * The writer to use to write the files.
 */
    private final BufferedWriter bw;
/**
 * The path matrix to export.
 */
    private final Path[][] pathMatrix;
/**
 * The graph of interest.
 */
    private final Graph graph;

    /**
     * Constructor.
     * 
     * @param pathMatrix The path matrix to export
     * @param graph The graph of interest
     * @param destinationFile The file where to write the output
     * 
     * @throws IOException Exception thrown if an error occurred while writing to the file.
     */
    public PathExport(Path[][] pathMatrix, Graph graph, File destinationFile) throws IOException {

        this.pathMatrix = pathMatrix;
        this.graph = graph;

        FileOutputStream fileStream = new FileOutputStream(destinationFile);
        GZIPOutputStream gzipStream = new GZIPOutputStream(fileStream);
        OutputStreamWriter encoder = new OutputStreamWriter(gzipStream, encoding);
        bw = new BufferedWriter(encoder);

    }

    /**
     * Writes the export.
     * 
     * @throws IOException Exception thrown if an error occurred while writing to the file.
     */
    public void writeExport() throws IOException {

        String header = String.join(separator, "i", "j", "from", "to", "weight", "length", "path");
        bw.write(header);
        bw.newLine();

        int nVertices = graph.vertices.length;

        for (int i = 0; i < nVertices; i++) {
            for (int j = 0; j < nVertices; j++) {

                if (i != j) {

                    Path shortestPath = pathMatrix[i][j];

                    if (shortestPath != null) {

                        String line = String.join(separator,
                                Integer.toString(i),
                                Integer.toString(j),
                                Integer.toString(shortestPath.getStart()),
                                Integer.toString(shortestPath.getEnd()),
                                Double.toString(shortestPath.weight),
                                Integer.toString(shortestPath.length()),
                                shortestPath.getPathToString());

                        bw.write(line);
                        bw.newLine();

                    }
                }
            }
        }
    }
    
    /**
     * Closes the writer.
     * 
     * @throws IOException Exception thrown if an error occurred while writing to the file.
     */
    public void close() throws IOException {
        bw.close();
    }

}
