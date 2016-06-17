package com.rdc.blackwhite.dispatcher.util;

public class AssertUtil {

    public static <T> T checkNotNull(T obj) {
        if(obj == null)
            throw new NullPointerException();

        return obj;
    }
}
