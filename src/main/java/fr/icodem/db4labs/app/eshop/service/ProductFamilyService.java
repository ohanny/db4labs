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
public class ProductFamilyService {

    @Inject private AppContainer container;
    @Inject private AppNameProvider appNameProvider;

    public ObservableList<PersistentObject> findProductFamilyList() throws Exception {
        ObservableList<PersistentObject> result = container.select("product_family");
        return result;
    }

    public ObservableList<PersistentObject> findProductFamilyTree() throws Exception {
        WhereDescriptor where = WhereDescriptor.build("parent_id is null");
        ObservableList<PersistentObject> result = container.select("product_family", where);
        for (PersistentObject family : result) {
            int family_id = (int) family.getProperty("id");
            where = WhereDescriptor.build("parent_id = ?")
                                   .addParameter(family_id, DataType.INTEGER);
            ObservableList<PersistentObject> children = container.select("product_family", where);
            family.setObject("children", children);
        }
        return result;
    }

    public PersistentObject findProductFamilyById(int id) throws Exception {
        //WhereDescriptor where = WhereDescriptor.build("id = ?")
        //        .addParameter(id, DataType.INTEGER);

        //PersistentObject family = container.selectUnique("product_family", where);
        PersistentObject family = container.selectByPK("product_family", id);
        if (family.getProperty("parent_id") != null) {
            //PersistentObject parent = findProductFamilyById((int)family.getProperty("parent_id"));
            //where = WhereDescriptor.build("id = ?")
                                   //.addParameter(family.getProperty("parent_id"), DataType.INTEGER);
            //PersistentObject parent =  container.selectUnique("product_family", where);
            PersistentObject parent =  container.selectByPK("product_family", family.getProperty("parent_id"));
            family.setObject("parent", parent);
        }

        return family;
    }

    public void saveProductFamily(PersistentObject family) throws Exception {
        if (family.getProperty("id") != null) {
            container.update(family);
        } else {
            container.insert(family);
        }
    }

    public void deleteProductFamily(PersistentObject family) throws Exception {
        // check if family can be deleted
        checkDeleteProductFamily(family);

        // ok, delete family
        container.delete(family);

    }

    public void checkDeleteProductFamily(PersistentObject family) throws Exception {
        // check if family is linked to a product
        WhereDescriptor where = WhereDescriptor.build("family_id = ?")
                                               .addParameter(family.getProperty("id"), DataType.INTEGER);
        int count = container.count("product", where);
        if (count > 0) throw new IllegalArgumentException("Cannot delete family because it is linked at least to one product");

        // check if family has children
        where = WhereDescriptor.build("parent_id = ?")
                               .addParameter(family.getProperty("id"), DataType.INTEGER);
        count = container.count("product_family", where);
        if (count > 0) throw new IllegalArgumentException("Cannot delete family because it's used as parent of another family");

    }
}
