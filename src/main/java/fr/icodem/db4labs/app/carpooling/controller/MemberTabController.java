package fr.icodem.db4labs.app.carpooling.controller;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import fr.icodem.db4labs.app.carpooling.event.ModelAddedEvent;
import fr.icodem.db4labs.app.carpooling.service.BrandService;
import fr.icodem.db4labs.app.carpooling.service.CityService;
import fr.icodem.db4labs.app.carpooling.service.MemberService;
import fr.icodem.db4labs.app.carpooling.service.ModelService;
import fr.icodem.db4labs.component.FormState;
import fr.icodem.db4labs.component.ImageInput;
import fr.icodem.db4labs.container.AppContainer;
import fr.icodem.db4labs.database.PersistentObject;
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
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static javafx.collections.FXCollections.observableArrayList;

@Singleton
public class MemberTabController implements Initializable {

    @FXML private TableView<PersistentObject> tableView;
    @FXML private GridPane tablePane;
    @FXML private GridPane formPane;
    @FXML private GridPane mainFormPane;
    @FXML private GridPane detailsFormPane;

    @FXML private Button editButton;
    @FXML private Button deleteButton;

    @FXML private TextField usernameTextField;
    @FXML private TextField passwordTextField;
    @FXML private TextField firstNameTextField;
    @FXML private TextField lastNameTextField;
    @FXML private ComboBox<Integer> birthYearComboBox;
    @FXML private RadioButton maleRadioButton;
    @FXML private RadioButton femaleRadioButton;
    @FXML private TextField streetTextField;
    @FXML private TextField postcodeTextField;
    @FXML private ComboBox<PersistentObject> cityComboBox;
    @FXML private DatePicker registerDatePicker;
    @FXML private ImageInput imageInput;
    @FXML private TextArea minibioTextField;

    @FXML private ToggleGroup chatGroup;
    @FXML private ToggleGroup musicGroup;
    @FXML private ToggleGroup animalGroup;
    @FXML private ToggleGroup smokingGroup;

    @FXML private RadioButton chatLowRadioButton;
    @FXML private RadioButton chatMediumRadioButton;
    @FXML private RadioButton chatHighRadioButton;
    @FXML private RadioButton musicLowRadioButton;
    @FXML private RadioButton musicMediumRadioButton;
    @FXML private RadioButton musicHighRadioButton;
    @FXML private RadioButton animalLowRadioButton;
    @FXML private RadioButton animalMediumRadioButton;
    @FXML private RadioButton animalHighRadioButton;
    @FXML private RadioButton smokingLowRadioButton;
    @FXML private RadioButton smokingMediumRadioButton;
    @FXML private RadioButton smokingHighRadioButton;
    @FXML private ComboBox<PersistentObject> brandComboBox;
    @FXML private ComboBox<PersistentObject> modelComboBox;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> colorComboBox;
    @FXML private ComboBox<String> comfortComboBox;

    private MessageBinders messageBinders;

    private FormState formState;

    @Inject private AppContainer container;
    @Inject private CityService cityService;
    @Inject private ModelService modelService;
    @Inject private BrandService brandService;
    @Inject private MemberService memberService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        formPane.widthProperty().addListener((observableValue, oldValue, newValue) -> {
            formPane.translateXProperty().setValue(formPane.getWidth());
        });

        tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tableView.getSelectionModel().getSelectedIndices().addListener((ListChangeListener<Integer>) change -> {
            if (change.getList().size() == 1) {
                editButton.setDisable(false);
                deleteButton.setDisable(false);
            }
            else {
                editButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });

        // load items
        find(null);

        // configure radio buttons
        chatLowRadioButton.setUserData("Low");
        chatMediumRadioButton.setUserData("Medium");
        chatHighRadioButton.setUserData("High");
        musicLowRadioButton.setUserData("Low");
        musicMediumRadioButton.setUserData("Medium");
        musicHighRadioButton.setUserData("High");
        animalLowRadioButton.setUserData("Low");
        animalMediumRadioButton.setUserData("Medium");
        animalHighRadioButton.setUserData("High");
        smokingLowRadioButton.setUserData("Low");
        smokingMediumRadioButton.setUserData("Medium");
        smokingHighRadioButton.setUserData("High");

        // select city from postcode
        postcodeTextField.setOnKeyReleased(event -> {
            String postcode = postcodeTextField.getText().trim();
            String old = (String) postcodeTextField.getUserData();
            if (postcode.length() == 5 && !postcode.equals(old)) {
                postcodeTextField.setUserData(postcode);
                try {
                    ObservableList<PersistentObject> cities = cityService.findCityByPostcodeList(postcode);
                    cityComboBox.setItems(cities);
                    Platform.runLater(() -> cityComboBox.getSelectionModel().selectFirst());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // birth years
        ObservableList<Integer> years =
                observableArrayList(range(1930, 2010).boxed().collect(toList()));
        birthYearComboBox.setItems(years);

        // vehicle
        try {
            ObservableList<PersistentObject> brands = brandService.findBrandList();
            brandComboBox.setItems(brands);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // models
        brandComboBox.getSelectionModel().selectedItemProperty().addListener((selected, oldValue, newValue) -> {
            if (selected.getValue() == null || (int)selected.getValue().getProperty("id") == -1) {
                modelComboBox.setItems(FXCollections.observableArrayList());
                modelComboBox.setDisable(true);
                return;
            }

            try {
                modelComboBox.setDisable(false);
                int brandId = (int) selected.getValue().getProperty("id");
                ObservableList<PersistentObject> models = modelService.findModelByBrand(brandId);
                modelComboBox.setItems(models);
                categoryComboBox.setDisable(false);
                colorComboBox.setDisable(false);
                comfortComboBox.setDisable(false);
                Platform.runLater(() -> {
                    modelComboBox.getSelectionModel().clearSelection();
                    //modelComboBox.getSelectionModel().selectFirst();
                    categoryComboBox.getSelectionModel().clearSelection();
                    colorComboBox.getSelectionModel().clearSelection();
                    comfortComboBox.getSelectionModel().clearSelection();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        // message binders
        messageBinders = new MessageBindersBuilder()
                .bind("id").to(usernameTextField)
                .bind("password").to(passwordTextField)
                .bind("first_name").to(firstNameTextField)
                .bind("last_name").to(lastNameTextField)
                .bind("birth_year").to(birthYearComboBox)
                .bind("model_id").to(modelComboBox)
                .bind("category").to(categoryComboBox)
                .bind("comfort").to(comfortComboBox)
                .bind("color").to(colorComboBox)
                .build();

    }

    @FXML
    public void find(ActionEvent event) {
        try {
            ObservableList<PersistentObject> result = memberService.findMemberList();
            tableView.setItems(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void add(ActionEvent event) {
        formState = FormState.Add;
        usernameTextField.clear();
        passwordTextField.clear();
        maleRadioButton.setSelected(true);
        usernameTextField.setEditable(true);
        usernameTextField.setFocusTraversable(true);
        firstNameTextField.clear();
        lastNameTextField.clear();
        streetTextField.clear();
        postcodeTextField.clear();
        cityComboBox.setItems(FXCollections.emptyObservableList());
        minibioTextField.clear();
        registerDatePicker.setValue(null);
        imageInput.setData(null);

        chatGroup.selectToggle(chatMediumRadioButton);
        musicGroup.selectToggle(musicMediumRadioButton);
        animalGroup.selectToggle(animalMediumRadioButton);
        smokingGroup.selectToggle(smokingMediumRadioButton);

        // birth years
        birthYearComboBox.getSelectionModel().selectFirst();

        // vehicle
        brandComboBox.getSelectionModel().clearSelection();
        categoryComboBox.getSelectionModel().clearSelection();
        colorComboBox.getSelectionModel().clearSelection();
        comfortComboBox.getSelectionModel().clearSelection();

        showForm();
    }

    @FXML
    protected void edit(ActionEvent event) {
        formState = FormState.Edit;
        usernameTextField.setEditable(false);
        usernameTextField.setFocusTraversable(false);

        PersistentObject member = tableView.getSelectionModel().getSelectedItem();

        try {
            member = memberService.findMemberById((String) member.getProperty("id"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        PersistentObject user = (PersistentObject) member.getObject("user");

        usernameTextField.setText(member.getProperty("id").toString());
        passwordTextField.setText(user.getProperty("password").toString());
        firstNameTextField.setText(member.getProperty("first_name").toString());
        lastNameTextField.setText(member.getProperty("last_name").toString());
        if ("M".equals(member.getProperty("sex"))) {
            maleRadioButton.setSelected(true);
        } else {
            femaleRadioButton.setSelected(true);
        }

        // select birth year
        birthYearComboBox.getSelectionModel().clearSelection();
        int birthYear = (int) member.getProperty("birth_year");
        selectValueItem(birthYear, birthYearComboBox);

        // register date
        Date regDateLegacy = (Date) member.getProperty("register_date");
        LocalDate regDate = Instant.ofEpochMilli(regDateLegacy.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        registerDatePicker.setValue(regDate);

        // address
        String postcode = (String)member.getProperty("postcode");
        Integer cityId = (Integer)member.getProperty("city_id");
        streetTextField.setText((String)member.getProperty("street"));
        postcodeTextField.setText(postcode);
        if (member.getProperty("postcode") != null) {
            try {
                ObservableList<PersistentObject> cities = cityService.findCityByPostcodeList(postcode);
                cityComboBox.setItems(cities);
                if (cityId != null) {
                    Platform.runLater(() -> selectItem(cityId, cityComboBox));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // photo
        PersistentObject photo = (PersistentObject) member.getObject("photo");
        if (photo != null) {
            imageInput.setData((byte[])photo.getProperty("content"));
        } else {
            imageInput.setData(null);
        }

        // details
        minibioTextField.setText((String)member.getProperty("minibio"));

        // vehicle
        PersistentObject vehicle = (PersistentObject) member.getObject("vehicle");
        if (vehicle != null) {
            PersistentObject brand = (PersistentObject) vehicle.getObject("brand");
            if (brand != null) {
                final Integer brandId = (Integer) brand.getProperty("id");
                selectItem(brandId, brandComboBox);

                PersistentObject model = (PersistentObject) vehicle.getObject("model");
                if (model != null) {
                    try {
                        ObservableList<PersistentObject> models = modelService.findModelByBrand(brandId);
                        modelComboBox.setItems(models);
                        categoryComboBox.setDisable(false);
                        colorComboBox.setDisable(false);
                        comfortComboBox.setDisable(false);
                        Platform.runLater(() -> {
                            selectItem((Integer) model.getProperty("id"), modelComboBox);
                            selectValueItem(vehicle.getProperty("category"), categoryComboBox);
                            selectValueItem(vehicle.getProperty("color"), colorComboBox);
                            selectValueItem(vehicle.getProperty("comfort"), comfortComboBox);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else {
            brandComboBox.getSelectionModel().clearSelection();
            modelComboBox.getSelectionModel().clearSelection();
            modelComboBox.setEditable(false);
            //brandComboBox.getSelectionModel().selectFirst();
            categoryComboBox.getSelectionModel().clearSelection();
            colorComboBox.getSelectionModel().clearSelection();
            comfortComboBox.getSelectionModel().clearSelection();
            categoryComboBox.setDisable(true);
            colorComboBox.setDisable(true);
            comfortComboBox.setDisable(true);
        }


        // preferences
        selectToggle((String) member.getProperty("chat"), chatGroup);
        selectToggle((String) member.getProperty("music"), musicGroup);
        selectToggle((String) member.getProperty("animal"), animalGroup);
        selectToggle((String) member.getProperty("smoking"), smokingGroup);


        showForm();
    }

    private void selectToggle(String value, ToggleGroup group) {
        for (Toggle toggle : group.getToggles()) {
            if (value.equals(toggle.getUserData())) {
                toggle.setSelected(true);
                break;
            }
        }
    }

    private boolean selectValueItem(Object value, ComboBox<?> comboBox) {
        boolean found = false;
        for (int i = 0; i < comboBox.getItems().size(); i++) {
            Object obj = comboBox.getItems().get(i);
            if (obj.equals(value)) {
                comboBox.getSelectionModel().select(i);
                found = true;
                break;
            }
        }
        return found;
    }

    private boolean selectItem(int itemId, ComboBox<PersistentObject> comboBox) { // TODO factorize
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
                    item = new PersistentObject("member");
                    break;
                case Edit:
                    // populate data
                    item = tableView.getSelectionModel().getSelectedItem();
                    try {
                        item = memberService.findMemberById((String) item.getProperty("id"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }

                    break;
            }

            populateData(item);

            // validate data and update database
            PersistentObject vehicle = (PersistentObject) item.getObject("vehicle");
            if (container.validate(new MemberValidator(), formPane, messageBinders, item)
                    && (vehicle == null ||
                    container.validate(new VehicleValidator(), formPane, messageBinders, vehicle))) {
                memberService.saveMember(item);
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
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();
        String firstName = firstNameTextField.getText();
        String lastName = lastNameTextField.getText();

        Integer birthYear = null;
        if (birthYearComboBox.getSelectionModel().getSelectedItem() != null) {
            birthYear = (Integer) birthYearComboBox.getSelectionModel().getSelectedItem();
        }

        String sex = maleRadioButton.isSelected()?"M":"F";
        String street = streetTextField.getText().trim();
        street = street.isEmpty()?null:street;
        String postcode = postcodeTextField.getText();
        postcode = postcode.isEmpty()?null:postcode;
        PersistentObject city = cityComboBox.getSelectionModel().getSelectedItem();
        Date regDate = null;
        if (registerDatePicker.getValue() != null) {
            regDate = Date.from(registerDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        byte[] photoContent = imageInput.getData();
        String phone = null;
        String minibio = minibioTextField.getText().trim();
        minibio = minibio.isEmpty()?null:minibio;
        String chat = (String) chatGroup.getSelectedToggle().getUserData();
        String music = (String) musicGroup.getSelectedToggle().getUserData();
        String animal = (String) animalGroup.getSelectedToggle().getUserData();
        String smoking = (String) smokingGroup.getSelectedToggle().getUserData();

        PersistentObject brand = brandComboBox.getSelectionModel().getSelectedItem();
        PersistentObject model = modelComboBox.getSelectionModel().getSelectedItem();
        String category = categoryComboBox.getValue();
        String color = colorComboBox.getValue();
        String comfort = comfortComboBox.getValue();

        // populate model
        po.setProperty("id", username);
        po.setProperty("first_name", firstName);
        po.setProperty("last_name", lastName);
        po.setProperty("sex", sex);
        po.setProperty("birth_year", birthYear);
        po.setProperty("phone", phone);
        po.setProperty("minibio", minibio);
        po.setProperty("register_date", regDate);
        po.setProperty("chat", chat);
        po.setProperty("music", music);
        po.setProperty("animal", animal);
        po.setProperty("smoking", smoking);
        po.setProperty("street", street);
        po.setProperty("postcode", postcode);

        PersistentObject user = new PersistentObject("user_data");
        user.setProperty("username", username);
        user.setProperty("password", password);

        PersistentObject photo = null;
        if (photoContent != null) {
            photo = new PersistentObject("photo");
            photo.setProperty("content", photoContent);
        }

        PersistentObject vehicle = null;
        if (model != null) {
            vehicle = new PersistentObject("vehicle");
            vehicle.setProperty("category", category);
            vehicle.setProperty("color", color);
            vehicle.setProperty("comfort", comfort);
            vehicle.setObject("model", model);
            vehicle.setObject("brand", brand);
            vehicle.setProperty("model_id", model.getProperty("id"));
        }

        po.setObject("user", user);
        po.setObject("photo", photo);
        po.setObject("city", city);
        po.setObject("vehicle", vehicle);
    }

    @FXML
    protected void delete(ActionEvent event) {
        PersistentObject po = tableView.getSelectionModel().getSelectedItem();
        try {
            memberService.deleteMember(po);
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

        EventHandler onFinished = t -> tablePane.setDisable(false);

        final Timeline timeline = new Timeline();
        final KeyValue kv = new KeyValue(formPane.translateXProperty(), formPane.getWidth());
        final KeyFrame kf = new KeyFrame(Duration.millis(300), onFinished, kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }

    @Subscribe
    public void memberAdded(ModelAddedEvent event) {
        // update table
        Platform.runLater(() -> {
            ObservableList<PersistentObject> items = tableView.getItems();
            items.add(event.getModel());
        });
    }

    @FXML
    protected void showMainForm(ActionEvent event) {
        mainFormPane.setVisible(true);
        detailsFormPane.setVisible(false);
    }

    @FXML
    protected void showDetailsForm(ActionEvent event) {
        mainFormPane.setVisible(false);
        detailsFormPane.setVisible(true);
    }


}
