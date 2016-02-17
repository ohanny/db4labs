package fr.icodem.db4labs.app.carpooling.controller;

import fr.icodem.db4labs.dbtools.validation.Validator;
import fr.icodem.db4labs.dbtools.validation.ValidatorResult;

public class ModelValidator extends Validator {

    @Override
    protected void validateProperty(String table, String name, Object value, ValidatorResult result) throws Exception {
        switch (name) {
            case "name":
                String str = checkStringNotNull(value, "The name may not be null !");
                checkStringMaxLength(str, 30, "Length must be less than 30");
                result.setConvertedValue(str);
                break;

            case "brand_id":
                checkNotNull(value, "The brand may not be null !");
                int brand = (int) value;
                result.setConvertedValue(brand);
                break;

        }

    }
}
