package com.rdc.blackwhite.dispatcher.dispatch;


import android.os.Looper;

/**
 * dispatch线程所采用的分发器，传入的是HandlerThread的Looper
 */
public class PendingPostHandler extends EventHandler<PendingPost>{

    private PosterRunner mPosterRunner;

    public PendingPostHandler(Looper looper) {
        super(looper);
    }

    void addPosterRunner(PosterRunner posterRunner) {
        mPosterRunner = posterRunner;
    }

    void post(PendingPost pendingPost) {
        enqueue(pendingPost);
    }

    @Override
    void handleEvent(PendingPost pendingPost) {
        mPosterRunner.run(pendingPost);
    }


    interface PosterRunner {
        void run(PendingPost pendingPost);
    }
}
