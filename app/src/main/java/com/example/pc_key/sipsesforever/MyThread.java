package com.example.pc_key.sipsesforever;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by PC_key on 19.06.2017.
 */

public class MyThread extends Thread {

    private Paint paint = new Paint();
    private SurfaceHolder surfaceHolder;
    Context context;
    private Random rnd = new Random();
    private int score;
    private float lastXX;
    private volatile boolean running = false; //Показывает, запущен ли поток
    private static float deltaT = 0;
    private int w, h; //Размеры экрана
    private float vxboard;
    private Bitmap btmBackGr, btmBoard, btmBall;
    private Bitmap[] btmBlock;
    private RectF dstBackGr;
    private Board board;
    private Ball ball;
    private List<Block> blocks = new ArrayList<>();
    private static int COLS = 3, ROWS = 2; // Строго контролировать!
    int[][] field = new int[ROWS + 2][COLS + 2];
    private float stepH, stepV; //Шаги между блоками
    private SoundPool soundPool;
    private int soundBounce,soundCrack,soundEnd;
    public SharedPreferences.Editor editor;
    public int max_firmness, y_speed;
    public double my_time;
    public Boolean isTimeOn, isTiltOn;
    public String time;
    private double start_time;

    public static float getDeltaT() {
        return deltaT;
    }

    public static void setDeltaT(float deltaT) {
        MyThread.deltaT = deltaT;
    }

