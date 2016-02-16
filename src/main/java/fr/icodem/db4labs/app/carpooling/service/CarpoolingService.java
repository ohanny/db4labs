package fr.icodem.db4labs.app.carpooling.service;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import fr.icodem.db4labs.app.carpooling.controller.ModelValidator;
import fr.icodem.db4labs.app.carpooling.event.ModelAddedEvent;
import fr.icodem.db4labs.container.AppContainer;
import fr.icodem.db4labs.database.PersistentObject;
import fr.icodem.db4labs.dbtools.service.FileImporter;
import fr.icodem.db4labs.dbtools.transaction.Transactionnal;
import fr.icodem.db4labs.dbtools.validation.ValidatorException;
import fr.icodem.db4labs.dbtools.validation.ValidatorResult;
import fr.icodem.db4labs.dbtools.validation.ValidatorResults;
import javafx.collections.ObservableList;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.InputStream;

@Transactionnal
public class CarpoolingService {

    @Inject private AppContainer container;
    @Inject private FileImporter fileImporter;
    @Inject private EventBus eventBus;

    public void importData(File file) throws Exception {
        fileImporter.importData(file, this::importData);
    }

    private void importData(InputStream is) throws Exception {
        // base properties
        String name = null;

        // brand
        Integer brandId = null;

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

                        case "brand":
                            String brandStr = reader.getText();
                            if (brandStr != null) {
                                PersistentObject brand = findBrandByName(brandStr);
                                if (brand != null) {
                                    brandId = (Integer) brand.getProperty("id");
                                }
                            }
                            if (brandId == null) {
                                System.out.println("Brand is incorrect : " + name);
                                throw new IllegalArgumentException("Brand is incorrect");
                            }

                            break;

                    }
                    break;

                case XMLEvent.START_ELEMENT:
                    currentElement = reader.getLocalName();
                    break;
            }
        }


//        System.out.println("name="+name);
//        System.out.println("brandId="+brandId);

        PersistentObject po = new PersistentObject("model");
        populateModel(po, name, brandId);


        // perform validation
        ValidatorResults results = new ModelValidator().validate(po);

        // validation errors
        if (!results.isValid()) {
            for (ValidatorResult vr : results.getResults()) {
                System.out.println(vr.getProperty() + " : " + vr.getMessage() + " : " + vr.getOriginalValue());
            }
            throw new ValidatorException("Model validation failed", results);
        }

        // check if model is not already present in database
        if (findEateryByNameAndCity(name, cityId) != null) {
            throw new IllegalArgumentException("Eatery already exists in database : "
                    + name + " (" + cityStr + ")");
        }

        // save model
        saveModel(po);

        // notify car model tab controller
        eventBus.post(new ModelAddedEvent(po));

    }



}
