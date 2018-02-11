package com.jsdiuf.jsdiuf.constant;

/**
 * @author weicc
 * @create 2018-01-30 17:03
 **/
public enum TimerFormatterEnum {
    H_M_S("HH:mm:ss"),
    H_M_MS("HH:mm:ss:SSS"),
    Y_M_D_H_M_S("yyyy-MM-dd HH:mm:ss");

    TimerFormatterEnum(String format) {
        this.value = format;
    }

    private String value;

    public String getValue() {
        return value;
    }
}
