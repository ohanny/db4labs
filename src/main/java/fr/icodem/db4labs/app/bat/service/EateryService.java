package fr.icodem.db4labs.app.bat.service;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import fr.icodem.db4labs.app.bat.controller.EateryValidator;
import fr.icodem.db4labs.app.bat.event.EateryAddedEvent;
import fr.icodem.db4labs.app.eshop.controller.ProductValidator;
import fr.icodem.db4labs.app.eshop.event.ProductAddedEvent;
import fr.icodem.db4labs.component.ImageInput;
import fr.icodem.db4labs.container.AppContainer;
import fr.icodem.db4labs.database.DataType;
import fr.icodem.db4labs.database.PersistentObject;
import fr.icodem.db4labs.database.WhereDescriptor;
import fr.icodem.db4labs.dbtools.service.FileImporter;
import fr.icodem.db4labs.dbtools.transaction.Transactionnal;
import fr.icodem.db4labs.dbtools.validation.ValidatorException;
import fr.icodem.db4labs.dbtools.validation.ValidatorResult;
import fr.icodem.db4labs.dbtools.validation.ValidatorResults;
import fr.icodem.db4labs.event.DataImportDoneEvent;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

@Singleton @Transactionnal
public class EateryService {

    @Inject private AppContainer container;
    @Inject private ImageService imageService;
    @Inject private FileImporter fileImporter;
    @Inject private EventBus eventBus;

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

    public PersistentObject findCityByName(String name) throws IOException, SQLException {
        WhereDescriptor where = WhereDescriptor.build("lower(name) = ?")
                                               .addParameter(name.toLowerCase(), DataType.VARCHAR);
        PersistentObject city = container.selectUnique("city", where);
        return city;
    }

    public PersistentObject findCookingStyleByName(String name) throws IOException, SQLException {
        WhereDescriptor where = WhereDescriptor.build("lower(name) = ?")
                                               .addParameter(name.toLowerCase(), DataType.VARCHAR);
        PersistentObject cookingStyle = container.selectUnique("cooking_style", where);
        return cookingStyle;
    }

    public PersistentObject findEateryByNameAndCity(String name, int cityId) throws IOException, SQLException {
        WhereDescriptor where = WhereDescriptor.build("lower(name) = ? and city_id = ?")
                                               .addParameter(name.toLowerCase(), DataType.VARCHAR)
                                               .addParameter(cityId, DataType.INTEGER);
        PersistentObject eatery = container.selectUnique("eatery", where);
        return eatery;
    }

    public void importData(File file) throws Exception {
        fileImporter.importData(file, this::importData);
    }

