package no.uib.pap.pathwaymatcher.dsd.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import no.uib.pap.pathwaymatcher.dsd.model.Path;

/**
 * File used to write and retrieve paths.
 *
 * @author Marc Vaudel
 */
public class PathFile {

    /**
     * Encoding, cf the second rule.
     */
    public static final String encoding = "UTF-8";
    /**
     * The separator to use for the different columns.
     */
    public static final char separator = ' ';
    /**
     * The separator to use for the different columns.
     */
    public static final String separatorString = Character.toString(separator);
    /**
     * The end of line string.
     */
    public static final String eol = System.getProperty("line.separator");
    /**
     * The number of paths to keep in cache.
     */
    public static final int cacheSize = 10;
    /**
     * The random access file to use.
     */
    private final RandomAccessFile raf;
    /**
     * The channel of the file.
     */
    private final FileChannel fc;
    /**
     * The number of vertices of the graph.
     */
    private final int nVertices;
    /**
     * The index of each vertex.
     */
    private final int[] startIndexes;
    /**
     * The length of each vertex line.
     */
    private final int[] lineLengths;
    /**
     * Mutex for the file writing.
     */
    private final Semaphore fileMutex = new Semaphore(1);
    /**
     * Mutex for the cache writing.
     */
    private final Semaphore cacheMutex = new Semaphore(1);
    /**
     * The cache for paths to keep in memory.
     */
    private final HashMap<Integer, Path> cache = new HashMap<>(cacheSize);
    /**
     * The list of vertices indexes in cache.
     */
    private final LinkedList<Integer> cacheContent = new LinkedList<>();
    /**
     * Current line index.
     */
    private int index = 0;

    /**
     * Constructor.
     *
     * @param destinationFile The file to write to
     * @param nVertices the number of vertices in the graph.
     *
     * @throws IOException exception thrown if an error occurred while writing
     * to the file
     */
    public PathFile(File destinationFile, int nVertices) throws IOException {

        raf = new RandomAccessFile(destinationFile, "rw");
        fc = raf.getChannel();

        writeHeader();

        this.nVertices = nVertices;
        int nPaths = nVertices * nVertices;
        startIndexes = new int[nPaths];
        lineLengths = new int[nPaths];

    }

    /**
     * Writes the header.
     *
     * @throws IOException exception thrown if an error occurred while writing
     * to the file
     */
    public void writeHeader() throws IOException {

        String header = String.join(separatorString, "from", "to", "weight", "length", "path");

        String lineEol = String.join("", header, eol);
        byte[] compressedLine = deflate(lineEol);
        writeLine(compressedLine);

    }

    /**
     * Adds a path to the file.
     *
     * @param path the path to write
     *
     * @throws IOException exception thrown if an error occurred while writing
     * to the file
     * @throws InterruptedException exception thrown if a thread gets
     * interrupted
     */
    public void addPath(Path path) throws IOException, InterruptedException {

        int pathIndex = getPathIndex(path.getStart(), path.getEnd());
        String line = String.join(separatorString,
                Integer.toString(path.getStart()),
                Integer.toString(path.getEnd()),
                Double.toString(path.weight),
                Integer.toString(path.length()),
                path.getPathToString());

        String lineEol = String.join("", line, eol);
        byte[] compressedLine = deflate(lineEol);
        int lineIndex = index;

        fileMutex.acquire();

        writeLine(compressedLine);

        fileMutex.release();

        startIndexes[pathIndex] = lineIndex;
        lineLengths[pathIndex] = compressedLine.length;

    }

