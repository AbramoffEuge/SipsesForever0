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
    public float x, y;
    private Bitmap backGr, board;
    private RectF dstBackGr, dstBoard;

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
        board = BitmapFactory.decodeResource(context.getResources(), R.mipmap.board);
        x = w / 2;
        y = h - 50 - board.getHeight() / 2;
        dstBoard = new RectF(x - board.getWidth() / 2, y - board.getHeight() / 2,
                x + board.getWidth() / 2, y + board.getHeight() / 2);
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
                        dstBoard.set(x - board.getWidth() / 2, y - board.getHeight() / 2,
                                x + board.getWidth() / 2, y + board.getHeight() / 2);
                        canvas.drawBitmap(board, null, dstBoard, paint);
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

    void checkTouchDown(float xx, float yy){
        if ((yy > h - board.getHeight() - 150)
                & (xx < dstBoard.centerX() + board.getWidth()/ 2 + 50)
                & (xx > dstBoard.centerX() - board.getWidth()/ 2 - 50))
            if ((xx < w - board.getWidth() / 2) & (xx > board.getWidth() / 2))
                x = xx;
    }

    private void updateAll(){

    }

    private void drawAll(Canvas canvas){

    }



}
