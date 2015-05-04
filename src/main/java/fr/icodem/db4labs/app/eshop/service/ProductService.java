package fr.icodem.db4labs.app.eshop.service;

import com.google.common.base.Splitter;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import fr.icodem.db4labs.app.eshop.controller.ProductValidator;
import fr.icodem.db4labs.app.eshop.event.ProductAddedEvent;
import fr.icodem.db4labs.dbtools.service.FileImporter;
import fr.icodem.db4labs.dbtools.validation.ValidatorException;
import fr.icodem.db4labs.dbtools.validation.ValidatorResult;
import fr.icodem.db4labs.dbtools.validation.ValidatorResults;
import fr.icodem.db4labs.event.DataImportDoneEvent;
import javafx.collections.ObservableList;
import fr.icodem.db4labs.app.AppNameProvider;
import fr.icodem.db4labs.container.AppContainer;
import fr.icodem.db4labs.database.DataType;
import fr.icodem.db4labs.database.PersistentObject;
import fr.icodem.db4labs.database.WhereDescriptor;
import fr.icodem.db4labs.dbtools.transaction.Transactionnal;
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
import java.util.*;
import java.util.stream.Stream;

@Singleton @Transactionnal
public class ProductService {

    @Inject private AppContainer container;
    @Inject private AppNameProvider appNameProvider;
    @Inject private EventBus eventBus;
    @Inject private FileImporter fileImporter;

    public ObservableList<PersistentObject> findProductList() throws SQLException, IOException {
        ObservableList<PersistentObject> result = container.select("product");
        return result;
    }

    public PersistentObject findProductById(int id) throws SQLException, IOException {
        PersistentObject product = container.selectByPK("product", id);
        if (product == null) return null;

        // image
        if (product.getProperty("image_id") != null) {
            //where = WhereDescriptor.build("id = ?")
              //      .addParameter(product.getProperty("image_id"), DataType.INTEGER);
            //PersistentObject image = container.selectUnique("product_image", where);
            PersistentObject image = container.selectByPK("product_image", product.getProperty("image_id"));
            product.setObject("image", image);
        }

        // tags
        WhereDescriptor where = WhereDescriptor.build("product_id = ?")
                               .addParameter(id, DataType.INTEGER);
        List<PersistentObject> productTags = container.select("product_product_tag", where);
        List<PersistentObject> tags = new ArrayList<>();
        for (PersistentObject productTag : productTags) {
            //where = WhereDescriptor.build("id = ?")
            //                       .addParameter(productTag.getProperty("tag_id"), DataType.INTEGER);
            //PersistentObject tag = container.selectUnique("product_tag", where);
            PersistentObject tag = container.selectByPK("product_tag", productTag.getProperty("tag_id"));
            tags.add(tag);
        }
        product.setObject("tags", tags);

        // specific product properties
        boolean subitemFound = false;

        switch (appNameProvider.getAppName()) {
            case EShopMedia:
                if (!subitemFound) {
                    PersistentObject book = container.selectByPK("book", product.getProperty("id"));
                    if (book != null) {
                        product.setObject("book", book);
                        product.setObject("type", "book");
                        subitemFound = true;

                        // authors
                        where = WhereDescriptor.build("id in (select author_id from book_author where book_id = ?)")
                                .addParameter(id, DataType.INTEGER);
                        List<PersistentObject> authors = container.select("figure", where);
                        book.setObject("authors", authors);
                    }
                }

                if (!subitemFound) {
                    PersistentObject movie = container.selectByPK("movie", product.getProperty("id"));
                    if (movie != null) {
                        product.setObject("movie", movie);
                        product.setObject("type", "movie");
                        subitemFound = true;

                        // actors
                        where = WhereDescriptor.build("id in (select actor_id from movie_actor where movie_id = ?)")
                                               .addParameter(id, DataType.INTEGER);
                        List<PersistentObject> actors = container.select("figure", where);
                        movie.setObject("actors", actors);

                        // director
                        where = WhereDescriptor.build("id in (select director_id from movie_director where movie_id = ?)")
                                               .addParameter(id, DataType.INTEGER);
                        PersistentObject director = container.selectUnique("figure", where);
                        movie.setObject("director", director);

                        // languages
                        where = WhereDescriptor.build("movie_id = ?")
                                               .addParameter(id, DataType.INTEGER);
                        List<PersistentObject> languages = container.select("movie_language", where);
                        movie.setObject("languages", languages);
                    }
                }

                if (!subitemFound) {
                    PersistentObject album = container.selectByPK("album", product.getProperty("id"));
                    if (album != null) {
                        product.setObject("album", album);
                        product.setObject("type", "album");
                        subitemFound = true;

                        // artists
                        where = WhereDescriptor.build("id in (select artist_id from album_artist where album_id = ?)")
                                .addParameter(id, DataType.INTEGER);
                        List<PersistentObject> artists = container.select("figure", where);
                        album.setObject("artists", artists);
                    }
                }
                break;

            case EShopGrocery:
                //TODO ajouter Grocery
                break;
        }

        return product;
    }

