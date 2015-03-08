package fr.icodem.db4labs.event;

import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventType;

public class DataImportDoneEvent {
    private EventType<WorkerStateEvent> state;

    public DataImportDoneEvent(EventType<WorkerStateEvent> state) {
        this.state = state;
    }

    public EventType<WorkerStateEvent> getState() {
        return state;
    }
}
