package com.android.diagnosislibrary.module.websocket;

public class MsgConstant {
    public static final String KEY_TYPE = "type";
    public static final String KEY_CODE = "code";

    public static final int DELAY_CUSTOM_CMD_DEFAULT = 3 * 60 * 1000;
    public static final int DELAY_PING_DEFAULT = 60;
    public static final int DELAY_RELOGIN_DEFAULT = 3 * 1000;

    public static final String CMD_START = "start";
    public static final String CMD_STOP = "stop";
    public static final String CMD_GETINFO = "getinfo";

    public static final String CMD_LOGCAT_BEGIN = "logcat_begin";
    public static final String CMD_LOGCAT_END = "logcat_end";

    public static final String CMD_GETPROP = "getprop";
    public static final String CMD_SETPROP = "setprop";
    public static final String CMD_AM = "am";

    public static final String CMD_CUSTOM = "custom_";

    public static final String ACTION_CHECK = "check";

    public static final String ACTION_EASYAPP = "easyapp";

    public static final String ACTION_KEYEVENT = "keyevent";

    public static final String ACTION_EASYGET = "easyget";

    public static final String ACTION_EASYRESPONSE = "easyresponse";

    public static final String ACTION_EASYP2P = "easyP2P";

    public static final String ACTION_MESSAGE = "message";

    public static final String ACTION_EASYINSTRUCTION = "easyinstruction";

    public static final String ACTION_EASYNOTIFY = "easynotify";

    public enum CMD {
        UPGRADE("custom_upgrade"), UPLOAD_LOG("custom_log"), CHECK_PORT("custom_port");

        private String name;

        public String getName() {
            return this.name;
        }

        CMD(String name) {
            this.name = name;
        }
    }
}
