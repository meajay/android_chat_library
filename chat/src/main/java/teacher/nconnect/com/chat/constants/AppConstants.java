package teacher.nconnect.com.chat.constants;

import teacher.nconnect.com.chat.BuildConfig;

/**
 * Created by Ajay on 25-06-2018.
 */
public class AppConstants {

    public static final String BASE_URL = BuildConfig.NCONNECT_BASE_URL;
    public static final String s3FolderName = "nconnect-chat";
    public static final String fileNamePrefix = "nchat-";

    //CHAT API END POINTS
    public static final String CHAT = "/chatSocket?id_user=";

    public static final String CHAT_TAG = "CHAT_LIBRARY";

    //Database version
    public static final int VERSION = 2 ;
}
