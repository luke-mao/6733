package com.example.a6733.functions;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ouyangshen on 2017/9/14.
 */
public class DateUtil {
    public static String getNowDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(new Date());
    }

    public static String getNowTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        return sdf.format(new Date());
    }

}