    public MyThread(Context context, SurfaceHolder surfaceHolder, int w, int h) {
        this.context = context;
        this.w = w;
        this.h = h;
        this.surfaceHolder = surfaceHolder;

        try {
            max_firmness = MainActivity.prefs1.getInt("firmness", 4);
            y_speed = MainActivity.prefs2.getInt("speed", 1);
            isTimeOn = MainActivity.prefs3.getBoolean("time_mode", false);
            isTiltOn = MainActivity.prefs4.getBoolean("tilt", false);
            time = MainActivity.prefs5.getString("time", "");
            if (isTimeOn){
                String[] words = time.split(":");
                my_time = Integer.parseInt(words[0])*60 +Integer.parseInt(words[1]);
            }

        } catch (Exception e) {
        }

        btmBackGr = BitmapFactory.decodeResource(context.getResources(), R.mipmap.my_backgr);
        dstBackGr = new RectF(0, 0, w, h);

        btmBoard = BitmapFactory.decodeResource(context.getResources(), R.mipmap.board0);
        board = new Board(w / 2, h - 50 - btmBoard.getHeight() / 2, btmBoard);

        btmBlock = new Bitmap[]{BitmapFactory.decodeResource(context.getResources(), R.mipmap.block0),
                BitmapFactory.decodeResource(context.getResources(), R.mipmap.block1),
                BitmapFactory.decodeResource(context.getResources(), R.mipmap.block2),
                BitmapFactory.decodeResource(context.getResources(), R.mipmap.block3)};

        /*stepH = (w - btmBlock[3].getWidth()*COLS)/(COLS + 1);
        stepV = (h / 2 - btmBlock[3].getHeight()*ROWS)/(ROWS + 1);
        for (int i = 0; i < ROWS; i++){
            for (int j = 0; j < COLS; j++){
                blocks.add(new Block(stepH*(j + 1) + btmBlock[3].getWidth()*j + btmBlock[3].getWidth()/2,
                        stepV*(i + 1) + btmBlock[3].getHeight()*i + btmBlock[3].getHeight()/2, btmBlock[3], 4));
            }
        }*/
        stepH = (w - COLS * btmBlock[3].getWidth()) / 2;
        stepV = (h / 2 - ROWS * btmBlock[3].getHeight()) / 2;
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                blocks.add(new Block(stepH + btmBlock[3].getWidth() * j + btmBlock[3].getWidth() / 2,
                        stepV + btmBlock[3].getHeight() * i + btmBlock[3].getHeight() / 2,
                        btmBlock[max_firmness - 1], max_firmness));
            }
        }

        btmBall = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ball_m);
        ball = new Ball(w / 4, 3 * h / 4, btmBall);
        ball.vx = w / 4;
        ball.vy = -y_speed * h / 6;
        board.vx = 0;

        soundPool = new SoundPool(6, AudioManager.STREAM_MUSIC, 1);
        soundBounce = soundPool.load(context, R.raw.bounce, 2);
        soundCrack = soundPool.load(context, R.raw.crack, 2);
        soundEnd = soundPool.load(context, R.raw.end, 2);

        for (int i = 1; i < ROWS + 1; i++) {
            for (int j = 1; j < ROWS + 1; j++) {
                field[i][j] = 4;
            }
        }
        for (int j = 0; j < COLS + 2; j++) {
            field[0][j] = 0;
            field[ROWS + 1][j] = 0;
        }
        for (int i = 0; i < ROWS + 2; i++) {
            field[i][0] = 0;
            field[i][COLS + 1] = 0;
        }
        editor = MainActivity.prefs.edit();
        lastXX = 0;


    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {
        Canvas canvas;
        double lastTime = System.currentTimeMillis() / 1000.0;
        double currentTime;
        float lastX = board.x;
        score = 0;
        paint.setColor(0xFFFA6C00);
        start_time = System.currentTimeMillis() / 1000.0;
        while (running) {
            canvas = surfaceHolder.lockCanvas();
            if (canvas != null)
                try {
                    synchronized (surfaceHolder) {
                        canvas.drawBitmap(btmBackGr, null, dstBackGr, paint);
                        canvas.drawLine(0, board.y, w, board.y, paint);
                        board.draw(canvas);
                        for (Block b : blocks)
                            b.draw(canvas);

                        currentTime = System.currentTimeMillis() / 1000.0;
                        //deltaT += (float) (currentTime - lastTime);
                        deltaT = (float) (currentTime - lastTime);
                        vxboard = (board.x - lastX) / deltaT;

                        ball.x += ball.vx * deltaT;
                        ball.y += ball.vy * deltaT;
                        ball.draw(canvas);
                        if (blocks.isEmpty()) {
                            stepH = (w - COLS * btmBlock[3].getWidth()) / 2;
                            stepV = (h / 2 - ROWS * btmBlock[3].getHeight()) / 2;
                            for (int i = 0; i < ROWS; i++) {
                                for (int j = 0; j < COLS; j++) {
                                    blocks.add(new Block(stepH + btmBlock[3].getWidth() * j + btmBlock[3].getWidth() / 2,
                                            stepV + btmBlock[3].getHeight() * i + btmBlock[3].getHeight() / 2, btmBlock[3], 4));
                                }
                            }
                            ball.x = w / 4;
                            ball.y = 3 * h / 4;
                            ball.vx = w / 4;
                            ball.vy = -y_speed * h / 6;
                        }
                        lastTime = currentTime;
                        lastX = board.x;
                        if (ball.x + btmBall.getWidth() / 2 > w) {
                            ball.vx = -ball.vx;
                            ball.x = w - btmBall.getWidth() / 2;
                            soundPool.play(soundBounce, 1, 1, 1, 0, 1f);
                        }
                        if (ball.x - btmBall.getWidth() / 2 < 0) {
                            ball.vx = -ball.vx;
                            ball.x = btmBall.getWidth() / 2;
                            soundPool.play(soundBounce, 1, 1, 1, 0, 1f);
                        }
                        if ((ball.y + btmBall.getHeight() / 2 > board.y)|(System.currentTimeMillis() / 1000.0
                                - start_time >= my_time)) {
                            ball.vy = 0;
                            ball.vx = 0;
                            if (ball.y + btmBall.getHeight() / 2 > board.y)
                                ball.y = board.y - btmBall.getHeight() / 2;
                            soundPool.play(soundEnd, 1, 1, 1, 0, 1f);
                            if (score > MainActivity.prefs.getInt("key", 0)) {
                                editor.putInt("key", score);
                                editor.commit();
                            }
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.putExtra("SCORE", score);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            context.startActivity(intent);
                            break;
                        }
                        if (ball.y - btmBall.getHeight() / 2 < 0) {
                            ball.vy = -ball.vy;
                            ball.y = btmBall.getHeight() / 2;
                            soundPool.play(soundBounce, 1, 1, 1, 0, 1f);
                        }
                        for (Iterator<Block> it = blocks.iterator(); it.hasNext(); ) {
                            Block b = it.next();
                            if ((ball.x < b.x) & (ball.x + btmBall.getWidth() / 2 > b.x - btmBlock[0].getWidth() / 2) &
                                    (ball.y > b.y - btmBlock[0].getHeight() / 2) & (ball.y < b.y + btmBlock[0].getHeight() / 2)) {
                                ball.vx = -ball.vx;
                                ball.x = b.x - btmBlock[0].getWidth() / 2 - btmBall.getWidth() / 2;
                                b.firmness--;
                                if (b.firmness == 0) {
                                    score++;
                                    it.remove();
                                    soundPool.play(soundCrack, 1, 1, 1, 0, 1f);
                                } else {
                                    b.block = btmBlock[b.firmness - 1];
                                    soundPool.play(soundBounce, 1, 1, 1, 0, 1f);
                                }
                                break;
                            }
                            if ((ball.x > b.x) & (ball.x - btmBall.getWidth() / 2 < b.x + btmBlock[0].getWidth() / 2) &
                                    (ball.y > b.y - btmBlock[0].getHeight() / 2) & (ball.y < b.y + btmBlock[0].getHeight() / 2)) {
                                ball.vx = -ball.vx;
                                ball.x = b.x + btmBlock[0].getWidth() / 2 + btmBall.getWidth() / 2;
                                b.firmness--;
                                if (b.firmness == 0) {
                                    score++;
                                    it.remove();
                                    soundPool.play(soundCrack, 1, 1, 1, 0, 1f);
                                } else {
                                    b.block = btmBlock[b.firmness - 1];
                                    soundPool.play(soundBounce, 1, 1, 1, 0, 1f);
                                }
                                break;
                            }
                            if ((ball.y < b.y) & (ball.y + btmBall.getHeight() / 2 > b.y - btmBlock[0].getHeight() / 2) &
                                    (ball.x > b.x - btmBlock[0].getWidth() / 2) & (ball.x < b.x + btmBlock[0].getWidth() / 2)) {
                                ball.vy = -ball.vy;
                                ball.y = b.y - btmBlock[0].getHeight() / 2 - btmBall.getHeight() / 2;
                                b.firmness--;
                                if (b.firmness == 0) {
                                    score++;
                                    it.remove();
                                    soundPool.play(soundCrack, 1, 1, 1, 0, 1f);
                                } else {
                                    b.block = btmBlock[b.firmness - 1];
                                    soundPool.play(soundBounce, 1, 1, 1, 0, 1f);
                                }
                                break;
                            }
                            if ((ball.y > b.y) & (ball.y - btmBall.getHeight() / 2 < b.y + btmBlock[0].getHeight() / 2) &
                                    (ball.x > b.x - btmBlock[0].getWidth() / 2) & (ball.x < b.x + btmBlock[0].getWidth() / 2)) {
                                ball.vy = -ball.vy;
                                ball.y = b.y + btmBlock[0].getHeight() / 2 + btmBall.getHeight() / 2;
                                b.firmness--;
                                if (b.firmness == 0) {
                                    score++;
                                    it.remove();
                                    soundPool.play(soundCrack, 1, 1, 1, 0, 1f);
                                } else {
                                    b.block = btmBlock[b.firmness - 1];
                                    soundPool.play(soundBounce, 1, 1, 1, 0, 1f);
                                }
                                break;
                            }
                        }

                        if ((ball.vy > 0) & (ball.y < board.y) & (ball.y + btmBall.getHeight() / 2 > board.y - btmBoard.getHeight() / 2) &
                                (ball.x > board.x - btmBoard.getWidth() / 2) & (ball.x < board.x + btmBoard.getWidth() / 2)) {
                            ball.vy = -ball.vy;
                            //ball.vx += vxboard * 0.4f;
                            //ball.vx = ball.vx * 0.7f + vxboard * 0.3f;
                            ball.vx = -1.5f * ball.vy * (ball.x - board.x) / (btmBoard.getWidth() / 2);
                            ball.y = board.y - btmBoard.getHeight() / 2 - btmBall.getHeight() / 2;
                            soundPool.play(soundBounce, 1, 10, 10, 0, 1f);
                        }

                        drawAll(canvas);
                        updateAll();
                    }
                } finally {
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

    public void accelerometerChange(float xx) {
        if (board.x - btmBoard.getWidth() / 2 >= 0 & board.x + btmBoard.getWidth() / 2 <= w) {
            if (xx * lastXX > 0)
                board.vx -= 0.4f * xx;
            else
                board.vx = 0;
            board.x += board.vx;
            if (board.x - btmBoard.getWidth() / 2 < 0) {
                board.x = btmBoard.getWidth() / 2;
                board.vx = 0;
            }
            if (board.x + btmBoard.getWidth() / 2 > w) {
                board.x = w - btmBoard.getWidth() / 2;
                board.vx = 0;
            }
            lastXX = xx;
        }
    }

    private void updateAll() {

    }

    private void drawAll(Canvas canvas) {

    }


}