    public boolean existsProduct(String type, String name) throws IOException, SQLException {
        WhereDescriptor where = WhereDescriptor.build("lower(name) = ?")
                .addParameter(name.toLowerCase(), DataType.VARCHAR);
        List<PersistentObject> products = container.select("product", where);
        for (PersistentObject po : products) {
            PersistentObject subitem = container.selectByPK(type, po.getProperty("id"));
            if (subitem != null) return true;
        }
        return false;
    }

    public void saveProduct(PersistentObject item) throws SQLException, IOException {
        // *** retrieve specialized product
        PersistentObject subitem = null;
        if (item.getObject("book") != null) {//TODO ajouter Grocery
            subitem = (PersistentObject) item.getObject("book");
        }
        else if (item.getObject("movie") != null) {
            subitem = (PersistentObject) item.getObject("movie");
        }
        else if (item.getObject("album") != null) {
            subitem = (PersistentObject) item.getObject("album");
        }

        // *** image
        PersistentObject imgItem = (PersistentObject) item.getObject("image");
        PersistentObject imgItemOld = (PersistentObject) item.getObject("image.old");
        if (imgItem != null) {
            if (imgItem.getProperty("id") == null) {
                container.insert(imgItem);
                item.setProperty("image_id", imgItem.getProperty("id"));
            } else {
                container.update(imgItem);
            }
        } else if (imgItemOld != null) {
            container.delete(imgItemOld);
        }

        // *** product
        if (item.getProperty("id") != null) {
            container.update(item);
        } else {
            container.insert(item);
        }

        if (subitem != null) {
            if (subitem.getProperty("id") != null) {
                container.update(subitem);
            } else {
                subitem.setProperty("id", item.getProperty("id"));
                container.insert(subitem);
            }
        }

        // tags
        WhereDescriptor where = WhereDescriptor.build("product_id = ?")
                                               .addParameter(item.getProperty("id"), DataType.INTEGER);
        container.delete("product_product_tag", where);
        List<PersistentObject> tags = (List<PersistentObject>) item.getObject("tags");
        for (PersistentObject tag : tags) {
            PersistentObject productTag = new PersistentObject("product_product_tag")
                                                   .setProperty("product_id", item.getProperty("id"))
                                                   .setProperty("tag_id", tag.getProperty("id"));
            container.insert(productTag);
        }

        // *** associations of specialized products
        switch ((String)item.getObject("type")) {
            case "book":
                saveBookAuthors(subitem);
                deleteOrphanedFigures();
                break;
            case "movie":
                saveMovieLanguages(subitem);
                saveMovieDirector(subitem);
                saveMovieActors(subitem);
                deleteOrphanedFigures();
                break;
            case "album":
                saveAlbumArtists(subitem);
                deleteOrphanedFigures();
                break;
            //todo add Grocery
        }


    }

    private void saveBookAuthors(PersistentObject book) throws SQLException, IOException {
        // get authors from database, create them if not found
        List<PersistentObject> authors = (List<PersistentObject>) book.getObject("authors");
        for (PersistentObject author : authors) {
            String authorName = (String) author.getProperty("name");
            WhereDescriptor wd = WhereDescriptor.build("lower(name) = ?")
                    .addParameter(authorName.trim().toLowerCase(), DataType.VARCHAR);
            PersistentObject a = container.selectUnique("figure", wd);
            if (a != null) {
                author.setProperty("id", a.getProperty("id"));
            } else {
                container.insert(author);
            }

        }

        // delete previous book / author links
        WhereDescriptor wd = WhereDescriptor.build("book_id = ?")
                .addParameter(book.getProperty("id"), DataType.INTEGER);
        container.delete("book_author", wd);

        // add new author list
        for (PersistentObject author : authors) {
            PersistentObject bookAuthor = new PersistentObject("book_author")
                    .setProperty("book_id", book.getProperty("id"))
                    .setProperty("author_id", author.getProperty("id"));
            container.insert(bookAuthor);
        }

        // remove old author from cache
        book.setObject("authors.old", null);
    }

