package com.rdc.blackwhite.dispatcher.dispatch;

/**
 * Created by slimxu on 2016/5/20.
 * 分发器
 */
public interface Dispatcher {

    String DEFAULT_GROUP_NAME = "default_group";

    void registerSubscriber(String group,Subscriber subscriber);

    void unRegisterSubscriber(Subscriber subscriber);

    void dispatch(Dispatchable dispatchable);

    void dispatch(String group,Dispatchable dispatchable);

}
