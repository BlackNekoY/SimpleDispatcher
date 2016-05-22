package com.rdc.blackwhite.dispatcher.dispatch;

import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;

/**
 * 接收UI事件，因为要调用UI的方法，所以要多一个泛型参数表示
 * 采取弱引用防止内存泄漏
 *
 * @param <T> Context
 * @param <EVENT> 接收的事件类型
 */
public abstract class UIEventReceiver <T,EVENT extends Dispatchable> extends Subscriber.SingleEventSubscriber<EVENT> {

    protected WeakReference<T> mRef;

     UIEventReceiver(@NonNull T t) {
        mRef = new WeakReference<T>(t);
    }

    @Override
    final void onDispatch2(EVENT event) {
        T t = mRef.get();
        if(t != null) {
            onEvent(t,event);
        }
    }

    abstract void onEvent(@NonNull T t,@NonNull EVENT event);

}
