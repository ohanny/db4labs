package fr.icodem.db4labs.app.bat.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import fr.icodem.db4labs.app.bat.service.EateryTagService;
import fr.icodem.db4labs.component.FormState;
import fr.icodem.db4labs.container.AppContainer;
import fr.icodem.db4labs.database.PersistentObject;
import fr.icodem.db4labs.dbtools.validation.MessageBinder;
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
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

@Singleton
public class EateryTagTabController implements Initializable {

    @FXML private TableView<PersistentObject> tableView;
    @FXML private GridPane tablePane;
    @FXML private GridPane formPane;

    @FXML private Button editButton;
    @FXML private Button deleteButton;

    @FXML private TextField idTextField;
    @FXML private TextField nameTextField;

    @FXML private List<MessageBinder> messageBinders;

    private FormState formState;

    @Inject private AppContainer container;
    @Inject private EateryTagService eateryTagService;

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

        // load items
        find(null);
    }

    @FXML
    public void find(ActionEvent event) {
        try {
            ObservableList<PersistentObject> result = eateryTagService.findEateryTagList();
            tableView.setItems(result);
        } catch (Exception e) {
            e.printStackTrace();
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

        PersistentObject tag = tableView.getSelectionModel().getSelectedItem();

        try {
            tag = eateryTagService.findEateryTagById((Integer) tag.getProperty("id"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        idTextField.setText(tag.getProperty("id").toString());
        nameTextField.setText(tag.getProperty("name").toString());
        showForm();
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
                    item = new PersistentObject("eatery_tag");
                    break;
                case Edit:
                    // populate data
                    item = tableView.getSelectionModel().getSelectedItem();
                    try {
                        item = eateryTagService.findEateryTagById((Integer) item.getProperty("id"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }

                    break;
            }

            populateData(item);

            // validate data and update database
            if (container.validate(new EateryTagValidator(), formPane, messageBinders, item)) {
                eateryTagService.saveEateryTag(item);
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
        po.setProperty("name", nameTextField.getText());
    }

    @FXML
    protected void delete(ActionEvent event) {
        PersistentObject po = tableView.getSelectionModel().getSelectedItem();
        try {
            eateryTagService.deleteEateryTag(po);
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