    private void saveMovieLanguages(PersistentObject movie) throws SQLException {
        // first delete previous languages
        WhereDescriptor where = WhereDescriptor.build("movie_id = ?")
                                               .addParameter(movie.getProperty("id"), DataType.INTEGER);
        container.delete("movie_language", where);

        // insert new values
        List<PersistentObject> languages = (List<PersistentObject>) movie.getObject("languages");
        for (PersistentObject language : languages) {
            language.setProperty("movie_id", movie.getProperty("id"));
            container.insert(language);
        }

    }

    private void saveMovieDirector(PersistentObject movie) throws SQLException, IOException {
        // get director from database, create it if not found
        PersistentObject director = (PersistentObject) movie.getObject("director");
        if (director == null) return;
        String directorName = (String) director.getProperty("name");
        WhereDescriptor wd = WhereDescriptor.build("lower(name) = ?")
                .addParameter(directorName.trim().toLowerCase(), DataType.VARCHAR);
        PersistentObject d = container.selectUnique("figure", wd);
        if (d != null) {
            director.setProperty("id", d.getProperty("id"));
        } else {
            container.insert(director);
        }

        // update or create movie / director link
        wd = WhereDescriptor.build("movie_id = ?")
                            .addParameter(movie.getProperty("id"), DataType.INTEGER);
        PersistentObject movieDirector = container.selectUnique("movie_director", wd);
        if (movieDirector != null) {
            movieDirector.setProperty("director_id", director.getProperty("id"));
        } else {
            movieDirector = new PersistentObject("movie_director")
                    .setProperty("movie_id", movie.getProperty("id"))
                    .setProperty("director_id", director.getProperty("id"));
            container.insert(movieDirector);
        }

    }

    private void saveMovieActors(PersistentObject movie) throws SQLException, IOException {
        // get actors from database, create them if not found
        List<PersistentObject> actors = (List<PersistentObject>) movie.getObject("actors");
        for (PersistentObject actor : actors) {
            String actorName = (String) actor.getProperty("name");
            WhereDescriptor wd = WhereDescriptor.build("lower(name) = ?")
                    .addParameter(actorName.trim().toLowerCase(), DataType.VARCHAR);
            PersistentObject a = container.selectUnique("figure", wd);
            if (a != null) {
                actor.setProperty("id", a.getProperty("id"));
            } else {
                container.insert(actor);
            }

        }

        // delete previous movie / actor links
        WhereDescriptor wd = WhereDescriptor.build("movie_id = ?")
                .addParameter(movie.getProperty("id"), DataType.INTEGER);
        container.delete("movie_actor", wd);

        // add new actor list
        for (PersistentObject actor : actors) {
            PersistentObject movieActor = new PersistentObject("movie_actor")
                    .setProperty("movie_id", movie.getProperty("id"))
                    .setProperty("actor_id", actor.getProperty("id"));
            container.insert(movieActor);
        }

        // remove old actor from cache
        movie.setObject("actors.old", null);
    }

    private void saveAlbumArtists(PersistentObject album) throws SQLException, IOException {
        // get artists from database, create them if not found
        List<PersistentObject> artists = (List<PersistentObject>) album.getObject("artists");
        for (PersistentObject artist : artists) {
            String artistName = (String) artist.getProperty("name");
            WhereDescriptor wd = WhereDescriptor.build("lower(name) = ?")
                    .addParameter(artistName.trim().toLowerCase(), DataType.VARCHAR);
            PersistentObject a = container.selectUnique("figure", wd);
            if (a != null) {
                artist.setProperty("id", a.getProperty("id"));
            } else {
                container.insert(artist);
            }

        }

        // delete previous album / artist links
        WhereDescriptor wd = WhereDescriptor.build("album_id = ?")
                .addParameter(album.getProperty("id"), DataType.INTEGER);
        container.delete("album_artist", wd);

        // add new artist list
        for (PersistentObject artist : artists) {
            PersistentObject albumArtist = new PersistentObject("album_artist")
                    .setProperty("album_id", album.getProperty("id"))
                    .setProperty("artist_id", artist.getProperty("id"));
            container.insert(albumArtist);
        }

        // remove old artist from cache
        album.setObject("artists.old", null);
    }

