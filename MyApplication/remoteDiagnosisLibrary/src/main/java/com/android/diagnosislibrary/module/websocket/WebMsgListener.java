package com.android.diagnosislibrary.module.websocket;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.android.diagnosislibrary.module.handlerManager.LogcatEndCmdImpl;
import com.android.diagnosislibrary.module.handlerManager.LogcatStartCmdImpl;
import com.android.diagnosislibrary.module.handlerManager.ShellCmdManager;
import com.android.diagnosislibrary.module.handlerManager.UploadLogCmdImpl;
import com.android.diagnosislibrary.utils.DevUtils;
import com.android.diagnosislibrary.utils.JsonUtil;
import com.android.diagnosislibrary.utils.Logger.Logger;
import com.android.diagnosislibrary.utils.StringUtils;

import org.java_websocket.client.WebSocketClient;

import java.util.ArrayList;
import java.util.List;

public class WebMsgListener {
    private static final String TAG = "MsgListener";

    private static long mDeltatime = 0;
    private static long mLastSyncWebTime = 0;

    private WebSocketClient mWebSocketClient = null;

    private String lastId = null;

    private static WebMsgListener sInstance;
    private static Context mContext = null;

    private List<ICmdHandler> cmdHandlers = null;

    private WebMsgListener() {
        cmdHandlers = new ArrayList<>();
    }

    public void init(Context ctx){
        if (ctx == null) {
            return;
        }
        mContext = ctx;
        LogcatEndCmdImpl.getInstance().init();
        LogcatStartCmdImpl.getInstance().init();
        UploadLogCmdImpl.getInstance().init(mContext);
    }

    public static WebMsgListener getInstance() {
        if (sInstance == null) {
            synchronized (WebMsgListener.class) {
                if (sInstance == null) {
                    sInstance = new WebMsgListener();
                }
            }
        }
        return sInstance;
    }

    public void onMessageReceived(WebSocketClient client, String message) {
        if (client == null || TextUtils.isEmpty(message)) {
            Logger.e(TAG, "onMessageReceived message: is null");
            return;
        }

        Logger.d(TAG, "onMessageReceived message:" + message);
        WebSocketBody webSocketBody = JsonUtil.getInstance().fromJson(message, WebSocketBody.class);
        if (webSocketBody != null) {
            if (mDeltatime == 0) {
//                syncWebTime();
            }
            mWebSocketClient = client;

            lastId = webSocketBody.getFrom();
            parse(lastId, webSocketBody.getMessage());
        }
    }

//    /**
//     * 同步xmpp后台时间
//     */
//    private void syncWebTime(){
//        LogUtil.d(TAG, " sync xmpp web time . ");
//        String url = SPUtil.getString(SPConstants.KEY_IEPG_URL, Constants.DEFAULT_IEPG_URL) +"/getSystemTime";
//
//        UtilCallback callback = new UtilCallback(){
//            @Override
//            public void onSuccess(String response) throws RemoteException {
//                try {
//                    SystemTimeInfo timeInfo = JsonUtil.getInstance().fromJson(response, SystemTimeInfo.class);
//                    if(timeInfo != null && "0".equals(timeInfo.getRet())){
//                        try {
//                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//                            long time = sdf.parse(timeInfo.getDateTime()).getTime();
//                            if(time > sdf.parse("2015-01-01 00:00:00").getTime()){ //时间大于2015年才要
//                                mDeltatime = System.currentTimeMillis() - time;
//                                LogUtil.d(TAG, " get epg time delta : "+mDeltatime);
//                            }
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//        };
//
//        if(CommonUtil.isMainProcess()) {
//            try {
//                TVLiveService.iUtilInterface.simpleGet(url, callback);
//            } catch (Exception e) {
//                LogUtil.e(TAG, e.getMessage());
//            }
//        } else {
//            PubPostManager.simpleGet(url, null, callback);
//        }
//    }

    public interface ICmdHandler {
        String getCmdName();
        void cmdHandler(String id, String command);
    }

    public interface CommandCallBack {
        void sendResult(String line);
    }

    public CommandCallBack getCommandCallBack(@NonNull final String id) {
        return new WebMsgListener.CommandCallBack() {
            @Override
            public void sendResult(String line) {
                sendDiagnoseResponse(line, id);
            }
        };
    }

    public void addCmd(ICmdHandler cmdhander) {
        synchronized (this) {
            cmdHandlers.add(cmdhander);
        }
    }

//    public void delCmd(ICmdHandler cmdhander) {
//        synchronized (this) {
//            cmdHandlers.remove(cmdhander);
//        }
//    }

