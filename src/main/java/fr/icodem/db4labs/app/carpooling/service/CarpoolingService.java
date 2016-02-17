package fr.icodem.db4labs.app.carpooling.service;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import fr.icodem.db4labs.app.carpooling.controller.CityValidator;
import fr.icodem.db4labs.app.carpooling.event.BrandAddedEvent;
import fr.icodem.db4labs.app.carpooling.event.CityAddedEvent;
import fr.icodem.db4labs.app.carpooling.event.ModelAddedEvent;
import fr.icodem.db4labs.app.eshop.controller.ProductValidator;
import fr.icodem.db4labs.database.PersistentObject;
import fr.icodem.db4labs.dbtools.service.FileImporter;
import fr.icodem.db4labs.dbtools.transaction.Transactionnal;
import fr.icodem.db4labs.dbtools.validation.ValidatorException;
import fr.icodem.db4labs.dbtools.validation.ValidatorResult;
import fr.icodem.db4labs.dbtools.validation.ValidatorResults;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.InputStream;

@Transactionnal
public class CarpoolingService {

    @Inject private FileImporter fileImporter;
    @Inject private EventBus eventBus;
    @Inject private CityService cityService;
    @Inject private BrandService brandService;
    @Inject private ModelService modelService;

    public void importData(File file) throws Exception {
        fileImporter.importData(file, this::importData);
    }

    private void importData(InputStream is) throws Exception {

        // attributes
        String nameAtt = null;
        String postcodeAtt = null;
        String longitudeAtt = null;
        String latitudeAtt = null;
        String brandAtt = null;

        // parse XML content
        XMLInputFactory factory = XMLInputFactory.newInstance();

        XMLStreamReader reader = factory.createXMLStreamReader(is);

        String currentElement = null;
        while (reader.hasNext()) {
            int eventType = reader.next();
            switch (eventType) {
                case XMLEvent.CDATA:
                case XMLEvent.CHARACTERS:
                    switch (currentElement) {
                        case "brand":
                            String brandStr = reader.getText();
                            if (brandStr != null && !brandStr.trim().isEmpty()) {
                                brandStr = brandStr.trim();
                                PersistentObject brand = brandService.findBrandByName(brandStr);
                                if (brand != null) {
                                    System.out.println("Brand already exists : " + brandStr);
                                }
                                else {
                                    PersistentObject po = new PersistentObject("brand");
                                    po.setProperty("name", brandStr);
                                    brandService.saveBrand(po);

                                    // notify brand tab controller
                                    eventBus.post(new BrandAddedEvent(po));
                                }
                            }

                            break;

                    }
                    break;

                case XMLEvent.START_ELEMENT:
                    currentElement = reader.getLocalName();
                    switch (currentElement) {
                        case "city":
                            nameAtt = reader.getAttributeValue(null, "name");
                            postcodeAtt = reader.getAttributeValue(null, "postcode");
                            longitudeAtt = reader.getAttributeValue(null, "longitude");
                            latitudeAtt = reader.getAttributeValue(null, "latitude");
                            if (nameAtt != null) {
                                PersistentObject city = cityService.findCityByNameAndPostCode(nameAtt, postcodeAtt);
                                if (city != null) {
                                    System.out.println("City already exists : " + nameAtt + " - " + postcodeAtt);
                                }
                                else {
                                    PersistentObject po = new PersistentObject("city");
                                    cityService.populateCity(po, nameAtt, postcodeAtt, longitudeAtt, latitudeAtt);

                                    // perform validation
                                    ValidatorResults results = new CityValidator().validate(po);

                                    // validation errors
                                    if (!results.isValid()) {
                                        for (ValidatorResult vr : results.getResults()) {
                                            System.out.println(vr.getProperty() + " : " + vr.getMessage() + " : " + vr.getOriginalValue());
                                        }
                                        throw new ValidatorException("City validation failed", results);
                                    }

                                    // save to database
                                    cityService.saveCity(po);

                                    // notify city tab controller
                                    eventBus.post(new CityAddedEvent(po));
                                }
                            }
                            break;

                        case "model":
                            nameAtt = reader.getAttributeValue(null, "name");
                            brandAtt = reader.getAttributeValue(null, "brand");
                            if (nameAtt != null) {
                                PersistentObject brand = brandService.findBrandByName(brandAtt);
                                PersistentObject model = modelService.findModelByNameAndBrand(nameAtt, (Integer)brand.getProperty("id"));
                                if (model != null) {
                                    System.out.println("Model already exists : " + nameAtt);
                                }
                                else {
                                    //PersistentObject brand = brandService.findBrandByName(brandAtt);
                                    if (brand == null) {
                                        System.out.println("Brand is incorrect : " + brandAtt);
                                    } else {
                                        Integer brandId = (Integer) brand.getProperty("id");
                                        PersistentObject po = new PersistentObject("model");
                                        modelService.populateModel(po, nameAtt, brandId);
                                        modelService.saveModel(po);

                                        // notify car model tab controller
                                        eventBus.post(new ModelAddedEvent(po));
                                    }

                                }
                            }

                            break;
                    }
                    break;

            }
        }

    }

}
