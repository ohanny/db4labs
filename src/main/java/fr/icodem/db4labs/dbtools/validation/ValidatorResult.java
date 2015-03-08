package fr.icodem.db4labs.dbtools.validation;

public class ValidatorResult {

    public static enum State {Valid, Warning, Error}

    private String property;
    private Object originalValue;
    private Object convertedValue;
    private State state;
    private String message;

    public ValidatorResult() {
        state = State.Valid;
    }

    // getters and setters
    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public Object getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(Object originalValue) {
        this.originalValue = originalValue;
    }

    public Object getConvertedValue() {
        return convertedValue;
    }

    public void setConvertedValue(Object convertedValue) {
        this.convertedValue = convertedValue;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
