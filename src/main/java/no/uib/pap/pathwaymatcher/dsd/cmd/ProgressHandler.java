package no.uib.pap.pathwaymatcher.dsd.cmd;

import java.time.Instant;
import java.util.HashMap;

/**
 * this class displays feedback on the progress in command line.
 *
 * @author Marc Vaudel
 */
public class ProgressHandler {

    /**
     * Map to keep track of the tasks start and end time.
     */
    private final HashMap<String, Instant> startTime = new HashMap<>();

    /**
     * Constructor.
     */
    public ProgressHandler() {

    }

    /**
     * New task started.
     * 
     * @param taskName the name of the task.
     */
    public void start(String taskName) {

        Instant t = Instant.now();
        
        writeLine(t, taskName);
        startTime.put(taskName, t);
        
    }

    /**
     * Task ended.
     * 
     * @param taskName the name of the task
     */
    public void end(String taskName) {

        Instant t = Instant.now();

        Instant start = startTime.get(taskName);
        startTime.remove(taskName);

        StringBuilder text = new StringBuilder(taskName);
        text.append(" Completed");

        if (start != null) {

            long duration = t.getEpochSecond() - start.getEpochSecond();

            if (duration < 120) {

                text.append(" (").append(duration).append("s)");

            } else {

                double tempDuration = duration / 60.0;
                long durationInMin = Math.round(tempDuration);
                int restInSec = (int) ((tempDuration - durationInMin) * 60.0);

                if (durationInMin < 120) {

                    text.append(" (").append(durationInMin).append("min ").append(restInSec).append("s)");

                } else {

                    tempDuration = durationInMin / 60.0;
                    long durationInH = Math.round(tempDuration);
                    int restInMin = (int) ((tempDuration - durationInH) * 60.0);

                    if (durationInH < 24) {

                        text.append(" (").append(durationInH).append("h ").append(restInMin).append("min ").append(restInSec).append("s)");

                    } else {

                        tempDuration = durationInMin / 24.0;
                        long durationInD = Math.round(tempDuration);
                        int restInH = (int) ((tempDuration - durationInH) * 24.0);

                        text.append(" (").append(durationInD).append("d ").append(restInH).append("h ").append(restInMin).append("min ").append(restInSec).append("s)");

                    }
                }
            }
        }
        
        writeLine(t, text.toString());

    }

    /**
     * Writes a new line to the output.
     * 
     * @param t the time to write
     * @param text the text to write
     */
    public void writeLine(Instant t, String text) {

        String line = String.join(": ", t.toString(), text);

        System.out.println(line);

    }

    /**
     * Writes a new line to the output with the current time. 
     * 
     * @param text the text to write
     */
    public void writeLine(String text) {

        writeLine(Instant.now(), text);

    }

}
