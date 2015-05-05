package fr.icodem.db4labs.app.bat.controller;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import fr.icodem.db4labs.app.AppNameProvider;
import fr.icodem.db4labs.app.bat.event.EateryAddedEvent;
import fr.icodem.db4labs.app.bat.service.CookingStyleService;
import fr.icodem.db4labs.app.bat.service.CountryService;
import fr.icodem.db4labs.app.bat.service.EateryService;
import fr.icodem.db4labs.app.bat.service.EateryTagService;
import fr.icodem.db4labs.app.eshop.event.ProductAddedEvent;
import fr.icodem.db4labs.component.FormState;
import fr.icodem.db4labs.component.ImageInput;
import fr.icodem.db4labs.container.AppContainer;
import fr.icodem.db4labs.database.PersistentObject;
import fr.icodem.db4labs.dbtools.validation.MessageBinder;
import fr.icodem.db4labs.dbtools.validation.MessageBinders;
import fr.icodem.db4labs.dbtools.validation.MessageBindersBuilder;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

@Singleton
public class EateryTabController implements Initializable {

    @FXML private TableView<PersistentObject> tableView;
    @FXML private GridPane tablePane;
    @FXML private GridPane formPane;

    @FXML private GridPane mainFormPane;
    @FXML private GridPane menuFormPane;
    @FXML private GridPane eateryImagesFormPane;
    @FXML private GridPane foodSpottingImagesFormPane;

    @FXML private FlowPane tagsBox;

    @FXML private Button editButton;
    @FXML private Button deleteButton;
    
    @FXML private TextField filterTextField;

    // base product properties
    @FXML private TextField idTextField;
    @FXML private TextField nameTextField;
    @FXML private TextField executiveChefTextField;
    @FXML private TextField postCodeTextField;
    @FXML private TextField streetTextField;
    @FXML private TextArea descriptionTextArea;
    @FXML private TextField parkingTextField;
    @FXML private TextField gettingThereTextField;
    @FXML private TextField hoursOfOperationTextField1;
    @FXML private TextField hoursOfOperationTextField2;
    @FXML private TextField priceTextField;
    @FXML private TextField paymentOptionsTextField;
    @FXML private ComboBox<PersistentObject> cookingStyleComboBox;
    @FXML private ComboBox<PersistentObject> countryComboBox;
    @FXML private ComboBox<PersistentObject> regionComboBox;
    @FXML private ComboBox<PersistentObject> cityComboBox;

    @FXML private TextArea menuTextArea;

    @FXML private ImageInput eateryImageInput1;
    @FXML private ImageInput eateryImageInput2;
    @FXML private ImageInput eateryImageInput3;
    @FXML private ImageInput eateryImageInput4;
    @FXML private ImageInput eateryImageInput5;
    @FXML private ImageInput eateryImageInput6;
    @FXML private ImageInput eateryImageInput7;
    @FXML private ImageInput eateryImageInput8;
    @FXML private ImageInput foodSpottingImageInput1;
    @FXML private ImageInput foodSpottingImageInput2;
    @FXML private ImageInput foodSpottingImageInput3;
    @FXML private ImageInput foodSpottingImageInput4;
    @FXML private ImageInput foodSpottingImageInput5;
    @FXML private ImageInput foodSpottingImageInput6;
    @FXML private ImageInput foodSpottingImageInput7;
    @FXML private ImageInput foodSpottingImageInput8;

    // message binders
    private MessageBinders messageBinders;

    private FormState formState;

    @Inject private AppContainer container;

    @Inject private AppNameProvider appNameProvider;

    @Inject private EateryService eateryService;
    @Inject private CookingStyleService cookingStyleService;
    @Inject private CountryService countryService;
    @Inject private EateryTagService eateryTagService;

    private final DecimalFormat fmtPrice = new DecimalFormat("0.00");
    
