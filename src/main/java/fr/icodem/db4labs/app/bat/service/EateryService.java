package fr.icodem.db4labs.app.bat.service;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import fr.icodem.db4labs.container.AppContainer;
import fr.icodem.db4labs.database.DataType;
import fr.icodem.db4labs.database.PersistentObject;
import fr.icodem.db4labs.database.WhereDescriptor;
import fr.icodem.db4labs.dbtools.transaction.Transactionnal;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

@Singleton @Transactionnal
public class EateryService {

    @Inject private AppContainer container;
    @Inject private ImageService imageService;

    public ObservableList<PersistentObject> findEateryList() throws Exception {
        ObservableList<PersistentObject> result = container.select("eatery");
        return result;
    }

    public PersistentObject findEateryById(int id) throws Exception {
        PersistentObject eatery = container.selectByPK("eatery", id);
        if (eatery == null) return null;

        // menu
        if (eatery.getProperty("menu_id") != null) {
            PersistentObject menu = container.selectByPK("menu", eatery.getProperty("menu_id"));
            eatery.setObject("menu", menu);
        }

        // practical information
        PersistentObject practicalInfo = container.selectByPK("practical_information", id);
        eatery.setObject("practicalInformation", practicalInfo);

        // images
        imageService.loadImages(eatery, "eateryImage", "eatery", id);
        ObservableList<PersistentObject> imgEateryList = (ObservableList<PersistentObject>) eatery.getObject("eateryImage");
        for (int i = 0; i < imgEateryList.size(); i++) {
            eatery.setObject("eateryImage" + (i + 1), imgEateryList.get(i));
        }
        eatery.setObject("eateryImage", null);

        imageService.loadImages(eatery, "foodSpottingImage", "food-spotting", id);
        ObservableList<PersistentObject> imgFSList = (ObservableList<PersistentObject>) eatery.getObject("foodSpottingImage");
        for (int i = 0; i < imgFSList.size(); i++) {
            eatery.setObject("foodSpottingImage" + (i + 1), imgFSList.get(i));
        }
        eatery.setObject("foodSpottingImage", null);

        // tags
        WhereDescriptor where = WhereDescriptor.build("eatery_id = ?")
                               .addParameter(id, DataType.INTEGER);
        List<PersistentObject> eateryTags = container.select("eatery_eatery_tag", where);
        List<PersistentObject> tags = new ArrayList<>();
        for (PersistentObject eateryTag : eateryTags) {
            PersistentObject tag = container.selectByPK("eatery_tag", eateryTag.getProperty("tag_id"));
            tags.add(tag);
        }
        eatery.setObject("tags", tags);

        // *** city, region, country
        PersistentObject city = container.selectByPK("city", eatery.getProperty("city_id"));
        eatery.setObject("city", city);
        PersistentObject region = container.selectByPK("region", city.getProperty("region_id"));
        eatery.setObject("region", region);
        PersistentObject country = container.selectByPK("country", city.getProperty("country_id"));
        eatery.setObject("country", country);

        return eatery;
    }

    public void saveEatery(PersistentObject eatery) throws Exception {

        // *** menu
        PersistentObject menu = (PersistentObject) eatery.getObject("menu");
        if (menu != null) {
            // delete menu if empty
            if (menu.getProperty("id") != null
                    && (Strings.isNullOrEmpty((String) menu.getProperty("content")))) {
                container.delete(menu);
                eatery.setProperty("menu_id", null);
            } else if (menu.getProperty("id") != null) {
                container.update(menu);
                eatery.setProperty("menu_id", menu.getProperty("id"));
            } else {
                container.insert(menu);
                eatery.setProperty("menu_id", menu.getProperty("id"));
            }
        }

        // *** eatery
        if (eatery.getProperty("id") != null) {
            container.update(eatery);
        } else {
            container.insert(eatery);
        }

        // *** practical information
        PersistentObject practicalInfo = (PersistentObject) eatery.getObject("practicalInformation");
        if (practicalInfo != null) {
            // delete practical info if empty
            if (Strings.isNullOrEmpty((String) practicalInfo.getProperty("hours_of_operation"))
                    && Strings.isNullOrEmpty((String) practicalInfo.getProperty("payment_options"))
                    && Strings.isNullOrEmpty((String) practicalInfo.getProperty("price"))
                    && Strings.isNullOrEmpty((String) practicalInfo.getProperty("parking"))) {
                if (practicalInfo.getProperty("id") != null) {
                    container.delete(practicalInfo);
                }
            }
            else {// save or update practical info
                if (practicalInfo.getProperty("id") == null) {
                    practicalInfo.setProperty("id", eatery.getProperty("id"));
                    container.insert(practicalInfo);
                }
                else {
                    container.update(practicalInfo);
                }
            }
        }

        // *** tags
        WhereDescriptor where = WhereDescriptor.build("eatery_id = ?")
                                               .addParameter(eatery.getProperty("id"), DataType.INTEGER);
        container.delete("eatery_eatery_tag", where);
        List<PersistentObject> tags = (List<PersistentObject>) eatery.getObject("tags");
        for (PersistentObject tag : tags) {
            PersistentObject eateryTag = new PersistentObject("eatery_eatery_tag")
                                                   .setProperty("eatery_id", eatery.getProperty("id"))
                                                   .setProperty("tag_id", tag.getProperty("id"));
            container.insert(eateryTag);
        }

        // *** images
        int eateryId = (int)eatery.getProperty("id");

        imageService.deleteImage("eatery", eateryId);
        for (int i = 1; i < 9; i++) {
            imageService.saveImage(eatery, "eateryImage" + i, "eatery", eateryId, true, 200, 200);
        }

        imageService.deleteImage("food-spotting", eateryId);
        for (int i = 1; i < 9; i++) {
            imageService.saveImage(eatery, "foodSpottingImage" + i, "food-spotting", eateryId, true, 200, 200);
        }

    }

    public void deleteEatery(PersistentObject eatery) throws Exception {
        WhereDescriptor where;
        int id = (int) eatery.getProperty("id");
        eatery = findEateryById(id);

        // delete tags
        where = WhereDescriptor.build("eatery_id = ?")
                               .addParameter(id, DataType.INTEGER);
        container.delete("eatery_eatery_tag", where);

        // delete practical information
        where = WhereDescriptor.build("id = ?")
                .addParameter(id, DataType.INTEGER);
        container.delete("practical_information", where);


        // delete eatery
        container.delete(eatery);

        // delete image
        for (int i = 1; i < 9; i++) {
            imageService.deleteImage("eatery", id);
            imageService.deleteImage("food-spotting", id);
        }

    }

}
