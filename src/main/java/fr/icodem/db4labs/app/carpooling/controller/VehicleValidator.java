package fr.icodem.db4labs.app.carpooling.controller;

import fr.icodem.db4labs.dbtools.validation.Validator;
import fr.icodem.db4labs.dbtools.validation.ValidatorResult;

public class VehicleValidator extends Validator {

    @Override
    protected void validateProperty(String table, String name, Object value, ValidatorResult result) throws Exception {
        switch (name) {
            case "category":
                String str = checkStringNotNull(value, "The category may not be null !");
                checkStringMaxLength(str, 15, "Length must be less than 15");
                result.setConvertedValue(str);
                break;

            case "comfort":
                str = checkStringNotNull(value, "The comfort may not be null !");
                checkStringMaxLength(str, 10, "Length must be less than 10");
                result.setConvertedValue(str);
                break;

            case "color":
                str = checkStringNotNull(value, "The color may not be null !");
                checkStringMaxLength(str, 15, "Length must be less than 15");
                result.setConvertedValue(str);
                break;

            case "car_model_id":
                checkNotNull(value, "The model may not be null !");
                int modelId = (int) value;
                result.setConvertedValue(modelId);
                break;


        }

    }

}