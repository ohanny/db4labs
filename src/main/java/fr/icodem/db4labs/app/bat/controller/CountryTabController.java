package fr.icodem.db4labs.app.bat.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import fr.icodem.db4labs.app.bat.service.CountryService;
import fr.icodem.db4labs.component.FormState;
import fr.icodem.db4labs.component.ImageInput;
import fr.icodem.db4labs.container.AppContainer;
import fr.icodem.db4labs.database.PersistentObject;
import fr.icodem.db4labs.dbtools.validation.MessageBinders;
import fr.icodem.db4labs.dbtools.validation.MessageBindersBuilder;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

@Singleton
public class CountryTabController implements Initializable {

    @FXML private TreeView<PersistentObject> treeView;
    @FXML private GridPane tablePane;
    @FXML private GridPane formPane;

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    @FXML private Text titleText;
    @FXML private TextField idTextField;
    @FXML private TextField nameTextField;
    @FXML private Label postCodeLabel;
    @FXML private TextField postCodeTextField;
    @FXML private Label foremostLabel;
    @FXML private CheckBox foremostCheckBox;
    @FXML private Label cityImageLabel;
    @FXML private ImageInput cityImageInput;

    @FXML private HBox buttonBox;

    private MessageBinders messageBinders;

    private FormState formState;

