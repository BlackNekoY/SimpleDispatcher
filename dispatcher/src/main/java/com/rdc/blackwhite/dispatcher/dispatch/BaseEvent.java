package com.rdc.blackwhite.dispatcher.dispatch;

/**
 * 能被UIBaseEventReceiver接收的BaseEvent，必须继承此类
 * 携带了一些状态参数,状态由发起请求回包后设置
 */
public class BaseEvent implements Dispatcher.Event {

    /**
     * 错误信息，如果isFailed为true，则此项必须有内容
     */
    public ErrorInfo errorInfo = new ErrorInfo();
    public boolean isFailed;

    public class ErrorInfo {
        public int errorCode;
        public String errorMsg;
    }
}
