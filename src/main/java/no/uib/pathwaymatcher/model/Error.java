package no.uib.pathwaymatcher.model;

public enum Error {

    NO_ARGUMENTS(1, "PathwayMatcher did not receive any arguments.");

    private final int code;
    private final String message;

    private Error(int code, String message){
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
    public String toString(){
        return "Error " + code + ": " + message;
    }
}
