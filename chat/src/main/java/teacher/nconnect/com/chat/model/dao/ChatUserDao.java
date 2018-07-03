package teacher.nconnect.com.chat.model.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import io.reactivex.Single;
import teacher.nconnect.com.chat.model.ChatUser;

/**
 * Created by Ajay on 25-06-2018.
 */
@Dao
public interface ChatUserDao {
    @Query("SELECT * FROM chat_user")
    Single<List<ChatUser>> getChatUserList();

    @Query("SELECT * FROM chat_user where name LIKE  :name")
    Single<ChatUser> findByName(String name);

    @Query("SELECT * FROM chat_user where id_user LIKE  :idUser")
    Single<ChatUser> findByUserId(Long idUser);

    @Query("SELECT COUNT(*) from chat_user")
    Single<Integer> countUsers();

    @Insert
    void insertAll(ChatUser... chatUsers);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ChatUser chatUser);

    @Delete
    void delete(ChatUser chatUser);
}
