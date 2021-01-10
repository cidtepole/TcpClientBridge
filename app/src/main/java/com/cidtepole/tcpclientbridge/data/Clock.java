package com.cidtepole.tcpclientbridge.data;

/**
 * Created by CidTepole on 24/01/2017.
 */
import android.text.format.Time;

/**
 * Clock utility.
 */
public class Clock {

    /**
     * Get current time in human-readable form.
     *
     * @return current time as a string.
     */
    public static String getNow() {
        Time now = new Time();
        now.setToNow();
        String sTime = now.format("%Y/%m/%d   %T");
        return sTime;
    }


    public static String getTime() {
        Time now = new Time();
        now.setToNow();
        String sTime = now.format("%T");
        return sTime;
    }

    /**
     * Get current time in human-readable form without spaces and special characters.
     * The returned value may be used to compose a file name.
     *
     * @return current time as a string.
     */
    public static String getTimeStamp() {
        Time now = new Time();
        now.setToNow();
        String sTime = now.format("%Y_%m_%d_%H_%M_%S");
        return sTime;
    }
}
