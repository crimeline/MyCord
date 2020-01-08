package com.android.diagnosislibrary.utils.base.framework.enums;


public enum ServiceType {
    SERVER_CONFIG("SERVER_CONFIG"),         // serverConfig: 服务器地址
    APP_UPDATE_URL("APP_UPDATE_URL"),       // 终端升级服务器地址
    EPG_URL("EPG_URL"),                     // EPG服务器地址:
    IUC_URL("IUC_URL"),                     //  IUC服务器地址:
    APP_URL("APP_URL"),                     //  APP应用商店服务器地址
    FUC_URL("FUC_URL"),                     //  用户中心地址
    DEPG_URL("DEPG_URL"),                   //  深度EPG服务器地址
    IDC_URL("IDC_URL"),                     // 数据采集上报接口
    UMS_URL("UMS_URL"),                     // 设备绑定服务地址
    YNM_URL("YNM_URL"),                     // 网管服务器地址
    QRC_REQUEST("QRC_REQUEST"),             //二维码下载页面

    xmpp_url("xmpp_url"),                   // 消息引擎UDP通信地址
    xmpp_domain("xmpp_domain");             //Xmpp中JID的domain（域名）部分

    private String value;

    private ServiceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ServiceType getServiceType(String value) {
        for (ServiceType type : ServiceType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return ServiceType.FUC_URL;
    }
}
