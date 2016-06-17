package com.rdc.blackwhite.dispatcher.dispatch;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

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
                //TODO 如何处理分发时间过长的问题，现在的处理方法是接收的Subscriber都为主线程修改UI的Subscriber，直接通过Handler投递到主线程去执行
                //所以这里不消耗时间
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
