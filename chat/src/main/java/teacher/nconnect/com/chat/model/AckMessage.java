package teacher.nconnect.com.chat.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ajay on 25-06-2018.
 */
public class AckMessage {

    @SerializedName("id_msg")
    private Long idMsg;

    @SerializedName("id_receiver")
    private Long idReceiver;


    public Long getIdMsg() {
        return idMsg;
    }

    public void setIdMsg(Long idMsg) {
        this.idMsg = idMsg;
    }

    public Long getIdReceiver() {
        return idReceiver;
    }

    public void setIdReceiver(Long idReceiver) {
        this.idReceiver = idReceiver;
    }
}

