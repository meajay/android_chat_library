package teacher.nconnect.com.chat.model;

import java.io.Serializable;

/**
 * Created by Ajay on 26-06-2018.
 */
public class ChatPair implements Serializable {

    public Long idSender;
    public Long idReceiver;
    public String receiverName = "";
    public String senderName = "";

    public String senderImage = "";

    public ChatPair(Long idSender, Long idReceiver) {
        this.idSender = idSender;
        this.idReceiver = idReceiver;
    }

    public ChatPair(Long idSender, Long idReceiver, String receiverName, String senderName,String senderImage) {
        this.idSender = idSender;
        this.idReceiver = idReceiver;
        this.receiverName = receiverName;
        this.senderName = senderName;
        this.senderImage = senderImage;
    }
}
