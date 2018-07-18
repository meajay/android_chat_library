package teacher.nconnect.com.chat.listeners;

/**
 * Created by Ajay on 13-07-2018.
 */
public interface AttachmentListener {
    void fileUploadStatus(String s3Key, boolean isSuccessful);

    void fileUploadProgress( int percentageDone);
}
