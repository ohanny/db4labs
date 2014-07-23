package fr.icodem.db4labs.app.eshop.controller;

import com.google.common.base.Splitter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import fr.icodem.db4labs.component.FormState;
import fr.icodem.db4labs.dbtools.validation.MessageBinders;
import fr.icodem.db4labs.dbtools.validation.MessageBindersBuilder;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import fr.icodem.db4labs.app.AppNameProvider;
import fr.icodem.db4labs.app.eshop.service.ProductFamilyService;
import fr.icodem.db4labs.app.eshop.service.ProductService;
import fr.icodem.db4labs.app.eshop.service.ProductTagService;
import fr.icodem.db4labs.component.ImageInput;
import fr.icodem.db4labs.container.AppContainer;
import fr.icodem.db4labs.database.PersistentObject;
import fr.icodem.db4labs.dbtools.validation.MessageBinder;

@Singleton
public class ProductTabController implements Initializable {

    @FXML private TableView<PersistentObject> tableView;
    @FXML private GridPane tablePane;
    @FXML private GridPane formPane;
    @FXML private GridPane formBook;
    @FXML private BookFormController formBookController;
    @FXML private Pane formMovie;
    @FXML private MovieFormController formMovieController;
    @FXML private Pane formAlbum;
    @FXML private AlbumFormController formAlbumController;
    @FXML private GridPane formGrocery;
    @FXML private GroceryProductFormController formGroceryController;

    @FXML private FlowPane tagsBox;

    @FXML private HBox typeMediaBox;
    @FXML private ToggleGroup typeMediaGroup;
    @FXML private RadioButton bookRadioButton;
    @FXML private RadioButton movieRadioButton;
    @FXML private RadioButton albumRadioButton;

    @FXML private HBox typeGroceryBox;
    @FXML private ToggleGroup typeGroceryGroup;
    @FXML private RadioButton everydayProductRadioButton;
    @FXML private RadioButton foodProductRadioButton;

    @FXML private Button editButton;
    @FXML private Button deleteButton;
    
    @FXML private TextField filterTextField;

    // base product properties
    @FXML private TextField idTextField;
    @FXML private TextField nameTextField;
    @FXML private TextArea descriptionTextArea;
    @FXML private TextField priceTextField;
    @FXML private CheckBox availableCheckBox;
    @FXML private ImageInput imageInput;
    @FXML private ComboBox<PersistentObject> familyComboBox;
    @FXML private ComboBox<PersistentObject> subFamilyComboBox;

    // media product properties
    @FXML private TextField releaseDateBookTextField;
    @FXML private TextField isbnTextField;
    @FXML private TextField languageTextField;
    @FXML private TextField pagesTextField;
    @FXML private TextField authorsTextField;

    @FXML private TextField releaseDateMovieTextField;
    @FXML private TextField lengthMovieTextField;
    @FXML private TextField languagesMovieTextField;
    @FXML private TextField directorTextField;
    @FXML private TextField actorsTextField;

    @FXML private TextField releaseDateAlbumTextField;
    @FXML private TextField lengthAlbumTextField;
    @FXML private TextField artistsTextField;

    // grocery product properties
    @FXML private TextField quantityTextField;
    @FXML private ComboBox<String> unitComboBox;
    @FXML private TextArea compositionTextArea;
    @FXML private TextArea nutritionalValueTextArea;

    // message binders
    private MessageBinders messageBinders;

    private FormState formState;

    @Inject private AppContainer container;

    @Inject private AppNameProvider appNameProvider;

    @Inject private ProductService productService;
    @Inject private ProductFamilyService productFamilyService;
    @Inject private ProductTagService productTagService;

    private final DecimalFormat fmtPrice = new DecimalFormat("0.00");
    
    private ObservableList<PersistentObject> products;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // get input fields for media products
        releaseDateBookTextField = formBookController.getReleaseDateBookTextField();
        isbnTextField = formBookController.getIsbnTextField();
        languageTextField = formBookController.getLanguageTextField();
        pagesTextField = formBookController.getPagesTextField();
        authorsTextField = formBookController.getAuthorsTextField();

