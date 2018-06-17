package no.uib.pap.pathwaymatcher.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

public class ListDiff {

    public static void main(String args[]) throws IOException {

        System.out.println(System.getProperty("user.dir"));

        HashSet<String> set1 = new HashSet<>();
        HashSet<String> set2 = new HashSet<>();
        HashSet<String> diff = new HashSet<>();

        FileInputStream fis = new FileInputStream("extra/SampleDatasets/AllProteoforms/ConnectionGraphProteoforms.tsv");
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));

        String line;
        while ((line = br.readLine()) != null)   {
            set1.add(line);
        }
        br.close();

        fis = new FileInputStream("extra/SampleDatasets/AllProteoforms/allMappingProteoforms.tsv");
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
}
