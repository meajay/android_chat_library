package teacher.nconnect.com.chat.db;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import io.socket.client.Socket;
import teacher.nconnect.com.chat.constants.AppConstants;
import teacher.nconnect.com.chat.constants.Channel;
import teacher.nconnect.com.chat.listeners.GetChatMessageList;
import teacher.nconnect.com.chat.listeners.GetLocalChatUserList;
import teacher.nconnect.com.chat.model.AckMessage;
import teacher.nconnect.com.chat.model.ChatMessage;
import teacher.nconnect.com.chat.model.ChatPair;
import teacher.nconnect.com.chat.model.ChatUser;
import teacher.nconnect.com.chat.utils.GsonUtils;
import timber.log.Timber;

/**
 * Created by Ajay on 25-06-2018.
 */
public class DbChatService {

    private NChatDatabase nChatDatabase;
    private Context context;
    private CompositeDisposable subscriptions;
    private Long idSender;
    private ArrayList<ChatMessage> chatMessages;

    private DbChatService(Context context, Long idSender) {
        this.context = context;
        this.idSender = idSender;
        nChatDatabase = NChatDatabase.getAppDatabase(context);
        subscriptions = new CompositeDisposable();
    }

    public static DbChatService getDbService(Context context, Long idSender) {
        return new DbChatService(context, idSender);
    }