    private void deleteOrphanedFigures() throws SQLException {
        String sql = "id not in (select author_id from book_author) and " +
                "id not in (select actor_id from movie_actor) and " +
                "id not in (select director_id from movie_director) and " +
                "id not in (select artist_id from album_artist)";
        WhereDescriptor wd = WhereDescriptor.build(sql);
        container.delete("figure", wd);
    }

    public void deleteProduct(PersistentObject product) throws SQLException, IOException {
        WhereDescriptor where;
        int id = (int) product.getProperty("id");
        product = findProductById(id);

        // process first specialized object
        switch ((String)product.getObject("type")) {
            case "book":
                // delete book links
                where = WhereDescriptor.build("book_id = ?")
                                       .addParameter(id, DataType.INTEGER);
                container.delete("book_author", where);

                // delete book
                where = WhereDescriptor.build("id = ?")
                                       .addParameter(id, DataType.INTEGER);
                container.delete("book", where);
                deleteOrphanedFigures();
                break;

            case "movie":
                // delete movie links
                where = WhereDescriptor.build("movie_id = ?")
                        .addParameter(id, DataType.INTEGER);
                container.delete("movie_actor", where);
                container.delete("movie_director", where);
                container.delete("movie_language", where);

                // delete movie
                where = WhereDescriptor.build("id = ?")
                        .addParameter(id, DataType.INTEGER);
                container.delete("movie", where);
                deleteOrphanedFigures();
                break;

            case "album":
                // delete album links
                where = WhereDescriptor.build("album_id = ?")
                        .addParameter(id, DataType.INTEGER);
                container.delete("album_artist", where);

                // delete album
                where = WhereDescriptor.build("id = ?")
                        .addParameter(id, DataType.INTEGER);
                container.delete("album", where);
                deleteOrphanedFigures();
                break;

        }

        // delete tags
        where = WhereDescriptor.build("product_id = ?")
                               .addParameter(id, DataType.INTEGER);
        container.delete("product_product_tag", where);

        // delete product
        container.delete(product);

        // delete image
        if (product.getProperty("image_id") != null) {
            where = WhereDescriptor.build("id = ?")
                    .addParameter(product.getProperty("image_id"), DataType.INTEGER);
            container.delete("product_image", where);
        }

    }

    public void importData(File file) throws Exception {
        fileImporter.importData(file, this::importData);
    }

