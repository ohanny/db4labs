package fr.icodem.db4labs.app.carpooling.service;

import com.google.inject.Inject;
import fr.icodem.db4labs.container.AppContainer;
import fr.icodem.db4labs.database.DataType;
import fr.icodem.db4labs.database.PersistentObject;
import fr.icodem.db4labs.database.WhereDescriptor;
import fr.icodem.db4labs.dbtools.transaction.Transactionnal;
import javafx.collections.ObservableList;

@Transactionnal
public class MemberService {

    @Inject private AppContainer container;

    public ObservableList<PersistentObject> findMemberList() throws Exception {
        ObservableList<PersistentObject> result = container.select("member");
        return result;
    }

    public PersistentObject findMemberById(String id) throws Exception {
        PersistentObject member = container.selectByPK("member", id);
        PersistentObject user = container.selectByPK("user_data", id);
        PersistentObject photo = null;
        if (member.getProperty("photo_id") != null) {
            photo = container.selectByPK("photo", member.getProperty("photo_id"));
        }
        PersistentObject city = null;
        if (member.getProperty("city_id") != null) {
            city = container.selectByPK("city", member.getProperty("city_id"));
        }
        PersistentObject vehicle = container.selectByPK("vehicle", id);
        if (vehicle != null) {
            PersistentObject model = container.selectByPK("car_model", vehicle.getProperty("car_model_id"));
            vehicle.setObject("car_model", model);
            if (model != null) {
                PersistentObject brand = container.selectByPK("brand", model.getProperty("brand_id"));
                vehicle.setObject("brand", brand);
            }
        }

        member.setObject("user", user);
        member.setObject("photo", photo);
        member.setObject("city", city);
        member.setObject("vehicle", vehicle);

        return member;
    }

    public void saveMember(PersistentObject member) throws Exception {
        String id = (String) member.getProperty("id");
        PersistentObject user = (PersistentObject) member.getObject("user");
        PersistentObject photo = (PersistentObject) member.getObject("photo");
        PersistentObject city = (PersistentObject) member.getObject("city");
        PersistentObject vehicle = (PersistentObject) member.getObject("vehicle");

        PersistentObject actualMember = container.selectByPK("member", id);

        // city
        if (city != null) {
            member.setProperty("city_id", city.getProperty("id"));
        } else {
            member.setProperty("city_id", null);
        }

        if (container.selectByPK("user_data", id) != null &&  actualMember != null) {
            container.update(user);
            container.update(member);
        } else {
            container.insert(user);
            container.insert(member);
        }

        // delete / create actual photo
        if (actualMember != null  && actualMember.getProperty("photo_id") != null) {
            int photoId = (int) actualMember.getProperty("photo_id");
            actualMember.setProperty("photo_id", null);
            container.update(actualMember);
            container.delete("photo", WhereDescriptor.build("id = ?").addParameter(photoId, DataType.INTEGER));
        }
        if (photo != null) {
            photo.removeProperty("id");
            container.insert(photo);
            member.setProperty("photo_id", photo.getProperty("id"));
            container.update(member);
        }

        // vehicle
        PersistentObject actualVehicle = container.selectByPK("vehicle", id);
        if (vehicle == null) {
            if (actualVehicle != null) container.delete(actualVehicle);
        } else {
            vehicle.setProperty("id", id);
            if (actualVehicle == null) {
                container.insert(vehicle);
            } else {
                container.update(vehicle);
            }
        }

        // new username
        String newUsername = (String) user.getObject("newUsername");
        if (newUsername != null) {
            String oldUsername = (String) member.getProperty("id");

            PersistentObject newMember = member.clone();
            newMember.setProperty("id", newUsername);

            PersistentObject newUser = (PersistentObject) member.getObject("user");
            newUser.setProperty("username", newUsername);
            newUser.setObject("newUsername", null);

            PersistentObject newVehicle = (PersistentObject) member.getObject("vehicle");
            if (newVehicle != null) {
                newVehicle.setProperty("id", newUsername);
            }
            saveMember(newMember);
            deleteMember(member);
        }
    }

    public void deleteMember(PersistentObject member) throws Exception {
        member = container.selectByPK("member", member.getProperty("id"));
        String username = (String) member.getProperty("id");
        Integer photoId = (Integer) member.getProperty("photo_id");
        container.delete("vehicle", WhereDescriptor.build("id = ?").addParameter(username, DataType.VARCHAR));
        container.delete("member", WhereDescriptor.build("id = ?").addParameter(username, DataType.VARCHAR));
        container.delete("user_data", WhereDescriptor.build("username = ?").addParameter(username, DataType.VARCHAR));
        if (photoId != null) {
            container.delete("photo", WhereDescriptor.build("id = ?").addParameter(photoId, DataType.INTEGER));
        }
    }

}
