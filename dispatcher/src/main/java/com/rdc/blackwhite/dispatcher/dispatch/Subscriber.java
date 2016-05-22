package com.rdc.blackwhite.dispatcher.dispatch;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
     * 只接收一个事件的接收者，在主线程中执行
     * @param <EVENT>
     */
    abstract class SingleEventSubscriber<EVENT extends Dispatchable> extends LooperSubscriber {

        private Class mClazz;

        public SingleEventSubscriber() {
            super(Looper.getMainLooper());
            //通过这种方法可以获取当前泛型的class对象
            Type genType = this.getClass().getGenericSuperclass();
            Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
            mClazz = (Class) params[params.length - 1];
        }

        @Override
        public final void accept(List<Class<? extends Dispatchable>> acceptClass) {
            acceptClass.add(mClazz);
        }

        @Override
        protected final void onDispatch(Dispatchable dispatchable) {
            //这里将Dispatchable转换成接收的事件类型
            onDispatch2((EVENT) dispatchable);
        }

        abstract void onDispatch2(EVENT event);
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
