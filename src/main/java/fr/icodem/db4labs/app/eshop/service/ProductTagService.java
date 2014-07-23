package fr.icodem.db4labs.app.eshop.service;

import com.google.inject.Inject;
import javafx.collections.ObservableList;
import fr.icodem.db4labs.app.AppNameProvider;
import fr.icodem.db4labs.container.AppContainer;
import fr.icodem.db4labs.database.DataType;
import fr.icodem.db4labs.database.PersistentObject;
import fr.icodem.db4labs.database.WhereDescriptor;
import fr.icodem.db4labs.dbtools.transaction.Transactionnal;

@Transactionnal
public class ProductTagService {

    @Inject private AppContainer container;
    @Inject private AppNameProvider appNameProvider;

    public ObservableList<PersistentObject> findProductTagList() throws Exception {
        ObservableList<PersistentObject> result = container.select("product_tag");
        return result;
    }

    public PersistentObject findProductTagById(int id) throws Exception {
        //WhereDescriptor where = WhereDescriptor.build("id = ?")
          //      .addParameter(id, DataType.INTEGER);

        //PersistentObject family = container.selectUnique("product_tag", where);
        PersistentObject tag = container.selectByPK("product_tag", id);

        return tag;
    }

    public void saveProductTag(PersistentObject tag) throws Exception {
        if (tag.getProperty("id") != null) {
            container.update(tag);
        } else {
            container.insert(tag);
        }
    }

    public void deleteProductTag(PersistentObject tag) throws Exception {
        WhereDescriptor where = WhereDescriptor.build("tag_id = ?")
                                               .addParameter(tag.getProperty("id"), DataType.INTEGER);

        container.delete("product_product_tag", where);
        container.delete(tag);

    }

}
