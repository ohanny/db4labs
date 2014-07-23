package fr.icodem.db4labs.app.bat.controller;

import fr.icodem.db4labs.dbtools.validation.Validator;
import fr.icodem.db4labs.dbtools.validation.ValidatorResult;

public class CountryRegionCityValidator extends Validator {

    @Override
    protected void validateProperty(String table, String name, Object value, ValidatorResult result) throws Exception {
        switch (name) {
            case "name":
                String str = checkStringNotNull(value, "The name may not be null !");
                checkStringMaxLength(str, 25, "Length must be less than 25");
                result.setConvertedValue(str);
                break;

            case "postCode":
                if ("city".equals(table)) {
                    str = checkStringNotNull(value, "The post code may not be null !");
                    checkStringMaxLength(str, 10, "Length must be less than 10");
                    result.setConvertedValue(str);
                }
                break;
        }
    }

}
