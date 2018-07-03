package teacher.nconnect.com.chat.listeners;

import java.util.ArrayList;

import teacher.nconnect.com.chat.model.ChatMessage;

/**
 * Created by Ajay on 30-06-2018.
 */
public interface GetChatMessageList {
    void getChatMessages(ArrayList<ChatMessage> listMessage);
}