    private void importData(InputStream is) throws Exception {
        // base properties
        String name = null;
        String executiveChef = null;
        String postCode = null;
        String street = null;
        String description = null;

        // menu
        String menuContent = null;

        // practical information
        String gettingThere = null;
        String parking = null;
        String hoursOfOperation1 = null;
        String hoursOfOperation2 = null;
        String price = null;
        String paymentOptions = null;

        // cooking style and city
        Integer cookingStyleId = null;
        Integer cityId = null;

        // images
        List<byte[]> images = new ArrayList<>();

        // parse XML content
        XMLInputFactory factory = XMLInputFactory.newInstance();

        XMLStreamReader reader = factory.createXMLStreamReader(is);

        String currentElement = null;
        String cityStr = null;
        while (reader.hasNext()) {
            int eventType = reader.next();
            switch (eventType) {
                case XMLEvent.CDATA:
                case XMLEvent.CHARACTERS:
                    switch (currentElement) {
                        case "name":
                            name = reader.getText();
                            break;

                        case "executive-chef":
                            executiveChef = reader.getText();
                            break;

                        case "postal-code":
                            postCode = reader.getText();
                            break;

                        case "street":
                            street = reader.getText();
                            break;

                        case "description":
                            description = reader.getText();
                            break;

                        case "menu":
                            menuContent = reader.getText();
                            break;

                        case "getting-there":
                            gettingThere = reader.getText();
                            break;

                        case "parking":
                            parking = reader.getText();
                            break;

                        case "hours1":
                            hoursOfOperation1 = reader.getText();
                            break;

                        case "hours2":
                            hoursOfOperation2 = reader.getText();
                            break;

                        case "price":
                            price = reader.getText();
                            break;

                        case "payment-options":
                            paymentOptions = reader.getText();
                            break;

                        case "image":
                            String img64 = reader.getText();
                            if (img64 != null) {
                                byte[] img = Base64.getDecoder().decode(img64);
                                images.add(img);
                            }
                            break;

                        case "city":
                            cityStr = reader.getText();
                            if (cityStr != null) {
                                PersistentObject city = findCityByName(cityStr);
                                if (city != null) {
                                    cityId = (Integer) city.getProperty("id");
                                }
                            }
                            if (cityId == null) {
                                System.out.println("City is incorrect : " + name);
                                throw new IllegalArgumentException("City is incorrect");
                            }
                            break;

                        case "cooking-style":
                            String cookingStyleStr = reader.getText();
                            if (cookingStyleStr != null) {
                                PersistentObject cookingStyle = findCookingStyleByName(cookingStyleStr);
                                if (cookingStyle != null) {
                                    cookingStyleId = (Integer) cookingStyle.getProperty("id");
                                }
                            }
                            if (cookingStyleId == null) {
                                System.out.println("CookingStyle is incorrect : " + name);
                                throw new IllegalArgumentException("Cooking style is incorrect");
                            }

                            break;

                    }
                    break;

                case XMLEvent.START_ELEMENT:
                    currentElement = reader.getLocalName();
                    break;
            }
        }

        byte[] eateryImage1 = (images.size() > 0)?images.get(0):null;
        byte[] eateryImage2 = (images.size() > 1)?images.get(1):null;
        byte[] eateryImage3 = (images.size() > 2)?images.get(2):null;
        byte[] eateryImage4 = (images.size() > 3)?images.get(3):null;
        byte[] eateryImage5 = (images.size() > 4)?images.get(4):null;
        byte[] eateryImage6 = (images.size() > 5)?images.get(5):null;
        byte[] eateryImage7 = (images.size() > 6)?images.get(6):null;
        byte[] eateryImage8 = (images.size() > 7)?images.get(7):null;
        byte[] foodSpottingImage1 = (images.size() > 8)?images.get(8):null;
        byte[] foodSpottingImage2 = (images.size() > 9)?images.get(9):null;
        byte[] foodSpottingImage3 = (images.size() > 10)?images.get(10):null;
        byte[] foodSpottingImage4 = (images.size() > 11)?images.get(11):null;
        byte[] foodSpottingImage5 = (images.size() > 12)?images.get(12):null;
        byte[] foodSpottingImage6 = (images.size() > 13)?images.get(13):null;
        byte[] foodSpottingImage7 = (images.size() > 14)?images.get(14):null;
        byte[] foodSpottingImage8 = (images.size() > 15)?images.get(15):null;

        List<PersistentObject> tags = new ArrayList<>();

//        System.out.println("name="+name);
//        System.out.println("executiveChef="+executiveChef);
//        System.out.println("postCode="+postCode);
//        System.out.println("street="+street);
//        System.out.println("description="+description);
//        System.out.println("menu="+menuContent);
//        System.out.println("gettingThere="+gettingThere);
//        System.out.println("parking="+parking);
//        System.out.println("hoursOfOperation1="+hoursOfOperation1);
//        System.out.println("hoursOfOperation2="+hoursOfOperation2);
//        System.out.println("price="+price);
//        System.out.println("paymentOptions="+paymentOptions);
//        System.out.println("cookingStyleId="+cookingStyleId);
//        System.out.println("cityId="+cityId);
//        System.out.println("eateryImage1="+eateryImage1);
//        System.out.println("eateryImage2="+eateryImage2);
//        System.out.println("eateryImage3="+eateryImage3);
//        System.out.println("eateryImage4="+eateryImage4);
//        System.out.println("eateryImage5="+eateryImage5);
//        System.out.println("eateryImage6="+eateryImage6);
//        System.out.println("eateryImage7="+eateryImage7);
//        System.out.println("eateryImage8="+eateryImage8);
//        System.out.println("foodSpottingImage1="+foodSpottingImage1);
//        System.out.println("foodSpottingImage2="+foodSpottingImage2);
//        System.out.println("foodSpottingImage3="+foodSpottingImage3);
//        System.out.println("foodSpottingImage4="+foodSpottingImage4);
//        System.out.println("foodSpottingImage5="+foodSpottingImage5);
//        System.out.println("foodSpottingImage6="+foodSpottingImage6);
//        System.out.println("foodSpottingImage7="+foodSpottingImage7);
//        System.out.println("foodSpottingImage8="+foodSpottingImage8);

        PersistentObject po = new PersistentObject("eatery");
        populateEatery(po, name, executiveChef, postCode, street,
                description, menuContent, gettingThere, parking,
                hoursOfOperation1, hoursOfOperation2, price, paymentOptions,
                cookingStyleId, cityId, eateryImage1, eateryImage2, eateryImage3,
                eateryImage4, eateryImage5, eateryImage6, eateryImage7, eateryImage8,
                foodSpottingImage1, foodSpottingImage2, foodSpottingImage3, foodSpottingImage4,
                foodSpottingImage5, foodSpottingImage6, foodSpottingImage7, foodSpottingImage8,
                tags);


        // perform validation
        ValidatorResults results = new EateryValidator().validate(po);

        // validation errors
        if (!results.isValid()) {
            for (ValidatorResult vr : results.getResults()) {
                System.out.println(vr.getProperty() + " : " + vr.getMessage() + " : " + vr.getOriginalValue());
            }
            throw new ValidatorException("Eatery validation failed", results);
        }

        // check if eatery is not already present in database
        if (findEateryByNameAndCity(name, cityId) != null) {
            throw new IllegalArgumentException("Eatery already exists in database : "
                    + name + " (" + cityStr + ")");
        }

        // save eatery
        saveEatery(po);

        // notify eatery tab controller
        eventBus.post(new EateryAddedEvent(po));

    }

