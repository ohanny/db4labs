package fr.icodem.db4labs.app.bat.service;

import com.google.inject.Inject;
import fr.icodem.db4labs.app.AppNameProvider;
import fr.icodem.db4labs.container.AppContainer;
import fr.icodem.db4labs.database.DataType;
import fr.icodem.db4labs.database.PersistentObject;
import fr.icodem.db4labs.database.WhereDescriptor;
import fr.icodem.db4labs.dbtools.transaction.Transactionnal;
import javafx.collections.ObservableList;

@Transactionnal
public class EateryTagService {

    @Inject private AppContainer container;

    public ObservableList<PersistentObject> findEateryTagList() throws Exception {
        ObservableList<PersistentObject> result = container.select("eatery_tag");
        return result;
    }

    public PersistentObject findEateryTagById(int id) throws Exception {
        PersistentObject tag = container.selectByPK("eatery_tag", id);

        return tag;
    }

    public void saveEateryTag(PersistentObject tag) throws Exception {
        if (tag.getProperty("id") != null) {
            container.update(tag);
        } else {
            container.insert(tag);
        }
    }

    public void deleteEateryTag(PersistentObject tag) throws Exception {
        WhereDescriptor where = WhereDescriptor.build("tag_id = ?")
                                               .addParameter(tag.getProperty("id"), DataType.INTEGER);

        container.delete("eatery_eatery_tag", where);
        container.delete(tag);

    }

}
