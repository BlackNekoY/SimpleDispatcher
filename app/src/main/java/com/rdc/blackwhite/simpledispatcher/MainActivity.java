package com.rdc.blackwhite.simpledispatcher;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.rdc.blackwhite.dispatcher.dispatch.BaseEvent;
import com.rdc.blackwhite.dispatcher.dispatch.Dispatchable;
import com.rdc.blackwhite.dispatcher.dispatch.Dispatcher;
import com.rdc.blackwhite.dispatcher.dispatch.Dispatchers;
import com.rdc.blackwhite.dispatcher.dispatch.Subscriber;
import com.rdc.blackwhite.dispatcher.dispatch.UIBaseEventReceiver;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends BaseActivity implements View.OnClickListener{

    private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.btn);
        button.setOnClickListener(this);
    }

    private void doSomething() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ButtonEvent event = new ButtonEvent();
                //模拟从网络上获取显示的字符串
                try {
                    Thread.sleep(1000);
                    //模拟获取成功
                    String str = "获取的字符串";
                    event.str = str;
                } catch (InterruptedException e) {
                    //模拟获取失败
                    event.errorInfo.errorCode = 10086;
                    event.errorInfo.errorMsg = "网络异常";
                } finally {
                    Dispatchers.get().dispatch(event);
                }
            }
        }).start();
    }

    @Override
    protected void onCreateSubscribers(Map<String, Subscriber> subscribers) {
        subscribers.put("",new ButtonEventReceiver(this));
    }

    @Override
    public void onClick(View v) {
        doSomething();
    }

    public void setButtonText(String str) {
        button.setText(str);
    }

    static class ButtonEvent extends BaseEvent {
        public String str;
    }

    static class ButtonEventReceiver extends UIBaseEventReceiver<MainActivity,ButtonEvent> {

        public ButtonEventReceiver(@NonNull MainActivity uiClass) {
            super(uiClass);
        }

        @Override
        public void onSuccess(MainActivity uiClass, ButtonEvent event) {
            uiClass.setButtonText(event.str);
        }

        @Override
        public void onError(MainActivity uiClass, ButtonEvent event) {
            BaseEvent.ErrorInfo info = event.errorInfo;
            Toast.makeText(uiClass,info.errorMsg,Toast.LENGTH_SHORT).show();
        }
    }

}
