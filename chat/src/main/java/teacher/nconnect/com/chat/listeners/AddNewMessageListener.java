package teacher.nconnect.com.chat.listeners;

import teacher.nconnect.com.chat.model.ChatMessage;

/**
 * Created by Ajay on 29-06-2018.
 */
public interface AddNewMessageListener {
    void onNewMessageReceive(ChatMessage chatMessage);
}
