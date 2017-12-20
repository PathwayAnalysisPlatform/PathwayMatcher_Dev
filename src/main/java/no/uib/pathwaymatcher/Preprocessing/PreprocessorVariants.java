package no.uib.pathwaymatcher.Preprocessing;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.model.Pair;
import no.uib.pathwaymatcher.model.Snp;

import java.io.*;
import java.util.zip.GZIPInputStream;

import static no.uib.pathwaymatcher.Conf.strMap;

public abstract class PreprocessorVariants extends Preprocessor {

    public BufferedReader getBufferedReader(String path) throws FileNotFoundException, IOException {
        BufferedReader br = null;
        if (path.endsWith(".gz")) {
            File file = new File(path);
            InputStream fileStream = new FileInputStream(file);
            InputStream gzipStream = new GZIPInputStream(fileStream);
            Reader decoder = new InputStreamReader(gzipStream);
            br = new BufferedReader(decoder);
        } else {
            br = new BufferedReader(new FileReader(strMap.get(Conf.StrVars.input)));
        }
        return br;
    }

    public BufferedReader getBufferedReaderFromResource(String path) throws FileNotFoundException, IOException {
        BufferedReader br = null;
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(path).getFile());
        if (path.endsWith(".gz")) {
            InputStream fileStream = new FileInputStream(file);
            InputStream gzipStream = new GZIPInputStream(fileStream);
            Reader decoder = new InputStreamReader(gzipStream);
            br = new BufferedReader(decoder);
        } else {
            br = new BufferedReader(new FileReader(file));
        }
        return br;
    }

    public static Multimap<Snp, String> getSNPAndSwissProtFromVep(String line) {
        TreeMultimap<Snp, String> mapping = TreeMultimap.create();
        String[] fields = line.split(" ");
        Integer chr = Integer.valueOf(fields[0]);
        Long bp = Long.valueOf(fields[1]);

        String[] rsids = fields[Conf.intMap.get(Conf.IntVars.rsidIndex)].split(",");
        String[] uniprots = fields[Conf.intMap.get(Conf.IntVars.swissprotIndex)].split(",");

        for(String rsid : rsids){
            for(String uniprot : uniprots){
                Snp snp = new Snp(chr, bp, rsid);
                mapping.put(snp, uniprot);
            }
        }
        return mapping;
    }
}
