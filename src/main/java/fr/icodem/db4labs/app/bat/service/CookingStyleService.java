package fr.icodem.db4labs.app.bat.service;

import com.google.inject.Inject;
import fr.icodem.db4labs.container.AppContainer;
import fr.icodem.db4labs.database.DataType;
import fr.icodem.db4labs.database.PersistentObject;
import fr.icodem.db4labs.database.WhereDescriptor;
import fr.icodem.db4labs.dbtools.transaction.Transactionnal;
import javafx.collections.ObservableList;

@Transactionnal
public class CookingStyleService {

    @Inject private AppContainer container;

    public ObservableList<PersistentObject> findCookingStyleList() throws Exception {
        ObservableList<PersistentObject> result = container.select("cooking_style");
        return result;
    }

    public PersistentObject findCookingStyleById(int id) throws Exception {
        PersistentObject cookingStyle = container.selectByPK("cooking_style", id);

        return cookingStyle;
    }

    public void saveCookingStyle(PersistentObject cookingStyle) throws Exception {
        if (cookingStyle.getProperty("id") != null) {
            container.update(cookingStyle);
        } else {
            container.insert(cookingStyle);
        }
    }

    public void deleteCookingStyle(PersistentObject cookingStyle) throws Exception {
        // check if cooking style can be deleted
        checkDeleteCookingStyle(cookingStyle);

        // ok, delete cooking style
        container.delete(cookingStyle);
    }

    public void checkDeleteCookingStyle(PersistentObject cookingStyle) throws Exception {
        // check if cooking style is linked to an eatery
        WhereDescriptor where = WhereDescriptor.build("cooking_style_id = ?")
                                               .addParameter(cookingStyle.getProperty("id"), DataType.INTEGER);
        int count = container.count("eatery", where);
        if (count > 0) throw new IllegalArgumentException("Cannot delete cooking style because it is linked at least to one eatery");
    }
}
