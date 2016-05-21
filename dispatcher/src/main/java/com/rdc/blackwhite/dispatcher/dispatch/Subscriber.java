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


    interface Wrapper {
        Subscriber get();
    }

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
    }
}
