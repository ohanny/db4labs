package fr.icodem.db4labs.app.carpooling.service;

import com.google.inject.Inject;
import fr.icodem.db4labs.container.AppContainer;
import fr.icodem.db4labs.database.DataType;
import fr.icodem.db4labs.database.PersistentObject;
import fr.icodem.db4labs.database.WhereDescriptor;
import fr.icodem.db4labs.dbtools.transaction.Transactionnal;
import javafx.collections.ObservableList;

@Transactionnal
public class ModelService {

    @Inject private AppContainer container;

    public ObservableList<PersistentObject> findModelList() throws Exception {
        ObservableList<PersistentObject> result = container.select("model");
        for (PersistentObject model : result) {
            if (model.getProperty("brand_id") != null) {
                PersistentObject brand = container.selectByPK("brand", model.getProperty("brand_id"));
                model.setProperty("brandName", brand.getProperty("name"));
            }
        }
        return result;
    }

    public PersistentObject findModelById(int id) throws Exception {
        PersistentObject model = container.selectByPK("model", id);

        // brand
        if (model.getProperty("brand_id") != null) {
            PersistentObject brand = container.selectByPK("brand", model.getProperty("brand_id"));
            model.setObject("brand", brand);
        }

        return model;
    }

    public void saveModel(PersistentObject model) throws Exception {
        if (model.getProperty("id") != null) {
            container.update(model);
        } else {
            container.insert(model);
        }
    }

    public void deleteModel(PersistentObject model) throws Exception {
        // check if model can be deleted
        checkDeleteModel(model);

        // ok, delete model
        container.delete(model);
    }

    public void checkDeleteModel(PersistentObject model) throws Exception {}

    public void populateModel(PersistentObject po, String name, Integer brandId) {
        // base properties
        po.setProperty("name", name);

        // brand
        po.setProperty("brand_id", brandId);
    }

    public PersistentObject findModelByNameAndBrand(String name, int brandId) throws Exception {
        WhereDescriptor where = WhereDescriptor.build("lower(name) = ? and brand_id = ?")
                .addParameter(name.toLowerCase(), DataType.VARCHAR)
                .addParameter(brandId, DataType.INTEGER);
        PersistentObject model = container.selectUnique("model", where);
        return model;
    }

    public ObservableList<PersistentObject> findModelByBrand(int brandId) throws Exception {
        WhereDescriptor where = WhereDescriptor.build("brand_id = ?")
                .addParameter(brandId, DataType.INTEGER);
        ObservableList<PersistentObject> models = container.select("model", where);
        return models;
    }


}
