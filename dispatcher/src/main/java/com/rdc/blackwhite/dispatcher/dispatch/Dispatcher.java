package com.rdc.blackwhite.dispatcher.dispatch;

/**
 * 分发器
 */
public interface Dispatcher {

    String DEFAULT_GROUP_NAME = "default_group";

    void registerSubscriber(String group, Subscriber subscriber);

    void unRegisterSubscriber(Subscriber subscriber);

    void dispatch(Dispatchable dispatchable);

    void dispatch(String group, Dispatchable dispatchable);

    void dispatchDelay(Dispatchable dispatchable, long delayTime);

    void dispatchDelay(String group, Dispatchable dispatchable, long delayTime);

    /**
     * 可以分发的事件接口
     */
    interface Event extends Dispatchable {

    }

}
