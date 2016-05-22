package com.rdc.blackwhite.dispatcher.dispatch;

import android.support.annotation.NonNull;

/**
 * Created by slimxu on 2016/5/22.
 * <p/>
 * 这里将onEvent的回调分成两个，onSuccess和onError，方便UI层处理事件
 */
public abstract class UIBaseEventReceiver<T, EVENT extends BaseEvent> extends UIEventReceiver<T, EVENT> {

    public UIBaseEventReceiver(@NonNull T uiClass) {
        super(uiClass);
    }

    @Override
    final void onEvent(@NonNull T t, @NonNull EVENT event) {
        if (event.isFailed) {
            onError(t, event);
        } else {
            onSuccess(t, event);
        }
    }

    public abstract void onSuccess(T uiClass, EVENT event);

    public abstract void onError(T uiClass, EVENT event);
}
