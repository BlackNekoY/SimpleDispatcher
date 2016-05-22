package com.rdc.blackwhite.dispatcher.dispatch;

import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.rdc.blackwhite.dispatcher.util.AssertUtil;

import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by slimxu on 2016/5/20.
 */
public class DefaultDispatcher implements Dispatcher {

    private static final String TAG = "DefaultDispatcher";
    /**
     * 用于保存各个分发处理器
     * key：使用makekey获取
     * value:每一组的Subscriber
     */
    private Map<SubscriberKey, CopyOnWriteArraySet<Subscriber.Wrapper>> mSubscriberMap = new ConcurrentHashMap<>(10);

    /**
     * 用于保存每个分发处理的分组
     * key:Subscriber的hashcode
     * value:该Subscriber所在的所有分组
     */
    private Map<Integer, CopyOnWriteArraySet<String>> mGroupsBySubscriber = new ConcurrentHashMap<>(10);

    /**
     * 使用ThreadLocal来确保线程唯一
     */
    private ThreadLocal<PostingThreadState> mCurrentPostingState = new ThreadLocal<PostingThreadState>() {
        @Override
        protected PostingThreadState initialValue() {
            return new PostingThreadState();
        }
    };

    @Override
    public void registerSubscriber(String group, Subscriber subscriber) {
        AssertUtil.checkNotNull(subscriber);
        if (TextUtils.isEmpty(group)) {
            group = DEFAULT_GROUP_NAME;
        }
        insertSubscriber(group, new Subscriber.DefaultWrapper(subscriber));
    }

    @Override
    public void unRegisterSubscriber(Subscriber subscriber) {
        AssertUtil.checkNotNull(subscriber);

        List<Class<? extends Dispatchable>> acceptList = new ArrayList<>(2);

        subscriber.accept(acceptList);
        if (acceptList.size() == 0) {
            return;
        }
        CopyOnWriteArraySet<String> groups = mGroupsBySubscriber.get(subscriber.hashCode());

        //取出每一个 Class 和 group，合成SubscriberKey，去remove
        for (Class<? extends Dispatchable> acceptClass : acceptList) {
            if (groups != null) {
                for (String group : groups) {
                    removeSubscriber(group, acceptClass, subscriber);
                }
            } else {
                //一般来讲取出的groups不会为null，因为register的时候有判断，没有传group就会使用DEFAULT_GROUP_NAME
                removeSubscriber(DEFAULT_GROUP_NAME, acceptClass, subscriber);
            }
        }
    }

    @Override
    public void dispatch(Dispatchable dispatchable) {
        dispatch(DEFAULT_GROUP_NAME, dispatchable);
    }

    @Override
    public void dispatch(String group, Dispatchable dispatchable) {
        AssertUtil.checkNotNull(group);
        AssertUtil.checkNotNull(dispatchable);

        PostingThreadState state = mCurrentPostingState.get();
        List<PendingPost> queue = state.eventQueue;

        PendingPost pendingPost = PendingPost.obtainPendingPost(group, dispatchable);
        queue.add(pendingPost);

        /*
        当有事件要分发的时候，检查当前状态是不是在分发中，如果正在分发，则简单入一下队列
        如果当前不在分发状态，则将队列中的事件逐一弹出，进行分发
         */
        if (!state.isPosting) {
            //如果现在不在分发事件
            if (state.canceled) {
                throw new IllegalStateException("Abort state has not set");
            }
            state.isPosting = true;
            try {
                while (!queue.isEmpty()) {
                    PendingPost  post = queue.remove(0);
                    dispatchSingle(post);
                    post.recycle();
                }
            } finally {
                state.isPosting = false;
            }
        }

    }

    /**
     * 分发单个事件
     */
    private void dispatchSingle(PendingPost pendingPost) {

        if (Looper.myLooper() == Looper.getMainLooper()) {
            //如果当前分发线程是主线程，则放到dispatch线程执行，因为主线程不能做耗时操作
        } else {
            //如果不是在主线程分发，不如直接做了吧
            doDispatch(pendingPost);
        }
    }