    private void parse(String id, String msg) {
        try {
            if (StringUtils.isNullOrEmpty(msg)) {
                return;
            }
            MessageBody messageBody = JsonUtil.getInstance().fromJson(msg, MessageBody.class);
            if (messageBody != null) {
                if (messageBody.getTimestamp() > 0 && (System.currentTimeMillis() - mDeltatime - messageBody.getTimestamp() > 10 * 1000)) { //消息大于10秒的直接丢掉
                    Logger.d(TAG, " msg delay than 10s , last sync web time : " + mLastSyncWebTime);
                    if (System.currentTimeMillis() - mLastSyncWebTime > 10 * 60 * 1000) { //发现接收到的xmpp消息一直是大于10秒时则每隔10分钟同步一次xmpp后台服务器时间
                        mLastSyncWebTime = System.currentTimeMillis();
//                        syncWebTime();
                    }
                    return;
                }

                Logger.d(TAG, "parse: msg " + msg);
                if (messageBody.getCommand() != null) {
                    List<String> commands = messageBody.getCommand();

                    for (int i = 0; i < commands.size(); i++) {
                        String command = commands.get(i);
                        for (ICmdHandler cmdhandler : cmdHandlers) {
                            if (command.startsWith(MsgConstant.CMD_AM)) {
                                Logger.d(TAG, "handleAndroidCmd =====  ");
                                //handleAndroidCmd(id, command);
                                return ;
                            }else if(command.startsWith(cmdhandler.getCmdName())){
                                cmdhandler.cmdHandler(id, command);
                                return ;
                            }
//                            else if (command.startsWith(MsgConstant.CMD_CUSTOM)) {
//                                Logger.d(TAG, "handleCustomCmd =====  ");
//                                //抓取日志
//                                handleCustomCmd(id, command);
//                                return ;
//                            }
                        }
                        //执行命令
                        Logger.d(TAG, "handleShellCmd =====  " + command);
                        ShellCmdManager.getInstance().runShellCmd(id, command);
                    }
                } else {
                    Logger.d(TAG, "parse: handleElseMessage ");
                    handleElseMessage(id, msg);
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
        }

    }

    private void handleElseMessage(String id, String msg) {
        MessageBody msgBody = MessageBody.build();
        msgBody.setErrInfo("无法识别的消息体");
        msgBody.setResult("1");
        sendElseResponse(id, msgBody);
    }

    /**
     * android命令
     */
//    private void handleAndroidCmd(String id, @NonNull String command) {
//        try {
//            if(command.startsWith(CMD_GETPROP)) {
//                String[] params = command.split("\\s+");
//                String value = DeviceUtil.getProperties(params[1], "");
//                sendDiagnoseResponse(value, id);
//            } else if(command.startsWith(CMD_SETPROP)) {
//                String[] params = command.split("\\s+");
//                DeviceUtil.setProperties(params[1], params[2]);
//                sendDiagnoseResponse("", id);
//            } else if (command.startsWith(CMD_AM)) {
//                AmUtils.execCmd(BaseApplication.getContext(), command, "\\s+");
//                sendDiagnoseResponse("", id);
//            }
//        } catch (Exception e) {
//            Logger.e(TAG, e.getMessage());
//        }
//    }

    public void sendDiagnoseResponse(String line, String id) {
        try {
            MessageBody result = new MessageBody();

            result.setResult("0");
            result.setErrInfo("");
            result.setCommandResult(line);

            WebSocketBody webSocketBody = new WebSocketBody();
            webSocketBody.setTo(id);
            webSocketBody.setFrom(DevUtils.getSn(mContext));
            webSocketBody.setMessage(JsonUtil.getInstance().toJson(result));

            if (mWebSocketClient != null) {
                mWebSocketClient.send(JsonUtil.getInstance().toJson(webSocketBody));
            }
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
        }
    }

    private void sendElseResponse(String id, MessageBody msgBody) {
        try {
            WebSocketBody webSocketBody = new WebSocketBody();
            webSocketBody.setFrom(DevUtils.getSn(mContext));
            webSocketBody.setTo(id);
            webSocketBody.setMessage(JsonUtil.getInstance().toJson(msgBody));
            if (mWebSocketClient != null) {
                mWebSocketClient.send(JsonUtil.getInstance().toJson(webSocketBody));
            }
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
        }
    }
}