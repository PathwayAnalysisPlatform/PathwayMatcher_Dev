package no.uib.pathwaymatcher.model.stages;

import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.model.Pair;

import java.io.*;
import java.util.zip.GZIPInputStream;

import static no.uib.pathwaymatcher.Conf.strMap;

public abstract class PreprocessorVariants extends Preprocessor {

    public static BufferedReader getBufferedReader(String path) throws FileNotFoundException, IOException {
        BufferedReader br = null;
        if (path.endsWith(".gz")) {
            InputStream fileStream = new FileInputStream(path);
            InputStream gzipStream = new GZIPInputStream(fileStream);
            Reader decoder = new InputStreamReader(gzipStream);
            br = new BufferedReader(decoder);
        } else {
            br = new BufferedReader(new FileReader(strMap.get(Conf.StrVars.input)));
        }
        return br;
    }

    protected static Pair<String, String> getRsIdAndSwissProtFromVep(String line) {
        String[] fields = line.split(" ");
        return new Pair<>(fields[Conf.intMap.get(Conf.IntVars.rsidIndex)], fields[Conf.intMap.get(Conf.IntVars.swissprotIndex)]);
    }
}
