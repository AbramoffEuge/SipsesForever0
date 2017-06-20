package com.example.pc_key.sipsesforever;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Created by PC_key on 19.06.2017.
 */

public class Block{

    Paint paint = new Paint();
    float x, y;
    Bitmap block;
    RectF dstBlock = new RectF();
    int firmness;

    public Block(float x, float y, Bitmap block, int firmness) {
        this.x = x;
        this.y = y;
        this.block = block;
        this.firmness = firmness;
    }

    public void draw(Canvas canvas) {
        dstBlock.set(x - block.getWidth()/2, y - block.getHeight()/2,
                x + block.getWidth()/2, y + block.getHeight()/2);
        canvas.drawBitmap(block, null, dstBlock, paint);
    }
}
