package fr.icodem.db4labs.app.carpooling.service;

import com.google.inject.Inject;
import fr.icodem.db4labs.container.AppContainer;
import fr.icodem.db4labs.database.DataType;
import fr.icodem.db4labs.database.PersistentObject;
import fr.icodem.db4labs.database.WhereDescriptor;
import fr.icodem.db4labs.dbtools.transaction.Transactionnal;
import javafx.collections.ObservableList;

@Transactionnal
public class BrandService {

    @Inject private AppContainer container;

    public ObservableList<PersistentObject> findBrandList() throws Exception {
        ObservableList<PersistentObject> result = container.select("brand");
        return result;
    }

    public PersistentObject findBrandById(int id) throws Exception {
        PersistentObject brand = container.selectByPK("brand", id);

        return brand;
    }

    public void saveBrand(PersistentObject brand) throws Exception {
        if (brand.getProperty("id") != null) {
            container.update(brand);
        } else {
            container.insert(brand);
        }
    }

    public void deleteBrand(PersistentObject brand) throws Exception {
        // check if brand can be deleted
        checkDeleteBrand(brand);

        // ok, delete brand
        container.delete(brand);
    }

    public void checkDeleteBrand(PersistentObject brand) throws Exception {
        // check if brand is linked to a model
        WhereDescriptor where = WhereDescriptor.build("brand_id = ?")
                                               .addParameter(brand.getProperty("id"), DataType.INTEGER);
        int count = container.count("model", where);
        if (count > 0) throw new IllegalArgumentException("Cannot delete brand because it is linked at least to one model");
    }
}
