package no.uib.pap.pathwaymatcher.Preprocessing;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import no.uib.pap.model.Snp;
import no.uib.pap.pathwaymatcher.Conf;

import static no.uib.pap.pathwaymatcher.Conf.strMap;

import java.io.*;
import java.util.zip.GZIPInputStream;

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

    public BufferedReader getBufferedReaderFromResource(String fileName) throws FileNotFoundException, IOException {

        BufferedReader br = null;
        InputStream fileStream = ClassLoader.getSystemResourceAsStream(fileName);
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream);
        br = new BufferedReader(decoder);

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
