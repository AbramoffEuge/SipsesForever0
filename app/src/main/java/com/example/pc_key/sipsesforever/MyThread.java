package com.example.pc_key.sipsesforever;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.SurfaceHolder;

/**
 * Created by PC_key on 19.06.2017.
 */

public class MyThread extends Thread {
    private Paint paint = new Paint();
    private SurfaceHolder surfaceHolder;
    Context context;
    private volatile boolean running = false;
    private static float deltaT = 0;
    private int w, h;
    //private RectF dstBoard = new RectF();
    //private Bitmap board;
    public float x, y;
    private Bitmap backGr;
    private RectF dstBackGr;

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
        backGr = BitmapFactory.decodeResource(context.getResources(), R.mipmap.my_backgr);
        dstBackGr = new RectF(0, 0, w, h);
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {
        Canvas canvas = null;
        double lastTime = System.currentTimeMillis() / 1000.0;
        double currentTime;
        paint.setColor(Color.CYAN);

        while (running){
            canvas = surfaceHolder.lockCanvas();
            if (canvas != null)
                try {
                    synchronized(surfaceHolder){
                        canvas.drawBitmap(backGr, null, dstBackGr, paint);
                        currentTime = System.currentTimeMillis() / 1000.0;
                        deltaT += (float) (currentTime - lastTime);
                        lastTime = currentTime;
                        //canvas.drawRect(0, 0, w, h, paint);
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

    /*void checkTouchDown(float xx, float yy){
        if ((yy > h - board.getHeight() - 75)
                & (xx < dstBoard.centerX() + board.getWidth()/ 2 + 50)
                & (xx > dstBoard.centerX() - board.getWidth()/ 2 - 50)) {
            x = xx;
        }
    }*/

    private void updateAll(){

    }

    private void drawAll(Canvas canvas){

    }



}