    private void importData(InputStream is) throws Exception {

        // base properties
        String type = null;
        String name = null;
        String description = null;
        String price = null;
        boolean available = false;
        byte[] img = null;
        Integer familyId = null;

        // properties depending on product type
        String releaseDate = null;
        String isbn = null;
        String language = null;
        String pages = null;
        String authors = null;
        String length = null;
        String languages = null;
        String actors = null;
        String director = null;
        String artists = null;
        String quantity = null;
        String unit = null;
        String composition = null;
        String nutritionalValue = null;

        // parse XML content
        XMLInputFactory factory = XMLInputFactory.newInstance();

        XMLStreamReader reader = factory.createXMLStreamReader(is);

        String currentElement = null;
        while (reader.hasNext()) {
            int eventType = reader.next();
            switch (eventType) {
                case XMLEvent.CDATA:
                    //case XMLEvent.SPACE:
                case XMLEvent.CHARACTERS:
                    switch (currentElement) {
                        case "type":
                            type = reader.getText();
                            break;

                        case "name":
                            name = reader.getText();
                            break;

                        case "description":
                            description = reader.getText();
                            break;

                        case "price":
                            price = reader.getText();
                            if (price == null || price.equals("0")) {
                                System.out.println("Price should be more than 0");
                                throw new IllegalArgumentException("Price should be more than 0");
                            }
                            break;

                        case "available":
                            available = Boolean.parseBoolean(reader.getText());
                            break;

                        case "image":
                            String img64 = reader.getText();
                            if (img64 != null) {
                                img = Base64.getDecoder().decode(img64);
                            }
                            break;

                        case "family":
                            familyId = Integer.parseInt(reader.getText());
                            break;

                        case "date":
                            releaseDate = reader.getText();
                            if ("01/01/1970".equals(releaseDate)) {
                                System.out.println("Date should not be 01/01/1970");
                                throw new IllegalArgumentException("Date should not be 01/01/1970");
                            }
                            break;

                        case "isbn":
                            isbn = reader.getText();
                            break;

                        case "language":
                            language = reader.getText();
                            break;

                        case "pages":
                            pages = reader.getText();
                            break;

                        case "authors":
                            authors = reader.getText();
                            break;

                        case "length":
                            length = reader.getText();
                            break;

                        case "languages":
                            languages = reader.getText();
                            break;

                        case "actors":
                            actors = reader.getText();
                            break;

                        case "director":
                            director = reader.getText();
                            break;

                        case "artists":
                            artists = reader.getText();
                            break;

                    }
                    break;

                case XMLEvent.START_ELEMENT:
                    currentElement = reader.getLocalName();
                    break;
            }
        }

//        System.out.println("type="+type);
//        System.out.println("name="+name);
//        if (description != null) System.out.println("description="+description.substring(1, Math.min(description.length(), 20)));
//        else System.out.println("description="+description);
//        System.out.println("price="+price);
//        System.out.println("available="+available);
//        System.out.println("img="+img);
//        System.out.println("familyId="+familyId);
//        System.out.println("releaseDate="+releaseDate);
//        System.out.println("isbn="+isbn);
//        System.out.println("language="+language);
//        System.out.println("pages="+pages);
//        System.out.println("authors="+authors);
//        System.out.println("length="+length);
//        System.out.println("languages="+languages);
//        System.out.println("actors="+actors);
//        System.out.println("director="+director);
//        System.out.println("artists="+artists);

        PersistentObject po = new PersistentObject("product");
        populateProduct(po, name, description, price, available, img, familyId, null,
                type, releaseDate, isbn, language, pages, authors, length, languages,
                director, actors, artists, quantity, unit, composition, nutritionalValue);

        // retrieve media product for validation
        PersistentObject subitem = null;
        if (po.getObject("book") != null) {
            subitem = (PersistentObject) po.getObject("book");
        }
        else if (po.getObject("movie") != null) {
            subitem = (PersistentObject) po.getObject("movie");
        }
        else if (po.getObject("album") != null) {
            subitem = (PersistentObject) po.getObject("album");
        }

        // retrieve grocery product for validation
        if (po.getObject("everyday") != null) {
            subitem = (PersistentObject) po.getObject("everyday");
        }
        else if (po.getObject("food") != null) {
            subitem = (PersistentObject) po.getObject("food");
        }

        // perform validation
        ValidatorResults results = new ProductValidator().validate(po, subitem);

        // validation errors
        if (!results.isValid()) {
            for (ValidatorResult vr : results.getResults()) {
                System.out.println(vr.getProperty() + " : " + vr.getMessage() + " : " + vr.getOriginalValue());
            }
            throw new ValidatorException("Product validation failed", results);
        }

        if (existsProduct(type, name)) {
            System.out.println("Product already exists in database : " + name);
            throw new IllegalArgumentException("Product already exists in database : " + name);
        }

        // save product
        saveProduct(po);

        // notity product tab controller
        eventBus.post(new ProductAddedEvent(po));

    }