    private ObservableList<PersistentObject> eateries;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        formPane.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                formPane.translateXProperty().setValue(formPane.getWidth());
            }
        });

        tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tableView.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<Integer>() {
            @Override
            public void onChanged(Change<? extends Integer> change) {
                if (change.getList().size() == 1) {
                    editButton.setDisable(false);
                    deleteButton.setDisable(false);
                }
                else {
                    editButton.setDisable(true);
                    deleteButton.setDisable(true);
                }
            }

        });

        // country combo box selection change
        countryComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<PersistentObject>() {
            @Override
            public void changed(ObservableValue<? extends PersistentObject> selected,
                                PersistentObject oldValue, PersistentObject newValue) {
                if (selected.getValue() == null) {
                    regionComboBox.setItems(FXCollections.<PersistentObject>observableArrayList());
                    return;
                }
                ObservableList<PersistentObject> regions = (ObservableList<PersistentObject>) selected.getValue().getObject("regions");
                if (regions != null) {
                    regionComboBox.setItems(regions);
                } else {
                    regionComboBox.setItems(FXCollections.<PersistentObject>observableArrayList());
                }
            }
        });

        // region combo box selection change
        regionComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<PersistentObject>() {
            @Override
            public void changed(ObservableValue<? extends PersistentObject> selected,
                                PersistentObject oldValue, PersistentObject newValue) {
                if (selected.getValue() == null) {
                    cityComboBox.setItems(FXCollections.<PersistentObject>observableArrayList());
                    return;
                }
                ObservableList<PersistentObject> cities = (ObservableList<PersistentObject>) selected.getValue().getObject("cities");
                if (cities != null) {
                    cityComboBox.setItems(cities);
                } else {
                    cityComboBox.setItems(FXCollections.<PersistentObject>observableArrayList());
                }
            }
        });

        // load items
        find(null);
        
        // filter product list
        filterTextField.textProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldVal, Object newVal) {
                filter((String) oldVal, (String) newVal);
            }
        });

        // message binders
        messageBinders = new MessageBindersBuilder()
                .bind("name").to(nameTextField)
                .bind("description").to(descriptionTextArea)
                .bind("executive_chef").to(executiveChefTextField)
                .bind("street").to(streetTextField)
                .bind("post_code").to(postCodeTextField)
                .bind("cooking_style_id").to(cookingStyleComboBox)
                .bind("city_id").to(cityComboBox)
                .bind("getting_there").to(gettingThereTextField)
                .bind("parking").to(parkingTextField)
                .bind("hours_of_operation1").to(hoursOfOperationTextField1)
                .bind("hours_of_operation2").to(hoursOfOperationTextField2)
                .bind("price").to(priceTextField)
                .bind("payment_options").to(paymentOptionsTextField)
                .build();

    }
    
    public void filter(String oldVal, String newVal) {
        if (oldVal != null && (newVal.length() < oldVal.length())) {
            tableView.setItems(eateries);
        }
        String value = newVal.toLowerCase().trim();
        ObservableList<PersistentObject> subentries = FXCollections.observableArrayList();
        for (Object entry : tableView.getItems()) {
            PersistentObject po = (PersistentObject) entry;
            String entryText = (String) po.getProperty("name");
            if (entryText.toLowerCase().contains(value)) {
                subentries.add(po);
            }
        }
        tableView.setItems(subentries);
    }

    @FXML
    public void find(ActionEvent event) {
        try {
            eateries = eateryService.findEateryList();
            tableView.setItems(eateries);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void add(ActionEvent event) {
        formState = FormState.Add;
        idTextField.setText("");
        nameTextField.setText("");
        executiveChefTextField.setText("");
        postCodeTextField.setText("");
        streetTextField.setText("");
        descriptionTextArea.setText("");
        hoursOfOperationTextField1.setText("");
        hoursOfOperationTextField2.setText("");
        priceTextField.setText("");
        parkingTextField.setText("");
        gettingThereTextField.setText("");
        paymentOptionsTextField.setText("");
        menuTextArea.setText("");

        eateryImageInput1.setData(null);
        eateryImageInput2.setData(null);
        eateryImageInput3.setData(null);
        eateryImageInput4.setData(null);
        eateryImageInput5.setData(null);
        eateryImageInput6.setData(null);
        eateryImageInput7.setData(null);
        eateryImageInput8.setData(null);
        foodSpottingImageInput1.setData(null);
        foodSpottingImageInput2.setData(null);
        foodSpottingImageInput3.setData(null);
        foodSpottingImageInput4.setData(null);
        foodSpottingImageInput5.setData(null);
        foodSpottingImageInput6.setData(null);
        foodSpottingImageInput7.setData(null);
        foodSpottingImageInput8.setData(null);

        // retrieve countries
        try {
            ObservableList<PersistentObject> countries = countryService.findCountryList();
            countryComboBox.setItems(countries);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // select country
        countryComboBox.getSelectionModel().clearSelection();

        // retrieve cooking styles
        try {
            ObservableList<PersistentObject> countries = cookingStyleService.findCookingStyleList();
            cookingStyleComboBox.setItems(countries);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // select cooking style
        cookingStyleComboBox.getSelectionModel().clearSelection();

        try {
            displayTags(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        showForm();
    }
    
    @FXML
    protected void select(MouseEvent event) {
        if (event.getClickCount() == 2) {
            edit(null);
        }
    }

    @FXML
    protected void edit(ActionEvent event) {
        formState = FormState.Edit;

        PersistentObject eatery = tableView.getSelectionModel().getSelectedItem();

        try {
            eatery = eateryService.findEateryById((Integer) eatery.getProperty("id"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        displayText(eatery, "id", idTextField);
        displayText(eatery, "name", nameTextField);
        displayText(eatery, "executive_chef", executiveChefTextField);
        displayText(eatery, "post_code", postCodeTextField);
        displayText(eatery, "street", streetTextField);
        if (eatery.getProperty("description") != null) {
            descriptionTextArea.setText(eatery.getProperty("description").toString());
        } else descriptionTextArea.setText("");

        // menu
        PersistentObject menu = (PersistentObject) eatery.getObject("menu");
        if (menu != null) {
            menuTextArea.setText(menu.getProperty("content").toString());
        } else menuTextArea.setText("");

        // practical info
        PersistentObject practicalInfo = (PersistentObject) eatery.getObject("practicalInformation");
        displayText(practicalInfo, "getting_there", gettingThereTextField);
        displayText(practicalInfo, "parking", parkingTextField);
        displayText(practicalInfo, "hours_of_operation1", hoursOfOperationTextField1);
        displayText(practicalInfo, "hours_of_operation2", hoursOfOperationTextField2);
        displayText(practicalInfo, "price", priceTextField);
        displayText(practicalInfo, "payment_options", paymentOptionsTextField);

        // images
        displayImage(eatery, "eateryImage1", eateryImageInput1);
        displayImage(eatery, "eateryImage2", eateryImageInput2);
        displayImage(eatery, "eateryImage3", eateryImageInput3);
        displayImage(eatery, "eateryImage4", eateryImageInput4);
        displayImage(eatery, "eateryImage5", eateryImageInput5);
        displayImage(eatery, "eateryImage6", eateryImageInput6);
        displayImage(eatery, "eateryImage7", eateryImageInput7);
        displayImage(eatery, "eateryImage8", eateryImageInput8);

        displayImage(eatery, "foodSpottingImage1", foodSpottingImageInput1);
        displayImage(eatery, "foodSpottingImage2", foodSpottingImageInput2);
        displayImage(eatery, "foodSpottingImage3", foodSpottingImageInput3);
        displayImage(eatery, "foodSpottingImage4", foodSpottingImageInput4);
        displayImage(eatery, "foodSpottingImage5", foodSpottingImageInput5);
        displayImage(eatery, "foodSpottingImage6", foodSpottingImageInput6);
        displayImage(eatery, "foodSpottingImage7", foodSpottingImageInput7);
        displayImage(eatery, "foodSpottingImage8", foodSpottingImageInput8);

        // retrieve countries
        try {
            ObservableList<PersistentObject> countries = countryService.findCountryList();
            countryComboBox.setItems(countries);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // retrieve cooking styles
        try {
            ObservableList<PersistentObject> countries = cookingStyleService.findCookingStyleList();
            cookingStyleComboBox.setItems(countries);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // select city, region, country
        PersistentObject city = (PersistentObject) eatery.getObject("city");
        PersistentObject region = (PersistentObject) eatery.getObject("region");
        PersistentObject country = (PersistentObject) eatery.getObject("country");
        int countryId = (int) country.getProperty("id");
        selectItem(countryId, countryComboBox);
        int regionId = (int) region.getProperty("id");
        selectItem(regionId, regionComboBox);
        int cityId = (int) city.getProperty("id");
        selectItem(cityId, cityComboBox);

        // select cooking style
        cookingStyleComboBox.getSelectionModel().clearSelection();
        int cookingStyleId = (int) eatery.getProperty("cooking_style_id");
        selectItem(cookingStyleId, cookingStyleComboBox);

        try {
            displayTags(eatery);
        } catch (Exception e) {
            e.printStackTrace();
        }

        showForm();
    }

    private void displayText(PersistentObject item, String property, TextField textField) {
        if (item != null) {
            if (item.getProperty(property) != null) {
                textField.setText(item.getProperty(property).toString());
            } else textField.setText("");
        } else {
            textField.setText("");
        }
    }

    private void displayImage(PersistentObject eatery, String objectName, ImageInput imageInput) {
        if (eatery.getObject(objectName) != null) {
            PersistentObject image = (PersistentObject) eatery.getObject(objectName);
            imageInput.setData((byte[]) image.getProperty("content"));
        } else imageInput.setData(null);
    }

    private boolean selectItem(int itemId, ComboBox<PersistentObject> comboBox) {
        boolean found = false;
        for (int i = 0; i < comboBox.getItems().size(); i++) {
            PersistentObject po = comboBox.getItems().get(i);
            if (po.getProperty("id").equals(itemId)) {
                comboBox.getSelectionModel().select(i);
                found = true;
                break;
            }
        }
        return found;
    }


    private void displayTags(PersistentObject product) throws Exception {
        tagsBox.getChildren().clear();
        List<PersistentObject> tags = eateryTagService.findEateryTagList();
        for (PersistentObject tag : tags) {
            CheckBox chk = new CheckBox((String) tag.getProperty("name"));
            //chk.setId("tagCheckBox" + tag.getProperty("id"));
            chk.setUserData(tag);
            tagsBox.getChildren().add(chk);

            if (product != null && product.getObject("tags") != null) {
                List<PersistentObject> productTags = (List<PersistentObject>) product.getObject("tags");
                for (PersistentObject productTag : productTags) {
                    if (productTag.getProperty("id").equals(tag.getProperty("id"))) {
                        chk.setSelected(true);
                    }
                }
            }
        }
    }

    @FXML
    protected void cancel(ActionEvent event) {
        hideForm();
    }

    @FXML
    protected void save(ActionEvent event) {
        ObservableList<PersistentObject> items = tableView.getItems();

        try {
            PersistentObject item = null;
            switch (formState) {
                case Add:
                    // populate data
                    item = new PersistentObject("eatery");
                    item.setObject("menu", new PersistentObject("menu"));
                    item.setObject("practicalInformation", new PersistentObject("practical_information"));
                    break;
                case Edit:
                    // populate data
                    item = tableView.getSelectionModel().getSelectedItem();
                    try {
                        item = eateryService.findEateryById((Integer) item.getProperty("id"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }

                    break;
            }

            populateData(item);

            // validate data and update database
            PersistentObject practicalInfo = (PersistentObject) item.getObject("practicalInformation");
            if (container.validate(new EateryValidator(), formPane, messageBinders, item, practicalInfo)) {
                eateryService.saveEatery(item);

                switch (formState) {
                    case Add:
                        items.add(item);
                        break;
                    case Edit:
                        // merge result with data in observable list
                        for (int i = 0; i < items.size(); i++) {
                            PersistentObject pd =  items.get(i);
                            if (pd.getProperty("id").equals(item.getProperty("id"))) {
                                pd.merge(item);
                                break;
                            }
                        }
                        break;
                }
                hideForm();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateData(PersistentObject po) {
        // base properties
        String name = nameTextField.getText();
        String executiveChef = executiveChefTextField.getText();
        String postCode = postCodeTextField.getText();
        String street = streetTextField.getText();
        String description = descriptionTextArea.getText();

        // menu
        String menuContent = menuTextArea.getText().trim();

        // practical information
        String gettingThere = gettingThereTextField.getText();
        String parking = parkingTextField.getText();
        String hoursOfOperation1 = hoursOfOperationTextField1.getText();
        String hoursOfOperation2 = hoursOfOperationTextField2.getText();
        String price = priceTextField.getText();
        String paymentOptions = paymentOptionsTextField.getText();

        // cooking style
        Integer cookingStyleId = null;
        if (cookingStyleComboBox.getSelectionModel().getSelectedItem() != null) {
            cookingStyleId = (Integer) cookingStyleComboBox.getSelectionModel().getSelectedItem().getProperty("id");
        }

        // city
        Integer cityId = null;
        if (cityComboBox.getSelectionModel().getSelectedItem() != null) {
            cityId = (Integer) cityComboBox.getSelectionModel().getSelectedItem().getProperty("id");
        }

        // images
        byte[] eateryImage1 = eateryImageInput1.getData();
        byte[] eateryImage2 = eateryImageInput2.getData();
        byte[] eateryImage3 = eateryImageInput3.getData();
        byte[] eateryImage4 = eateryImageInput4.getData();
        byte[] eateryImage5 = eateryImageInput5.getData();
        byte[] eateryImage6 = eateryImageInput6.getData();
        byte[] eateryImage7 = eateryImageInput7.getData();
        byte[] eateryImage8 = eateryImageInput8.getData();
        byte[] foodSpottingImage1 = foodSpottingImageInput1.getData();
        byte[] foodSpottingImage2 = foodSpottingImageInput2.getData();
        byte[] foodSpottingImage3 = foodSpottingImageInput3.getData();
        byte[] foodSpottingImage4 = foodSpottingImageInput4.getData();
        byte[] foodSpottingImage5 = foodSpottingImageInput5.getData();
        byte[] foodSpottingImage6 = foodSpottingImageInput6.getData();
        byte[] foodSpottingImage7 = foodSpottingImageInput7.getData();
        byte[] foodSpottingImage8 = foodSpottingImageInput8.getData();

        // tags
        List<PersistentObject> tags = new ArrayList<>();
        for (Node tagCheckBox : tagsBox.getChildren()) {
            CheckBox chk = (CheckBox)tagCheckBox;
            if (chk.isSelected()) {
                tags.add((PersistentObject) chk.getUserData());
            }
        }

        eateryService.populateEatery(po, name, executiveChef, postCode, street,
                description, menuContent, gettingThere, parking,
                hoursOfOperation1, hoursOfOperation2, price, paymentOptions,
                cookingStyleId, cityId, eateryImage1, eateryImage2, eateryImage3,
                eateryImage4, eateryImage5, eateryImage6, eateryImage7, eateryImage8,
                foodSpottingImage1, foodSpottingImage2, foodSpottingImage3, foodSpottingImage4,
                foodSpottingImage5, foodSpottingImage6, foodSpottingImage7, foodSpottingImage8,
                tags);

    }

    @FXML
    protected void delete(ActionEvent event) {
        PersistentObject po = tableView.getSelectionModel().getSelectedItem();
        try {
            eateryService.deleteEatery(po);
            for (Iterator<PersistentObject> it = tableView.getItems().iterator(); it.hasNext(); ) {
                PersistentObject item =  it.next();
                if (item.getProperty("id").equals(po.getProperty("id"))) {
                    it.remove();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void showMainForm(ActionEvent event) {
        mainFormPane.setVisible(true);
        menuFormPane.setVisible(false);
        eateryImagesFormPane.setVisible(false);
        foodSpottingImagesFormPane.setVisible(false);
    }

    @FXML
    protected void showMenuForm(ActionEvent event) {
        mainFormPane.setVisible(false);
        menuFormPane.setVisible(true);
        eateryImagesFormPane.setVisible(false);
        foodSpottingImagesFormPane.setVisible(false);
    }

    @FXML
    protected void showEateryImagesForm(ActionEvent event) {
        mainFormPane.setVisible(false);
        menuFormPane.setVisible(false);
        eateryImagesFormPane.setVisible(true);
        foodSpottingImagesFormPane.setVisible(false);
    }

    @FXML
    protected void showFoodSpottingImagesForm(ActionEvent event) {
        mainFormPane.setVisible(false);
        menuFormPane.setVisible(false);
        eateryImagesFormPane.setVisible(false);
        foodSpottingImagesFormPane.setVisible(true);
    }

    private void showForm() {
        tablePane.setDisable(true);

        final Timeline timeline = new Timeline();
        final KeyValue kv = new KeyValue(formPane.translateXProperty(), 0);
        final KeyFrame kf = new KeyFrame(Duration.millis(300), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }

    private void hideForm() {
        container.clearValidationMessages(formPane, messageBinders);

        EventHandler onFinished = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                tablePane.setDisable(false);
            }
        };

        final Timeline timeline = new Timeline();
        final KeyValue kv = new KeyValue(formPane.translateXProperty(), formPane.getWidth());
        final KeyFrame kf = new KeyFrame(Duration.millis(300), onFinished, kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }

    @Subscribe
    public void eateryAdded(EateryAddedEvent event) {
        // update table
        Platform.runLater(() -> {
            ObservableList<PersistentObject> items = tableView.getItems();
            items.add(event.getEatery());
        });
    }


}
