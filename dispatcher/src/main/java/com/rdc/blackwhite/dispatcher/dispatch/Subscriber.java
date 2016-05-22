package com.rdc.blackwhite.dispatcher.dispatch;

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
