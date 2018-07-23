package teacher.nconnect.com.chat;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import teacher.nconnect.com.chat.constants.AppConstants;
import teacher.nconnect.com.chat.constants.Channel;
import teacher.nconnect.com.chat.constants.MessageType;
import teacher.nconnect.com.chat.db.DbChatService;
import teacher.nconnect.com.chat.listeners.AddNewMessageListener;
import teacher.nconnect.com.chat.listeners.AttachmentListener;
import teacher.nconnect.com.chat.listeners.DefaultSocketListeners;
import teacher.nconnect.com.chat.listeners.FileDbUpdateSuccess;
import teacher.nconnect.com.chat.listeners.FileDownloadListener;
import teacher.nconnect.com.chat.listeners.FileUploadListener;
import teacher.nconnect.com.chat.listeners.GetChatMessageList;
import teacher.nconnect.com.chat.listeners.GetLocalChatUserList;
import teacher.nconnect.com.chat.listeners.NewMessagesListener;
import teacher.nconnect.com.chat.listeners.OldMessageListener;
import teacher.nconnect.com.chat.model.ChatJoin;
import teacher.nconnect.com.chat.model.ChatMessage;
import teacher.nconnect.com.chat.model.ChatUser;
import teacher.nconnect.com.chat.utils.GsonUtils;
import timber.log.Timber;

/**
 * Created by Ajay on 23-06-2018.
 */
public class MainChat {

    private Socket chatSocket;
    private Long idSender;
    private Long currentReceiverId = 0L;
    private DbChatService dbService;
    private OldMessageListener oldMessageListener;
    private ArrayList<DefaultSocketListeners> defaultSocketListenerList = new ArrayList<>();
    private ArrayList<NewMessagesListener> newMessagesListenerList = new ArrayList<>();
    private AddNewMessageListener addNewMessageListener;
    private Context context;
    private AWSMobileClient awsMobileClient;
    private FileUploadListener fileUploadListener;
    private FileDownloadListener fileDownloadListener;


    private static MainChat INSTANCE = null;

