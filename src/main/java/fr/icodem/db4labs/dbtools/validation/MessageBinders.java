package fr.icodem.db4labs.dbtools.validation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MessageBinders implements Iterable<MessageBinder> {
    private List<MessageBinder> binders;

    public void add(MessageBinder binder) {
        if (binders == null) {
            binders = new ArrayList<>();
        }
        binders.add(binder);
    }

    @Override
    public Iterator<MessageBinder> iterator() {
        return binders.iterator();
    }
}
