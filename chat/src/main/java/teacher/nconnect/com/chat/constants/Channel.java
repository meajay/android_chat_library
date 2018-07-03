package teacher.nconnect.com.chat.constants;

/**
 * Created by Ajay on 25-06-2018.
 */
public enum Channel {
    JOIN("join"),                       //EMIT to join the server
    OLD_MSG_LIST("old_message_list"),           // EMIT to get the unread messages from server
    NEW_MESSAGE("new_message"),             // EMIT AND LISTEN to send and receive a message
    MSG_RECEIVED("message_received");   // EMIT to send saying the message is delivered

    private String channel;

    Channel(String  channel){
        this.channel = channel;
    }

    public String getValue() {
        return channel;
    }
}
