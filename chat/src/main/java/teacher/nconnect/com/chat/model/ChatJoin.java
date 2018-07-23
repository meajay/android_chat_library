package teacher.nconnect.com.chat.model;

import com.google.gson.annotations.SerializedName;

import teacher.nconnect.com.chat.utils.GsonUtils;

/**
 * Created by Ajay on 25-06-2018.
 */

public class ChatJoin {

    @SerializedName("id_user")
    Long idUser;

    public ChatJoin(Long idUser) {
        this.idUser = idUser;
    }

    @Override
    public String toString() {
        return GsonUtils.convertToJSON(this);
    }
}