    public void populateProduct(PersistentObject po, String name, String description, String price,
                                boolean available, byte[] img, Integer familyId,
                                List<PersistentObject> tags, String type,
                                String releaseDate, String isbn, String language,// book
                                String pages, String authorsData,// book
                                String length, String languagesData,// movie
                                String directorData, String actorsData,// movie
                                String artistsData,// album
                                String quantity, String unit, String composition,// everyday products
                                String nutritionalValue // food product
                                ) {

        // base properties
        po.setProperty("name", name);
        po.setProperty("description", description);
        po.setProperty("price", price);
        po.setProperty("available", available);

        // family
        po.setProperty("family_id", familyId);

        // image
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
        if (tags == null) tags = new ArrayList<>();
        po.setObject("tags", tags);

        // properties depending on product type
        switch (appNameProvider.getAppName()) {
            case EShopMedia:
                if ("book".equals(type)) {
                    PersistentObject book = (PersistentObject) po.getObject("book");
                    if (book == null) {
                        book = new PersistentObject("book");
                        po.setObject("book", book);
                        po.setObject("type", "book");
                    }
                    book.setProperty("release_date", releaseDate);
                    book.setProperty("isbn", isbn);
                    book.setProperty("language", language);
                    book.setProperty("pages", pages);


                    // authors
                    book.moveObject("authors", "authors.old");
                    List<PersistentObject> authors = new ArrayList<>();
                    authorsData = (authorsData == null)?"":authorsData;
                    Iterable<String> authorNames = Splitter.on(',')
                            .trimResults()
                            .omitEmptyStrings()
                            .split(authorsData);
                    for (String authorName : authorNames) {
                        PersistentObject author = new PersistentObject("figure");
                        author.setProperty("name", authorName);
                        authors.add(author);
                    }
                    book.setObject("authors", authors);

                }
                else if ("movie".equals(type)) {
                    PersistentObject movie = (PersistentObject) po.getObject("movie");
                    if (movie == null) {
                        movie = new PersistentObject("movie");
                        po.setObject("movie", movie);
                        po.setObject("type", "movie");
                    }
                    movie.setProperty("release_date", releaseDate);
                    movie.setProperty("length", length);

                    // languages
                    Set<String> languagesSet = new HashSet<>();
                    languagesData = (languagesData == null)?"":languagesData;
                    Iterable<String> languagesString = Splitter.on(',')
                            .trimResults()
                            .omitEmptyStrings()
                            .split(languagesData);
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
                    if (directorData != null && !directorData.trim().isEmpty()) {
                        PersistentObject director = new PersistentObject("figure");
                        director.setProperty("name", directorData);
                        movie.setObject("director", director);
                    }

                    // actors
                    actorsData = (actorsData == null)?"":actorsData;
                    movie.moveObject("actors", "actors.old");
                    List<PersistentObject> actors = new ArrayList<>();
                    Iterable<String> actorNames = Splitter.on(',')
                            .trimResults()
                            .omitEmptyStrings()
                            .split(actorsData);
                    for (String authorName : actorNames) {
                        PersistentObject actor = new PersistentObject("figure");
                        actor.setProperty("name", authorName);
                        actors.add(actor);
                    }
                    movie.setObject("actors", actors);

                }
                else if ("album".equals(type)) {
                    PersistentObject album = (PersistentObject) po.getObject("album");
                    if (album == null) {
                        album = new PersistentObject("album");
                        po.setObject("album", album);
                        po.setObject("type", "album");
                    }
                    album.setProperty("release_date", releaseDate);
                    album.setProperty("length", length);

                    // artists
                    artistsData = (artistsData == null)?"":artistsData;
                    album.moveObject("artists", "artists.old");
                    List<PersistentObject> artists = new ArrayList<>();
                    Iterable<String> artistNames = Splitter.on(',')
                            .trimResults()
                            .omitEmptyStrings()
                            .split(artistsData);
                    for (String artistName : artistNames) {
                        PersistentObject artist = new PersistentObject("figure");
                        artist.setProperty("name", artistName);
                        artists.add(artist);
                    }
                    album.setObject("artists", artists);

                }

                break;

            case EShopGrocery:
                if ("everydayProduct".equals(type)) {
                    PersistentObject everyday = (PersistentObject) po.getObject("everyday");
                    if (everyday == null) {
                        everyday = new PersistentObject("everyday");
                        po.setObject("everyday", everyday);
                        po.setObject("type", "everyday");
                    }
                    everyday.setProperty("quantity", quantity);
                    everyday.setProperty("unit", unit);
                    everyday.setProperty("composition", composition);
                }
                else if ("foodProduct".equals(type)) {
                    PersistentObject food = (PersistentObject) po.getObject("food");
                    if (food == null) {
                        food = new PersistentObject("food");
                        po.setObject("food", food);
                        po.setObject("type", "food");
                    }
                    food.setProperty("quantity", quantity);
                    food.setProperty("unit", unit);
                    food.setProperty("composition", composition);
                    food.setProperty("nutritional_value", nutritionalValue);
                }
                break;
        }

    }



}
