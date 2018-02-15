package no.uib.pap.pathwaymatcher.Preprocessing;

import static no.uib.pap.pathwaymatcher.Conf.strMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import no.uib.pap.model.Snp;
import no.uib.pap.pathwaymatcher.Conf;
import no.uib.pap.pathwaymatcher.PathwayMatcher14;

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

        String[] rsids = fields[PathwayMatcher14.rsidColumnIndex].split(",");
        String[] uniprots = fields[PathwayMatcher14.swissprotColumnIndex].split(",");

        for(String rsid : rsids){
            for(String uniprot : uniprots){
                Snp snp = new Snp(chr, bp, rsid);
                mapping.put(snp, uniprot);
            }
        }
        return mapping;
    }
}
