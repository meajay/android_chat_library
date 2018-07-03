package teacher.nconnect.com.chat.listeners;

import teacher.nconnect.com.chat.model.ChatMessage;

/**
 * Created by Ajay on 25-06-2018.
 */
public interface NewMessagesListener {
    public void onNewMessageReceive(ChatMessage chatMessage);
    public void onNewMessageError(String s);
}
