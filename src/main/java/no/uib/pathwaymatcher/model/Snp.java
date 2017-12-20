package no.uib.pathwaymatcher.model;

import java.util.Iterator;
import java.util.Map;

public class Snp implements Comparable<Snp> {
    private String rsid;
    private Integer chr;    // Chromosome
    private Long bp;    // Base pair

    public Snp(String rsid) {
        this.rsid = rsid;
        this.chr = null;
        this.bp = null;
    }

    public Snp(Integer chr, Long bp) {
        this.rsid = null;
        this.chr = chr;
        this.bp = bp;
    }

    public Snp(Integer chr, Long bp, String rsid) {
        this.rsid = rsid;
        this.chr = chr;
        this.bp = bp;
    }

    public String getRsid() {
        return rsid;
    }

    public void setRsid(String rsid) {
        this.rsid = rsid;
    }

    public Integer getChr() {
        return chr;
    }

    public void setChr(Integer chr) {
        this.chr = chr;
    }

    public Long getBp() {
        return bp;
    }

    public void setBp(Long bp) {
        this.bp = bp;
    }

    @Override
    public int hashCode() {
        return this.rsid != null ? this.rsid.hashCode() : 0;
    }

    /**
     * Two snps are equal if the rsid is the same or both the chromosome and base pair are the same.
     * If the chromosome and base pair are null in both snps, then they do not count as equal.
     *
     * @param obj The object instance to compare with this
     * @return If they are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Snp that = (Snp) obj;

        // If one of the snps just contains rsid
        if ((this.chr == null && this.bp == null) || (that.chr == null && that.bp == null)) {
            //Then if the rsid is the same, snps are equal
            return (this.rsid != null ? rsid.equals(that.rsid) : that.rsid == null);
        }
        // If one of the snps does not contain rsid
        else if (this.getRsid() == null || that.getRsid() == null) {
            //Then if both the chr and the chr and bp are the same, snps are equal
            if (this.chr != null ? !chr.equals(that.chr) : that.chr != null) return false;
            return (this.bp != null ? bp.equals(that.bp) : that.bp == null);
        } else {
            //If both snps have chr, bp and rsid
            if (this.chr != null ? !chr.equals(that.chr) : that.chr != null) return false;
            if (this.bp != null ? !bp.equals(that.bp) : that.bp != null) return false;
            return (this.rsid != null ? rsid.equals(that.rsid) : that.rsid == null);
        }
    }

    /**
     * Compares two Snp to decide how to order them.
     *
     * @param that
     * @return before = -1, equal = 0, after = 1
     */
    @Override
    public int compareTo(Snp that) {

        if (!this.equals(that)) {
            if (!(this.chr == null && that.chr == null)) {
                if (this.chr == null) {
                    return -1;
                }
                if (that.chr == null) {
                    return 1;
                }
                if (this.chr != that.chr) {
                    return Integer.compare(this.chr, that.chr);
                }
            }

            if (!(this.bp == null && that.bp == null)) {
                if (this.bp == null) {
                    return -1;
                }
                if (that.bp == null) {
                    return 1;
                }
                if (this.bp != that.bp) {
                    return Long.compare(this.bp, that.bp);
                }
            }

            if (!(this.rsid == null && that.rsid == null)) {
                if (this.rsid == null) {
                    return -1;
                }
                if (that.rsid == null) {
                    return 1;
                }
                if (this.rsid != that.rsid) {
                    return this.rsid.compareTo(that.rsid);
                }
            }
        }

        assert this.equals(that) : "Check consistency with equals";

        return 0;
    }

    /**
     * Gets snp instance from a string line. The line can contain an rsid or a duple of chromosome and base pair.
     *
     * @param line The line with the snp attributes as string
     * @return The snp instance according to the line
     */
    public static Snp getSnp(String line) {
        String[] fields = line.split(" ");
        Snp snp = null;
        if (fields.length == 1) {
            snp = new Snp(fields[0]);
        } else if (fields.length == 2) {
            Integer chr = Integer.valueOf(fields[0]);
            Long bp = Long.valueOf(fields[1]);
            snp = new Snp(chr, bp);
        }
        return snp;
    }
}
