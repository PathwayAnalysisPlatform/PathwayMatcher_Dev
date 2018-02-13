package no.uib.pap.pathwaymatcher.vep;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.exceptions.exception_handlers.CommandLineExceptionHandler;

/**
 * This class processes an entire folder with one VEP file per chromosome.
 *
 * @author Marc Vaudel
 */
public class VepFolderProcessor {

    /**
     * A handler for the exceptions.
     */
    private ExceptionHandler exceptionHandler = new CommandLineExceptionHandler();

    /**
     * Constructor.
     */
    public VepFolderProcessor() {

    }

    /**
     * This file processes a folder with chromosome VEP files. The files must be names chrX.txt with X from 1 to 22.
     * 
     * @param folderPath the folder to process
     * @param nThreads the number of threads to use
     */
    public void processFolder(String folderPath, int nThreads) {

        try {

            // Make a pool of sequence processors
            ExecutorService pool = Executors.newFixedThreadPool(nThreads);
            for (int chr = 1; chr <= 22; chr++) {
                String vepName = "chr" + chr + ".txt";
                String outputName = "chr" + chr + "_processed.txt";
                File vepFile = new File(folderPath, vepName);
                File outputFile = new File(folderPath, outputName);
                FileProcessor fileProcessor = new FileProcessor(vepFile, outputFile);
                pool.submit(fileProcessor);
            }

            // Execute
            pool.shutdown();
            if (!pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS)) {
                throw new TimeoutException("Conversion timed out.");
            }

        } catch (Exception e) {

        }

    }

    /**
     * The main method takes a folder as input and processes the vep files
     * found. No sanity check is conducted.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {

        VepFolderProcessor vepFolderProcessor = new VepFolderProcessor();

        String folderPath = args[0];
        int nThreads = Integer.parseInt(args[1]);

        vepFolderProcessor.processFolder(folderPath, nThreads);
    }

    /**
     * This class processes a file.
     */
    private class FileProcessor implements Runnable {

        /**
         * The VEP file converter.
         */
        private VepFileConverter vepFileConverter = new VepFileConverter();
        /**
         * The VEP file to convert.
         */
        private File vepFile;
        /**
         * The output file.
         */
        private File outputFile;

        /**
         * Constructor.
         *
         * @param vepFile the VEP file
         * @param outputFile the output file
         */
        public FileProcessor(File vepFile, File outputFile) {

            this.vepFile = vepFile;
            this.outputFile = outputFile;
        }

        @Override
        public void run() {

            try {

                vepFileConverter.processFile(vepFile, outputFile);

            } catch (Exception e) {
                exceptionHandler.catchException(e);
            }
        }
    }
}
