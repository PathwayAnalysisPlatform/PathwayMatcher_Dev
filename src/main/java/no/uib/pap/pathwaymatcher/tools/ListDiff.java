package no.uib.pap.pathwaymatcher.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListDiff {

    public static void main(String args[]) throws IOException {

        System.out.println(System.getProperty("user.dir"));

        HashSet<String> set1 = new HashSet<>();
        HashSet<String> set2 = new HashSet<>();
        HashSet<String> diff = new HashSet<>();

        FileInputStream fis = new FileInputStream("resources/networks/datasets/all_proteoforms/proteinVertices.tsv");
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));

        String line;
        while ((line = br.readLine()) != null)   {
            set1.add(line);
        }
        br.close();

        fis = new FileInputStream("resources/networks/datasets/all_proteins/proteinVertices.tsv");
        br = new BufferedReader(new InputStreamReader(fis));

        while ((line = br.readLine()) != null)   {
            set2.add(line);
        }
        br.close();

        if(set1.size() > set2.size()){
            set1.removeAll(set2);
            diff = set1;
        }
        else{
            set2.removeAll(set1);
            diff = set2;
        }

        for(String proteoform : diff){
            System.out.println(proteoform);
        }
    }

    public static boolean anyContains(String substring, Collection<String> collection) {
        for(String s : collection) {
            if(s.contains(substring)) {
                return true;
            }
        }
        return false;
    }

    public static boolean anyMatches(String pattern_str, Collection<String> collection) {
        Pattern pattern = Pattern.compile(pattern_str);

        for(String s : collection) {
            Matcher matcher = pattern.matcher(s);
            if(matcher.find()) {
                return true;
            }
        }
        return false;
    }
}