    public void findOrInsertLocalUser(ChatUser user) {

        Completable insertUser = Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                nChatDatabase.chatUserDao().insert(user);
            }
        });

        nChatDatabase.chatUserDao().findByUserId(user.getIdUser())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new SingleObserver<ChatUser>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        subscriptions.add(d);
                    }

                    @Override
                    public void onSuccess(ChatUser chatUser) {
                        Timber.tag(AppConstants.CHAT_TAG)
                                .d("user already exist:: %s", chatUser.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        insertUser.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new CompletableObserver() {
                                    @Override
                                    public void onSubscribe(Disposable d) {
                                        subscriptions.add(d);
                                    }

                                    @Override
                                    public void onComplete() {
                                        Timber.tag(AppConstants.CHAT_TAG)
                                                .d("new user inserted successfully:: %s",
                                                        user.toString());
                                        getAllLocalChatUsers(idSender);
                                    }


                                    @Override
                                    public void onError(Throwable e) {
                                        Timber.tag(AppConstants.CHAT_TAG)
                                                .d("error inserting new user:: %s",
                                                        e.getMessage());
                                        getAllLocalChatUsers(idSender);
                                    }
                                });
                    }
                });
    }

    public void checkAndInsertNewUser(ChatUser chatUser) {
        if (chatUser != null) {
            findOrInsertLocalUser(chatUser);
        }

    }

    public void doAfterMessageReceived(ChatMessage message, Socket chatSocket,
                                       boolean isReceivedMsg) {
        AckMessage ackMessage = new AckMessage();
        ackMessage.setIdMsg(message.getIdMessage());
        ackMessage.setIdReceiver(message.getIdReceiver());
        ArrayList<AckMessage> recMsgList = new ArrayList<>();
        recMsgList.add(ackMessage);
        chatSocket.emit(Channel.MSG_RECEIVED.getValue(), GsonUtils.convertToJSON(recMsgList));
    }

    public void insertChatMessage(ChatMessage message, boolean isReceivedMsg) {

        Completable insertMessage = Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                nChatDatabase.chatMessageDao().insert(message);
            }
        });

        if (!isReceivedMsg) {
            insertMessage
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            subscriptions.add(d);
                        }

                        @Override
                        public void onComplete() {
                            Timber.tag(AppConstants.CHAT_TAG)
                                    .d("message inserted successfully:: %s", message
                                            .toString());
                        }

                        @Override
                        public void onError(Throwable e) {
                            Timber.tag(AppConstants.CHAT_TAG)
                                    .d("message insertetion failed:: %s", e.getMessage());

                        }
                    });
        } else { //check if already inserted..
            nChatDatabase.chatMessageDao().findMsgById(message.getIdMessage())
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(new SingleObserver<ChatMessage>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(ChatMessage chatMessage) {
                            Timber.tag(AppConstants.CHAT_TAG)
                                    .d("message already inserted:: %s",
                                            message.toString());
                        }

                        @Override
                        public void onError(Throwable e) {
                            // insert new message
                            insertMessage
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new CompletableObserver() {
                                        @Override
                                        public void onSubscribe(Disposable d) {
                                            subscriptions.add(d);
                                        }

                                        @Override
                                        public void onComplete() {
                                            Timber.tag(AppConstants.CHAT_TAG)
                                                    .d("message inserted successfully:: %s"
                                                            , message.toString());
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Timber.tag(AppConstants.CHAT_TAG)
                                                    .d("message insertion failed:: %s",
                                                            e.getMessage());

                                        }
                                    });
                        }
                    });

        }

    }

    public void getAllLocalChatUsers(Long idSender) {
        nChatDatabase.chatUserDao().getChatUserList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<ChatUser>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        subscriptions.add(d);
                    }

                    @Override
                    public void onSuccess(List<ChatUser> chatUsers) {
                        Timber.tag(AppConstants.CHAT_TAG).d("localChatUsers list %s",
                                GsonUtils.convertToJSON(chatUsers));
                        if (chatUsers != null && chatUsers.size() > 0) {
                            ArrayList<ChatPair> chatPairList = new ArrayList<>();
                            for (int i = 0; i < chatUsers.size(); i++) {
                                chatPairList.add(new ChatPair(idSender, chatUsers.get(i)
                                        .getIdUser()));
                                chatUsers.get(i).idSender = idSender;
                            }
                            getLastTenMessageListForMultiple(chatPairList, chatUsers);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.tag(AppConstants.CHAT_TAG).d("localChatUsers list error %s"
                                , e.getMessage());

                    }
                });
    }

    private void getLastTenMessageListForMultiple(ArrayList<ChatPair> chatPairList,
                                                  List<ChatUser> chatUserList) {

        Observable.fromIterable(chatUserList)
                .flatMapSingle(chatUser ->
                        nChatDatabase.chatMessageDao().findLastMessageList(chatUser.idSender,
                                chatUser.getIdUser()))
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<List<ChatMessage>>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        subscriptions.add(d);
                    }

                    @Override
                    public void onSuccess(List<List<ChatMessage>> lists) {
                        for (int i = 0; i < lists.size(); i++) {
                            chatUserList.get(i).chatMessageList = new ArrayList<>(lists.get(i));
                        }
                        Timber.tag(AppConstants.CHAT_TAG).
                                d("localChatUsers ten message list %s", GsonUtils
                                        .convertToJSON(chatPairList));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.tag(AppConstants.CHAT_TAG)
                                .d("localChatUsers  ten message  list %s", e.getMessage());
                    }
                });
    }

    public void doAfterMessageListReceived(ArrayList<ChatMessage> unReadMsgList, Socket chatSocket,
                                           boolean isReceivedMsg) {
        ArrayList<AckMessage> recMsgList = new ArrayList<>();
        for (ChatMessage message : unReadMsgList) {
            AckMessage ackMessage = new AckMessage();
            ackMessage.setIdMsg(message.getIdMessage());
            ackMessage.setIdReceiver(message.getIdReceiver());
            recMsgList.add(ackMessage);
        }

        chatSocket.emit(Channel.MSG_RECEIVED.getValue(), GsonUtils.convertToJSON(recMsgList));
        for (ChatMessage message : unReadMsgList)
            insertChatMessage(message, isReceivedMsg);
    }

    public void updateChatMessageReadList(
            ArrayList<ChatMessage> chatMessageList) {
        for (ChatMessage message : chatMessageList)
            updateChatMessageRead(message);
    }

    public void updateChatMessageRead(ChatMessage chatMessage) {

        Completable updateMessage = Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                nChatDatabase.chatMessageDao().updateMsgRead(chatMessage.getIdMessage());
            }
        });

        updateMessage
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        subscriptions.add(d);
                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    public void getChatMessageList(Long idSender, Long idReceiver, GetChatMessageList getChatMessageList) {

        nChatDatabase.chatMessageDao().findMessages(idSender, idReceiver)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<ChatMessage>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        subscriptions.add(d);
                    }

                    @Override
                    public void onSuccess(List<ChatMessage> chatMessages) {
                        Timber.tag(AppConstants.CHAT_TAG).d("chat message list..%s", chatMessages.size());
                        if (chatMessages.size() > 0) {
                            getChatMessageList.getChatMessages(new ArrayList<>(chatMessages));
                            // setChatMessages(new ArrayList<>(chatMessages));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    public void updateMediaPath(ChatMessage chatMessage) {
        nChatDatabase.chatMessageDao().updateMediaPath(chatMessage.getFileLocalPath(), chatMessage.getIdMessage());
    }

    public void getAllLocalChatUsers(GetLocalChatUserList localChatUserList) {
        nChatDatabase.chatUserDao().getChatUserList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<ChatUser>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        subscriptions.add(d);
                    }

                    @Override
                    public void onSuccess(List<ChatUser> chatUsers) {
                        Timber.tag(AppConstants.CHAT_TAG).d("localChatUsers list %s",
                                GsonUtils.convertToJSON(chatUsers));
                        if (chatUsers != null && chatUsers.size() > 0) {
                            ArrayList<ChatPair> chatPairList = new ArrayList<>();
                            for (int i = 0; i < chatUsers.size(); i++) {
                                chatPairList.add(new ChatPair(idSender, chatUsers.get(i)
                                        .getIdUser()));
                                chatUsers.get(i).idSender = idSender;
                            }

                            Observable.fromIterable(chatUsers)
                                    .flatMapSingle(chatUser ->
                                            nChatDatabase.chatMessageDao().findLastMessageList(chatUser.idSender,
                                                    chatUser.getIdUser()))
                                    .toList()
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<List<List<ChatMessage>>>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {
                                            subscriptions.add(d);
                                        }

                                        @Override
                                        public void onSuccess(List<List<ChatMessage>> lists) {
                                            for (int i = 0; i < lists.size(); i++) {
                                                chatUsers.get(i).chatMessageList = new ArrayList<>(lists.get(i));
                                            }
                                            Timber.tag(AppConstants.CHAT_TAG).
                                                    d("localChatUsers ten message list %s", GsonUtils
                                                            .convertToJSON(chatPairList));
                                            localChatUserList.getChatUserList(new ArrayList<>(chatUsers));
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Timber.tag(AppConstants.CHAT_TAG)
                                                    .d("localChatUsers  ten message  list %s", e.getMessage());
                                        }
                                    });

                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.tag(AppConstants.CHAT_TAG).d("localChatUsers list error %s"
                                , e.getMessage());

                    }
                });
    }
}
