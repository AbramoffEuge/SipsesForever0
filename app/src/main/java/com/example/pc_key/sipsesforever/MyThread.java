package com.example.pc_key.sipsesforever;

import android.content.Context;
import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * Created by PC_key on 19.06.2017.
 */

public class MyThread extends Thread {
    private SurfaceHolder surfaceHolder;
    Context context;
    private volatile boolean running = false;
    private static float deltaT = 0;
    private int w, h;

    public static float getDeltaT() {
        return deltaT;
    }

    public static void setDeltaT(float deltaT) {
        MyThread.deltaT = deltaT;
    }
    public MyThread(Context context, SurfaceHolder surfaceHolder, int w, int h){
        this.context = context;
        this.w = w;
        this.h = h;
        this.surfaceHolder = surfaceHolder;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {
        Canvas canvas = null;
        double lastTime = System.currentTimeMillis() / 1000.0;
        double currentTime;

        while (running){
            canvas = surfaceHolder.lockCanvas();
            if (canvas != null)
                try {
                    synchronized(surfaceHolder){
                        currentTime = System.currentTimeMillis() / 1000.0;
                        deltaT += (float) (currentTime - lastTime);
                        lastTime = currentTime;
                        canvas.drawRect(0, 0, w, h, null);
                        updateAll();
                        drawAll(canvas);
                    }
                }
                finally {
                    if (canvas != null)
                        surfaceHolder.unlockCanvasAndPost(canvas);
                }
        }
    }

    private void updateAll(){

    }

    private void drawAll(Canvas canvas){

    }



}
