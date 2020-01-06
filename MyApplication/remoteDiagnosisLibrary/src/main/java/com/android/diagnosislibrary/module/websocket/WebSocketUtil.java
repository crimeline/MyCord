package com.android.diagnosislibrary.module.websocket;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import com.android.diagnosislibrary.config.RDConfig;
import com.android.diagnosislibrary.utils.CommonUtil;
import com.android.diagnosislibrary.utils.DevUtils;
import com.android.diagnosislibrary.utils.Logger.Logger;
import com.android.diagnosislibrary.utils.StringUtils;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketUtil {
    private final static String TAG = "WebSocketUtil";
    private static WebSocketUtil mInstance = null;
    private static Context mContext = null;
    private WebSocketClient mWebSocketClient = null;

    private WebSocketUtil(Context ctx) {
        this.mContext = ctx;
    }

    public static WebSocketUtil getInstance(Context ctx) {
        if (mInstance == null) {
            synchronized (WebSocketUtil.class) {
                if (mInstance == null) {
                    if (ctx == null) {
                        Logger.d(TAG, "getInstance : context error!");
                        return null;
                    }
                    mInstance = new WebSocketUtil(ctx);
                    mHandlerThread = new HandlerThread("webSocketUtil");
                    mHandlerThread.start();
                    mHandler = new Handler(mHandlerThread.getLooper());
                }
            }
        }
        return mInstance;
    }

    Runnable umsUrlRunnable = new Runnable() {
        @Override
        public void run() {
//            String umsUrl =  SPUtil.getString(SPConstants.KEY_WSS_UMS_URL, "");
            String umsUrl = "wss://iepg-sy.vosnewland.com";
            Log.d(TAG, "umsUrl: " + umsUrl);
            if (TextUtils.isEmpty(umsUrl)) {
                mHandler.removeCallbacks(umsUrlRunnable);
                mHandler.postDelayed(umsUrlRunnable, 3 * 1000);
            } else {
                mHandler.removeCallbacks(umsUrlRunnable);
                connect();
            }
        }
    };

    Runnable reconnectRunnable = new Runnable() {
        @Override
        public void run() {
            Logger.d(TAG, "reconnectRunnable : connect!");
            mHandler.removeCallbacks(reconnectRunnable);
            connect();
        }
    };

    public void startReconnect() {
        Logger.d(TAG, " mHandler: " + mHandler + "tangtang reconnectRunnable: " + reconnectRunnable + " , " + android.os.Process.myPid());
        if (CommonUtil.isMainProcess(mContext) && mHandler != null) {
            Logger.d(TAG, "send massage to reconnectRunnable");
            mHandler.removeCallbacks(reconnectRunnable);
            mHandler.postDelayed(reconnectRunnable, 5 * 1000);
        } else {
            Logger.d(TAG, "startReconnect is null");
        }
        Logger.d(TAG, "startReconnect is null");
    }

    private void closeSocket() {
        if (mWebSocketClient != null && !mWebSocketClient.isClosed()) {
            mWebSocketClient.close();
            pongSystime = 0;
            mWebSocketClient = null;
        }
    }

    public void connect() {
        String umsUrl = RDConfig.getInstance().getUrl();
        if (StringUtils.isNullOrEmpty(umsUrl)) {
            Logger.e(TAG, "connect: url is null");
            return;
        }

        if (TextUtils.isEmpty(umsUrl)) {
            mHandler.removeCallbacks(umsUrlRunnable);
            mHandler.post(umsUrlRunnable);
            return;
        }

        try {
            String wsUrl = umsUrl + "/websocket?id=" + DevUtils.getWebSocketUserID(mContext) + "&cityCode=0&appKey=" + DevUtils.getAppkey(mContext);
            Logger.d(TAG, "web socket url = " + wsUrl);

            URI uri = new URI(wsUrl);

            if (null == mWebSocketClient) {
                mWebSocketClient = new WebSocketClient(uri) {
                    @Override
                    public void onOpen(ServerHandshake serverHandshake) {
                        Logger.d(TAG, "onOpen sucess ");
                        reconnectCheck();
                    }

                    @Override
                    public void onMessage(String msg) {
                        Logger.d(TAG, "onMessage msg: " + msg);
                        WebMsgListener.getInstance(mContext).onMessageReceived(mWebSocketClient, msg);
                    }

                    @Override
                    public void onClose(int i, String s, boolean b) {
                        Logger.d(TAG, "onClose !!");
                        if (mWebSocketClient != null) {
                            mWebSocketClient = null;
                        }

                        pongSystime = 0;
                        startReconnect();
                    }

                    @Override
                    public void onError(Exception e) {
                        Logger.d(TAG, "onError e: " + e.getMessage());

                        closeSocket();
                    }

                    @Override
                    public void onWebsocketPing(WebSocket conn, Framedata f) {

                        Logger.d(TAG, "onWebsocketPing: ");
                        super.onWebsocketPing(conn, f);
                    }

                    @Override
                    public void onWebsocketPong(WebSocket conn, Framedata f) {
                        Logger.d(TAG, "onWebsocketPong: ");
                        pongSystime = System.currentTimeMillis();
                        super.onWebsocketPong(conn, f);
                    }
                };
                Logger.d(TAG, " web socket connect .");
                if (mWebSocketClient != null) {
                    mWebSocketClient.connect();
                }

            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            closeSocket();
        }
    }

    private static Handler mHandler = null;

    private static HandlerThread mHandlerThread = null;
    private static boolean bPong = false;

    private static int PING_INTERVAL = 30 * 1000;

    private static int RECONNECT_INTERVAL = 60 * 1000;

    private static long pongSystime = 0;

    Runnable pingRunnable = new Runnable() {
        @Override
        public void run() {
            bPong = false;
            try {
                Logger.d(TAG, "  mWebSocketClient.isOpen: " + mWebSocketClient.isOpen());
                if (mWebSocketClient != null && mWebSocketClient.isOpen()) {
                    Logger.d(TAG, " sendPing .");
                    mWebSocketClient.sendPing();
                }
                mHandler.removeCallbacks(pingRunnable);
                mHandler.postDelayed(pingRunnable, PING_INTERVAL);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    Runnable pongRunnable = new Runnable() {
        @Override
        public void run() {

            try {
                long interval = System.currentTimeMillis() - pongSystime;
                Logger.d(TAG, "  interval: " + interval);

                if (pongSystime != 0 && interval > RECONNECT_INTERVAL) {
                    pongSystime = System.currentTimeMillis(); //pong 消息一直不来，避免过度频繁重连
                    Logger.d(TAG, " mWebSocketClient  reconnect ...");
                    closeSocket();
                }
                mHandler.removeCallbacks(pongRunnable);
                mHandler.postDelayed(pongRunnable, RECONNECT_INTERVAL);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    private void reconnectCheck() {
        mHandler.removeCallbacks(pingRunnable);
        mHandler.postDelayed(pingRunnable, PING_INTERVAL);

        mHandler.removeCallbacks(pongRunnable);
        mHandler.postDelayed(pongRunnable, RECONNECT_INTERVAL);
    }

//    public void sendEasyResponse(int cmd, String param){
//        WebMsgListener.getInstance().sendEasyResponse(cmd, param);
//    }
//
//    public void sendGameMessage(String msg){
//        WebMsgListener.getInstance().sendGameMessage(msg);
//    }
//
//    public void sendEasyNotify(String instruction, String param, String jid){
//        Logger.d(TAG, " sendEasyNotify instruction : "+instruction+" ;  param : "+param+"; id : "+jid);
//
//        WebMsgListener.getInstance().sendEasyNotify(instruction, param, jid);
//    }
}
