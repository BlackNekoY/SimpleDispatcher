package com.rdc.blackwhite.simpledispatcher;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.rdc.blackwhite.dispatcher.dispatch.Dispatchers;
import com.rdc.blackwhite.dispatcher.dispatch.Subscriber;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BaseActivity extends AppCompatActivity {

    protected Map<String,Subscriber> mSubscriberMap = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCreateSubscribers(mSubscriberMap);

        if(mSubscriberMap.size() != 0) {
            Set<Map.Entry<String,Subscriber>> set =  mSubscriberMap.entrySet();
            for(Map.Entry<String,Subscriber> entry : set) {
                String group = entry.getKey();
                Subscriber subscriber = entry.getValue();

                Dispatchers.get().registerSubscriber(group,subscriber);
            }
        }
    }

    protected void onCreateSubscribers(Map<String,Subscriber> subscribers){

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mSubscriberMap.size() != 0) {
            Set<Map.Entry<String,Subscriber>> set =  mSubscriberMap.entrySet();
            for(Map.Entry<String,Subscriber> entry : set) {
                Subscriber subscriber = entry.getValue();
                Dispatchers.get().unRegisterSubscriber(subscriber);
            }
        }
        mSubscriberMap.clear();
    }
}
