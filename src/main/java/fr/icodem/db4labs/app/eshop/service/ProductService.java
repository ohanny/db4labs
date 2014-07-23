package fr.icodem.db4labs.app.eshop.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.collections.ObservableList;
import fr.icodem.db4labs.app.AppNameProvider;
import fr.icodem.db4labs.container.AppContainer;
import fr.icodem.db4labs.database.DataType;
import fr.icodem.db4labs.database.PersistentObject;
import fr.icodem.db4labs.database.WhereDescriptor;
import fr.icodem.db4labs.dbtools.transaction.Transactionnal;

import java.util.ArrayList;
import java.util.List;

@Singleton @Transactionnal
public class ProductService {

    @Inject private AppContainer container;
    @Inject private AppNameProvider appNameProvider;

    public ObservableList<PersistentObject> findProductList() throws Exception {
        ObservableList<PersistentObject> result = container.select("product");
        return result;
    }

    public PersistentObject findProductById(int id) throws Exception {
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

    public void saveProduct(PersistentObject item) throws Exception {
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

    private void saveBookAuthors(PersistentObject book) throws Exception {
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

    private void saveMovieLanguages(PersistentObject movie) throws Exception {
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

    private void saveMovieDirector(PersistentObject movie) throws Exception {
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

    private void saveMovieActors(PersistentObject movie) throws Exception {
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

    private void saveAlbumArtists(PersistentObject album) throws Exception {
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

    private void deleteOrphanedFigures() throws Exception {
        String sql = "id not in (select author_id from book_author) and " +
                "id not in (select actor_id from movie_actor) and " +
                "id not in (select director_id from movie_director) and " +
                "id not in (select artist_id from album_artist)";
        WhereDescriptor wd = WhereDescriptor.build(sql);
        container.delete("figure", wd);
    }

    public void deleteProduct(PersistentObject product) throws Exception {
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

}
