package teacher.nconnect.com.chat.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;

import java.util.ArrayList;

import teacher.nconnect.com.chat.utils.GsonUtils;

/**
 * Created by Ajay on 25-06-2018.
 * Receiver
 */
@Entity(tableName = "chat_user")
public class ChatUser {

    @ColumnInfo(name = "name")
    public String name;   //receiverName

    @PrimaryKey
    @ColumnInfo(name = "id_user")
    public Long idUser;    // idReceiver

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    @ColumnInfo(name = "user_image")
    public String userImage;

    // derived attributes not to insert in db,using these to forward
    @Ignore
    public ArrayList<ChatMessage> chatMessageList = new ArrayList<>();

    @Ignore
    public Long idSender;

    public ChatUser(Long idUser, String name) {
        this.name = name;
        this.idUser = idUser;
    }

    public ChatUser() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    @Override
    public String toString() {
        return GsonUtils.convertToJSON(this);
    }

}
