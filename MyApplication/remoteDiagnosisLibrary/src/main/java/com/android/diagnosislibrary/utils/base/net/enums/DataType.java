package com.android.diagnosislibrary.utils.base.net.enums;

public enum DataType {
    JSON(0), XML(1), A7(2);

    private int value;

    private DataType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DataType getDataType(int value) {
        for (DataType type : DataType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return DataType.JSON;
    }
}
