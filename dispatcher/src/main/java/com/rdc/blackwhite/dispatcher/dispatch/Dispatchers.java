package com.rdc.blackwhite.dispatcher.dispatch;

import android.os.HandlerThread;

public class Dispatchers {

    private static final String TAG = "dispatch_thread";

    private static volatile Dispatcher mDispatcher;

    private Dispatchers(){}

    public static Dispatcher get() {
        if(mDispatcher == null) {
            synchronized (Dispatchers.class) {
                if(mDispatcher == null) {
                    HandlerThread handlerThread = new HandlerThread(TAG);
                    handlerThread.start();
                    mDispatcher = new DefaultDispatcher(handlerThread.getLooper());
                }
            }
        }
        return mDispatcher;
    }

}
