package teacher.nconnect.com.chat.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import teacher.nconnect.com.chat.constants.AppConstants;
import teacher.nconnect.com.chat.model.ChatMessage;
import teacher.nconnect.com.chat.model.ChatUser;
import teacher.nconnect.com.chat.model.dao.ChatMessageDao;
import teacher.nconnect.com.chat.model.dao.ChatUserDao;

/**
 * Created by Ajay on 25-06-2018.
 */
@Database(entities = {ChatUser.class, ChatMessage.class}  ,version = AppConstants.VERSION)
public abstract class NChatDatabase extends RoomDatabase {

        private static NChatDatabase INSTANCE = null;

        public abstract ChatUserDao chatUserDao();

        public abstract ChatMessageDao chatMessageDao();

        public static NChatDatabase getAppDatabase(Context context) {
            if (INSTANCE == null) {
                INSTANCE =
                        Room.databaseBuilder(context.getApplicationContext(), NChatDatabase.class,
                                "nconnect-chat-db")
                                // allow queries on the main thread.
                                // Don't do this on a real app! See PersistenceBasicSample for an example.
                                // .allowMainThreadQueries()
                                .build();
            }
            return INSTANCE;
        }

        public static void destroyInstance() {
            INSTANCE = null;
        }
    }

