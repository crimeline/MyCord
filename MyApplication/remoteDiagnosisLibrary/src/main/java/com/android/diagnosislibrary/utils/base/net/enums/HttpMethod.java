package com.android.diagnosislibrary.utils.base.net.enums;

public enum HttpMethod {
    GET(0), POST(1);

    private int value;

    private HttpMethod(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static HttpMethod getHttpMethod(int value) {
        for (HttpMethod type : HttpMethod.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return HttpMethod.GET;
    }
}
