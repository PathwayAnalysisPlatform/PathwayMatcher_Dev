package no.uib.pathwaymatcher.model;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Ordering;
import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.tools.*;

import java.util.*;

import static no.uib.pathwaymatcher.tools.Parser.interpretCoordinateFromLongToString;
import static no.uib.pathwaymatcher.tools.Parser.interpretCoordinateFromStringToLong;

/**
 * @author Luis Francisco Hernández Sánchez
 */
public class Proteoform implements Comparable<Proteoform> {
    private String UniProtAcc;          // The uniprot accession number including the optional isoform
    private Long startCoordinate;       // The start coordinate of the protein subsequence
    private Long endCoordinate;         // The end coordinate of the protein subsequence
    private LinkedListMultimap<String, Long> ptms; // The list of post-translational modifications: PSI-MOD type -> Sites set
    // * This structure can not take "null" as a value, then when the coordinates are null they are represented as -1.

    public Proteoform(String uniProtAcc) {
        UniProtAcc = uniProtAcc;
        startCoordinate = -1L;
        endCoordinate = -1L;
        ptms = LinkedListMultimap.create();
    }

    public Proteoform(String uniProtAcc, LinkedListMultimap<String, Long> ptms) {
        UniProtAcc = uniProtAcc;
        startCoordinate = -1L;
        endCoordinate = -1L;
        this.ptms = ptms;
    }

    public String getUniProtAcc() {
        return UniProtAcc;
    }

    public void setUniProtAcc(String uniProtAcc) {
        UniProtAcc = uniProtAcc;
    }

    /********************/
    public void setStartCoordinate(Long startCoordinate) {
        this.startCoordinate = startCoordinate == null ? -1L : startCoordinate;
    }

    public Long getStartCoordinate() {
        return this.startCoordinate == -1L ? null : this.startCoordinate;
    }

    public void setStringStartCoordinate(String coordinate) {
        this.startCoordinate = interpretCoordinateFromStringToLong(coordinate);
    }

    public String getStringStartCoordinate() {
        return interpretCoordinateFromLongToString(this.startCoordinate);
    }

    /***************************/

    public void setEndCoordinate(Long endCoordinate) {
        this.endCoordinate = endCoordinate == null ? -1L : endCoordinate;
    }

    public Long getEndCoordinate() {
        return this.endCoordinate == -1L ? null : this.endCoordinate;
    }

    public void setStringEndCoordinate(String coordinate) {
        this.endCoordinate = interpretCoordinateFromStringToLong(coordinate);
    }

    public String getStringEndCoordinate() {
        return interpretCoordinateFromLongToString(this.endCoordinate);
    }


    public ListMultimap<String, Long> getPtms() {
        return this.ptms;
    }

//    public List<Map.Entry<String, Long>> getPtmsSorted(){
//        List<Map.Entry<String, Long>> sortedPtms = new ArrayList<>(this.ptms.entries());
//        Collections.sort(sortedPtms, (o1, o2) -> {
//            if (o1.getKey() != o2.getKey()){
//                return o1.getKey().compareTo(o2.getKey());
//            }
//            return (o1.getValue()).compareTo(o2.getValue());
//        });
//        return sortedPtms;
//    }

    public void setPtms(LinkedListMultimap<String, Long> ptms) {
        this.ptms = ptms;
    }

    public String toString(Conf.ProteoformFormat format) {

        Parser parser = ParserFactory.createParser(format);
        if (parser != null) {
            return parser.getString(this);
        } else {
            return UniProtAcc + "," + startCoordinate + "-" + endCoordinate + "," + ptms.toString();
        }
    }

    public void addPtm(String motType, Long coordinate) {
        if (coordinate == null) {
            coordinate = -1L;
        }
        ptms.put(motType, coordinate);
    }

    public void sortPtms(){
        List<Map.Entry<String, Long>> sortedPtms = new ArrayList<>(this.ptms.entries());
        Collections.sort(sortedPtms, (o1, o2) -> {
            if (o1.getKey() != o2.getKey()){
                return o1.getKey().compareTo(o2.getKey());
            }
            return (o1.getValue()).compareTo(o2.getValue());
        });
        this.ptms.clear();
        ptms = LinkedListMultimap.create();
        for(Map.Entry<String, Long> entry : sortedPtms){
            this.ptms.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public int hashCode() {
        return UniProtAcc != null ? UniProtAcc.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Proteoform that = (Proteoform) obj;

        //noinspection RedundantIfStatement
        if (UniProtAcc != null ? !UniProtAcc.equals(that.UniProtAcc) : that.UniProtAcc != null) return false;

        if (startCoordinate != null ? !startCoordinate.equals(that.startCoordinate) : that.startCoordinate != null)
            return false;

        if (endCoordinate != null ? !endCoordinate.equals(that.endCoordinate) : that.endCoordinate != null)
            return false;

        // Verify the number of ptms is equal
        if (ptms != null ? ptms.size() != that.getPtms().size() : that.getPtms() != null) return false;

        // Verify the ptms are all equal
        for (Map.Entry<String, Long> entry : ptms.entries()) {
            if (!that.getPtms().containsEntry(entry.getKey(), entry.getValue())) return false;
        }

        return true;
    }

    /**
     * Compares two proteoforms to decide how to order them.
     *
     * @param that
     * @return before = -1, equal = 0, after = 1
     */
    @Override
    public int compareTo(Proteoform that) {
        if (this.equals(that)) {
            return 0;
        }

        if (!this.UniProtAcc.equals(that.UniProtAcc)) {
            return this.UniProtAcc.compareTo(that.UniProtAcc);
        }


        // If both are not null
        if (this.startCoordinate != null && that.startCoordinate != null) {
            if (!this.startCoordinate.equals(that.startCoordinate)) {
                return this.startCoordinate.compareTo(that.startCoordinate);
            }
        }

        // If one is null
        if (this.startCoordinate == null && that.startCoordinate != null) {
            return -1;
        } else if (this.startCoordinate != null && that.startCoordinate == null) {
            return 1;
        }

        // If both are not null
        if (this.endCoordinate != null && that.endCoordinate != null) {
            if (!this.endCoordinate.equals(that.endCoordinate)) {
                return this.endCoordinate.compareTo(that.endCoordinate);
            }
        }

        // If one is null
        if (this.endCoordinate == null && that.endCoordinate != null) {
            return -1;
        } else if (this.endCoordinate != null && that.endCoordinate == null) {
            return 1;
        }

        // If they have different number of ptms
        if (this.ptms.entries().size() != that.ptms.entries().size()) {
            return Integer.compare(this.ptms.size(), that.ptms.size());
        }

        //If both have no ptms
        if (this.ptms.size() == 0) {
            return 0;
        }

        Iterator<Map.Entry<String, Long>> itThis = this.getPtms().entries().iterator();
        Iterator<Map.Entry<String, Long>> itThat = that.getPtms().entries().iterator();
        while (itThis.hasNext() && itThat.hasNext()) {
            Map.Entry<String, Long> thisPtm = itThis.next();
            Map.Entry<String, Long> thatPtm = itThat.next();

            if (!thisPtm.getKey().equals(thatPtm.getKey())) {
                return thisPtm.getKey().compareTo(thatPtm.getKey());
            }
            if (!thisPtm.getValue().equals(thatPtm.getValue())) {
                return thisPtm.getValue().compareTo(thatPtm.getValue());
            }
        }

        return 0;
    }
}
