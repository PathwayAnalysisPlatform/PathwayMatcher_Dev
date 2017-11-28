package no.uib.pathwaymatcher.util;

public class Statistics {

    public static double getMean(Long[] values) {
        double total = 0.0;
        for (int I = 0; I < values.length; I++) {
            total += values[I];
        }
        return total / values.length;
    }

    public static double getStandardDeviation(Long[] values, double mean) {
        double sum = 0.0;
        for (int I = 0; I < values.length; I++) {
            sum += Math.pow(values[I] - mean, 2);
        }
        return Math.sqrt(sum/values.length);
    }
}
