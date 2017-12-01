package no.uib.pathwaymatcher.model;

import no.uib.pathwaymatcher.PathwayMatcher;

import java.util.logging.Level;

/**
 * Situations that are potentially not desired, but do not halt the execution of PathwayMatcher.
 */
public enum Warning {

    INVALID_ROW(11, "Invalid entry row."),
    COULD_NOT_CREATE_LOG_FILE(15, "Could not create or write to log file.");

    private final int code;
    private final String message;

    Warning(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Error " + code + ": " + message;
    }

    public static void sendWarning(Warning warning){
        PathwayMatcher.logger.log(Level.WARNING, warning.getMessage());
    }

    public static void sendWarning(Warning warning, int num){
        PathwayMatcher.logger.log(Level.WARNING, warning.getMessage() + " " + num);
    }
}
