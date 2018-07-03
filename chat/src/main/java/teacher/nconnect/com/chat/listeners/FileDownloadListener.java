package teacher.nconnect.com.chat.listeners;

import teacher.nconnect.com.chat.model.ChatMessage;

/**
 * Created by Ajay on 29-06-2018.
 */
public interface FileDownloadListener {
    void fileDownloadStatus(String filePath, boolean isSuccessful,ChatMessage sendingMsg);
}
