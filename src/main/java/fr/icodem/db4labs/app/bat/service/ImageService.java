package fr.icodem.db4labs.app.bat.service;

import com.google.inject.Inject;
import fr.icodem.db4labs.component.ImageUtils;
import fr.icodem.db4labs.container.AppContainer;
import fr.icodem.db4labs.database.DataType;
import fr.icodem.db4labs.database.PersistentObject;
import fr.icodem.db4labs.database.WhereDescriptor;
import fr.icodem.db4labs.dbtools.transaction.Transactionnal;
import javafx.collections.ObservableList;

@Transactionnal
public class ImageService {
    @Inject private AppContainer container;


    public void loadImage(PersistentObject item, String objectName, String target, int targetId) throws Exception {
        WhereDescriptor where = WhereDescriptor.build("target = ? and target_id = ?")
                                               .addParameter(target, DataType.VARCHAR)
                                               .addParameter(targetId, DataType.INTEGER);
        PersistentObject img = container.selectUnique("image_data", where);
        item.setObject(objectName, img);
    }

    public void loadImages(PersistentObject item, String objectName, String target, int targetId) throws Exception {
        WhereDescriptor where = WhereDescriptor.build("target = ? and target_id = ? and (size = 'big' or size = null)")
                                               .addParameter(target, DataType.VARCHAR)
                                               .addParameter(targetId, DataType.INTEGER);
        ObservableList<PersistentObject> imgList = container.select("image_data", where);
        item.setObject(objectName, imgList);
    }

    public void saveImage(PersistentObject item, String objectName,
                          String target, int targetId, boolean withSmallCopy,
                          int smallWidth, int smallHeight) throws Exception {
        PersistentObject imgItem = (PersistentObject) item.getObject(objectName);

        if (imgItem != null) {
            imgItem.setProperty("target", target);
            imgItem.setProperty("target_id", targetId);
            if (withSmallCopy) {
                imgItem.setProperty("size", "big");
            } else {
                imgItem.setProperty("size", null);
            }
            byte[] originalImgData = (byte[]) imgItem.getProperty("content");
            byte[] jpegImgData = ImageUtils.convertToJPEG(originalImgData);
            imgItem.setProperty("content", jpegImgData);
            container.insert(imgItem);

            if (withSmallCopy) {// small copy
                PersistentObject smallImgItem = imgItem.clone();
                smallImgItem.setProperty("id", null);
                byte[] smallImgData = ImageUtils.reduceImage(jpegImgData, smallWidth, smallHeight);
                smallImgItem.setProperty("content", smallImgData);
                smallImgItem.setProperty("size", "small");
                container.insert(smallImgItem);
            }
        }
    }

    public void deleteImage(String target, int targetId) throws Exception {
        WhereDescriptor where = WhereDescriptor.build("target = ? and target_id = ?")
                                               .addParameter(target, DataType.VARCHAR)
                                               .addParameter(targetId, DataType.INTEGER);

        container.delete("image_data", where);
    }

}
