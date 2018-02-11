package com.jsdiuf.jsdiuf.util;

import com.jsdiuf.jsdiuf.constant.TimerFormatterEnum;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author weicc
 * @create 2018-01-25 11:16
 **/
public class TimeUtils {

    /**
     * get current time ,the format is HH:mm:ss
     * @return
     */
    public static String getNowTime() {
        return new SimpleDateFormat(TimerFormatterEnum.H_M_S.getValue()).format(new Date());
    }

    /**
     * get current time ,the format is HH:mm:ss
     * @return
     */
    public static String getNowTimeMillions(){
        return new SimpleDateFormat(TimerFormatterEnum.H_M_MS.getValue()).format(new Date());
    }
}
