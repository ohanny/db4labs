package fr.icodem.db4labs.component;

public class Message {
    public enum Severity {Info, Warning, Error}

    private String text;
    private Severity severity;
    private String target;

    public Message() {
    }

    public Message(String text) {
        this.text = text;
        this.severity = Severity.Info;
    }

    public Message(String text, String target) {
        this.text = text;
        this.target = target;
        this.severity = Severity.Info;
    }

    public Message(String text, Severity severity) {
        this.text = text;
        this.severity = severity;
    }

    public Message(String text, String target, Severity severity) {
        this.text = text;
        this.target = target;
        this.severity = severity;
    }

    // getters and setters
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }
}
