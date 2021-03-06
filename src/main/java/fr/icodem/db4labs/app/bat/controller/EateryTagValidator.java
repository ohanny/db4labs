package fr.icodem.db4labs.app.bat.controller;

import fr.icodem.db4labs.dbtools.validation.Validator;
import fr.icodem.db4labs.dbtools.validation.ValidatorResult;

public class EateryTagValidator extends Validator {

    @Override
    protected void validateProperty(String table, String name, Object value, ValidatorResult result) throws Exception {
        switch (name) {
            case "name":
                String str = checkStringNotNull(value, "The name may not be null !");
                checkStringMaxLength(str, 20, "Length must be less than 20");
                result.setConvertedValue(str);
                break;
        }

    }
}
