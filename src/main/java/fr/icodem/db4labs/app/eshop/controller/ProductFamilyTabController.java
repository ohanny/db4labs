package fr.icodem.db4labs.app.eshop.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import fr.icodem.db4labs.component.FormState;
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
import javafx.util.Duration;
import fr.icodem.db4labs.app.eshop.service.ProductFamilyService;
import fr.icodem.db4labs.container.AppContainer;
import fr.icodem.db4labs.database.PersistentObject;
import fr.icodem.db4labs.dbtools.validation.MessageBinder;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Singleton
public class ProductFamilyTabController implements Initializable {

    //@FXML private TableView<PersistentObject> tableView;
    @FXML private TreeView<PersistentObject> treeView;
    @FXML private GridPane tablePane;
    @FXML private GridPane formPane;

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    @FXML private TextField idTextField;
    @FXML private TextField nameTextField;

    @FXML private List<MessageBinder> messageBinders;

    private FormState formState;

    @Inject private AppContainer container;
    @Inject private ProductFamilyService productFamilyService;

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
                        productFamilyService.checkDeleteProductFamily(treeItem.getValue());
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
    }

    @FXML
    public void find(ActionEvent event) {
        try {
            ObservableList<PersistentObject> result = productFamilyService.findProductFamilyList();//container.select("product_family");

            PersistentObject poRoot = new PersistentObject();
            poRoot.setObject("treeview.root", "Families");
            TreeItem<PersistentObject> root = new TreeItem<PersistentObject>(poRoot);
            root.setExpanded(true);
            addChildrenToTreeItem(root, result);
            treeView.setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addChildrenToTreeItem(TreeItem<PersistentObject> treeItem, ObservableList<PersistentObject> families) {
        // add children of tree item argument
        for (PersistentObject family : families) {
            // case root
            if (treeItem.getValue().getObject("treeview.root") != null) {
                if (family.getProperty("parent_id") == null) {
                    treeItem.getChildren().add(new TreeItem<PersistentObject>(family));
                }
            } else {// nodes corresponding to effective family
                int parentId = (int)treeItem.getValue().getProperty("id");
                if (family.getProperty("parent_id") != null &&
                        (int)family.getProperty("parent_id") == parentId) {
                    treeItem.getChildren().add(new TreeItem<PersistentObject>(family));
                }
            }
        }

        // add nodes on children of tree item argument
        for (TreeItem<PersistentObject> child : treeItem.getChildren()) {
            addChildrenToTreeItem(child, families);
        }
    }

    @FXML
    protected void add(ActionEvent event) {
        formState = FormState.Add;
        idTextField.setText("");
        nameTextField.setText("");
        showForm();
    }

    @FXML
    protected void edit(ActionEvent event) {
        formState = FormState.Edit;

        TreeItem<PersistentObject> treeItem = treeView.getSelectionModel().getSelectedItem();
        PersistentObject family = treeItem.getValue();

        try {
            family = productFamilyService.findProductFamilyById((Integer) family.getProperty("id"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        idTextField.setText(family.getProperty("id").toString());
        nameTextField.setText(family.getProperty("name").toString());
        showForm();
    }

    @FXML
    protected void cancel(ActionEvent event) {
        hideForm();
    }

    @FXML
    protected void save(ActionEvent event) {
        //ObservableList<PersistentObject> items = tableView.getItems();

        try {
            PersistentObject item = null;
            PersistentObject parent = null;
            switch (formState) {
                case Add:
                    // populate data
                    item = new PersistentObject("product_family");
                    parent = treeView.getSelectionModel().getSelectedItem().getValue();
                    break;
                case Edit:
                    // populate data
                    item = treeView.getSelectionModel().getSelectedItem().getValue();
                    try {
                        item = productFamilyService.findProductFamilyById((Integer) item.getProperty("id"));
                        if (item.getProperty("parent_id") != null) {
                            parent = (PersistentObject) item.getObject("parent");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }

                    break;
            }

            populateData(item, parent);

            // validate data and update database
            if (container.validate(new ProductFamilyValidator(), formPane, messageBinders, item)) {
                productFamilyService.saveProductFamily(item);
                switch (formState) {
                    case Add:
                        if (item.getProperty("parent_id") != null) {
                            TreeItem<PersistentObject> parentTreeItem =
                                    findTreeItem(treeView.getRoot(), (int) item.getProperty("parent_id"));
                            parentTreeItem.getChildren().add(new TreeItem<>(item));
                        } else {
                            treeView.getRoot().getChildren().add(new TreeItem<>(item));
                        }
                        break;
                    case Edit:
                        // merge result with data in tree view
                        TreeItem<PersistentObject> treeItem =
                                findTreeItem(treeView.getRoot(), (int) item.getProperty("id"));
                        if (treeItem != null) {
                            //treeItem.getValue().merge(item);
                            treeItem.setValue(item);
                        }
                        break;
                }
                hideForm();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TreeItem<PersistentObject> findTreeItem(TreeItem<PersistentObject> node, int familyId) {
        for (TreeItem<PersistentObject> child : node.getChildren()) {
            Object id = child.getValue().getProperty("id");
            if (id != null && (int)id == familyId) {
                return child;
            } else {
                TreeItem<PersistentObject> result = findTreeItem(child, familyId);
                if (result != null) return result;
            }
        }
        return null;
    }

    private void populateData(PersistentObject po, PersistentObject parent) {
        // base properties
        po.setProperty("name", nameTextField.getText());
        if (parent == null || parent.getObject("treeview.root") != null) {// is root
            po.setProperty("parent_id", null);
        } else {
            po.setProperty("parent_id", parent.getProperty("id"));
        }
    }

    @FXML
    protected void delete(ActionEvent event) {
        TreeItem<PersistentObject> treeItem = treeView.getSelectionModel().getSelectedItem();
        PersistentObject po = treeItem.getValue();
        try {
            System.out.println(po);
            //container.delete(data);
            productFamilyService.deleteProductFamily(po);

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
