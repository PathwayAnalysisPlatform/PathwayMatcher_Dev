package no.uib.pathwaymatcher.model.stages;

public abstract class MatcherProteoforms extends Matcher{

//        private static void getCandidateEWASWithPTMs() {
//        try {
//            //Read the list and create a set with all the possible candidate EWAS for every Protein
//            BufferedReader br = new BufferedReader(new FileReader(strMap.get(StrVars.standardFilePath.toString())));
//            String line = "";
//            while ((line = br.readLine()) != null) {
//                String[] parts = line.split(";");
//                String[] modifications = (parts.length > 1) ? parts[1].split(",") : new String[0];
//                Proteoform mp = new Proteoform();
//                mp.baseProtein = new Protein();
//                mp.baseProtein.id = parts[0];       //Set the uniprot id
//
//                for (int ptm = 0; ptm < modifications.length; ptm++) { //Set the requested PTMs
//                    String[] modParts = modifications[ptm].split(":");
//                    mp.PTMs.add(new Modification(modParts[0], modParts[1].equals("null") ? null : Integer.valueOf(modParts[1])));
//                }
//
//                //Query reactome for the candidate EWAS
//                queryForCandidateEWAS(mp);
//            }
//        } catch (FileNotFoundException ex) {
//            System.out.println("The standarized file was not found on: " + strMap.get(StrVars.standardFilePath.toString()));
//            System.exit(2);
//            //Logger.getLogger(Gatherer.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            System.out.println("Error while trying to read the file: " + strMap.get(StrVars.standardFilePath.toString()));
//            System.exit(2);
//            //Logger.getLogger(Gatherer.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

}
