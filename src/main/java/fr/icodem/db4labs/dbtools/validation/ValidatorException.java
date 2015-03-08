package fr.icodem.db4labs.dbtools.validation;

public class ValidatorException extends Exception{
    private ValidatorResults results;

    public ValidatorException(String message, ValidatorResults results) {
        super(message);
        this.results = results;
    }

}
