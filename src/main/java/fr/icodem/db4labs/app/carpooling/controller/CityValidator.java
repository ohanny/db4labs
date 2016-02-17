package fr.icodem.db4labs.app.carpooling.controller;

import fr.icodem.db4labs.dbtools.validation.Validator;
import fr.icodem.db4labs.dbtools.validation.ValidatorResult;

public class CityValidator extends Validator {

    @Override
    protected void validateProperty(String table, String name, Object value, ValidatorResult result) throws Exception {
        switch (name) {
            case "name":
                String str = checkStringNotNull(value, "The name may not be null !");
                checkStringMaxLength(str, 50, "Length must be less than 50");
                result.setConvertedValue(str);
                break;

            case "postcode":
                String postcode = checkStringNotNull(value, "The postcode may not be null !");
                checkStringMaxLength(postcode, 5, "Length must be less than 5");
                result.setConvertedValue(postcode);
                break;

            case "longitude":
                str = checkStringNotNull(value, "The longitude may not be null !");
                double longitude = tryParseDouble(str, "##.########", "Not a valid longitude");
                result.setConvertedValue(longitude);
                break;

            case "latitude":
                str = checkStringNotNull(value, "The latitude may not be null !");
                double latitude = tryParseDouble(str, "###.########", "Not a valid latitude");
                result.setConvertedValue(latitude);
                break;
        }

    }
}
