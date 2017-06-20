

package com.example.pc_key.sipsesforever;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by PC_key on 19.06.2017.
 */

public class MyThread extends Thread {

    private Paint paint = new Paint();
    private SurfaceHolder surfaceHolder;
    Context context;
    private volatile boolean running = false; //Показывает, запущен ли поток
    private static float deltaT = 0;
    private int w, h; //Размеры экрана
    private float vxboard;
    private Bitmap btmBackGr, btmBoard, btmBlock, btmBall;
    private RectF dstBackGr;
    private Board board;
    private Ball ball;

    //  public static int health;
    private List<Block> blocks = new ArrayList<Block>();
    private static int COLS = 5, ROWS = 4; // Строго контролировать!
    private float stepH, stepV; //Шаги между блоками


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



        btmBackGr = BitmapFactory.decodeResource(context.getResources(), R.mipmap.my_backgr);
        dstBackGr = new RectF(0, 0, w, h);

        btmBoard = BitmapFactory.decodeResource(context.getResources(), R.mipmap.board);
        board = new Board(w / 2, h - 50 - btmBoard.getHeight() / 2, btmBoard);

        btmBlock = BitmapFactory.decodeResource(context.getResources(), R.mipmap.block);
        stepH = (w - btmBlock.getWidth()*COLS)/(COLS + 1);
        stepV = (h / 2 - btmBlock.getHeight()*ROWS)/(ROWS + 1);
        for (int i = 0; i < ROWS; i++){
            for (int j = 0; j < COLS; j++){

                blocks.add(new Block(stepH*(j + 1) + btmBlock.getWidth()*j + btmBlock.getWidth()/2,
                        stepV*(i + 1) + btmBlock.getHeight()*i + btmBlock.getHeight()/2, btmBlock));

            }
        }

        btmBall = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ball);
        ball = new Ball(w / 4, 3 * h / 4, btmBall);
        ball.vx = w / 3;
        ball.vy = h / 4;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {
        Canvas canvas = null;
        double lastTime = System.currentTimeMillis() / 1000.0;
        double currentTime;
        float lastX = board.x;
        paint.setColor(Color.CYAN);
        while (running){
            canvas = surfaceHolder.lockCanvas();
            if (canvas != null)
                try {
                    synchronized(surfaceHolder){
                        canvas.drawBitmap(btmBackGr, null, dstBackGr, paint);
                        board.draw(canvas);
                        for (Block b: blocks)

                            b.draw(canvas);

                        currentTime = System.currentTimeMillis() / 1000.0;
                        //deltaT += (float) (currentTime - lastTime);
                        deltaT = (float) (currentTime - lastTime);
                        vxboard = (board.x - lastX)/deltaT;

                        ball.x += ball.vx * deltaT;
                        ball.y += ball.vy * deltaT;
                        ball.draw(canvas);
                        lastTime = currentTime;
                        lastX = board.x;
                        if (ball.x + btmBall.getWidth()/2 > w) {
                            ball.vx = -ball.vx;
                            ball.x = w - btmBall.getWidth()/2;
                        }
                        if (ball.x - btmBall.getWidth()/2 < 0){
                            ball.vx = -ball.vx;
                            ball.x = btmBall.getWidth()/2;
                        }
                        if (ball.y + btmBall.getHeight()/2 > h) {
                            ball.vy = -ball.vy;
                            ball.y = h - btmBall.getHeight()/2;
                        }
                        if (ball.y - btmBall.getHeight()/2 < 0){
                            ball.vy = -ball.vy;
                            ball.y = btmBall.getHeight()/2;
                        }

                        for (Iterator<Block> it = blocks.iterator(); it.hasNext();) {
                            Block b = it.next();
                            if ((ball.x < b.x)&(ball.x + btmBall.getWidth()/2 > b.x - btmBlock.getWidth()/2)&
                                    (ball.y > b.y - btmBlock.getHeight()/2)&(ball.y < b.y + btmBlock.getHeight()/2)){
                                ball.vx = -ball.vx;
                                ball.x = b.x - btmBlock.getWidth()/2 - btmBall.getWidth()/2;
                                b.setHealth(b.health-1);
                                if (b.getHealth()<1){
                                    it.remove();}
                                break;
                            }
                            if ((ball.x > b.x)&(ball.x - btmBall.getWidth()/2 < b.x + btmBlock.getWidth()/2)&
                                    (ball.y > b.y - btmBlock.getHeight()/2)&(ball.y < b.y + btmBlock.getHeight()/2)){
                                ball.vx = -ball.vx;
                                ball.x = b.x + btmBlock.getWidth()/2 + btmBall.getWidth()/2;
                                b.setHealth(b.health-1);
                                if (b.getHealth()<1){
                                    it.remove();}
                                break;
                            }
                            if ((ball.y < b.y)&(ball.y + btmBall.getHeight()/2 > b.y - btmBlock.getHeight()/2)&
                                    (ball.x > b.x - btmBlock.getWidth()/2)&(ball.x < b.x + btmBlock.getWidth()/2)){
                                ball.vy = -ball.vy;
                                ball.y = b.y - btmBlock.getHeight()/2 - btmBall.getHeight()/2;
                                b.setHealth(b.health-1);
                                if (b.getHealth()<1){
                                    it.remove();}
                                break;
                            }
                            if ((ball.y > b.y)&(ball.y - btmBall.getHeight()/2 < b.y + btmBlock.getHeight()/2)&
                                    (ball.x > b.x - btmBlock.getWidth()/2)&(ball.x < b.x + btmBlock.getWidth()/2)){
                                ball.vy = -ball.vy;
                                ball.y = b.y + btmBlock.getHeight()/2 + btmBall.getHeight()/2;
                                b.setHealth(b.health-1);
                                if (b.getHealth()<1){
                                    it.remove();}
                                break;
                            }
                        }
                        //Log.d("dt = ", Float.toString(deltaT));

                        if ((ball.y < board.y)&(ball.y + btmBall.getHeight()/2 > board.y - btmBoard.getHeight()/2)&
                                (ball.x > board.x - btmBoard.getWidth()/2)&(ball.x < board.x + btmBoard.getWidth()/2)){
                            ball.vy = -ball.vy;
                            ball.vx += vxboard * 0.4f;
                            ball.y = board.y - btmBoard.getHeight()/2 - btmBall.getHeight()/2;
                        }

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

    void checkTouchDown(float xx, float yy) {
        if ((yy > h - btmBoard.getHeight() - 150) & (xx < board.x + btmBoard.getWidth() / 2 + 50)
                & (xx > board.x - btmBoard.getWidth() / 2 - 50))
            if ((xx < w - btmBoard.getWidth() / 2) & (xx > btmBoard.getWidth() / 2))
                board.x = xx;
    }

    private void updateAll(){

    }

    private void drawAll(Canvas canvas){

    }



}