    public void populateEatery(PersistentObject po, String name, String executiveChef,
                               String postCode, String street, String description,
                               String menuContent, String gettingThere, String parking,
                               String hoursOfOperation1, String hoursOfOperation2,
                               String price, String paymentOptions, Integer cookingStyleId,
                               Integer cityId, byte[] eateryImage1, byte[] eateryImage2,
                               byte[] eateryImage3, byte[] eateryImage4, byte[] eateryImage5,
                               byte[] eateryImage6, byte[] eateryImage7, byte[] eateryImage8,
                               byte[] foodSpottingImage1, byte[] foodSpottingImage2,
                               byte[] foodSpottingImage3, byte[] foodSpottingImage4,
                               byte[] foodSpottingImage5, byte[] foodSpottingImage6,
                               byte[] foodSpottingImage7, byte[] foodSpottingImage8,
                               List<PersistentObject> tags) {
        // base properties
        po.setProperty("name", name);
        po.setProperty("executive_chef", executiveChef);
        po.setProperty("post_code", postCode);
        po.setProperty("street", street);
        po.setProperty("description", description);

        // menu
        PersistentObject menu = (PersistentObject) po.getObject("menu");
        if (menu == null) {
            menu = new PersistentObject("menu");
            po.setObject("menu", menu);
        }
        menu.setProperty("content", menuContent);

        // practical information
        PersistentObject practicalInfo = (PersistentObject) po.getObject("practicalInformation");
        if (practicalInfo == null) {
            practicalInfo = new PersistentObject("practical_information");
            po.setObject("practicalInformation", practicalInfo);
        }
        practicalInfo.setProperty("getting_there", gettingThere);
        practicalInfo.setProperty("parking", parking);
        practicalInfo.setProperty("hours_of_operation1", hoursOfOperation1);
        practicalInfo.setProperty("hours_of_operation2", hoursOfOperation2);
        practicalInfo.setProperty("price", price);
        practicalInfo.setProperty("payment_options", paymentOptions);

        // cooking style
        po.setProperty("cooking_style_id", cookingStyleId);

        // city
        po.setProperty("city_id", cityId);

        // images
        populateImage(eateryImage1, po, "eateryImage1");
        populateImage(eateryImage2, po, "eateryImage2");
        populateImage(eateryImage3, po, "eateryImage3");
        populateImage(eateryImage4, po, "eateryImage4");
        populateImage(eateryImage5, po, "eateryImage5");
        populateImage(eateryImage6, po, "eateryImage6");
        populateImage(eateryImage7, po, "eateryImage7");
        populateImage(eateryImage8, po, "eateryImage8");
        populateImage(foodSpottingImage1, po, "foodSpottingImage1");
        populateImage(foodSpottingImage2, po, "foodSpottingImage2");
        populateImage(foodSpottingImage3, po, "foodSpottingImage3");
        populateImage(foodSpottingImage4, po, "foodSpottingImage4");
        populateImage(foodSpottingImage5, po, "foodSpottingImage5");
        populateImage(foodSpottingImage6, po, "foodSpottingImage6");
        populateImage(foodSpottingImage7, po, "foodSpottingImage7");
        populateImage(foodSpottingImage8, po, "foodSpottingImage8");

        // tags
        po.setObject("tags", tags);
    }

    private void populateImage(byte[] img, PersistentObject po, String objectName) {
        PersistentObject imgItem = (PersistentObject) po.getObject(objectName);
        if (img != null) {
            if (imgItem == null) {
                imgItem = new PersistentObject("image_data");
                imgItem.setProperty("content", img);
                po.setObject(objectName, imgItem);
            } else {
                imgItem.setProperty("content", img);
            }
        } else if (imgItem != null) {// no image, delete previous one
            po.setObject(objectName, null);
            po.setObject(objectName + ".old", imgItem);// so that service can delete old image
        }
    }

}
