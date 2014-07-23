package fr.icodem.db4labs.component;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.util.ResourceBundle;

public class ImageInput extends StackPane implements Initializable {

    @FXML
    private Text text;

    @FXML
    private ImageView imageView;

    private static int IMAGE_MAX_SIZE = 150 * 1024;
    private static String IMAGE_TOO_BIG = "Image too big";
    private byte[] data;

    public ImageInput() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/component/image_input.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            getStyleClass().add("image-input");
            getStyleClass().add("no-content");
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addDragDropSupport();
        //label.getStyleClass().add("message-label");
    }

    private void addDragDropSupport() {
        this.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();

                // accept drop : check image (file system, Firefox)
                if (db.getImage() != null) {
                    event.acceptTransferModes(TransferMode.COPY);
                } else {
                    // check if image from Chrome
                    for (DataFormat df : db.getContentTypes()) {
                        //System.out.println(df.getIdentifiers()); //org.chromium.image-html, public.url-name, com.apple.pasteboard.promised-file-content-type
                        if (df.getIdentifiers().contains("public.jpeg")) {
                            event.acceptTransferModes(TransferMode.COPY);
                            break;
                        }
                    }
                }
                event.consume();
            }
        });
        this.setOnDragEntered(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                getStyleClass().add("drag-over");
                Dragboard db = event.getDragboard();
                event.consume();
            }
        });
        this.setOnDragExited(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent dragEvent) {
                getStyleClass().remove("drag-over");
            }
        });
        this.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                //imageText.setText("hello: dropped");
                Dragboard db = event.getDragboard();

                boolean imageSizeOK = true;
                if (db.getImage() != null) {
                    // getProperty image data
                    if (db.getFiles().size() == 1) {// file system
                        File file = db.getFiles().get(0);
                        try (SeekableByteChannel channel = Files.newByteChannel(file.toPath());) {
                            if (channel.size() > IMAGE_MAX_SIZE) {
                                imageSizeOK = false;
                            }
                            data = readChannel(channel);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (db.getUrl() != null) {// Firefox
                        try {
                            URL url = new URL(db.getUrl());
                            try (ReadableByteChannel channel = Channels.newChannel(url.openStream());) {
                                data = readChannel(channel);
                            } catch (IllegalArgumentException e) {
                                imageSizeOK = false;
                                e.printStackTrace();
                            } catch (Exception e) {
                                imageSizeOK = false;
                                e.printStackTrace();
                            }
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }

                    // display image
                    if (imageSizeOK) {
                        imageView.setImage(db.getImage());
                        getStyleClass().remove("no-content");
                        resetMessage();
                    } else {
                        //data = null;
                        setMessage(IMAGE_TOO_BIG);
                        //imageView.setImage(null);
                        //getStyleClass().add("no-content");
                    }
                    event.setDropCompleted(true);
                } else {
                    // check if image from Chrome
                    for (DataFormat df : db.getContentTypes()) {
                        //System.out.println(df.getIdentifiers()); //org.chromium.image-html, public.url-name, com.apple.pasteboard.promised-file-content-type
                        if (df.getIdentifiers().contains("public.jpeg")) {
                            // getProperty image data
                            ByteBuffer buffer = (ByteBuffer) db.getContent(df);
                            data = buffer.array();
                            if (data.length > IMAGE_MAX_SIZE) {
                                data = null;
                                setMessage(IMAGE_TOO_BIG);
                                imageView.setImage(null);
                                getStyleClass().add("no-content");
                                break;
                            }

                            // display image
                            displayImage(data);
/*
                            InputStream is = new ByteArrayInputStream(data);
                            Image img = new Image(is);
                            imageView.setImage(img);
*/
                            event.setDropCompleted(true);
                            break;
                        }
                    }
                }
                event.consume();
            }
        });
    }

    @FXML
    protected void removeImage(ActionEvent event) {
        setData(null);
    }

    private void setMessage(String message) {
        text.setText(message);
        getStyleClass().add("message");
    }

    private void resetMessage() {
        getStyleClass().remove("message");
        text.setText("<no image>");
    }

    private void displayImage(byte[] imageData) {
        resetMessage();
        if (imageData == null) {
            imageView.setImage(null);
            getStyleClass().add("no-content");
            return;
        }

        // display image
        InputStream is = new ByteArrayInputStream(imageData);
        Image img = new Image(is);
        imageView.setImage(img);
        getStyleClass().remove("no-content");
    }

    private byte[] readChannel(ReadableByteChannel channel) throws Exception {

        ByteBuffer buffer = ByteBuffer.allocate(IMAGE_MAX_SIZE);

        // read channel and fill buffer
        while(channel.read(buffer) > 0) {}
        if (!buffer.hasRemaining()) {
            throw new IllegalArgumentException("Image size too big");
        }
        buffer.flip();

        byte[] result = new byte[buffer.limit()];
        buffer.get(result);
        return result;
    }

    private byte[] readChannel(SeekableByteChannel channel) throws Exception {
        long size = channel.size();
        ByteBuffer buffer = ByteBuffer.allocate((int)size);
        channel.read(buffer);
        buffer.flip();
        byte[] result = buffer.array();
        return result;
    }

    // getters and setters
    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
        displayImage(data);
    }
}
