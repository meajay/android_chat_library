package teacher.nconnect.com.chat.listeners;

import java.util.ArrayList;

import teacher.nconnect.com.chat.model.ChatUser;

/**
 * Created by Ajay on 19-07-2018.
 */
public interface GetLocalChatUserList {
    void getChatUserList(ArrayList<ChatUser> chatUsers);
}

