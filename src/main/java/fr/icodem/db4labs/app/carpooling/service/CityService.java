package fr.icodem.db4labs.app.carpooling.service;

import com.google.inject.Inject;
import fr.icodem.db4labs.container.AppContainer;
import fr.icodem.db4labs.database.DataType;
import fr.icodem.db4labs.database.PersistentObject;
import fr.icodem.db4labs.database.WhereDescriptor;
import fr.icodem.db4labs.dbtools.transaction.Transactionnal;
import javafx.collections.ObservableList;

@Transactionnal
public class CityService {

    @Inject private AppContainer container;

    public ObservableList<PersistentObject> findCityList() throws Exception {
        ObservableList<PersistentObject> result = container.select("city");
        return result;
    }

    public PersistentObject findCityById(int id) throws Exception {
        PersistentObject city = container.selectByPK("city", id);

        return city;
    }

    public void saveCity(PersistentObject city) throws Exception {
        if (city.getProperty("id") != null) {
            container.update(city);
        } else {
            container.insert(city);
        }
    }

    public void deleteCity(PersistentObject city) throws Exception {
        // check if city can be deleted
        checkDeleteCity(city);

        // ok, delete city
        container.delete(city);
    }

    public void checkDeleteCity(PersistentObject city) throws Exception {}

    public PersistentObject findCityByNameAndPostCode(String name, String postcode) throws Exception {
        WhereDescriptor where = WhereDescriptor.build("lower(name) = ? and postcode = ?")
                .addParameter(name.toLowerCase(), DataType.VARCHAR)
                .addParameter(postcode, DataType.VARCHAR);
        PersistentObject city = container.selectUnique("city", where);
        return city;
    }

    public void populateCity(PersistentObject po, String name, String postcode,
                              String longitude, String latitude) {
        // base properties
        po.setProperty("name", name);
        po.setProperty("postcode", postcode);
        po.setProperty("longitude", longitude);
        po.setProperty("latitude", latitude);
    }


}
