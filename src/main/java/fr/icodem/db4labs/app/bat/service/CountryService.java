package fr.icodem.db4labs.app.bat.service;

import com.google.inject.Inject;
import fr.icodem.db4labs.container.AppContainer;
import fr.icodem.db4labs.database.DataType;
import fr.icodem.db4labs.database.PersistentObject;
import fr.icodem.db4labs.database.WhereDescriptor;
import fr.icodem.db4labs.dbtools.transaction.Transactionnal;
import javafx.collections.ObservableList;

import java.util.List;

@Transactionnal
public class CountryService {

    @Inject private AppContainer container;
    @Inject ImageService imageService;

    public ObservableList<PersistentObject> findCountryList() throws Exception {
        ObservableList<PersistentObject> countries = container.select("country");
        for (PersistentObject country : countries) {

            // append regions of countries
            WhereDescriptor where = WhereDescriptor.build("country_id = ?")
                                                   .addParameter(country.getProperty("id"), DataType.INTEGER);
            ObservableList<PersistentObject> regions = container.select("region", where);
            country.setObject("regions", regions);

            // append cities to regions
            for (PersistentObject region : regions) {
                where = WhereDescriptor.build("region_id = ? and country_id = ?")
                                       .addParameter(region.getProperty("id"), DataType.INTEGER)
                                       .addParameter(country.getProperty("id"), DataType.INTEGER);
                ObservableList<PersistentObject> cities = container.select("city", where);
                region.setObject("cities", cities);
                for (PersistentObject city : cities) {
                    if (city.getProperty("post_code") != null && !"".equals(city.getProperty("post_code"))) {
                        city.setObject("name", city.getProperty("name") + " - " + city.getProperty("post_code"));
                    } else {
                        city.setObject("name", city.getProperty("name"));
                    }
                }
            }
        }
        return countries;
    }

    public PersistentObject findItemById(String table, int id) throws Exception {
        PersistentObject item = container.selectByPK(table, id);
        if ("city".equals(table)) {
            imageService.loadImage(item, "image", "city", id);
        }

        return item;
    }

    public PersistentObject findCountryByRegion(PersistentObject region) throws Exception {
        int id = (int) region.getProperty("country_id");
        PersistentObject country = container.selectByPK("country", id);

        return country;
    }

    public void saveItem(PersistentObject item) throws Exception {
        switch (item.getTable()) {
            case "country":
                break;
            case "region":
                PersistentObject country = (PersistentObject) item.getObject("country");
                item.setProperty("country_id", country.getProperty("id"));
                break;
            case "city":
                PersistentObject region = (PersistentObject) item.getObject("region");
                item.setProperty("region_id", region.getProperty("id"));

                country = findCountryByRegion(region);
                item.setProperty("country_id", country.getProperty("id"));
                break;
            default:
                throw new IllegalArgumentException("Cannot delete item because table is unknown (must be either 'country', 'region' or 'city'");
        }

        if (item.getProperty("id") != null) {
            container.update(item);
        } else {
            container.insert(item);
        }

        // city image
        if ("city".equals(item.getTable())) {
            imageService.deleteImage("city", (Integer) item.getProperty("id"));
            imageService.saveImage(item, "image", "city", (Integer) item.getProperty("id"), false, 0, 0);
        }
    }

    public void deleteItem(PersistentObject item) throws Exception {
        // check if item can be deleted
        checkDeleteItem(item);

        // ok, delete item
        WhereDescriptor where;
        switch (item.getTable()) {
            case "country":
                where = WhereDescriptor.build("country_id = ?")
                                       .addParameter(item.getProperty("id"), DataType.INTEGER);
                container.delete("city", where);
                container.delete("region", where);
                container.delete(item);

                break;
            case "region":
                where = WhereDescriptor.build("region_id = ?")
                                       .addParameter(item.getProperty("id"), DataType.INTEGER);
                container.delete("city", where);
                container.delete(item);
                break;
            case "city":
                container.delete(item);
                imageService.deleteImage("city", (Integer) item.getProperty("id"));
                break;
        }
    }

    public void checkDeleteItem(PersistentObject item) throws Exception {
         switch (item.getTable()) {
             case "country":
                 checkDeleteCountry(item);
                 break;
             case "region":
                 checkDeleteRegion(item);
                 break;
             case "city":
                 checkDeleteCity(item);
                 break;
             default:
                 throw new IllegalArgumentException("Cannot delete item because table is unknown (must be either 'country', 'region' or 'city'");
         }
    }

    private void checkDeleteCountry(PersistentObject country) throws Exception {
        WhereDescriptor where = WhereDescriptor.build("country_id = ?")
                                               .addParameter(country.getProperty("id"), DataType.INTEGER);
        List<PersistentObject> regions = container.select("region", where);
        for (PersistentObject region : regions) {
            checkDeleteRegion(region);
        }

    }

    public void checkDeleteRegion(PersistentObject region) throws Exception {
        WhereDescriptor where = WhereDescriptor.build("region_id = ?")
                                               .addParameter(region.getProperty("id"), DataType.INTEGER);
        List<PersistentObject> cities = container.select("city", where);
        for (PersistentObject city : cities) {
            checkDeleteCity(city);
        }

    }

    public void checkDeleteCity(PersistentObject city) throws Exception {
        // check if city is linked to an eatery
        WhereDescriptor where = WhereDescriptor.build("city_id = ?")
                .addParameter(city.getProperty("id"), DataType.INTEGER);
        int count = container.count("eatery", where);
        if (count > 0) throw new IllegalArgumentException("Cannot delete city " + city.getProperty("name")
                + " because it is linked at least to one eatery");
    }

}
