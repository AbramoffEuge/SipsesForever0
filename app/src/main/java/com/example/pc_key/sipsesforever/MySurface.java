package com.example.pc_key.sipsesforever;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

/**
 * Created by PC_key on 19.06.2017.
 */

public class MySurface extends SurfaceView implements SurfaceHolder.Callback {

    private MyThread myThread;

    public MyThread getMyThread() {
        return myThread;
    }

    public MySurface(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    public MySurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!myThread.isTiltOn)
            if (event.getAction() == MotionEvent.ACTION_MOVE)
                myThread.checkTouchDown(event.getX(), event.getY());
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        myThread = new MyThread(getContext(), holder, width, height);
        myThread.setRunning(true);
        myThread.setPriority(Thread.MAX_PRIORITY);
        myThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        myThread.setRunning(false);
        while (retry){
            try {
                myThread.join();
                retry = false;
            }
            catch (InterruptedException e) {

            }
        }
    }
}
