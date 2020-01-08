package com.android.diagnosislibrary.utils.base.framework.enums;

public enum TerminalType {
    UNKNOWN(-1), All(0), TV(1), PC(2), PHONE(3), PAD(4) ;

    private int value;

    private TerminalType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TerminalType getTerminalType(int value) {
        for (TerminalType type : TerminalType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return TerminalType.TV;
    }
};