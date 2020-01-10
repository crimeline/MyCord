package com.android.diagnosislibrary;

import android.content.Context;
import android.text.TextUtils;

import com.android.diagnosislibrary.config.RDConfig;
import com.android.diagnosislibrary.module.handlerManager.CmdConstant;
import com.android.diagnosislibrary.module.handlerManager.LogcatEndCmdImpl;
import com.android.diagnosislibrary.module.handlerManager.LogcatStartCmdImpl;
import com.android.diagnosislibrary.module.handlerManager.SetLogFilterCmdImpl;
import com.android.diagnosislibrary.module.handlerManager.UploadLogCmdImpl;
import com.android.diagnosislibrary.module.logCollectionManager.LogCollectionManager;
import com.android.diagnosislibrary.module.logCollectionManager.UploadLogManager;
import com.android.diagnosislibrary.module.shellCmdManager.ShellCmdManager;
import com.android.diagnosislibrary.module.websocket.MessageBody;
import com.android.diagnosislibrary.module.websocket.WebSocketBody;
import com.android.diagnosislibrary.module.websocket.WebSocketUtil;
import com.android.diagnosislibrary.utils.DevUtils;
import com.android.diagnosislibrary.utils.FileUtils;
import com.android.diagnosislibrary.utils.JsonUtil;
import com.android.diagnosislibrary.utils.Logger.Logger;
import com.android.diagnosislibrary.utils.StringUtils;

import org.java_websocket.client.WebSocketClient;

import java.util.ArrayList;
import java.util.List;

public class DiagnosisManagement {
    private static final String TAG = "DiagnosisManagement";

    private static long mDeltatime = 0;
    private static long mLastSyncWebTime = 0;

    private WebSocketClient mWebSocketClient = null;
    private String lastId = null;
    private static DiagnosisManagement sInstance;
    private static Context mContext = null;
    private List<ICmdHandler> cmdHandlers = null;

    private DiagnosisManagement() {
        cmdHandlers = new ArrayList<>();
    }

    private void baseCmdHandleInit() {
        LogcatEndCmdImpl.getInstance().init();
        LogcatStartCmdImpl.getInstance().init();
        UploadLogCmdImpl.getInstance().init(mContext);
        SetLogFilterCmdImpl.getInstance().init(mContext);
    }

    public static DiagnosisManagement getInstance() {
        if (sInstance == null) {
            synchronized (DiagnosisManagement.class) {
                if (sInstance == null) {
                    sInstance = new DiagnosisManagement();
                }
            }
        }
        return sInstance;
    }

    public void init(Context ctx, String filter, String websocketUrl, String uploadLogUrl, int maxsize, int timeout, String devId) {
        Logger.setLogLevel(Logger.LOG_LEVEL_DEBUG);
        if (ctx == null) {
            Logger.e(TAG, "init error: Context is null.");
            return;
        }
        mContext = ctx;
        FileUtils.checkSuPermissions(mContext);
        baseCmdHandleInit();
        setRDConfig(filter, websocketUrl, uploadLogUrl, maxsize, timeout, devId);
        UploadLogManager.getInstance(mContext).init();
        WebSocketUtil.getInstance(mContext).startReconnect();
    }

    /**
     * 逆初始化
     */
    public void uninit() {
        stopLog();
    }

    /**
     * 设置配置
     *
     * @param filter       日志过滤条件
     * @param websocketUrl websocket服务器地址
     * @param uploadLogUrl 日志上传地址
     * @param maxsize      日志大小上限
     * @param timeout      命令执行超时时间
     * @param devId        设备id
     * @return
     */
    public Boolean setRDConfig(String filter, String websocketUrl, String uploadLogUrl, int maxsize, int timeout, String devId) {
        RDConfig.getInstance().setConfig(filter, websocketUrl, uploadLogUrl, maxsize, timeout, devId);
        return true;
    }

    /**
     * 开始收集日志
     */
    public void startLog() {
        LogCollectionManager.getInstance(mContext).startLog();
    }

    /**
     * 停止收集日志
     */
    public void stopLog() {
        LogCollectionManager.getInstance(mContext).stopLog();
    }

    /**
     * websocket接受者
     *
     * @param client
     * @param message 消息
     */
    public void onMessageReceived(WebSocketClient client, String message) {
        if (client == null || TextUtils.isEmpty(message)) {
            Logger.e(TAG, "onMessageReceived message: is null");
            return;
        }

        try {
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
        } catch (Exception e) {
            Logger.e(TAG, "onMessageReceived error: " + e.toString());
        }
    }

    /**
     * 自定义命令接口
     */
    public interface ICmdHandler {
        //命令名称
        String getCmdName();

        //命令处理方法
        void cmdHandler(String id, String command);
    }

    /**
     * 添加自定义命令
     *
     * @param cmdhander
     */
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
                        String[] cmd = command.split(" ");
                        for (ICmdHandler cmdhandler : cmdHandlers) {
                            if (cmd[0].equals(CmdConstant.CMD_AM)) {
                                //handleAndroidCmd(id, command);
                                return;
                            } else if (cmd[0].equals(cmdhandler.getCmdName())) {
                                cmdhandler.cmdHandler(id, command);
                                return;
                            }
                        }
                        //执行命令
                        ShellCmdManager.getInstance().runShellCmd(id, command);
                    }
                } else {
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
}
