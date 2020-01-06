package com.android.diagnosislibrary.module.websocket;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.android.diagnosislibrary.module.shellCmdManager.RunCommand;
import com.android.diagnosislibrary.utils.DevUtils;
import com.android.diagnosislibrary.utils.JsonUtil;
import com.android.diagnosislibrary.utils.Logger.Logger;

import org.java_websocket.client.WebSocketClient;

import java.util.List;

public class WebMsgListener {
    private static final String TAG = "MsgListener";
    private static final int MSG_PHONE_INIT = 1;
    private static Handler mHandler = null;

    private static Handler sHandler = null;
    private static HandlerThread sHandlerThread = null;
    private static final int PARCE_SHELL_CMD = 0;


    private static long mDeltatime = 0;

    private static long mLastSyncWebTime = 0;

    private static String lastHearJson = "";

    private static int hearIndex = 0;

    private WebSocketClient mWebSocketClient = null;

    private String lastId = null;

    private String gameId = null;

    private static WebMsgListener sInstance;
    private static Context mContext = null;

    private WebMsgListener(Context ctx) {
        mContext = ctx;
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(android.os.Message msg) {
                mHandler.removeMessages(MSG_PHONE_INIT);
                mHandler.sendEmptyMessageDelayed(MSG_PHONE_INIT, 15000);
            }
        };

