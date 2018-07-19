package teacher.nconnect.com.chat.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;

import java.util.ArrayList;

import teacher.nconnect.com.chat.util.GsonUtils;

/**
 * Created by Ajay on 25-06-2018.
 */
@Entity(tableName = "chat_user")
public class ChatUser {

    @ColumnInfo(name = "name")
    public String name;

    @PrimaryKey
    @ColumnInfo(name = "id_user")
    public Long idUser;    // idReceiver

    // derived attributes
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

    protected ChatUser(Parcel in) {
        name = in.readString();
        if (in.readByte() == 0) {
            idUser = null;
        } else {
            idUser = in.readLong();
        }
        if (in.readByte() == 0) {
            idSender = null;
        } else {
            idSender = in.readLong();
        }
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
