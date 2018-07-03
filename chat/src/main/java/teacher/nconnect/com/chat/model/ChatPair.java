package teacher.nconnect.com.chat.model;

/**
 * Created by Ajay on 26-06-2018.
 */
public class ChatPair {

    private Long idSender;
    private Long idReceiver;

    public ChatPair(Long idSender, Long idReceiver) {
        this.idSender = idSender;
        this.idReceiver = idReceiver;
    }
}