    /**
     * Returns the path if in the file, null otherwise.
     *
     * @param start the start index of the path
     * @param end the end index of the path
     *
     * @return the path
     *
     * @throws IOException exception thrown if an error occurred while reading
     * the file
     * @throws InterruptedException exception thrown if a thread gets
     * interrupted
     * @throws DataFormatException exception thrown if the data format is not
     * supported
     */
    public Path getPath(int start, int end) throws IOException, InterruptedException, DataFormatException {

        int pathIndex = getPathIndex(start, end);

        Path path = cache.get(pathIndex);

        if (path != null) {

            return path;

        }

        int startIndex = startIndexes[pathIndex];

        if (startIndex == 0) {

            return null;

        }

        int lineLength = lineLengths[pathIndex];

        byte[] compressedLine = new byte[lineLength];

        MappedByteBuffer buffer = fc.map(MapMode.READ_WRITE, startIndex, lineLength);

        for (int k = 0; k < lineLength; k++) {

            compressedLine[k] = buffer.get(k);

        }

        String line = inflate(compressedLine);
        path = getPath(line);

        if (path.getStart() != start) {
            throw new IllegalArgumentException("Incorrect start for path " + line + ". " + start + " expected.");
        }
        if (path.getEnd() != end) {
            throw new IllegalArgumentException("Incorrect end for path " + line + ". " + end + " expected.");
        }

        cacheMutex.acquire();

        if (cache.size() == cacheSize) {

            int key = cacheContent.pollFirst();
            cache.remove(key);

        }

        cache.put(pathIndex, path);
        cacheContent.add(pathIndex);

        cacheMutex.release();

        return path;

    }

    /**
     * Writes the given line to the file.
     *
     * @param line the line to write
     * @param pathIndex the index of the path
     *
     * @throws IOException exception thrown if an error occurred while writing
     * to the file
     */
    private void writeLine(byte[] compressedLine) throws IOException {

        MappedByteBuffer buffer = fc.map(MapMode.READ_WRITE, index, compressedLine.length);

        for (int i = 0; i < compressedLine.length; i++) {

            buffer.put(i, (byte) compressedLine[i]);

        }

        index += compressedLine.length;

    }

    /**
     * Returns the internal path index.
     *
     * @param start the start index of the path
     * @param end the end index of the path
     *
     * @return the internal path index
     */
    private int getPathIndex(int start, int end) {

        return start * nVertices + end;

    }

    /**
     * Parses a path from the given line.
     *
     * @param line the line as char array
     *
     * @return the corresponding path
     */
    public static Path getPath(String line) {

        char[] lineAsCharArray = line.toCharArray();

        double weight = Double.NaN;

        int nSeparators = 0;
        int lastSeparator = -1;

        for (int i = 0; i < lineAsCharArray.length; i++) {

            if (lineAsCharArray[i] == separator) {

                nSeparators++;

                if (nSeparators == 3) {

                    String subString = new String(Arrays.copyOfRange(lineAsCharArray, lastSeparator + 1, i));
                    weight = Double.parseDouble(subString);

                }

                lastSeparator = i;

                if (nSeparators == 4) {

                    break;

                }
            }
        }

        char[] subString = Arrays.copyOfRange(lineAsCharArray, lastSeparator + 1, lineAsCharArray.length - eol.length());
        int[] path = Path.parsePathFromString(subString);

        return new Path(path, weight);

    }

    /**
     * Deflates a line.
     *
     * @param line the line to deflate
     *
     * @return the deflated line as byte array
     *
     * @throws UnsupportedEncodingException exception thrown if the encoding is
     * not supported
     */
    public byte[] deflate(String line) throws UnsupportedEncodingException {

        byte[] input = line.getBytes(encoding);

        byte[] output = new byte[8 * line.length()];
        Deflater deflater = new Deflater();
        deflater.setInput(input);
        deflater.finish();
        int newLength = deflater.deflate(output);

        output = Arrays.copyOf(output, newLength);

        return output;

    }

    /**
     * Inflates a line.
     *
     * @param input the deflated line
     *
     * @return the inflated line
     *
     * @throws DataFormatException exception thrown if the data format is not
     * supported
     * @throws UnsupportedEncodingException exception thrown if the encoding is
     * not supported
     */
    public String inflate(byte[] input) throws DataFormatException, UnsupportedEncodingException {

        Inflater inflater = new Inflater();
        inflater.setInput(input);
        byte[] result = new byte[8 * input.length];
        int newLength = inflater.inflate(result);
        inflater.end();

        return new String(result, 0, newLength, encoding);

    }

    /**
     * Closes the file.
     *
     * @throws IOException exception thrown if an error occurred while closing
     * the file
     */
    public void close() throws IOException {
        raf.close();
    }
}
