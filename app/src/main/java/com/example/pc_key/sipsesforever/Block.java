package com.example.pc_key.sipsesforever;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Created by PC_key on 19.06.2017.
 */

public class Block{

    Paint paint = new Paint();
    float x, y;
    Bitmap block;
    public int health;
    RectF dstBlock = new RectF();

    public Block(float x, float y, Bitmap block) {
        this.x = x;
        this.y = y;
        this.block = block;
        health=3;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void draw(Canvas canvas) {


        dstBlock.set(x - block.getWidth()/2, y - block.getHeight()/2,
                x + block.getWidth()/2, y + block.getHeight()/2);
        canvas.drawBitmap(block, null, dstBlock, paint);
        paint.setColor(Color.RED);
        paint.setTextSize(100);
        canvas.drawText(Integer.toString(health),x,y,paint);
    }
}
