package no.uib.pathwaymatcher.model.stages;

import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.PathwayMatcher;
import no.uib.pathwaymatcher.model.Pair;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import static no.uib.pathwaymatcher.Conf.boolMap;
import static no.uib.pathwaymatcher.Conf.strMap;
import static no.uib.pathwaymatcher.PathwayMatcher.print;
import static no.uib.pathwaymatcher.PathwayMatcher.println;
import static no.uib.pathwaymatcher.model.Error.INVALID_ROW;
import static no.uib.pathwaymatcher.util.InputPatterns.matches_Rsid;

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

    protected static Pair<String, String> getRsIdAndSwissProt(String line) {
        String[] fields = line.split(" ");
        return new Pair<>(fields[Conf.intMap.get(Conf.IntVars.rsidIndex)], fields[Conf.intMap.get(Conf.IntVars.swissprotIndex)]);
    }

    private static Pair<String, String> getRecordAndSwissProt(String line) {
        String[] fields = line.split(" ");
        String record = fields[0];
        for (int I = 1; I <= 3; I++) {
            record += " " + fields[I];
        }
        return new Pair<>(record, fields[Conf.intMap.get(Conf.IntVars.swissprotIndex)]);
    }
}
