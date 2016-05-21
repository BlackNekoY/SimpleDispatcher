package com.rdc.blackwhite.dispatcher.util;

/**
 * Created by slimxu on 2016/5/21.
 */
public class AssertUtil {

    public static <T> T checkNotNull(T obj) {
        if(obj == null)
            throw new NullPointerException();

        return obj;
    }
}
