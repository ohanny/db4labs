package fr.icodem.db4labs.dbtools.validation;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import fr.icodem.db4labs.component.Message;
import fr.icodem.db4labs.database.PersistentObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public abstract class Validator {

    public ValidatorResults validate(PersistentObject... dataList) {
        ValidatorResults results = new ValidatorResults();

        for (PersistentObject data : dataList) {
            if (data == null) continue;

            // validate properties
            for (Map.Entry<String, Object> entry : data.getProperties().entrySet()) {
                ValidatorResult result = new ValidatorResult();
                result.setProperty(entry.getKey());
                result.setOriginalValue(entry.getValue());
                result.setConvertedValue(entry.getValue());// by default, value is not converted
                                                           // so, we set the converted value now
                                                           // (useful in sub-class if a property
                                                           // do not need validation)

                // process validation rules
                try {
                    validateProperty(data.getTable(), entry.getKey(), entry.getValue(), result);

                    switch (result.getState()) {
                        case Valid:
                            entry.setValue(result.getConvertedValue());
                            break;
                        default:
                            results.add(entry.getKey(), result);
                    }
                } catch (Exception e) {
                    result.setMessage(e.getMessage());
                    result.setState(ValidatorResult.State.Error);
                    results.add(entry.getKey(), result);
                }
            }

            // validate objects
            if (data.getObjects() != null) {
                for (Map.Entry<String, Object> entry : data.getObjects().entrySet()) {
                    ValidatorResult result = new ValidatorResult();
                    result.setProperty(entry.getKey());
                    result.setConvertedValue(entry.getValue());// by default, value is not converted
                                                               // so, we set the converted value now
                                                               // (useful in sub-class if a property
                                                               // do not need validation)

                    // process validation rules
                    try {
                        validateObject(data.getTable(), entry.getKey(), entry.getValue(), result);

                        switch (result.getState()) {
                            case Valid:
                                entry.setValue(result.getConvertedValue());
                                break;
                            default:
                                results.add(entry.getKey(), result);
                        }
                    } catch (Exception e) {
                        result.setMessage(e.getMessage());
                        result.setState(ValidatorResult.State.Error);
                        results.add(entry.getKey(), result);
                    }
                }
            }


        }

        // other validation, for most complicated cases
        validateData(results, dataList);

        return results;
    }

    private Message.Severity convert(ValidatorResult.State state) {
        Message.Severity severity;
        switch (state) {
            case Error:
                severity = Message.Severity.Error;
                break;
            case Warning:
                severity = Message.Severity.Warning;
                break;
            default:
                severity = null;
        }
        return severity;
    }

    /**
     *
     * @param name
     * @param value
     * @param result
     * @throws Exception
     */
    protected abstract void validateProperty(String table, String name, Object value, ValidatorResult result) throws Exception;

    protected void validateObject(String table, String name, Object value, ValidatorResult result) throws Exception {

    }

    /**
     * Exceptions and result objects should be managed by sub-classes
     * @param dataList can be used when validation depends upon multiple fields
     */
    protected void validateData(ValidatorResults results, PersistentObject... dataList) {

    }

    // helper methods for processing validation in subclasses
    protected void checkNotNull(Object obj, String message) {
        Preconditions.checkNotNull(obj, message);
    }

    protected String checkStringNotNull(Object obj, String message) {
        String str = checkIsString(obj);
        checkNotNull(str, message);
        return str;
    }

    protected void checkStringMaxLength(String str, int length, String message) {
        Preconditions.checkArgument(str.length() <= length, message);
    }

    protected String checkIsString(Object obj) {
        return checkIsString(obj, true, true, "Not a valid string");
    }

    protected String checkIsString(Object obj, boolean trim, boolean emptyToNull, String message) {
        String str = null;
        try {
            str = (String)obj;
            if (trim && str != null) str = str.trim();
            if (emptyToNull && str != null) str = Strings.emptyToNull(str);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(message, e);
        }
        return str;
    }

    protected void checkArgument(boolean expression, String errorMessage) {
        Preconditions.checkArgument(expression, errorMessage);
    }

    protected void checkArgument(boolean expression, String errorMessage, Object... errorMessageArgs) {
        Preconditions.checkArgument(expression, errorMessage, errorMessageArgs);
    }

    protected int tryParseInteger(String str, String message) {
        try {
            int result = Integer.parseInt(str);
            return result;
        } catch (Exception e) {
            throw new IllegalArgumentException(message, e);
        }
    }

    protected double tryParseDouble(String str, String pattern, String message) {
        try {
            //if (str.contains("."))
            DecimalFormat fmt = new DecimalFormat(pattern);
            DecimalFormatSymbols custom=new DecimalFormatSymbols();
            if (str.contains(".")) {
                custom.setDecimalSeparator('.');
            }
            else if (str.contains(",")) {
                custom.setDecimalSeparator(',');
            }
            fmt.setDecimalFormatSymbols(custom);
            double result = fmt.parse(str).doubleValue();
            return result;
        } catch (ParseException e) {
            throw new IllegalArgumentException(message, e);
        }
    }

    protected Date tryParseDate(String str, String pattern, String message) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat(pattern);
            Date result = fmt.parse(str);
            return result;
        } catch (ParseException e) {
            throw new IllegalArgumentException(message, e);
        }
    }

}
