package fr.icodem.db4labs.database;

import com.google.inject.Singleton;

import java.text.ParseException;
import java.text.SimpleDateFormat;

@Singleton
public class ValueConverter {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:ss");
    private SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd hh:ss");

    public Object jsonToJava(Object value, DataType type) {
        // process null
        if (value == null) return null;

        // process type
        Object result;
        switch (type) {
            case VARCHAR:
            case CHARACTER:
                result = value;
                break;
            case INTEGER:
                result = ((Long)value).intValue();
                break;
            case NUMERIC:
                result = value;
                break;
            case DATE:
                try {
                    result = dateFormat.parse((String)value);
                } catch (ParseException e) {
                    throw new IllegalArgumentException("Date incorrectly formatted : " + value, e);
                }
                break;
            case TIME:
                try {
                    result = timeFormat.parse((String)value);
                } catch (ParseException e) {
                    throw new IllegalArgumentException("Time incorrectly formatted : " + value, e);
                }
                break;
            case TIMESTAMP:
                try {
                    result = timestampFormat.parse((String)value);
                } catch (ParseException e) {
                    throw new IllegalArgumentException("Timestamp incorrectly formatted : " + value, e);
                }
                break;
            case BOOLEAN:
                result = value;
                break;
            case BLOB:
                result = value;
/*
                BASE64Decoder decoder = new BASE64Decoder();
                try {
                    result = decoder.decodeBuffer((String)value);
                } catch (IOException e) {
                    throw new IllegalArgumentException("String cannot be decoded with BASE 64", e);
                }
*/
                break;
            default:
                throw new IllegalArgumentException("Type " + type + " not supported for conversion");
        }

        return result;
    }

    public Object javaToJson(Object value, DataType type) {
        if (value == null) return null;

        Object result;
        switch (type) {
            case VARCHAR:
            case CHARACTER:
                result = value;
                break;
            case INTEGER:
            case NUMERIC:
                result = value;
                break;
            case DATE:
                result = dateFormat.format(value);
                break;
            case TIME:
                result = timeFormat.format(value);
                break;
            case TIMESTAMP:
                result = timestampFormat.format(value);
                break;
            case BOOLEAN:
                result = value;
                break;
            case BLOB:
                //BASE64Encoder encoder = new BASE64Encoder();
                //result = encoder.encode((byte[])value);
                result = value; // array of bytes
                break;
            default:
                throw new IllegalArgumentException("Type " + type + " not supported for conversion");
        }

        return result;
    }


}