    public static MainChat getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MainChat();

        }
        return INSTANCE;
    }

    public void initializeChatModule(Context context, Long idSender) {
        this.idSender = idSender;
        this.context = context;
        if (awsMobileClient == null) {
            awsMobileClient = AWSMobileClient.getInstance();
        }
        if (dbService == null) {
            dbService = DbChatService.getDbService(context, idSender);
        }
        if (chatSocket == null)
            try {
                chatSocket = IO.socket(AppConstants.BASE_URL + AppConstants.CHAT + idSender);
            } catch (URISyntaxException e) {
                Timber.d(e);
            }
    }

    public boolean setUpDefaultSocketListeners(DefaultSocketListeners defaultSocketListeners) {
        if (chatSocket != null) {
            chatSocket.on(Socket.EVENT_CONNECT, onConnect);
            chatSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
            chatSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            chatSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            chatSocket.on(Channel.NEW_MESSAGE.getValue(), onNewMessage);
            chatSocket.on(Channel.OLD_MSG_LIST.getValue(), onOldMessage);
            this.defaultSocketListenerList.add(defaultSocketListeners);
            chatSocket.connect();
            return true;
        }
        return false;
    }

    public boolean setUpNewMessageListener(NewMessagesListener newMessagesListener) {
        if (chatSocket != null) {
            this.newMessagesListenerList.add(newMessagesListener);
            return true;
        }
        return false;
    }

    public boolean setUpOldMessageListener(OldMessageListener oldMessageListener) {
        if (chatSocket != null) {
            this.oldMessageListener = oldMessageListener;
            return true;
        }
        return false;
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Timber.tag(AppConstants.CHAT_TAG).d("chat connected...");
            chatSocket.emit(Channel.JOIN.getValue(), new ChatJoin(idSender));
            chatSocket.emit(Channel.OLD_MSG_LIST.getValue(), new ChatJoin(idSender));
            for (DefaultSocketListeners defaultSocketListeners : defaultSocketListenerList)
                defaultSocketListeners.onConnectListener();

            chatSocket.connect();
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            for (DefaultSocketListeners defaultSocketListeners : defaultSocketListenerList)
                defaultSocketListeners.onDisconnectListener();
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            for (DefaultSocketListeners defaultSocketListeners : defaultSocketListenerList) {
                defaultSocketListeners.onConnectionError();
            }
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String data = (String) args[0];
            ChatMessage receivedMsg = null;
            try {
                receivedMsg = GsonUtils.convertToObject(data, ChatMessage.class);
                ChatUser chatUser = new ChatUser();
                chatUser.setIdUser(receivedMsg.getIdSender());
                chatUser.setName(receivedMsg.getSenderName());
                dbService.checkAndInsertNewUser(chatUser);
                dbService.insertChatMessage(receivedMsg, true);
                for (NewMessagesListener newMessagesListener : newMessagesListenerList) {
                    newMessagesListener.onNewMessageReceive(receivedMsg);
                    dbService.doAfterMessageReceived(receivedMsg, chatSocket, true);

                }
            } catch (Exception e) {
                for (NewMessagesListener newMessagesListener : newMessagesListenerList) {
                    newMessagesListener.onNewMessageError(e.getMessage());
                    Timber.tag(AppConstants.CHAT_TAG).d(e);
                }
            }
        }
    };

    private Emitter.Listener onOldMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String data = (String) args[0];
            Type listType = new TypeToken<ArrayList<ChatMessage>>() {
            }.getType();
            try {
                ArrayList<ChatMessage> unReadMsgList = GsonUtils
                        .convertToObject(data, listType);
                Timber.tag(AppConstants.CHAT_TAG)
                        .d("message list %d", unReadMsgList.size());
                oldMessageListener.onOldMessagesReceive();
                if (unReadMsgList.size() > 0) {
                    dbService.doAfterMessageListReceived(unReadMsgList, chatSocket,
                            true);
                }

            } catch (Exception e) {
                oldMessageListener.onOldMessagesError(e.getMessage());
                Timber.tag(AppConstants.CHAT_TAG).d(e);
            }
        }
    };

    public void getChatUserList(GetLocalChatUserList localChatUserList) {
        if (dbService != null) {
            dbService.getAllLocalChatUsers(localChatUserList);
        }

    }

    public void getMessageList(GetChatMessageList getChatMessageList) {
        dbService.getChatMessageList(idSender, currentReceiverId, getChatMessageList);
    }

    public void removeListeners() {
        if (defaultSocketListenerList.size() > 1) {
            defaultSocketListenerList.remove(defaultSocketListenerList.size() - 1);
        }
        if (newMessagesListenerList.size() > 1) {
            newMessagesListenerList.remove(newMessagesListenerList.size() - 1);
        }

        addNewMessageListener = null;
        oldMessageListener = null;
    }

    public void releaseResources() {
        chatSocket.disconnect();
        chatSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        chatSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        chatSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnect);
        chatSocket.off(Channel.NEW_MESSAGE.getValue(), onNewMessage);
        chatSocket.off(Channel.OLD_MSG_LIST.getValue(), onOldMessage);
        oldMessageListener = null;
        removeListeners();
    }

    public void setCurrentReceiverId(Long receiverId) {
        this.currentReceiverId = receiverId;
    }

    public Long getCurrentReceiverId() {
        return currentReceiverId;
    }

    @SuppressLint("NewApi")
    public String getPath(Context context, Uri uri) throws URISyntaxException {
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;

        if (needToCheckUri && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public void createMessage(ChatMessage sendingMsg, String filePath, String msgType,
                              boolean isFromNewChat, boolean isFirstMessage,
                              AddNewMessageListener addNewMessageListener,
                              FileUploadListener fileUploadListener, Context context) {

        if (fileUploadListener != null)
            this.fileUploadListener = fileUploadListener;
        //todo check if calling early
        if (addNewMessageListener != null)
            this.addNewMessageListener = addNewMessageListener;

        if (msgType.equalsIgnoreCase(MessageType.TEXT.getValue())) {
            sendMessageToServer(sendingMsg, isFromNewChat, isFirstMessage);
        } else {
            File file = new File(filePath);
            sendingMsg.setS3Key(file.getName() + System.currentTimeMillis() + "");
            sendingMsg.setFileLocalPath(filePath);
            uploadWithTransferUtility(context, sendingMsg, filePath);
        }
    }

    public void sendMessageToServer(ChatMessage sendingMsg,
                                    boolean isFromNewChat, boolean isFirstMessageSent) {

        if (chatSocket.connected()) {
            chatSocket.emit(Channel.NEW_MESSAGE.getValue(), sendingMsg.toString());
            if (isFromNewChat && !isFirstMessageSent) {
                ChatUser chatUser = new ChatUser();
                chatUser.setIdUser(sendingMsg.getIdReceiver());
                chatUser.setName(sendingMsg.getReceiverName());
                dbService.checkAndInsertNewUser(chatUser);
                addNewMessageListener.onNewMessageReceive(sendingMsg);
            }
            dbService.insertChatMessage(sendingMsg, false);
        } else {
            Timber.tag(AppConstants.CHAT_TAG).d("Internet not available");
        }
    }

    public void downloadWithTransferUtility(String fileS3Key, ChatMessage receivedMsg,
                                            FileDownloadListener fileDownloadListener) {

        if (awsMobileClient == null) {
            fileDownloadListener.fileDownloadStatus("", false, receivedMsg);
            return;
        }

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(context)
                        .awsConfiguration(awsMobileClient.getConfiguration())
                        .s3Client(new AmazonS3Client(awsMobileClient.getCredentialsProvider()))
                        .build();

        File file = new File(Environment.getExternalStorageDirectory().toString() + "/"
                + fileS3Key);
        TransferObserver downloadObserver =
                transferUtility.download(AppConstants.s3FolderName + "/"
                        + AppConstants.fileNamePrefix + fileS3Key, file);

        downloadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    Timber.tag(AppConstants.CHAT_TAG).d("download completed");
                    fileDownloadListener.fileDownloadStatus(file.getPath(), true, receivedMsg);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int) percentDonef;

                Timber.tag(AppConstants.CHAT_TAG).d(" ID:" + id + "   bytesCurrent: "
                        + bytesCurrent +
                        "  bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                Timber.tag(AppConstants.CHAT_TAG).d("error in download %d %s", id,
                        ex.getMessage());
                Timber.tag(AppConstants.CHAT_TAG).d(ex);
                fileDownloadListener.fileDownloadStatus(file.getPath(), false, receivedMsg);
            }

        });
    }

    public void uploadWithTransferUtility(Context context, ChatMessage sendingMsg,
                                          String filePath) {

        if (filePath == null) {
            Timber.tag(AppConstants.CHAT_TAG).d("Could not find the " +
                    "filepath of the selected file");
            return;
        }
        if (awsMobileClient == null) {
            fileUploadListener.fileUploadStatus("", false, sendingMsg);
            return;
        }

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(context)
                        .awsConfiguration(awsMobileClient.getConfiguration())
                        .s3Client(new AmazonS3Client(awsMobileClient.getCredentialsProvider()))
                        .build();

        File file = new File(filePath);
        Timber.tag(AppConstants.CHAT_TAG).d("somrthing %s, %s", file.getName(), file.getAbsolutePath());
        TransferObserver uploadObserver = transferUtility.upload(AppConstants.s3FolderName +
                "/" + AppConstants.fileNamePrefix
                + file.getName(), file);

        Timber.tag(AppConstants.CHAT_TAG).d("Bytes Transferred: " + uploadObserver.getBytesTransferred());
        Timber.tag(AppConstants.CHAT_TAG).d("Bytes Total: " + uploadObserver.getBytesTotal());
        Timber.tag(AppConstants.CHAT_TAG).d("Bytes Total: " + uploadObserver.getState());


        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                Timber.tag(AppConstants.CHAT_TAG).d("state", state);

                if (TransferState.COMPLETED == state) {
                    fileUploadListener.fileUploadStatus(file.getPath(), true,
                            sendingMsg);
                    Timber.tag(AppConstants.CHAT_TAG).d("upload completed");
                } else if (TransferState.FAILED == state) {
                    fileUploadListener.fileUploadStatus(file.getPath(), false,
                            sendingMsg);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

                Timber.tag(AppConstants.CHAT_TAG).d("progress %s", bytesCurrent);

                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int) percentDonef;

                Timber.tag(AppConstants.CHAT_TAG).d("ID:" + id + " bytesCurrent: "
                        + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                Timber.tag(AppConstants.CHAT_TAG).d("error %d %s", id, ex.getMessage());
                Timber.tag(AppConstants.CHAT_TAG).d(ex);
                fileUploadListener.fileUploadStatus(file.getPath(), false,
                        sendingMsg);
            }

        });

    }

    public void uploadAttachment(Context context,
                                 String filePath, AttachmentListener attachmentListener) {

        if (filePath == null) {
            Timber.tag(AppConstants.CHAT_TAG).d("Could not find the " +
                    "filepath of the selected file");
            return;
        }
        if (awsMobileClient == null) {
            attachmentListener.fileUploadStatus("", false);
            return;
        }

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(context)
                        .awsConfiguration(awsMobileClient.getConfiguration())
                        .s3Client(new AmazonS3Client(awsMobileClient.getCredentialsProvider()))
                        .build();

        File file = new File(filePath);
        String s3Key = file.getName() + "_" + System.currentTimeMillis();
        Timber.tag(AppConstants.CHAT_TAG).d("somrthing %s, %s", file.getName(), file.getAbsolutePath());
        TransferObserver uploadObserver = transferUtility.upload(AppConstants.s3FolderName +
                "/" + s3Key, file);

        Timber.tag(AppConstants.CHAT_TAG).d("Bytes Transferred: " + uploadObserver.getBytesTransferred());
        Timber.tag(AppConstants.CHAT_TAG).d("Bytes Total: " + uploadObserver.getBytesTotal());
        Timber.tag(AppConstants.CHAT_TAG).d("Bytes Total: " + uploadObserver.getState());


        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                Timber.tag(AppConstants.CHAT_TAG).d("state", state);

                if (TransferState.COMPLETED == state) {
                    attachmentListener.fileUploadStatus(s3Key, true);
                    Timber.tag(AppConstants.CHAT_TAG).d("upload completed");
                } else if (TransferState.FAILED == state) {
                    attachmentListener.fileUploadStatus("", false);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

                Timber.tag(AppConstants.CHAT_TAG).d("progress %s", bytesCurrent);

                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int) percentDonef;
                attachmentListener.fileUploadProgress(percentDone);

                Timber.tag(AppConstants.CHAT_TAG).d("ID:" + id + " bytesCurrent: "
                        + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                Timber.tag(AppConstants.CHAT_TAG).d("error %d %s", id, ex.getMessage());
                Timber.tag(AppConstants.CHAT_TAG).d(ex);
                attachmentListener.fileUploadStatus("", false);
            }

        });

    }

    public void updateChatMessageMediaPath(ChatMessage chatMessage,
                                           FileDbUpdateSuccess fileDbUpdateSuccess) {
        Completable updateMessage = Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                dbService.updateMediaPath(chatMessage);
            }
        });

        updateMessage
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        fileDbUpdateSuccess.filepathDbUpdateSuccess(chatMessage);
                    }

                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }

    public void updateChatMessageReadList(ArrayList<ChatMessage> listMessage) {
        dbService.updateChatMessageReadList(listMessage);
    }

    public void insertChatUser(ChatUser chatUser) {
        if (dbService != null) {
            dbService.checkAndInsertNewUser(chatUser);
        }
    }
}



