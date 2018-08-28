package teacher.nconnect.com.chat.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import teacher.nconnect.com.chat.utils.GsonUtils;

/**
 * Created by tarun on 6/13/18.
 */

@Entity(tableName = "chat_message")
public class ChatMessage {

    //local field
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_chat_message")
    private Long idChatMessage;

    @ColumnInfo(name = "id_msg")
    @SerializedName("id_msg")
    private Long idMessage = 0L;

    @ColumnInfo(name = "id_sender")
    @SerializedName("id_sender")
    private Long idSender;

    @ColumnInfo(name = "sender_name")
    @SerializedName("sender_name")
    private String senderName;

    @ColumnInfo(name = "sender_image")
    @SerializedName("sender_image")
    private String senderImage;

    @ColumnInfo(name = "receiver_name")
    @SerializedName("receiver_name")
    private String receiverName;

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    @ColumnInfo(name = "id_receiver")
    @SerializedName("id_receiver")
    private Long idReceiver;

    @ColumnInfo(name = "msg")
    @SerializedName("msg")
    private String msg;

    @ColumnInfo(name = "status")
    @SerializedName("status")
    private String status;

    @ColumnInfo(name = "sent_time")
    @SerializedName("sent_time")
    private String sentTime;

    @ColumnInfo(name = "msg_type")
    @SerializedName("msg_type")
    private String msgType;

    @android.support.annotation.Nullable
    @ColumnInfo(name = "s3_key")
    @SerializedName("s3_key")
    private String s3Key = "";

    @android.support.annotation.Nullable
    @ColumnInfo(name = "file_local_path")
    @SerializedName("file_local_path")
    private String fileLocalPath = "";

    public ChatMessage() {
    }


    public Long getIdSender() {
        return idSender;
    }

    public void setIdSender(Long idSender) {
        this.idSender = idSender;
    }

    public Long getIdReceiver() {
        return idReceiver;
    }

    public void setIdReceiver(Long idReceiver) {
        this.idReceiver = idReceiver;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSentTime() {
        return sentTime;
    }

    public void setSentTime(String sentTime) {
        this.sentTime = sentTime;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public Long getIdChatMessage() {
        return idChatMessage;
    }

    public void setIdChatMessage(Long idChatMessage) {
        this.idChatMessage = idChatMessage;
    }

    public Long getIdMessage() {
        return idMessage;
    }

    public void setIdMessage(Long idMessage) {
        this.idMessage = idMessage;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    @Override
    public String toString() {
        return GsonUtils.convertToJSON(this);
    }

    public String getFileLocalPath() {
        return fileLocalPath;
    }

    public void setFileLocalPath(String fileLocalPath) {
        this.fileLocalPath = fileLocalPath;
    }

    public String getSenderImage() {
        return senderImage;
    }

    public void setSenderImage(String senderImage) {
        this.senderImage = senderImage;
    }
}