        releaseDateMovieTextField = formMovieController.getReleaseDateMovieTextField();
        lengthMovieTextField = formMovieController.getLengthMovieTextField();
        languagesMovieTextField = formMovieController.getLanguagesMovieTextField();
        directorTextField = formMovieController.getDirectorTextField();
        actorsTextField = formMovieController.getActorsTextField();

        releaseDateAlbumTextField = formAlbumController.getReleaseDateAlbumTextField();
        lengthAlbumTextField = formAlbumController.getLengthAlbumTextField();
        artistsTextField = formAlbumController.getArtistsTextField();

        // get input fields for grocery products
        quantityTextField = formGroceryController.getQuantityTextField();
        unitComboBox = formGroceryController.getUnitComboBox();
        compositionTextArea = formGroceryController.getCompositionTextArea();
        nutritionalValueTextArea = formGroceryController.getNutritionalValueTextArea();

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

        typeMediaGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            public void changed(ObservableValue<? extends Toggle> ov,
                                Toggle old_toggle, Toggle new_toggle) {
                if (ov != null) {
                    if (bookRadioButton.equals(ov.getValue())) {
                        displaySpecificProperties("book");
                    }
                    else if (movieRadioButton.equals(ov.getValue())) {
                        displaySpecificProperties("movie");
                    }
                    else if (albumRadioButton.equals(ov.getValue())) {
                        displaySpecificProperties("album");
                    }
                }
            }
        });

        typeGroceryGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            public void changed(ObservableValue<? extends Toggle> ov,
                                Toggle old_toggle, Toggle new_toggle) {
                if (ov != null) {
                    if (everydayProductRadioButton.equals(ov.getValue())) {
                        displaySpecificProperties("everyday");
                    }
                    else if (foodProductRadioButton.equals(ov.getValue())) {
                        displaySpecificProperties("food");
                    }
                }
            }
        });

        // family combo box selection change
        familyComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<PersistentObject>() {
            @Override
            public void changed(ObservableValue<? extends PersistentObject> selected,
                                PersistentObject oldValue, PersistentObject newValue) {
                if (selected.getValue() == null) {
                    subFamilyComboBox.setItems(FXCollections.<PersistentObject>observableArrayList());
                    return;
                }
                ObservableList<PersistentObject> subFamilies = (ObservableList<PersistentObject>) selected.getValue().getObject("children");
                if (subFamilies != null) {
                    subFamilyComboBox.setItems(subFamilies);
                } else {
                    subFamilyComboBox.setItems(FXCollections.<PersistentObject>observableArrayList());
                }
            }
        });

        // initalize unit combo-box for grocery product
        ObservableList<String> units = FXCollections.observableArrayList("kg", "g", "l", "dl", "cl");
        unitComboBox.setItems(units);

        // load items
        find(null);

        // message binders
        messageBinders = new MessageBindersBuilder()
                .bind("name").to(nameTextField)
                .bind("description").to(descriptionTextArea)
                .bind("price").to(priceTextField)
                .bind("family_id").to(familyComboBox)
                .build();

        // merge message binders from sub-app
        messageBinders.merge(formBookController.getMessageBinders());
        messageBinders.merge(formMovieController.getMessageBinders());
        messageBinders.merge(formAlbumController.getMessageBinders());
        messageBinders.merge(formGroceryController.getMessageBinders());


        // filter product list
        filterTextField.textProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldVal, Object newVal) {
                filter((String) oldVal, (String) newVal);
            }
        });
    }
    
    public void filter(String oldVal, String newVal) {
        if (oldVal != null && (newVal.length() < oldVal.length())) {
            tableView.setItems(products);
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
            products = productService.findProductList();//container.select("product");
            tableView.setItems(products);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void add(ActionEvent event) {
        formState = FormState.Add;
        idTextField.setText("");
        nameTextField.setText("");
        descriptionTextArea.setText("");
        priceTextField.setText(fmtPrice.format(0.0));
        availableCheckBox.setSelected(true);
        imageInput.setData(null);

        String defaultType;
        switch (appNameProvider.getAppName()) {
            case EShopMedia:
                defaultType = "book";
                releaseDateBookTextField.setText("");
                isbnTextField.setText("");
                languageTextField.setText("");
                pagesTextField.setText("0");
                authorsTextField.setText("");
                directorTextField.setText("");
                actorsTextField.setText("");
                artistsTextField.setText("");

                break;
            case EShopGrocery:
                defaultType = "everyday";
                quantityTextField.setText("");
                unitComboBox.getSelectionModel().clearSelection();
                compositionTextArea.setText("");
                nutritionalValueTextArea.setText("");
                break;
            default:
                defaultType = null;
        }

        displaySpecificProperties(defaultType);

        // type selection disabled
        typeMediaBox.setDisable(false);

        // retrieve families
        try {
            ObservableList<PersistentObject> families = productFamilyService.findProductFamilyTree();
            familyComboBox.setItems(families);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // select family
        familyComboBox.getSelectionModel().clearSelection();

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

        PersistentObject product = tableView.getSelectionModel().getSelectedItem();

        try {
            product = productService.findProductById((Integer) product.getProperty("id"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        idTextField.setText(product.getProperty("id").toString());
        nameTextField.setText(product.getProperty("name").toString());
        if (product.getProperty("description") != null) {
            descriptionTextArea.setText(product.getProperty("description").toString());
        } else descriptionTextArea.setText("");
        priceTextField.setText(fmtPrice.format(product.getProperty("price")));
        availableCheckBox.setSelected((Boolean) product.getProperty("available"));

        // image
        if (product.getProperty("image_id") != null) {
           PersistentObject image = (PersistentObject) product.getObject("image");
           imageInput.setData((byte[]) image.getProperty("content"));
        } else imageInput.setData(null);

        // specific product properties
        SimpleDateFormat fmtDate = new SimpleDateFormat("dd/MM/yyyy");//TODO reprendre formattage date
        PersistentObject subitem;
        switch ((String)product.getObject("type")) {
            // eshop media
            case "book":
                subitem = (PersistentObject) product.getObject("book");
                releaseDateBookTextField.setText(fmtDate.format(subitem.getProperty("release_date")));
                isbnTextField.setText((String)subitem.getProperty("isbn"));
                languageTextField.setText((String) subitem.getProperty("language"));
                pagesTextField.setText(subitem.getProperty("pages").toString());

                List<PersistentObject> authors = (List<PersistentObject>) subitem.getObject("authors");
                StringBuilder sbAuthors = new StringBuilder();
                for (PersistentObject author : authors) {
                    if (sbAuthors.length() > 0) sbAuthors.append(", ");
                    sbAuthors.append(author.getProperty("name"));
                }
                authorsTextField.setText(sbAuthors.toString());

                break;
            case "movie":
                subitem = (PersistentObject) product.getObject("movie");
                releaseDateMovieTextField.setText(fmtDate.format(subitem.getProperty("release_date")));
                lengthMovieTextField.setText(subitem.getProperty("length").toString());

                List<PersistentObject> actors = (List<PersistentObject>) subitem.getObject("actors");
                if (actors != null) {
                    StringBuilder sbActors = new StringBuilder();
                    for (PersistentObject actor : actors) {
                        if (sbActors.length() > 0) sbActors.append(", ");
                        sbActors.append(actor.getProperty("name"));
                    }
                    actorsTextField.setText(sbActors.toString());
                } else {
                    actorsTextField.setText("");
                }

                PersistentObject director = (PersistentObject)subitem.getObject("director");
                if (director != null) {
                    directorTextField.setText(director.getProperty("name").toString());
                } else {
                    directorTextField.setText("");
                }

                List<PersistentObject> languages = (List<PersistentObject>) subitem.getObject("languages");
                if (languages != null) {
                    StringBuilder sbLanguages = new StringBuilder();
                    for (PersistentObject language : languages) {
                        if (sbLanguages.length() > 0) sbLanguages.append(", ");
                        sbLanguages.append(language.getProperty("language"));
                    }
                    languagesMovieTextField.setText(sbLanguages.toString());
                } else {
                    languagesMovieTextField.setText("");
                }

                break;
            case "album":
                subitem = (PersistentObject) product.getObject("album");
                releaseDateAlbumTextField.setText(fmtDate.format(subitem.getProperty("release_date")));
                lengthAlbumTextField.setText(subitem.getProperty("length").toString());

                List<PersistentObject> artists = (List<PersistentObject>) subitem.getObject("artists");
                if (artists != null) {
                    StringBuilder sbArtists = new StringBuilder();
                    for (PersistentObject artist : artists) {
                        if (sbArtists.length() > 0) sbArtists.append(", ");
                        sbArtists.append(artist.getProperty("name"));
                    }
                    artistsTextField.setText(sbArtists.toString());
                } else {
                    artistsTextField.setText("");
                }

                break;

            // eshop media
            case "everyday":
                subitem = (PersistentObject) product.getObject("everyday");
                quantityTextField.setText(subitem.getProperty("quantity").toString());
                unitComboBox.getSelectionModel().select((Integer) subitem.getProperty("unit"));
                compositionTextArea.setText((String) subitem.getProperty("composition"));
                break;
            case "food":
                subitem = (PersistentObject) product.getObject("food");
                quantityTextField.setText(subitem.getProperty("quantity").toString());
                unitComboBox.getSelectionModel().select((Integer) subitem.getProperty("unit"));
                compositionTextArea.setText((String) subitem.getProperty("composition"));
                nutritionalValueTextArea.setText((String) subitem.getProperty("nutritional_value"));
                break;
        }

        displaySpecificProperties((String) product.getObject("type"));

        // type selection disabled
        typeMediaBox.setDisable(true);

        // retrieve families
        try {
            ObservableList<PersistentObject> families = productFamilyService.findProductFamilyTree();
            familyComboBox.setItems(families);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // select family
        familyComboBox.getSelectionModel().clearSelection();
        try {
            PersistentObject family = productFamilyService.findProductFamilyById((Integer) product.getProperty("family_id"));
            if (family.getProperty("parent_id") == null) {
                selectFamily((Integer) family.getProperty("id"), familyComboBox);
            } else {
                selectFamily((Integer) family.getProperty("parent_id"), familyComboBox);
                selectFamily((Integer) family.getProperty("id"), subFamilyComboBox);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            displayTags(product);
        } catch (Exception e) {
            e.printStackTrace();
        }

        showForm();
    }

    private boolean selectFamily(int familyId, ComboBox<PersistentObject> comboBox) {
        boolean found = false;
        for (int i = 0; i < comboBox.getItems().size(); i++) {
            PersistentObject po = comboBox.getItems().get(i);
            if (po.getProperty("id").equals(familyId)) {
                comboBox.getSelectionModel().select(i);
                found = true;
                break;
            }
        }
        return found;
    }

    private void displaySpecificProperties(String type) {

        switch (appNameProvider.getAppName()) {
            case EShop:
                // specific properties inputs depending on product type
                formBook.setVisible(false);
                formMovie.setVisible(false);
                formAlbum.setVisible(false);
                formGrocery.setVisible(false);

                // type selection box
                typeMediaBox.setVisible(false);
                typeGroceryBox.setVisible(false);
                break;

            case EShopMedia:
                // specific properties inputs depending on product type
                switch (type) {
                    case "book":
                        formBook.setVisible(true);
                        formMovie.setVisible(false);
                        formAlbum.setVisible(false);
                        bookRadioButton.setSelected(true);
                        break;
                    case "movie":
                        formBook.setVisible(false);
                        formMovie.setVisible(true);
                        formAlbum.setVisible(false);
                        movieRadioButton.setSelected(true);
                        break;
                    case "album":
                        formBook.setVisible(false);
                        formMovie.setVisible(false);
                        formAlbum.setVisible(true);
                        albumRadioButton.setSelected(true);
                        break;
                }
                formGrocery.setVisible(false);

                // type selection box
                typeMediaBox.setVisible(true);
                typeGroceryBox.setVisible(false);
                break;

            case EShopGrocery:
                // specific properties inputs depending on product type
                formBook.setVisible(false);
                formMovie.setVisible(false);
                formAlbum.setVisible(false);
                formGrocery.setVisible(true);
                switch (type) {
                    case "everyday":
                        nutritionalValueTextArea.setVisible(false);
                        break;
                    case "food":
                        nutritionalValueTextArea.setVisible(true);
                        break;
                }

                // type selection box
                typeMediaBox.setVisible(false);
                typeGroceryBox.setVisible(true);
                break;
        }

    }

    private void displayTags(PersistentObject product) throws Exception {
        tagsBox.getChildren().clear();
        List<PersistentObject> tags = productTagService.findProductTagList();
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
                    item = new PersistentObject("product");
                    break;
                case Edit:
                    // populate data
                    item = tableView.getSelectionModel().getSelectedItem();
                    try {
                        item = productService.findProductById((Integer) item.getProperty("id"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }

                    break;
            }

            populateData(item);

            // retrieve media product
            PersistentObject subitem = null;
            if (item.getObject("book") != null) {
                subitem = (PersistentObject) item.getObject("book");
            }
            else if (item.getObject("movie") != null) {
                subitem = (PersistentObject) item.getObject("movie");
            }
            else if (item.getObject("album") != null) {
                subitem = (PersistentObject) item.getObject("album");
            }

            // retrieve grocery product
            if (item.getObject("everyday") != null) {
                subitem = (PersistentObject) item.getObject("everyday");
            }
            else if (item.getObject("food") != null) {
                subitem = (PersistentObject) item.getObject("food");
            }

            // validate data and update database
            if (container.validate(new ProductValidator(), formPane, messageBinders, item, subitem)) {
                productService.saveProduct(item);

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
        po.setProperty("description", descriptionTextArea.getText());
        po.setProperty("price", priceTextField.getText());
        po.setProperty("available", availableCheckBox.isSelected());

        // family
        if (subFamilyComboBox.getSelectionModel().getSelectedItem() != null) {
            po.setProperty("family_id", subFamilyComboBox.getSelectionModel().getSelectedItem().getProperty("id"));
        } else {
            po.setProperty("family_id", null);
        }
        if (po.getProperty("family_id") == null &&
                familyComboBox.getSelectionModel().getSelectedItem() != null) {// no sub-family selected, choose a family
            po.setProperty("family_id", familyComboBox.getSelectionModel().getSelectedItem().getProperty("id"));
        }

        // image
        byte[] img = imageInput.getData();
        PersistentObject imgItem = (PersistentObject) po.getObject("image");
        if (img != null) {
            if (imgItem == null) {
                imgItem = new PersistentObject("product_image");
                imgItem.setProperty("content", img);
                po.setObject("image", imgItem);
            } else {
                imgItem.setProperty("content", img);
            }
        } else if (imgItem != null) {// no image, delete previous one
            po.setProperty("image_id", null);
            po.setObject("image", null);
            po.setObject("image.old", imgItem);// so that service can delete old image
        }

        // tags
        List<PersistentObject> tags = new ArrayList<>();
        for (Node tagCheckBox : tagsBox.getChildren()) {
            CheckBox chk = (CheckBox)tagCheckBox;
            if (chk.isSelected()) {
                tags.add((PersistentObject) chk.getUserData());
            }
        }
        po.setObject("tags", tags);

        // properties depending on product type
        switch (appNameProvider.getAppName()) {
            case EShopMedia:
                if (bookRadioButton.isSelected()) {
                    PersistentObject book = (PersistentObject) po.getObject("book");
                    if (book == null) {
                        book = new PersistentObject("book");
                        po.setObject("book", book);
                        po.setObject("type", "book");
                    }
                    book.setProperty("release_date", releaseDateBookTextField.getText());
                    book.setProperty("isbn", isbnTextField.getText());
                    book.setProperty("language", languageTextField.getText());
                    book.setProperty("pages", pagesTextField.getText());


                    // authors
                    book.moveObject("authors", "authors.old");
                    List<PersistentObject> authors = new ArrayList<>();
                    Iterable<String> authorNames = Splitter.on(',')
                                                           .trimResults()
                                                           .omitEmptyStrings()
                                                           .split(authorsTextField.getText());
                    for (String authorName : authorNames) {
                        PersistentObject author = new PersistentObject("figure");
                        author.setProperty("name", authorName);
                        authors.add(author);
                    }
                    book.setObject("authors", authors);

                }
                else if (movieRadioButton.isSelected()) {
                    PersistentObject movie = (PersistentObject) po.getObject("movie");
                    if (movie == null) {
                        movie = new PersistentObject("movie");
                        po.setObject("movie", movie);
                        po.setObject("type", "movie");
                    }
                    movie.setProperty("release_date", releaseDateMovieTextField.getText());
                    movie.setProperty("length", lengthMovieTextField.getText());

                    // languages
                    Set<String> languagesSet = new HashSet<>();
                    Iterable<String> languagesString = Splitter.on(',')
                                                               .trimResults()
                                                               .omitEmptyStrings()
                                                               .split(languagesMovieTextField.getText());
                    for (String langString : languagesString) {
                        languagesSet.add(langString);
                    }
                    List<PersistentObject> languages = new ArrayList<>();
                    for (String langString : languagesSet) {
                        PersistentObject l = new PersistentObject("movie_language")
                                                    //.setProperty("movie_id", movie.getProperty("id"))
                                                    .setProperty("language", langString);
                        languages.add(l);
                    }
                    movie.setObject("languages", languages);

                    // director
                    if (!directorTextField.getText().trim().isEmpty()) {
                        PersistentObject director = new PersistentObject("figure");
                        director.setProperty("name", directorTextField.getText());
                        movie.setObject("director", director);
                    }

                    // actors
                    movie.moveObject("actors", "actors.old");
                    List<PersistentObject> actors = new ArrayList<>();
                    Iterable<String> actorNames = Splitter.on(',')
                            .trimResults()
                            .omitEmptyStrings()
                            .split(actorsTextField.getText());
                    for (String authorName : actorNames) {
                        PersistentObject actor = new PersistentObject("figure");
                        actor.setProperty("name", authorName);
                        actors.add(actor);
                    }
                    movie.setObject("actors", actors);

                }
                else if (albumRadioButton.isSelected()) {
                    PersistentObject album = (PersistentObject) po.getObject("album");
                    if (album == null) {
                        album = new PersistentObject("album");
                        po.setObject("album", album);
                        po.setObject("type", "album");
                    }
                    album.setProperty("release_date", releaseDateAlbumTextField.getText());
                    album.setProperty("length", lengthAlbumTextField.getText());

                    // artists
                    album.moveObject("artists", "artists.old");
                    List<PersistentObject> artists = new ArrayList<>();
                    Iterable<String> artistNames = Splitter.on(',')
                            .trimResults()
                            .omitEmptyStrings()
                            .split(artistsTextField.getText());
                    for (String artistName : artistNames) {
                        PersistentObject artist = new PersistentObject("figure");
                        artist.setProperty("name", artistName);
                        artists.add(artist);
                    }
                    album.setObject("artists", artists);

                }

                break;

            case EShopGrocery:
                if (everydayProductRadioButton.isSelected()) {
                    PersistentObject everyday = (PersistentObject) po.getObject("everyday");
                    if (everyday == null) {
                        everyday = new PersistentObject("everyday");
                        po.setObject("everyday", everyday);
                        po.setObject("type", "everyday");
                    }
                    everyday.setProperty("quantity", quantityTextField.getText());
                    everyday.setProperty("unit", unitComboBox.getSelectionModel().getSelectedItem());
                    everyday.setProperty("composition", compositionTextArea.getText());
                }
                else if (foodProductRadioButton.isSelected()) {
                    PersistentObject food = (PersistentObject) po.getObject("food");
                    if (food == null) {
                        food = new PersistentObject("food");
                        po.setObject("food", food);
                        po.setObject("type", "food");
                    }
                    food.setProperty("quantity", quantityTextField.getText());
                    food.setProperty("unit", unitComboBox.getSelectionModel().getSelectedItem());
                    food.setProperty("composition", compositionTextArea.getText());
                    food.setProperty("nutritional_value", nutritionalValueTextArea.getText());
                }
                break;
        }

    }

    @FXML
    protected void delete(ActionEvent event) {
        PersistentObject po = tableView.getSelectionModel().getSelectedItem();
        try {
            productService.deleteProduct(po);
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
