package no.uib.pathwaymatcher.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputPatterns {


    private static final String FLOAT_POINT_NUMBER = "[+-]?([0-9]*[.])?[0-9]+";
    private static final String EXPRESSION_VALUES = "(\\s+" + FLOAT_POINT_NUMBER + ")*\\s*";

    /**
     * Regular expressions for one row of the specified formats
     */

    private static final String PROTEIN_ENSEMBL = "^(\\p{Upper}{3,7})?\\d{1,11}$";
    public static final String PROTEIN_UNIPROT = "^([OPQ][0-9][A-Z0-9]{3}[0-9]|[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2})([-]\\d{1,2})?$";
    private static final String UNIPROT_AND_SITES = "^([OPQ][0-9][A-Z0-9]{3}[0-9]|[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2})([-]\\d{1,2})?;((\\d{1,11}|[Nn][Uu][Ll][Ll]))?(,(\\d{1,11}|[Nn][Uu][Ll][Ll]))*$";
    public static final String PROTEOFORM_CUSTOM = "^([OPQ][0-9][A-Z0-9]{3}[0-9]|[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2})([-]\\d{1,2})?;(\\d{5}:(\\d{1,11}|[Nn][Uu][Ll][Ll]))?(,\\d{5}:(\\d{1,11}|[Nn][Uu][Ll][Ll]))*";

    private static final String PROTEOFORM_PRO = "^UniProtKB:([OPQ][0-9][A-Z0-9]{3}[0-9]|[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2})[-]?\\d?(,(\\d+-\\d+)?(,([A-Z][a-z]{2}-\\d+(\\/[A-Z][a-z]{2}-\\d+)*,MOD:\\d{5}(\\|[A-Z][a-z]{2}-\\d+(\\/[A-Z][a-z]{2}-\\d+)*,MOD:\\d{5})*)?)?)?\\s*";
    private static final String PROTEOFORM_PIR = "^PR:\\d{9}\\d*\\s*";
    private static final String PROTEOFORM_GPMDB = "(?i)^((([OPQ][0-9][A-Z0-9]{3}[0-9]|[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2})([-]\\d+)?)|(ENSP\\d{11})):pm.([A-Z]\\d+[+=](([A-Z][a-z]+)|(MOD:\\d{5}))(\\/(([A-Z][a-z]+)|(MOD:\\d{5})))*;)+\\s*";    // GPMDB (http://wiki.thegpm.org/wiki/Nomenclature_for_the_description_of_protein_sequence_modifications)
    private static final String PROTEOFORM_PEFF = ""; //TODO

    private static final String PEPTIDE = "^[ARNDBCEQZGHILKMFPSTWYV]+$";
    private static final String PEPTIDE_AND_SITES = "^[ARNDBCEQZGHILKMFPSTWYV]+(,\\d*)?(;\\d*)*$";
    private static final String PEPTIDE_AND_MOD_SITES = "^[ARNDBCEQZGHILKMFPSTWYV]+(,\\d{5}:\\d*)?(;\\d{5}:\\d*)*?$";

    private static final String RSID = "^rs\\d*$";
    private static final String GENE = "^[-.\\p{Alnum}]*$";
    private static final String VCF = "^[1-2]?[0-9] [0-9]{1,11} (rs[0-9]{1,20}|.) [ACTG].*$";
    private static final String VCFRECORDFIRST4COLS = "([1-2]?[0-9] [0-9]{1,11} (rs[0-9]{1,20}|.) [ACTG]*)";

    public static final String MAXQUANT = "Protein"; //The first row only
    private static final String UNKNOWN = "";

    /**
     * Compiled patterns for the regex
     */
    private static final Pattern PATTERN_PROTEIN_ENSEMBL = Pattern.compile(PROTEIN_ENSEMBL);
    private static final Pattern PATTERN_PROTEIN_UNIPROT = Pattern.compile(PROTEIN_UNIPROT);
    private static final Pattern PATTERN_UNIPROT_AND_SITES = Pattern.compile(UNIPROT_AND_SITES);
    private static final Pattern PATTERN_PROTEOFORM_CUSTOM = Pattern.compile(PROTEOFORM_CUSTOM);
    private static final Pattern PATTERN_PROTEOFORM_PRO = Pattern.compile(PROTEOFORM_PRO);
    private static final Pattern PATTERN_PROTEOFORM_PIR = Pattern.compile(PROTEOFORM_PIR);
    private static final Pattern PATTERN_PROTEOFORM_GPMDB = Pattern.compile(PROTEOFORM_GPMDB);
    private static final Pattern PATTERN_PROTEOFORM_PEFF = Pattern.compile(PROTEOFORM_PEFF);

    private static final Pattern PATTERN_PEPTIDE = Pattern.compile(PEPTIDE);
    private static final Pattern PATTERN_PEPTIDE_AND_SITES = Pattern.compile(PEPTIDE_AND_SITES);
    private static final Pattern PATTERN_PEPTIDE_AND_MOD_SITES = Pattern.compile(PEPTIDE_AND_MOD_SITES);

    private static final Pattern PATTERN_RSID = Pattern.compile(RSID);
    private static final Pattern PATTERN_GENE = Pattern.compile(GENE);
    private static final Pattern PATTERN_VCF = Pattern.compile(VCF);
    private static final Pattern PATTERN_VCFRECORDFIRST4COLS = Pattern.compile(VCFRECORDFIRST4COLS);

    private static final Pattern PATTERN_MAXQUANT = Pattern.compile(MAXQUANT);
    private static final Pattern PATTERN_UNKNOWN = Pattern.compile(UNKNOWN);

    private static final Pattern PATTERN_PROTEIN_UNIPROT_WITH_EXPRESSION_VALUES = Pattern.compile(PROTEIN_UNIPROT + EXPRESSION_VALUES);
    private static final Pattern PATTERN_PROTEIN_ENSEMBL_WITH_EXPRESSION_VALUES = Pattern.compile(PROTEIN_ENSEMBL + EXPRESSION_VALUES);
    private static final Pattern PATTERN_PROTEOFORM_CUSTOM_WITH_EXPRESSION_VALUES = Pattern.compile(PROTEOFORM_CUSTOM + EXPRESSION_VALUES);
    private static final Pattern PATTERN_PROTEOFORM_PRO_WITH_EXPRESSION_VALUES = Pattern.compile(PROTEOFORM_PRO + EXPRESSION_VALUES);
    private static final Pattern PATTERN_PROTEOFORM_PIR_WITH_EXPRESSION_VALUES = Pattern.compile(PROTEOFORM_PIR + EXPRESSION_VALUES);
    private static final Pattern PATTERN_PROTEOFORM_GPMDB_WITH_EXPRESSION_VALUES = Pattern.compile(PROTEOFORM_GPMDB + EXPRESSION_VALUES);

    public static boolean matches_Protein_Ensembl(String str) {
        Matcher m = PATTERN_PROTEIN_ENSEMBL.matcher(str);
        return m.matches();
    }

    public static boolean matches_Protein_Uniprot(String str) {
        Matcher m = PATTERN_PROTEIN_UNIPROT.matcher(str);
        return m.matches();
    }

    public static boolean matches_Protein_Uniprot_And_Sites(String str) {
        Matcher m = PATTERN_UNIPROT_AND_SITES.matcher(str);
        return m.matches();
    }

    public static boolean matches_Proteoform_Custom(String str) {
        Matcher m = PATTERN_PROTEOFORM_CUSTOM.matcher(str);
        return m.matches();
    }

    public static boolean matches_Proteoform_Pro(String str) {
        Matcher m = PATTERN_PROTEOFORM_PRO.matcher(str);
        return m.matches();
    }

    public static boolean matches_Proteoform_Pir(String str) {
        Matcher m = PATTERN_PROTEOFORM_PIR.matcher(str);
        return m.matches();
    }

    public static boolean matches_Proteoform_Gpmdb(String str) {
        Matcher m = PATTERN_PROTEOFORM_GPMDB.matcher(str);
        return m.matches();
    }

    public static boolean matches_Proteoform_Peff(String str) {
        Matcher m = PATTERN_PROTEOFORM_PEFF.matcher(str);
        return m.matches();
    }

    public static boolean matches_Peptite(String str) {
        Matcher m = PATTERN_PEPTIDE.matcher(str);
        return m.matches();
    }

    public static boolean matches_Peptite_And_Sites(String str) {
        Matcher m = PATTERN_PEPTIDE_AND_SITES.matcher(str);
        return m.matches();
    }

    public static boolean matches_Peptite_And_Mod_Sites(String str) {
        Matcher m = PATTERN_PEPTIDE_AND_MOD_SITES.matcher(str);
        return m.matches();
    }

    public static boolean matches_Rsid(String str){
        Matcher m = PATTERN_RSID.matcher(str);
        return m.matches();
    }

    public static boolean matches_Gene(String str){
        Matcher m = PATTERN_GENE.matcher(str);
        return m.matches();
    }

    public static boolean matches_Vcf(String str){
        Matcher m = PATTERN_VCF.matcher(str);
        return m.matches();
    }

    public static boolean matches_Vcf_Record(String str){
        Matcher m = PATTERN_VCFRECORDFIRST4COLS.matcher(str);
        return m.matches();
    }

    public static boolean matches_Maxquant(String str){
        Matcher m = PATTERN_MAXQUANT.matcher(str);
        return m.matches();
    }

    public static boolean matches_Unknown(String str){
        Matcher m = PATTERN_UNKNOWN.matcher(str);
        return m.matches();
    }

    public static boolean matches_Protein_Uniprot_With_Expression_Values(String str) {
        Matcher m = PATTERN_PROTEIN_UNIPROT_WITH_EXPRESSION_VALUES.matcher(str);
        return m.matches();
    }

    public static boolean matches_Protein_Ensembl_With_Expression_Values(String str) {
        Matcher m = PATTERN_PROTEIN_ENSEMBL_WITH_EXPRESSION_VALUES.matcher(str);
        return m.matches();
    }

    public static boolean matches_Proteoform_Custom_With_Expression_Values(String str) {
        Matcher m = PATTERN_PROTEOFORM_CUSTOM_WITH_EXPRESSION_VALUES.matcher(str);
        return m.matches();
    }

    public static boolean matches_Proteoform_Pro_With_Expression_Values(String str) {
        Matcher m = PATTERN_PROTEOFORM_PRO_WITH_EXPRESSION_VALUES.matcher(str);
        return m.matches();
    }

    public static boolean matches_Proteoform_Pir_With_Expression_Values(String str) {
        Matcher m = PATTERN_PROTEOFORM_PIR_WITH_EXPRESSION_VALUES.matcher(str);
        return m.matches();
    }

    public static boolean matches_Proteoform_Gpmdb_With_Expression_Values(String str) {
        Matcher m = PATTERN_PROTEOFORM_GPMDB_WITH_EXPRESSION_VALUES.matcher(str);
        return m.matches();
    }

    public static boolean isProteoformWithExpression(String str) {
        if (matches_Proteoform_Custom_With_Expression_Values(str)
                || matches_Proteoform_Pro_With_Expression_Values(str)
                || matches_Proteoform_Gpmdb_With_Expression_Values(str)
                || matches_Proteoform_Pir_With_Expression_Values(str)) {
            return true;
        }
        return false;
    }
}
