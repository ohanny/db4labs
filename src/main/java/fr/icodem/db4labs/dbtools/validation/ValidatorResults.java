package fr.icodem.db4labs.dbtools.validation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ValidatorResults {
    private Map<String, ValidatorResult> results;

    public void add(String property, ValidatorResult result) {
        if (results == null) results = new HashMap<>();
        results.put(property, result);
    }

    public ValidatorResult getResult(String property) {
        return (results == null)?null:results.get(property);
    }

    public Collection<ValidatorResult> getResults() {
        return results.values();
    }

    public boolean isValid() {
        return (results == null || results.size() == 0);
    }
}
