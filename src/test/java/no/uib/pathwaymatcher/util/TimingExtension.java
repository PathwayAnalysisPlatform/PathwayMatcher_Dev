package no.uib.pathwaymatcher.util;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import static no.uib.pathwaymatcher.util.ConstantHolder.*;
import static no.uib.pathwaymatcher.util.Statistics.getMean;
import static no.uib.pathwaymatcher.util.Statistics.getStandardDeviation;

public class TimingExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback, AfterAllCallback {

    private static final Logger logger = Logger.getLogger(TimingExtension.class.getName());

    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
        getStore(extensionContext).put(extensionContext.getRequiredTestMethod(), System.currentTimeMillis());
    }

    /**
     * After each test registers the time for the last 75% of executions, in order to ignore te warm-up runs.
     *
     * @param extensionContext
     * @throws Exception
     */
    @Override
    public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
        Method testMethod = extensionContext.getRequiredTestMethod();
        long start = getStore(extensionContext).remove(testMethod, long.class);
        long duration = System.currentTimeMillis() - start;

//        logger.info(() -> String.format("Method [%s] took %s ms.", testMethod.getName(), duration));

        repetitions.putIfAbsent(testMethod.getName(), 0);
        repetitions.put(testMethod.getName(), repetitions.get(testMethod.getName()) + 1);

        times.putIfAbsent(testMethod.getName(), new Long[REPETITIONS]);
        times.get(testMethod.getName())[repetitions.get(testMethod.getName()) - 1] = duration;

    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {

        System.out.println(System.getProperty("user.dir"));
        PrintWriter stdDevFile = new PrintWriter(new File(FILE_TIMES));
        PrintWriter timesFile = new PrintWriter(new File(FILE_STDDEV));
        // Calculate all the averages
        for (String entry : times.keySet()) {
            System.out.printf("%s:\t%10.5f\n", entry, calculateAverage(times.get(entry)));
            timesFile.printf("%s,%10.5f\n", entry, getMean(times.get(entry)));
//            stdDevFile.printf("%s,%10.5f\n", entry, getStandardDeviation(times.get(entry), getMean(times.get(entry))));
        }

        timesFile.close();
        stdDevFile.close();
    }

    /**
     * Calculate average excluding values away from mean more than 1 stdDev.
     *
     * @param longs
     * @return
     */
    public double calculateAverage(Long[] longs) {
        double mean = getMean(longs);
        double stdDev = getStandardDeviation(longs, mean);
        double sum = 0.0;
        int cont = 0;
        for(Long value : longs){
            if(Math.abs(value-mean) <= stdDev){
                cont++;
                sum += value;
            }
        }
        return sum/cont;
    }

    private ExtensionContext.Store getStore(ExtensionContext extensionContext) {
        return extensionContext.getStore(ExtensionContext.Namespace.create(getClass(), extensionContext));
    }
}
