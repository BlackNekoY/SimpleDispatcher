package com.rdc.blackwhite.dispatcher.dispatch;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by slimxu on 2016/5/20.
 * 监听事件的观察者
 */
public interface Subscriber {
    void accept(List<Class<? extends Dispatchable>> acceptClass);

    void handleDispatch(Dispatchable dispatchable);


    /**
     * 能够控制响应线程的Subscriber
     */
    abstract class LooperSubscriber extends Handler implements Subscriber {

        private final int MSG_HANDLE = 1;

        public LooperSubscriber(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE:
                    Dispatchable dispatchable = (Dispatchable) msg.obj;
                    onDispatch(dispatchable);
                    break;
            }
        }

        @Override
        public void handleDispatch(Dispatchable dispatchable) {
            //Handler.sendMessage的简洁形式
            Message.obtain(this,MSG_HANDLE,dispatchable).sendToTarget();
        }

        protected abstract void onDispatch(Dispatchable dispatchable);
    }



    /**
     * Subscriber的包装，便于以后扩展
     */
    interface Wrapper {
        Subscriber get();
    }

    /**
     * 默认的Subscriber包装
     */
    class DefaultWrapper implements Wrapper {

        private Subscriber mSubscriber;

        public DefaultWrapper(@NonNull Subscriber subscriber) {
            mSubscriber = subscriber;
        }

        @Override
        public Subscriber get() {
            return mSubscriber;
        }

        @Override
        public int hashCode() {
            return mSubscriber.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DefaultWrapper that = (DefaultWrapper) o;

            return mSubscriber != null ? mSubscriber.equals(that.mSubscriber) : that.mSubscriber == null;

        }
    }
}
