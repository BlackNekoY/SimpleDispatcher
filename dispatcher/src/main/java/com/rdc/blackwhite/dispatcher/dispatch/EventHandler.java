package com.rdc.blackwhite.dispatcher.dispatch;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by slimxu on 2016/5/22.
 */
public abstract class EventHandler<EVENT> {

    private static final String TAG = "EventHandler";
    private final int MSG_START = 0;
    private final int MSG_POST = 1;

    private Handler mHandler;
    private Queue<EVENT> mQueue;

    private boolean mIsStop;

    public EventHandler(Looper looper) {
        mHandler = new InnerHandler(looper);
        mQueue = new LinkedBlockingQueue<>();
    }

    void start() {
        mIsStop = false;
        //开始循环事件队列，执行线程为HandlerThread线程
        mHandler.sendEmptyMessage(MSG_START);
    }

    void stop() {
        mIsStop = true;
    }

    void enqueue(EVENT event) {
        mQueue.add(event);
    }

    private void startPosting() {
        while (!mIsStop) {
            EVENT event = mQueue.poll();
            if (event != null) {
                //通知外界分发事件
                //TODO 如何处理分发时间过长的问题
                handleEvent(event);
            }
        }
        Log.d(TAG,"EventHandler stop!");
    }

    /**
     * 子类处理Event方法
     * @param event
     */
    abstract void handleEvent(EVENT event);

    private class InnerHandler extends Handler {
        public InnerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START:
                    startPosting();
                    break;
            }
        }
    }
}
