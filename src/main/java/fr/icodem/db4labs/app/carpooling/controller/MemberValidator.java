package fr.icodem.db4labs.app.carpooling.controller;

import fr.icodem.db4labs.database.PersistentObject;
import fr.icodem.db4labs.dbtools.validation.Validator;
import fr.icodem.db4labs.dbtools.validation.ValidatorResult;
import fr.icodem.db4labs.dbtools.validation.ValidatorResults;

import java.util.List;

public class MemberValidator extends Validator {

    @Override
    protected void validateProperty(String table, String name, Object value, ValidatorResult result) throws Exception {
        switch (name) {
            case "id":
                String str = checkStringNotNull(value, "The username may not be null !");
                checkStringMaxLength(str, 30, "Length must be less than 30");
                result.setConvertedValue(str);
                break;

//            case "password":
//                str = checkStringNotNull(value, "The password may not be null !");
//                checkStringMaxLength(str, 20, "Length must be less than 20");
//                result.setConvertedValue(str);
//                break;

            case "first_name":
                str = checkStringNotNull(value, "The first name may not be null !");
                checkStringMaxLength(str, 20, "Length must be less than 20");
                result.setConvertedValue(str);
                break;

            case "last_name":
                str = checkStringNotNull(value, "The last name may not be null !");
                checkStringMaxLength(str, 20, "Length must be less than 20");
                result.setConvertedValue(str);
                break;

            case "birth_year":
                checkNotNull(value, "The birth year may not be null !");
                int birthYear = (int) value;
                result.setConvertedValue(birthYear);
                break;

        }

    }

    @Override
    protected void validateData(ValidatorResults results, PersistentObject... dataList) {
        for (PersistentObject po: dataList) {
            ValidatorResult result = new ValidatorResult();
            PersistentObject user = (PersistentObject)po.getObject("user");
            try {
                String str = checkStringNotNull(user.getProperty("password"), "The password may not be null !");
                checkStringMaxLength(str, 20, "Length must be less than 20");
            } catch (Exception e) {
                result.setProperty("password");
                result.setMessage(e.getMessage());
                result.setState(ValidatorResult.State.Error);
                results.add("password", result);
            }
        }
    }

}