        sHandlerThread = new HandlerThread("parse-worker");
        sHandlerThread.start();
        sHandler = new Handler(sHandlerThread.getLooper()){
            @Override
            public void handleMessage(android.os.Message msg) {

            }
        };
    }

    public static WebMsgListener getInstance(Context ctx) {
        if (sInstance == null) {
            synchronized (WebMsgListener.class) {
                if (sInstance == null) {
                    if (ctx == null) {
                        return null;
                    }
                    sInstance = new WebMsgListener(ctx);
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

//        sHandler.sendEmptyMessage(PARCE_SHELL_CMD);
        Logger.d(TAG, "onMessageReceived message:" + message);
        WebSocketBody webSocketBody = JsonUtil.getInstance().fromJson(message, WebSocketBody.class);
        if (webSocketBody != null) {
            mWebSocketClient = client;
            if (mDeltatime == 0) {
//                syncWebTime();
            }

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


    private void parse(String id, String msg) {
        try {
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
                    Logger.d(TAG, "parse: handleDiagnoseMessage ");
                    handleDiagnoseMessage(id, msg);
                } else if (messageBody.getAction() != null) {
                    Logger.d(TAG, "parse: handleMessageByAction ");
//                    handleMessageByAction(id, msg);
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

//    private static boolean powerOn = true;
//    private void handleMessageByAction(final String id, String msg) {
//        if(TextUtils.isEmpty(msg)){
//            return;
//        }
//
//        try {
//            final MessageBody messageBody = JsonUtil.getInstance().fromJson(msg, MessageBody.class);
//            String param = messageBody.getParam();
//            Logger.d(TAG, " handleMessageByAction getAction: " + messageBody.getAction());
//            if(ACTION_CHECK.equals(messageBody.getAction())){
//                HashMap<String, String> params = SKTextUtil.getString2Map(param, "&", "=");
//                if(params != null) {
//                    String type = params.get(MsgConstant.KEY_TYPE);
//                    if(MsgConstant.CMD_START.equals(type)){
//                        Logger.d(TAG, "start check");
//                        LogUtil.startCheck();
//                        CheckUtil.check(URL_10079);
//                        CheckUtil.check(URL_10090);
//                        sendResponse(id, messageBody);
//                    } else if(MsgConstant.CMD_STOP.equals(type)) {
//                        String code = params.get(MsgConstant.KEY_CODE);
//                        Logger.d(TAG, "stop check, order code " + code);
//                        if(!TextUtils.isEmpty(code)) {
//                            Logger.stopCheck();
//                            if(log != null) {
//                                Logger.d(TAG, "log is ready, start to upload");
//                                DeviceInfo info = new DeviceInfo();
//                                info.setAppVersion(DeviceUtil.getAppVersion(BaseApplication.getContext(), BaseApplication.getContext().getPackageName()));
//                                info.setMac(TextUtils.isEmpty(DeviceUtil.getMac())?DeviceUtil.DEFAULT_MAC:DeviceUtil.getMac());
//                                info.setBrand(DeviceUtil.getBrand());
//                                info.setModel(DeviceUtil.getMode());
//                                info.setSystemVersion(DeviceUtil.getSDK() + "");
//                                info.setTerminalType(DeviceUtil.getTerminalType());
//                                info.setSerialNumber(DeviceUtil.getSerialNumber());
//                                info.setHardwareVersion(DeviceUtil.getHardwareNumber());
//                                info.setSoftwareVersion(DeviceUtil.getSoftwareNumber());
//                                info.setAppkey(PackageUtil.getAppkey());
//
//                                if(CommonUtil.isMainProcess()) {
//                                    try {
//                                        TVLiveService.iUtilInterface.postDeviceLog(JsonUtil.getInstance().toJson(info), code, log.getAbsolutePath());
//                                    } catch (RemoteException e) {
//                                        Logger.e(TAG, e.getMessage());
//                                    }
//                                } else {
//                                    UMSPostManager.postDeviceLog(info, code, log);
//                                }
//                            } else {
//                                Logger.d(TAG, "log is not ready");
//                            }
//                            sendResponse(id, messageBody);
//                        }
//                    }
//                }
//            }else if(MsgConstant.ACTION_EASYAPP.equals(messageBody.getAction())){
//                Logger.d(TAG, " easyapp param : "+param);
//
//                String cmd = "";
//                if(!param.contains("appType") && !param.contains("appUrl")){
//                    cmd = AmUtils.getHiddenCmd(param);
//                }else{
//                    HashMap<String, String> params = SKTextUtil.getString2Map(param, ",", ":");
//                    if(params != null) {
//                        String appType = params.get("appType");
//                        String appUrl = params.get("appUrl");
//                        String alias = params.get("alias");
//                        String encrypt = params.get("encrypt");
//                        String packageName = alias.replace(".apk", "");
//
//                        Logger.d(TAG, " easyapp appUrl : "+ CommonUtil.getFromBase64(appUrl));
//                        cmd = AmUtils.getHiddenCmd(CommonUtil.getFromBase64(appUrl));
//                    }
//                }
//
//                Logger.d(TAG, " easyapp cmd : "+ cmd);
//                if(!TextUtils.isEmpty(cmd)){
//                    AmUtils.execCmd(BaseApplication.getContext(),cmd,"#");
//                }
//            }else if(MsgConstant.ACTION_KEYEVENT.equals(messageBody.getAction())){
//                HashMap<String, String> params = SKTextUtil.getString2Map(param, "&", "=");
//                if(params != null) {
//                    String keyCode = params.get("keycode");
//                    Logger.d(TAG, " keyevent keyCode : "+ keyCode);
//                    if(keyCode.equals("26")) {// TODO 待机键临时方案
//                        String cmd = "";
//                        if(powerOn) {
//                            cmd = "hdmi_stop";
//                            powerOn = false;
//                        } else {
//                            cmd = "hdmi_start";
//                            powerOn = true;
//                        }
//                        XMPPCommand xmppCommand = new XMPPCommand(cmd, 10);
//                        xmppCommand.start();
//                    } else {
//                        ControlManager.getInstance().doKeyAction(Integer.parseInt(keyCode), "");
//                    }
//                }
//            }else if(MsgConstant.ACTION_EASYGET.equals(messageBody.getAction())){
//                String url = "http://"+CommonUtil.getIpAddress()+":"+ Constants.SERVER_PORT+"/";
//                String requestUrl = url + param;
//
//                if(!TextUtils.isEmpty(param)  && param.contains(Constants.WebServer.ACTION_HEARTBEAT)){
//                    if(hearIndex < 15 && !TextUtils.isEmpty(lastHearJson)){
//                        try {
//                            Logger.d(TAG, " -- send lastHearJson : "+lastHearJson);
//                            WebSocketBody webSocketBody = new WebSocketBody();
//                            webSocketBody.setTo(id);
//                            webSocketBody.setFrom(DeviceUtil.getSerialNumber());
//                            webSocketBody.setMessage(lastHearJson);
//
//                            if(mWebSocketClient != null){
//                                mWebSocketClient.send(JsonUtil.getInstance().toJson(webSocketBody));
//                            }
//                            hearIndex++;
//
//                            return;
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//
//                final String httpParam = param;
//                Logger.d(TAG, " - easyget requestUrl : "+requestUrl);
//
//                UtilCallback callback = new UtilCallback(){
//                    @Override
//                    public void onSuccess(String response) throws RemoteException {
//                        Logger.d(TAG, " - easyget onResponse s : "+response);
//                        try {
//                            MessageBody resp = new MessageBody();
//                            resp.setErrInfo("");
//                            resp.setResult("0");
//                            resp.setTimestamp(System.currentTimeMillis());
//                            resp.setType(1);
//                            resp.setCmd(messageBody.getCmd());
//                            resp.setAction(messageBody.getAction());
//                            resp.setParam(response);
//
//                            final String hearJson = JsonUtil.getInstance().toJson(resp);
//                            if(mWebSocketClient != null){
//                                sHandler.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        try {
//                                            WebSocketBody webSocketBody = new WebSocketBody();
//                                            webSocketBody.setTo(id);
//                                            webSocketBody.setFrom(DeviceUtil.getSerialNumber());
//                                            webSocketBody.setMessage(hearJson);
//
//                                            if(mWebSocketClient != null){
//                                                mWebSocketClient.send(JsonUtil.getInstance().toJson(webSocketBody));
//                                            }
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                });
//                            }
//
//                            if(!TextUtils.isEmpty(httpParam)  && httpParam.contains(Constants.WebServer.ACTION_HEARTBEAT)){
//                                lastHearJson = hearJson;
//                                hearIndex = 0;
//                            }
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
//                    }
//                };
//
//                if(CommonUtil.isMainProcess()) {
//                    try {
//                        TVLiveService.iUtilInterface.simpleGet(requestUrl, callback);
//                    } catch (Exception e) {
//                        Logger.e(TAG, e.getMessage());
//                    }
//                } else {
//                    PubPostManager.simpleGet(requestUrl, null, callback);
//                }
//
//            } else if(com.seecloud.gamecenter.utils.Constants.ACTION_GAME.equals(messageBody.getAction())){
//                if(TextUtils.isEmpty(msg)){
//                    return;
//                }
//                MessageBody gameBody = JsonUtil.getInstance().fromJson(msg, MessageBody.class);
//                GameParam gameParam = JsonUtil.getInstance().fromJson(gameBody.getParam(),GameParam.class);
//                if(gameParam == null){
//                    handleElseMessage(id,msg);
//                    return;
//                }
//                String action = gameParam.getAction();
//                if(com.seecloud.gamecenter.utils.Constants.ACTION_PHONE_INIT.equals(action) && null != mWebSocketClient){
//                    mHandler.removeMessages(MSG_PHONE_INIT);
//                    Logger.i(TAG,"gameChat初始化成功!");
//                } else if(com.seecloud.gamecenter.utils.Constants.ACTION_PHONE_DATA.equals(action)){
//                    MsgCenter.getInstance().handlePhoneData(gameParam);
//                }
//            }else if(MsgConstant.ACTION_EASYP2P.equals(messageBody.getAction())){
//                HashMap<String, String> params = SKTextUtil.getString2Map(param, "&", "=");
//                if(params != null) {
//                    String eventCode = params.get("code");
//                    Logger.d(TAG, " easyP2P eventCode : "+ eventCode);
//                }
//            } else if(MsgConstant.ACTION_MESSAGE.equals(messageBody.getAction())) {
//                HashMap<String, String> params = SKTextUtil.getString2Map(param, "&", "=");
//                if(params != null) {
//                    // 0 节目单  1 应用  2 活动 3系统消息 4用户行为消息 5节目
//                    String type = params.get("type");
//                    if ("0".equals(type) || "5".equals(type)) {
//                        final String text = params.get("text");
//                        SPUtil.putString(KEY_SYSTEM_MESSAGE, text);
//                    }
//                }
//            } else if(MsgConstant.ACTION_EASYINSTRUCTION.equals(messageBody.getAction())){
//                if(!TextUtils.isEmpty(messageBody.getAppId())){
//                    MultiControlService.multiControl(id, messageBody.getAppId(), messageBody.getInstruction(), messageBody.getParam());
//                }else{
//                    if("getCurAppInfo".equals(messageBody.getInstruction())){
//                        String packageName = CommonUtil.getForegroundApp();
//                        RunningAppInfo appInfo = new RunningAppInfo();
//                        appInfo.setRet(0);
//                        appInfo.setAppId(MultiControlService.getAppIdByPackageName(packageName));
//                        appInfo.setPackageName(packageName);
//                        appInfo.setAppName(PackageUtil.getAppName(packageName));
//
//                        sendEasyNotify("getCurAppInfo", new Gson().toJson(appInfo), id);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    RunCommand mRunCommand = null;

    private void handleDiagnoseMessage(String id, String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        try {
            MessageBody messageBody = JsonUtil.getInstance().fromJson(msg, MessageBody.class);
            List<String> commands = messageBody.getCommand();

            for (int i = 0; i < commands.size(); i++) {
                String command = commands.get(i);
                if (command.startsWith(MsgConstant.CMD_AM)) {
                    Logger.d(TAG, "handleAndroidCmd =====  ");
//                    handleAndroidCmd(id, command);
                } else if (command.startsWith(MsgConstant.CMD_LOGCAT_END)) {
                    //结束logcat
                    Thread.sleep(500);
                    if (mRunCommand != null) {
                        mRunCommand.terminal();
                    }
                } else if (command.startsWith(MsgConstant.CMD_CUSTOM)) {
                    Logger.d(TAG, "handleCustomCmd =====  ");
                    //抓取日志
                    handleCustomCmd(id, command);
                } else {
                    //执行命令
                    Logger.d(TAG, "handleShellCmd =====  ");
                    if (command.startsWith(MsgConstant.CMD_LOGCAT_BEGIN)) {
                        command = "logcat  | grep -e \"VOS\" -e \"AndroidRuntime\" -e \"System.err\"";
                    }

                    if (mRunCommand != null) {
                        mRunCommand.terminal();
                        mRunCommand = null;
                    }
                    mRunCommand = handleShellCmd(id, messageBody, command);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    /**
     * 自定义日志查看命令
     */
    private void handleLogCmd(String id, String command) {
        if (MsgConstant.CMD_LOGCAT_BEGIN.equals(command)) {
            startLog(id);
        } else if (MsgConstant.CMD_LOGCAT_END.equals(command)) {
            stopLog(0);
        }
    }

    /**
     * 自定义命令
     */
    private void handleCustomCmd(String id, String command) {
        if (command.startsWith(MsgConstant.CMD.UPGRADE.getName())) {
            Logger.d(TAG, "handleCustomCmd: MsgConstant.CMD.UPGRADE");
            String[] params = command.split("\\s+");
            if (params != null && params.length > 1) {
                String url = params[1];
                CheckUtil.check(mContext, CheckUtil.buildUpgradeUrl(url));
            }
        } else if (command.startsWith(MsgConstant.CMD.UPLOAD_LOG.getName())) {
            Logger.d(TAG, "handleCustomCmd: MsgConstant.CMD.UPLOAD_LOG");
//            LogUtil.stopCheck();
//            LogcatStroreManager.getInstance().postLogInfo();
        } else if (command.startsWith(MsgConstant.CMD.CHECK_PORT.getName())) {
            Logger.d(TAG, "handleCustomCmd: MsgConstant.CMD.CHECK_PORT");
            CheckUtil.check(mContext, CheckUtil.URL_10090);
            CheckUtil.check(mContext, CheckUtil.URL_10079);
        }
    }

    private void startLog(final String id) {
        stopLog(0);

//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                IAgentInterface.Stub logCallback = new IAgentInterface.Stub() {
//                    @Override
//                    public void onPrint(String line) {
//                        sendDiagnoseResponse(line, id);
//                    }
//                };
//            }
//        });

        stopLog(MsgConstant.DELAY_CUSTOM_CMD_DEFAULT);
    }

    private void stopLog(long delayed) {
        mHandler.removeCallbacks(stopLogRunnable);
        mHandler.postDelayed(stopLogRunnable, delayed);
    }

    static Runnable stopLogRunnable = new Runnable() {
        @Override
        public void run() {
            try {
//                Logger.removeLogCallback();
            } catch (Exception e) {
                Logger.e(TAG, e.getMessage());
            }
        }
    };

    /**
     * linux 标准命令
     */
    private RunCommand handleShellCmd(@NonNull final String id, MessageBody messageBody, String command) {
        RunCommand runCommand = new RunCommand(command, messageBody.getTimeout());
        runCommand.setCallBack(new RunCommand.CommandCallBack() {
            @Override
            public void sendResult(String line) {
                Log.d(TAG, "sendResult: "+line);
                sendDiagnoseResponse(line, id);
            }
        });
        runCommand.start();
        return runCommand;
    }

    private void sendDiagnoseResponse(String line, String id) {
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
                Logger.d(TAG, "sendDiagnoseResponse: " + JsonUtil.getInstance().toJson(webSocketBody).toString());
                mWebSocketClient.send(JsonUtil.getInstance().toJson(webSocketBody));
            }
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
        }
    }

//    private void sendResponse(String id, MessageBody messageBody) {
//        try {
//            LogUtil.d(TAG, "sendResponse " + messageBody.getAction());
//            MessageBody result = MessageBody.build();
//            result.setAction(messageBody.getAction());
//
//            WebSocketBody webSocketBody = new WebSocketBody();
//            webSocketBody.setTo(id);
//            webSocketBody.setFrom(DeviceUtil.getSerialNumber());
//            webSocketBody.setMessage(JsonUtil.getInstance().toJson(result));
//
//            if(mWebSocketClient != null){
//                mWebSocketClient.send(JsonUtil.getInstance().toJson(webSocketBody));
//            }
//        } catch (Exception e) {
//            LogUtil.e(TAG, e.getMessage());
//        }
//    }

//    public void sendEasyNotify(String instruction, String param, String jid){
//        LogUtil.d(TAG, " sendEasyNotify instruction : "+instruction+" ;  param : "+param+"; id : "+jid);
//
//        MessageBody messageBody = new MessageBody();
//        messageBody.setErrInfo("");
//        messageBody.setResult("0");
//        messageBody.setTimestamp(System.currentTimeMillis());
//        messageBody.setType(1);
//        messageBody.setInstruction(instruction);
//        messageBody.setAction(MsgConstant.ACTION_EASYNOTIFY);
//        messageBody.setParam(Uri.encode(param));
//
//        WebSocketBody webSocketBody = new WebSocketBody();
//        webSocketBody.setMessage(JsonUtil.getInstance().toJson(messageBody));
//        webSocketBody.setFrom(DeviceUtil.getSerialNumber());
//
//        if(!TextUtils.isEmpty(jid)){
//            webSocketBody.setTo(jid);
//            if(mWebSocketClient != null){
//                try {
//                    LogUtil.d(TAG, " send msg to : "+jid + "; msg : "+JsonUtil.getInstance().toJson(webSocketBody));
//                    mWebSocketClient.send(JsonUtil.getInstance().toJson(webSocketBody));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }else{
//            try {
//                if(mWebSocketClient != null){
//                    String requestUrl = SPUtil.getString(SPConstants.KEY_UMS_URL, "")+"/queryBindingDeviceList?id="+ CommonUtil.getProperties("seecloud.stb.smartId", "")+"&type=2";
//                    LogUtil.d(TAG, " sendEasyNotify request url : "+requestUrl);
//                    OkHttpClient client = new OkHttpClient();
//                    Request request = new Request.Builder().url(requestUrl).build();
//                    client.newCall(request).enqueue(new Callback() {
//                        @Override
//                        public void onFailure(Call call, IOException e) {
//
//                        }
//
//                        @Override
//                        public void onResponse(Call call, Response response) throws IOException {
//                            DeviceListInfoJson deviceListInfoJson = JsonUtil.getInstance().fromJson(response.body().string(), DeviceListInfoJson.class);
//                            if(deviceListInfoJson != null && "0".equals(deviceListInfoJson.getRet())){
//                                List<BindedDevcieInfo> bindedInfos = deviceListInfoJson.getDevices();
//
//                                WebSocketBody webSocketBody = new WebSocketBody();
//                                if(bindedInfos != null && bindedInfos.size() > 0){
//                                    for(int i = 0; i < bindedInfos.size(); i++){
//                                        try {
//                                            String jid = bindedInfos.get(i).getUserId();
//                                            webSocketBody.setTo(jid);
//                                            LogUtil.d(TAG, " send msg to : "+jid + "msg : "+JsonUtil.getInstance().toJson(webSocketBody));
//
//                                            mWebSocketClient.send(JsonUtil.getInstance().toJson(webSocketBody));
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    });
//                }
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }
//    }

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

//    public void sendEasyResponse(int cmd, String param){
//        if(mWebSocketClient != null){
//            try{
//                LogUtil.d(TAG, " sendEasyResponse cmd : "+cmd+" ;  json : "+param);
//
//                MessageBody msgBody = new MessageBody();
//                msgBody.setErrInfo("");
//                msgBody.setResult("0");
//                msgBody.setTimestamp(System.currentTimeMillis());
//                msgBody.setType(1);
//                msgBody.setCmd(cmd);
//                msgBody.setAction(MsgConstant.ACTION_EASYRESPONSE);
//                msgBody.setParam(param);
//
//                WebSocketBody webSocketBody = new WebSocketBody();
//                webSocketBody.setFrom(DeviceUtil.getSerialNumber());
//                webSocketBody.setTo(lastId);
//                webSocketBody.setMessage(JsonUtil.getInstance().toJson(msgBody));
//                if(mWebSocketClient != null){
//                    mWebSocketClient.send(JsonUtil.getInstance().toJson(webSocketBody));
//                }
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        } else {
//            LogUtil.e(TAG,"mWebSocketClient is null!");
//        }
//    }

//    public void sendGameMessage(String msg){
//        if(mWebSocketClient != null){
//            try {
//                LogUtil.i(TAG,"sendGameMessage : " + msg);
//
//                WebSocketBody webSocketBody = new WebSocketBody();
//                webSocketBody.setFrom(DeviceUtil.getSerialNumber());
//                webSocketBody.setTo(gameId);
//                webSocketBody.setMessage(JsonUtil.getInstance().toJson(msg));
//
//                mWebSocketClient.send(JsonUtil.getInstance().toJson(webSocketBody));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else {
//            LogUtil.e(TAG,"mWebSocketClient is null!");
//        }
//    }

//    public void createGameChat(String gameId){
//        if (TextUtils.isEmpty(gameId)) {
//            return;
//        }
//        this.gameId = gameId;
//
//        mHandler.removeMessages(MSG_PHONE_INIT);
//        mHandler.sendEmptyMessage(MSG_PHONE_INIT);
//    }
}
