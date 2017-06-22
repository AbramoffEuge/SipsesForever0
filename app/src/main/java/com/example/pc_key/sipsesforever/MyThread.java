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
    private int score;
    private float lastXX;
    private volatile boolean running = false; //Показывает, запущен ли поток
    private static float deltaT = 0;
    private int w, h; //Размеры экрана
    private float vxboard;
    private Bitmap btmBackGr, btmBoard, btmBall, btmBomb;
    private Bitmap[] btmBlock;
    private RectF dstBackGr;
    private Board board;
    private Ball ball;
    private List<Block> blocks = new ArrayList<>();
    private List<Bomb> flyingBombs = new ArrayList<>();
    private static int COLS = 9, ROWS = 5; // Строго контролировать!
    private int stepH, stepV; //Шаги между блоками
    private SoundPool soundPool;
    private int soundBounce, soundCrack, soundEnd;
    public SharedPreferences.Editor editor;
    public int max_firmness, y_speed;
    public double my_time;
    public Boolean isTimeOn, isTiltOn;
    public String time;
    private double start_time;
    private Random random = new Random();

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
            if (isTimeOn) {
                String[] words = time.split(":");
                my_time = Integer.parseInt(words[0]) * 60 + Integer.parseInt(words[1]);
            }

        } catch (Exception e) {
        }

        btmBackGr = BitmapFactory.decodeResource(context.getResources(), R.mipmap.my_backgr);
        dstBackGr = new RectF(0, 0, w, h);

        btmBomb = BitmapFactory.decodeResource(context.getResources(), R.mipmap.bomb);

        btmBoard = BitmapFactory.decodeResource(context.getResources(), R.mipmap.board0);
        board = new Board(w / 2, h - 50 - btmBoard.getHeight() / 2, btmBoard);

        btmBlock = new Bitmap[]{BitmapFactory.decodeResource(context.getResources(), R.mipmap.block0_m),
                BitmapFactory.decodeResource(context.getResources(), R.mipmap.block1_m),
                BitmapFactory.decodeResource(context.getResources(), R.mipmap.block2_m),
                BitmapFactory.decodeResource(context.getResources(), R.mipmap.block3_m)};

        btmBall = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ball_s);
        ball = new Ball(w / 4, 3 * h / 4, btmBall);
        ball.vx = w / 4;
        if (y_speed == 1)
            ball.vy = -2 * 1.3f * h / 9;
        if (y_speed == 3)
            ball.vy = -2.7f * 1.3f * h / 9;
        if (y_speed == 2)
            ball.vy = -2.5f * 1.3f * h / 9;
        board.vx = 0;

        /*stepH = (w - btmBlock[3].getWidth()*COLS)/(COLS + 1);
        stepV = (h / 2 - btmBlock[3].getHeight()*ROWS)/(ROWS + 1);
        for (int i = 0; i < ROWS; i++){
            for (int j = 0; j < COLS; j++){
                blocks.add(new Block(stepH*(j + 1) + btmBlock[3].getWidth()*j + btmBlock[3].getWidth()/2,
                        stepV*(i + 1) + btmBlock[3].getHeight()*i + btmBlock[3].getHeight()/2, btmBlock[3], 4));
            }
        }*/

        stepH = 2 * btmBall.getWidth();
        COLS = (w - 2 * stepH) / btmBlock[0].getWidth();
        stepH = (w - COLS * btmBlock[0].getWidth()) / 2;
        stepV = h / 10;
        ROWS = (h / 2 - stepV) / btmBlock[0].getHeight();
        stepV = (h / 2 - ROWS * btmBlock[0].getHeight()) / 2;

        blockGen();

        soundPool = new SoundPool(6, AudioManager.STREAM_MUSIC, 1);
        soundBounce = soundPool.load(context, R.raw.bounce, 2);
        soundCrack = soundPool.load(context, R.raw.crack, 2);
        soundEnd = soundPool.load(context, R.raw.end, 2);

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
                        deltaT = (float) (currentTime - lastTime);
                        vxboard = (board.x - lastX) / deltaT;

                        for (Bomb bomb : flyingBombs) {
                            bomb.y += bomb.vy * deltaT;
                            bomb.draw(canvas);
                        }

                        ball.x += ball.vx * deltaT;
                        ball.y += ball.vy * deltaT;
                        ball.draw(canvas);
                        if (blocks.isEmpty()) {
                            stepH = (w - COLS * btmBlock[3].getWidth()) / 2;
                            stepV = (h / 2 - ROWS * btmBlock[3].getHeight()) / 2;
                            blockGen();
                            ball.x = w / 4;
                            ball.y = 3 * h / 4;
                            ball.vx = w / 4;
                            if (y_speed == 1)
                                ball.vy = -2 * 1.3f * h / 9;
                            if (y_speed == 3)
                                ball.vy = -2.7f * 1.3f * h / 9;
                            if (y_speed == 2)
                                ball.vy = -2.5f * 1.3f * h / 9;
                        }
                        lastTime = currentTime;
                        lastX = board.x;

                        for (Iterator<Bomb> it = flyingBombs.iterator(); it.hasNext(); ) {
                            Bomb bomb = it.next();
                            if ((bomb.x < board.x) & (bomb.x + btmBomb.getWidth() / 2 > board.x - btmBoard.getWidth() / 2) &
                                    (bomb.y > board.y - btmBoard.getHeight() / 2) & (bomb.y < board.y + btmBoard.getHeight() / 2)) {
                                soundPool.play(soundEnd, 1, 1, 1, 0, 1f);
                                it.remove();
                                gameOver();
                            } else {
                                if ((bomb.x > bomb.x) & (bomb.x - btmBomb.getWidth() / 2 < bomb.x + btmBoard.getWidth() / 2) &
                                        (bomb.y > bomb.y - btmBoard.getHeight() / 2) & (bomb.y < bomb.y + btmBoard.getHeight() / 2)) {
                                    soundPool.play(soundEnd, 1, 1, 1, 0, 1f);
                                    it.remove();
                                    gameOver();
                                } else {
                                    if (bomb.x - btmBomb.getWidth() / 2 > board.x - btmBoard.getWidth() / 2 & bomb.x - btmBomb.getWidth() / 2 < board.x + btmBoard.getWidth() / 2 &
                                            bomb.y + btmBomb.getHeight() / 2 > board.y - btmBoard.getHeight() / 2) {
                                        soundPool.play(soundEnd, 1, 1, 1, 0, 1f);
                                        it.remove();
                                        gameOver();
                                    } else if (bomb.y - btmBomb.getHeight() / 2 >= h)
                                        it.remove();

                                }


                            }


                        }

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
                        if ((ball.y + btmBall.getHeight() / 2 > board.y) | ((System.currentTimeMillis() / 1000.0
                                - start_time >= my_time) & (isTimeOn))) {
                            ball.vy = 0;
                            ball.vx = 0;
                            if (ball.y + btmBall.getHeight() / 2 > board.y)
                                ball.y = board.y - btmBall.getHeight() / 2;
                            gameOver();
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
                                    if (b.hasBomb)
                                        flyingBombs.add(new Bomb(b.x, b.y, y_speed * h / 6, btmBomb));
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
                                    if (b.hasBomb)
                                        flyingBombs.add(new Bomb(b.x, b.y, y_speed * h / 6, btmBomb));
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
                                    if (b.hasBomb)
                                        flyingBombs.add(new Bomb(b.x, b.y, y_speed * h / 6, btmBomb));
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
                                    if (b.hasBomb)
                                        flyingBombs.add(new Bomb(b.x, b.y, y_speed * h / 6, btmBomb));
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

    private void blockGen() {
        for (int i = 0; i < ROWS / 2; i++) {
            for (int j = 0; j < COLS / 2; j++) {
                int r = random.nextInt(max_firmness + 1);
                if (!(r == max_firmness)) {
                    int withBomb = random.nextInt(2);
                    blocks.add(new Block(stepH + btmBlock[r].getWidth() * j + btmBlock[r].getWidth() / 2,
                            stepV + btmBlock[r].getHeight() * i + btmBlock[r].getHeight() / 2,
                            btmBlock[r], r + 1));
                    blocks.get(blocks.size() - 1).hasBomb = (withBomb == 1);
                    withBomb = random.nextInt(2);
                    blocks.add(new Block(stepH + btmBlock[r].getWidth() * (COLS - j - 1) + btmBlock[r].getWidth() / 2,
                            stepV + btmBlock[r].getHeight() * (ROWS - i - 1) + btmBlock[r].getHeight() / 2,
                            btmBlock[r], r + 1));
                    blocks.get(blocks.size() - 1).hasBomb = (withBomb == 1);
                    withBomb = random.nextInt(2);
                    blocks.add(new Block(stepH + btmBlock[r].getWidth() * (COLS - j - 1) + btmBlock[r].getWidth() / 2,
                            stepV + btmBlock[r].getHeight() * i + btmBlock[r].getHeight() / 2,
                            btmBlock[r], r + 1));
                    blocks.get(blocks.size() - 1).hasBomb = (withBomb == 1);
                    withBomb = random.nextInt(2);
                    blocks.add(new Block(stepH + btmBlock[r].getWidth() * j + btmBlock[r].getWidth() / 2,
                            stepV + btmBlock[r].getHeight() * (ROWS - i - 1) + btmBlock[r].getHeight() / 2,
                            btmBlock[r], r + 1));
                    blocks.get(blocks.size() - 1).hasBomb = (withBomb == 1);
                }
            }
        }
        if (ROWS % 2 == 1) {
            for (int j = 0; j < COLS / 2; j++) {
                int r = random.nextInt(max_firmness + 1);
                if (!(r == max_firmness)) {
                    int withBomb = random.nextInt(2);
                    blocks.add(new Block(stepH + btmBlock[r].getWidth() * j + btmBlock[r].getWidth() / 2,
                            stepV + btmBlock[r].getHeight() * (ROWS / 2) + btmBlock[r].getHeight() / 2,
                            btmBlock[r], r + 1));
                    blocks.get(blocks.size() - 1).hasBomb = (withBomb == 1);
                    withBomb = random.nextInt(2);
                    blocks.add(new Block(stepH + btmBlock[r].getWidth() * (COLS - j - 1) + btmBlock[r].getWidth() / 2,
                            stepV + btmBlock[r].getHeight() * (ROWS / 2) + btmBlock[r].getHeight() / 2,
                            btmBlock[r], r + 1));
                    blocks.get(blocks.size() - 1).hasBomb = (withBomb == 1);
                }
            }
        }
        if (COLS % 2 == 1) {
            for (int i = 0; i < ROWS / 2; i++) {
                int r = random.nextInt(max_firmness + 1);
                if (!(r == max_firmness)) {
                    int withBomb = random.nextInt(2);
                    blocks.add(new Block(stepH + btmBlock[r].getWidth() * (COLS / 2) + btmBlock[r].getWidth() / 2,
                            stepV + btmBlock[r].getHeight() * i + btmBlock[r].getHeight() / 2,
                            btmBlock[r], r + 1));
                    blocks.get(blocks.size() - 1).hasBomb = (withBomb == 1);
                    withBomb = random.nextInt(2);
                    blocks.add(new Block(stepH + btmBlock[r].getWidth() * (COLS / 2) + btmBlock[r].getWidth() / 2,
                            stepV + btmBlock[r].getHeight() * (ROWS - i - 1) + btmBlock[r].getHeight() / 2,
                            btmBlock[r], r + 1));
                    blocks.get(blocks.size() - 1).hasBomb = (withBomb == 1);
                }
            }
        }
        if ((COLS % 2 == 1) & (ROWS % 2 == 1)) {
            int r = random.nextInt(max_firmness + 1);
            if (!(r == max_firmness)) {
                int withBomb = random.nextInt(2);
                blocks.add(new Block(stepH + btmBlock[r].getWidth() * COLS / 2 + btmBlock[r].getWidth() / 2,
                        stepV + btmBlock[r].getHeight() * ROWS / 2 + btmBlock[r].getHeight() / 2,
                        btmBlock[r], r + 1));
                blocks.get(blocks.size() - 1).hasBomb = (withBomb == 1);
            }
        }
    }

    private void gameOver() {
        soundPool.play(soundEnd, 1, 1, 1, 0, 1f);
        if (score > MainActivity.prefs.getInt("key", 0)) {
            editor.putInt("key", score);
            editor.commit();
        }
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("SCORE", score);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }


}
