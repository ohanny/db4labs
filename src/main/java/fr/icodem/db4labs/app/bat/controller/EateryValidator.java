package fr.icodem.db4labs.app.bat.controller;

import fr.icodem.db4labs.dbtools.validation.Validator;
import fr.icodem.db4labs.dbtools.validation.ValidatorResult;

public class EateryValidator extends Validator {

    @Override
    protected void validateProperty(String table, String name, Object value, ValidatorResult result) throws Exception {
        switch (name) {
            // base eatery properties
            case "name":
                String str = checkStringNotNull(value, "The name may not be null !");
                checkStringMaxLength(str, 25, "Length must be less than 25");
                result.setConvertedValue(str);
                break;

            case "description":
                str = checkIsString(value);
                if (str != null) checkStringMaxLength(str, 2000, "Length must be less than 2000");
                result.setConvertedValue(str);
                break;

            case "executive_chef":
                str = checkIsString(value);
                if (str != null) checkStringMaxLength(str, 30, "Length must be less than 30");
                result.setConvertedValue(str);
                break;

            case "street":
                str = checkIsString(value);
                if (str != null) checkStringMaxLength(str, 50, "Length must be less than 50");
                result.setConvertedValue(str);
                break;

            case "post_code":
                str = checkIsString(value);
                if (str != null) checkStringMaxLength(str, 10, "Length must be less than 10");
                result.setConvertedValue(str);
                break;

            case "cooking_style_id":
                checkNotNull(value, "The cooking style may not be null !");
                int cookingStyle = (int) value;
                result.setConvertedValue(cookingStyle);
                break;

            case "city_id":
                checkNotNull(value, "The city style may not be null !");
                int city = (int) value;
                result.setConvertedValue(city);
                break;

            // practical information properties
            case "getting_there":
                str = checkIsString(value);
                if (str != null) checkStringMaxLength(str, 100, "Length must be less than 100");
                result.setConvertedValue(str);
                break;

            case "parking":
                str = checkIsString(value);
                if (str != null) checkStringMaxLength(str, 100, "Length must be less than 100");
                result.setConvertedValue(str);
                break;

            case "hours_of_operation1":
            case "hours_of_operation2":
                str = checkIsString(value);
                if (str != null) checkStringMaxLength(str, 100, "Length must be less than 100");
                result.setConvertedValue(str);
                break;

            case "price":
                str = checkIsString(value);
                if (str != null) checkStringMaxLength(str, 100, "Length must be less than 100");
                result.setConvertedValue(str);
                break;

            case "payment_options":
                str = checkIsString(value);
                if (str != null) checkStringMaxLength(str, 150, "Length must be less than 150");
                result.setConvertedValue(str);
                break;


        }

    }

}
