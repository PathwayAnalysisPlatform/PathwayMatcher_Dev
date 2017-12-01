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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Snp that = (Snp) obj;

        //noinspection RedundantIfStatement
        if (this.rsid != null ? !rsid.equals(that.rsid) : that.rsid != null) return false;

        if (this.bp != null ? !bp.equals(that.bp) : that.bp != null) return false;

        if (this.chr != null ? !chr.equals(that.chr) : that.chr != null) return false;

        return true;
    }

    /**
     * Compares two Snp to decide how to order them.
     *
     * @param that
     * @return before = -1, equal = 0, after = 1
     */
    @Override
    public int compareTo(Snp that) {

        if (this == that) {
            return 0;
        }

        if (this.chr != that.chr){
            return Integer.compare(this.chr, that.chr);
        }

        if(this.bp != that.bp){
            return Long.compare(this.bp, that.bp);
        }

        if (this.rsid != that.rsid) {
            return rsid.compareTo(that.rsid);
        }

        return 0;
    }
}
