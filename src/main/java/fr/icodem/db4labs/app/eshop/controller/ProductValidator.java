package fr.icodem.db4labs.app.eshop.controller;

import com.google.common.base.Splitter;
import fr.icodem.db4labs.database.PersistentObject;
import fr.icodem.db4labs.dbtools.validation.Validator;
import fr.icodem.db4labs.dbtools.validation.ValidatorResult;

import java.util.Date;
import java.util.List;

public class ProductValidator extends Validator {

    @Override
    protected void validateProperty(String table, String name, Object value, ValidatorResult result) throws Exception {
        switch (name) {
            // base product properties
            case "name":
                String str = checkStringNotNull(value, "The name may not be null !");
                checkStringMaxLength(str, 100, "Length must be less than 100");
                result.setConvertedValue(str);
                break;

            case "description":
                //str = checkStringNotNull(value, "The description may not be null !");
                str = checkIsString(value);
                if (str != null) checkStringMaxLength(str, 2000, "Length must be less than 2000");
                result.setConvertedValue(str);
                break;

            case "price":
                str = checkStringNotNull(value, "The price may not be null !");
                double price = tryParseDouble(str, "0.00", "Not a valid price");
                result.setConvertedValue(price);
                break;

            case "family_id":
                checkNotNull(value, "The family may not be null !");
                int family = (int) value;
                result.setConvertedValue(family);
                break;


            // media properties
            case "release_date":
                str = checkStringNotNull(value, "The release date may not be null !");
                Date date = tryParseDate(str, "dd/MM/yyyy", "Not a valid date");//TODO use another pattern
                result.setConvertedValue(date);
                break;

            // book properties
            case "isbn":
                str = checkStringNotNull(value, "The isbn may not be null !");
                checkStringMaxLength(str, 14, "Length must be less than 14");
                result.setConvertedValue(str);
                break;

            case "language":
                str = checkStringNotNull(value, "The language may not be null !");
                result.setConvertedValue(str);
                break;

            case "pages":
                str = checkStringNotNull(value, "The pages may not be null !");
                int pages = tryParseInteger(str, "Not a valid number");
                result.setConvertedValue(pages);
                break;

            // movie and album properties
            case "length":
                str = checkStringNotNull(value, "The length may not be null !");
                int length = tryParseInteger(str, "Not a valid length");
                result.setConvertedValue(length);
                break;

            // grocery properties
            case "quantity":
                str = checkStringNotNull(value, "The isbn may not be null !");
                checkStringMaxLength(str, 13, "Length must be less than 13");
                result.setConvertedValue(str);
                break;

            case "unit":
                str = checkStringNotNull(value, "The unit may not be null !");
                result.setConvertedValue(str);
                break;

            case "composition":
                str = checkStringNotNull(value, "The composition may not be null !");
                checkStringMaxLength(str, 500, "Length must be less than 500");
                result.setConvertedValue(str);
                break;

            case "nutritional_value":
                str = checkStringNotNull(value, "The nutritional value may not be null !");
                checkStringMaxLength(str, 500, "Length must be less than 500");
                result.setConvertedValue(str);
                break;


        }

    }

    @Override
    protected void validateObject(String table, String name, Object value, ValidatorResult result) throws Exception {
        switch (name) {
            // book
            case "authors":
                List<PersistentObject> authors = (List<PersistentObject>) value;
                checkNotNull(authors, "Authors may not be null");
                checkArgument(authors.size() > 0, "At least one author is required");
                break;

            // movie
            case "languages":
                List<PersistentObject> languages = (List<PersistentObject>) value;
                checkNotNull(languages, "Languages may not be null");
                checkArgument(languages.size() > 0, "At least one language is required");
                break;

            case "director":
                PersistentObject director = (PersistentObject)value;
                if (director == null) break;
                //checkNotNull(director, "Director may not be null");
                checkNotNull(director.getProperty("name"), "Director name may not be null");
                Iterable<String> directorName = Splitter.on(',')
                        .split((String)director.getProperty("name"));
                int count = 0;
                for (String dn : directorName) {
                    count++;
                }
                checkArgument(count == 1, "Multiple names are not allowed");
                break;

            case "actors":

                List<PersistentObject> actors = (List<PersistentObject>) value;
                checkNotNull(actors, "Actors may not be null");
                checkArgument(actors.size() > 0, "At least one actor is required");

                break;

            // album
            case "artists":
                List<PersistentObject> artists = (List<PersistentObject>) value;
                checkNotNull(artists, "Authors may not be null");
                checkArgument(artists.size() > 0, "At least one artist is required");
                break;
        }

    }
}