    /**
     * 分发单个事件的具体逻辑
     */
    private void doDispatch(PendingPost pendingPost) {
        boolean isHit = false;
        SubscriberKey key = makeKey(pendingPost.group, pendingPost.dispatchable.getClass());

        CopyOnWriteArraySet<Subscriber.Wrapper> subscribers = mSubscriberMap.get(key);
        if (subscribers != null) {
            isHit = true;
            notifySubscribers(subscribers, key, pendingPost);
        }
        if (!isHit) {
            Log.d(TAG, "didn't hit!");
        }
    }

    private void notifySubscribers(CopyOnWriteArraySet<Subscriber.Wrapper> subscribers, SubscriberKey key, PendingPost pendingPost) {
        Dispatchable dispatchable = pendingPost.dispatchable;

        List<Subscriber.Wrapper> needRemoves = new ArrayList<>();
        Iterator<Subscriber.Wrapper> iterator = subscribers.iterator();

        while (iterator.hasNext()) {
            Subscriber.Wrapper wrapper = iterator.next();
            Subscriber subscriber = wrapper.get();
            if (subscriber == null) {
                needRemoves.add(wrapper);
            } else {
                subscriber.handleDispatch(dispatchable);
            }
        }

        for (Subscriber.Wrapper wrapper : needRemoves) {
            subscribers.remove(wrapper);
            mGroupsBySubscriber.remove(wrapper.hashCode());
        }

        if (subscribers.isEmpty()) {
            mSubscriberMap.remove(key);
        }
    }

    /**
     * group
     * -> Subscriber->Subscriber->...->Subscriber
     * class
     */
    private void insertSubscriber(String group, Subscriber.Wrapper wrapper) {

        Subscriber handler = wrapper.get();
        List<Class<? extends Dispatchable>> acceptList = new ArrayList<>(2);
        handler.accept(acceptList);

        if (acceptList.size() == 0) {
            Log.d(TAG, "Please override accept function in Subscriber");
            return;
        }

        for (Class<? extends Dispatchable> acceptClass : acceptList) {
            SubscriberKey key = makeKey(group, acceptClass);
            CopyOnWriteArraySet<Subscriber.Wrapper> set = mSubscriberMap.get(key);
            if (set == null) {
                set = new CopyOnWriteArraySet<Subscriber.Wrapper>();
                mSubscriberMap.put(key, set);
            }
            set.add(wrapper);
        }
        insertGroup(group, handler);
    }

    /**
     * Subscriber.hashcode(): group->group->...->group
     */
    private void insertGroup(String group, Subscriber handler) {
        int key = handler.hashCode();

        CopyOnWriteArraySet<String> groups = mGroupsBySubscriber.get(key);
        if (groups == null) {
            groups = new CopyOnWriteArraySet<>();
            mGroupsBySubscriber.put(key, groups);
        }
        groups.add(group);
    }

    private void removeSubscriber(String group, Class<? extends Dispatchable> acceptClass, Subscriber subscriber) {

        SubscriberKey key = makeKey(group, acceptClass);
        CopyOnWriteArraySet<Subscriber.Wrapper> set = mSubscriberMap.get(key);
        if (set != null) {
            set.remove(new Subscriber.DefaultWrapper(subscriber));
            if (set.isEmpty()) {
                mSubscriberMap.remove(key);
            }
        }
    }

    private SubscriberKey makeKey(String group, Class<? extends Dispatchable> acceptClass) {
        AssertUtil.checkNotNull(group);
        AssertUtil.checkNotNull(acceptClass);

        return new SubscriberKey(group, acceptClass);
    }

    private static class SubscriberKey {
        public String group;
        public Class<? extends Dispatchable> subscriberClass;

        public SubscriberKey(String group, Class<? extends Dispatchable> scbscriberClass) {
            this.group = group;
            this.subscriberClass = scbscriberClass;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SubscriberKey that = (SubscriberKey) o;

            if (group != null ? !group.equals(that.group) : that.group != null) return false;
            return subscriberClass != null ? subscriberClass.equals(that.subscriberClass) : that.subscriberClass == null;

        }

        @Override
        public int hashCode() {
            return subscriberClass.hashCode();
        }
    }

    /**
     * 每条线程对应的分发队列和状态，通过ThreadLocal保证线程唯一
     */
    final static class PostingThreadState {
        final List<PendingPost> eventQueue = new ArrayList<>();
        boolean isPosting;
        boolean canceled;
    }
}