    @Inject private AppContainer container;
    @Inject private CountryService countryService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        formPane.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                formPane.translateXProperty().setValue(formPane.getWidth());
            }
        });

        treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        treeView.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<Integer>() {
            @Override
            public void onChanged(Change<? extends Integer> change) {
                TreeItem<PersistentObject> treeItem = treeView.getSelectionModel().getSelectedItem();

                // disable / enable edit and delete button
                if (treeItem != null && !treeView.getRoot().equals(treeItem)
                        && change.getList().size() == 1) {
                    editButton.setDisable(false);
                    try {
                        countryService.checkDeleteItem(treeItem.getValue());
                        deleteButton.setDisable(false);
                    } catch (Exception e) {
                        deleteButton.setDisable(true);
                    }
                }
                else {
                    editButton.setDisable(true);
                    deleteButton.setDisable(true);
                }

                // disable / enable add button
                if (treeItem == null) {
                    addButton.setDisable(true);
                } else {
                    addButton.setDisable(false);
                }
            }
        });

        // load items
        find(null);

        // message binders
        messageBinders = new MessageBindersBuilder()
                .bind("name").to(nameTextField)
                .bind("post_code").to(postCodeTextField)
                .build();

    }

    @FXML
    public void find(ActionEvent event) {
        try {
            ObservableList<PersistentObject> result = countryService.findCountryList();

            PersistentObject poRoot = new PersistentObject("root");
            //poRoot.setObject("type", "root");
            poRoot.setObject("treeview.root", "Countries");
            TreeItem<PersistentObject> root = new TreeItem<>(poRoot);
            root.setExpanded(true);
            addChildrenToTreeItem(root, result);
            treeView.setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addChildrenToTreeItem(TreeItem<PersistentObject> root, ObservableList<PersistentObject> countries) {
        for (PersistentObject country : countries) {
            TreeItem<PersistentObject> countryTreeItem = new TreeItem<>(country);
            country.setObject("treeview.item", countryTreeItem);
            root.getChildren().add(countryTreeItem);
            countryTreeItem.setExpanded(true);
            ObservableList<PersistentObject> regions = (ObservableList<PersistentObject>) country.getObject("regions");

            if (regions != null) {
                for (PersistentObject region : regions) {
                    TreeItem<PersistentObject> regionTreeItem = new TreeItem<>(region);
                    region.setObject("treeview.item", regionTreeItem);
                    regionTreeItem.setExpanded(true);
                    countryTreeItem.getChildren().add(regionTreeItem);
                    ObservableList<PersistentObject> cities = (ObservableList<PersistentObject>) region.getObject("cities");

                    if (cities != null) {
                        for (PersistentObject city : cities) {
                            TreeItem<PersistentObject> cityTreeItem = new TreeItem<>(city);
                            city.setObject("treeview.item", cityTreeItem);
                            regionTreeItem.getChildren().add(cityTreeItem);
                        }
                    }
                }
            }
        }
    }

    @FXML
    protected void add(ActionEvent event) {
        formState = FormState.Add;

        PersistentObject item = getSelectedItem();

        switch(item.getTable()) {
            case "root":
                titleText.setText("Add new country");
                postCodeLabel.setVisible(false);
                postCodeTextField.setVisible(false);
                foremostLabel.setVisible(false);
                foremostCheckBox.setVisible(false);
                cityImageLabel.setVisible(false);
                cityImageInput.setVisible(false);
                GridPane.setRowIndex(buttonBox, 5);
                break;
            case "country":
                titleText.setText("Add new region");
                postCodeLabel.setVisible(false);
                postCodeTextField.setVisible(false);
                foremostLabel.setVisible(false);
                foremostCheckBox.setVisible(false);
                cityImageLabel.setVisible(false);
                cityImageInput.setVisible(false);
                GridPane.setRowIndex(buttonBox, 5);
                break;
            case "region":
                titleText.setText("Add new city");
                postCodeLabel.setVisible(true);
                postCodeTextField.setVisible(true);
                foremostLabel.setVisible(true);
                foremostCheckBox.setVisible(true);
                cityImageLabel.setVisible(true);
                cityImageInput.setVisible(true);
                GridPane.setRowIndex(buttonBox, 7);
                break;
        }

        idTextField.setText("");
        nameTextField.setText("");
        postCodeTextField.setText("");
        foremostCheckBox.setSelected(false);
        cityImageInput.setData(null);

        showForm();
    }

    @FXML
    protected void edit(ActionEvent event) {
        formState = FormState.Edit;

        PersistentObject item = getSelectedItem();
        try {
            item = countryService.findItemById(item.getTable(), (Integer) item.getProperty("id"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        switch(item.getTable()) {
            case "country":
                titleText.setText("Edit country");
                postCodeLabel.setVisible(false);
                postCodeTextField.setVisible(false);
                foremostLabel.setVisible(false);
                foremostCheckBox.setVisible(false);
                cityImageLabel.setVisible(false);
                cityImageInput.setVisible(false);
                GridPane.setRowIndex(buttonBox, 4);
                break;
            case "region":
                titleText.setText("Edit region");
                postCodeLabel.setVisible(false);
                postCodeTextField.setVisible(false);
                foremostLabel.setVisible(false);
                foremostCheckBox.setVisible(false);
                cityImageLabel.setVisible(false);
                cityImageInput.setVisible(false);
                GridPane.setRowIndex(buttonBox, 4);
                break;
            case "city":
                titleText.setText("Edit city");
                postCodeLabel.setVisible(true);
                postCodeTextField.setVisible(true);
                foremostLabel.setVisible(true);
                foremostCheckBox.setVisible(true);
                cityImageLabel.setVisible(true);
                cityImageInput.setVisible(true);
                GridPane.setRowIndex(buttonBox, 7);

                postCodeTextField.setText((String) item.getProperty("post_code"));
                foremostCheckBox.setSelected((Boolean) item.getProperty("foremost"));
                if (item.getObject("image") != null) {
                    PersistentObject image = (PersistentObject) item.getObject("image");
                    cityImageInput.setData((byte[]) image.getProperty("content"));
                } else cityImageInput.setData(null);

                break;
        }

        idTextField.setText(item.getProperty("id").toString());
        nameTextField.setText(item.getProperty("name").toString());

        showForm();
    }

    private PersistentObject getSelectedItem() {
        TreeItem<PersistentObject> treeItem = treeView.getSelectionModel().getSelectedItem();
        PersistentObject item = treeItem.getValue();
        return item;
    }

    @FXML
    protected void cancel(ActionEvent event) {
        hideForm();
    }

    @FXML
    protected void save(ActionEvent event) {
        try {
            PersistentObject item = null;
            PersistentObject parent = null;
            switch (formState) {
                case Add:
                    // populate data
                    parent = treeView.getSelectionModel().getSelectedItem().getValue();
                    item = getNewItem(parent);
                    break;
                case Edit:
                    // populate data
                    item = treeView.getSelectionModel().getSelectedItem().getValue();
                    parent = treeView.getSelectionModel().getSelectedItem().getParent().getValue();

                    break;
            }

            populateData(item, parent);

            // validate data and update database
            if (container.validate(new CountryRegionCityValidator(), formPane, messageBinders, item)) {
                countryService.saveItem(item);
                switch (formState) {
                    case Add:
                        TreeItem<PersistentObject> parentTreeItem = treeView.getSelectionModel().getSelectedItem();
                        TreeItem<PersistentObject> newTreeItem = new TreeItem<>(item);
                        parentTreeItem.getChildren().add(newTreeItem);
                        newTreeItem.setExpanded(true);
                        break;
                    case Edit:
                        // merge result with data in tree view
                        TreeItem<PersistentObject> treeItem = (TreeItem<PersistentObject>) item.getObject("treeview.item");
                        treeItem.setValue(item.clone());
                        break;
                }
                hideForm();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PersistentObject getNewItem(PersistentObject parent) {
        PersistentObject item = null;
        switch (parent.getTable()) {
            case "root":
                item = new PersistentObject("country");
                break;
            case "country":
                item = new PersistentObject("region");
                item.setObject("country", parent);
                break;
            case "region":
                item = new PersistentObject("city");
                item.setObject("region", parent);
                item.setProperty("foremost", false);
                break;
        }
        return item;
    }

    private void populateData(PersistentObject po, PersistentObject parent) {
        // base properties
        po.setProperty("name", nameTextField.getText());
        if ("city".equals(po.getTable())) {
            po.setProperty("post_code", postCodeTextField.getText());
            po.setProperty("foremost", foremostCheckBox.isSelected());
            if (cityImageInput.getData() != null) {
                PersistentObject image = new PersistentObject("image_data");
                image.setProperty("content", cityImageInput.getData());
                po.setObject("image", image);
            } else {
                po.setObject("image", null);
            }

        }
        po.setObject(parent.getTable(), parent);
    }

    @FXML
    protected void delete(ActionEvent event) {
        TreeItem<PersistentObject> treeItem = treeView.getSelectionModel().getSelectedItem();
        PersistentObject po = treeItem.getValue();
        try {
            countryService.deleteItem(po);

            treeItem.getParent().getChildren().removeAll(treeItem);

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

}
