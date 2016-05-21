package com.rdc.blackwhite.dispatcher.dispatch;

import com.rdc.blackwhite.dispatcher.util.AssertUtil;

/**
 * 被分发的Dispatchable的一层封装
 * 采取Android中Message的设计，加入了缓存池
 */
public class PendingPost {

    Dispatchable dispatchable;
    Object group;
    PendingPost next;

    private static PendingPost sPool;
    private final static int MAX_SIZE = 30;
    private static int sPoolSize = 0;

    private PendingPost() {}

    static PendingPost obtainPendingPost(Object group,Dispatchable dispatchable) {
        AssertUtil.checkNotNull(group);
        AssertUtil.checkNotNull(dispatchable);

        PendingPost p = null;
        if(sPool != null) {
            p = sPool;
            sPool = p.next;
            sPoolSize--;
        }else {
            p = new PendingPost();
        }
        p.dispatchable = dispatchable;
        p.group = group;
        return p;
    }

    void recycle() {
        dispatchable = null;
        group = null;

        if(sPoolSize < MAX_SIZE) {
            next = sPool;
            sPool.next = this;
            sPoolSize++;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PendingPost post = (PendingPost) o;

        if (dispatchable != null ? !dispatchable.equals(post.dispatchable) : post.dispatchable != null)
            return false;
        return group != null ? group.equals(post.group) : post.group == null;

    }

    @Override
    public int hashCode() {
        int result = dispatchable != null ? dispatchable.hashCode() : 0;
        result = 31 * result + (group != null ? group.hashCode() : 0);
        return result;
    }
}
