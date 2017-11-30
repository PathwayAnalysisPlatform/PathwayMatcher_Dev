package no.uib.pathwaymatcher.model;

public enum Error {

    NO_ARGUMENTS(1, "PathwayMatcher was run without arguments and there is no configuration file in the same directory."),
    NO_INPUT(2, "No input was specified with a command line argument nor in the configuration file."),
    COULD_NOT_READ_CONF_FILE(3, "It was not possible to read the configuration file."),
    COULD_NOT_CONNECT_TO_NEO4j(4, " Unable to connect to Neo4j, ensure the database is running and that there is a working network connection to it."),
    COMMAND_LINE_ARGUMENTS_PARSING_ERROR(5, "There was an error parsing the command line arguments."),
    INVALID_INPUT_TYPE(6, "The input type is invalid."),
    COULD_NOT_READ_INPUT_FILE(7, "There was an error reading the input file."),
    MISSING_ARGUMENT(8, "There is an argument missing to process the input."),
    INPUT_PARSING_ERROR(9, "There was an error processing the input."),
    ERROR_WRITING_STANDARIZED_FILE(10, "There was an error with the standarized file."),
    INVALID_ROW(11, "Invalid entry row."),
    ERROR_CREATING_STANDARIZED_FILE(12, "There was an error creating the standarized file."),
    ERROR_INITIALIZING_PEPTIDE_MAPPER(13,"There was an error initializing peptide mapper. Make sure the directories are writable."),
    ERROR_READING_VEP_TABLES(14,"There was a problem reading the vepTable for chromosomes."),
    COULD_NOT_CREATE_LOG_FILE(15, "Could not create or write to log file.");

    private final int code;
    private final String message;

    Error(int code, String message) {
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
}
