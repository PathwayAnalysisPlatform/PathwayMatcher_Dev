package no.uib.pathwaymatcher.model;

import com.google.common.collect.TreeMultimap;
import no.uib.pathwaymatcher.tools.Parser;
import no.uib.pathwaymatcher.tools.ParserProteoformNeo4j;
import no.uib.pathwaymatcher.tools.ParserProteoformPRO;
import no.uib.pathwaymatcher.tools.ParserProteoformSimple;

import java.util.Iterator;
import java.util.Map;

/**
 * @author Luis Francisco Hernández Sánchez
 */
public class Proteoform implements Comparable<Proteoform> {
    private String UniProtAcc;          // The uniprot accession number including the optional isoform
    private Long startCoordinate;       // The start coordinate of the protein subsequence
    private Long endCoordinate;         // The end coordinate of the protein subsequence
    private TreeMultimap<String, Long> ptms; // The list of post-translational modifications: PSI-MOD type -> Sites set
    // * This structure can not take "null" as a value, then when the coordinates are null they are represented as -1.

    public Proteoform(String uniProtAcc) {
        UniProtAcc = uniProtAcc;
        startCoordinate = -1L;
        endCoordinate = -1L;
        ptms = TreeMultimap.create();
    }

    public Proteoform(String uniProtAcc, TreeMultimap<String, Long> ptms) {
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

    public Long getStartCoordinate() {
        return startCoordinate;
    }

    public void setStartCoordinate(Long startCoordinate) {
        this.startCoordinate = startCoordinate;
    }

    public Long getEndCoordinate() {
        return endCoordinate;
    }

    public void setEndCoordinate(Long endCoordinate) {
        this.endCoordinate = endCoordinate;
    }

    public TreeMultimap<String, Long> getPtms() {
        return ptms;
    }

    public void setPtms(TreeMultimap<String, Long> ptms) {
        this.ptms = ptms;
    }

    public String toString(Parser.ProteoformFormat format) {

        Parser parser;
        switch (format) {
            case SIMPLE:
                parser = new ParserProteoformSimple();
                return parser.getString(this);
            case PRO:
                parser = new ParserProteoformPRO();
                return parser.getString(this);
            case NEO4J:
                parser = new ParserProteoformNeo4j();
                return parser.getString(this);
            default:
                return UniProtAcc + "," + startCoordinate + "-" + endCoordinate + "," + ptms.toString();
        }
    }

    public void addPtm(String s, Long site) {
        if (site == null) {
            site = -1L;
        }
        ptms.put(s, site);
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

        Iterator<Map.Entry<String, Long>> itThis = this.ptms.entries().iterator();
        Iterator<Map.Entry<String, Long>> itThat = that.ptms.entries().iterator();
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
