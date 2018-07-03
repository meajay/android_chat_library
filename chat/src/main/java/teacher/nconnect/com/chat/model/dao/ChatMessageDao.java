package teacher.nconnect.com.chat.model.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import java.util.List;

import io.reactivex.Single;
import teacher.nconnect.com.chat.model.ChatMessage;

/**
 * Created by Ajay on 25-06-2018.
 */
@Dao
public interface ChatMessageDao {
    @Query("SELECT * FROM chat_message where (id_sender = :idSender and id_receiver = :idReceiver) or (id_sender = :idReceiver and id_receiver = :idSender) ")
    Single<List<ChatMessage>> findMessages(Long idSender, Long idReceiver);

    @Query("SELECT * FROM chat_message where (id_sender = :idSender and id_receiver = :idReceiver) or (id_sender = :idReceiver and id_receiver = :idSender)  order by id_chat_message desc limit 10")
    Single<List<ChatMessage>> findLastMessageList(Long idSender, Long idReceiver);

    @Query("SELECT COUNT(*) from chat_message")
    Single<Integer> countMessages();

    @Query("SELECT * FROM chat_message where id_msg LIKE  :idMsg")
    Single<ChatMessage> findMsgById(Long idMsg);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ChatMessage chatMessage);

    @Query("UPDATE chat_message SET file_local_path = :filePath where id_msg = :idMsg")
    void updateMediaPath(String filePath, Long idMsg );



    @Query("UPDATE chat_message SET status = 'read' where id_msg = :idMsg")
    void updateMsgRead(Long idMsg);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(ChatMessage... chatMessages);
}
