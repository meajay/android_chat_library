package teacher.nconnect.com.chat.listeners;

/**
 * Created by Ajay on 25-06-2018.
 */
public interface DefaultSocketListeners {
  void onConnectListener();
  void onDisconnectListener();
  void onConnectionError();
}

