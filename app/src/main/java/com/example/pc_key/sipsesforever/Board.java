package com.example.pc_key.sipsesforever;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Created by PC_key on 19.06.2017.
 */

public class Board {
    Paint paint = new Paint();
    float x, y;
    Bitmap board;
    RectF dstBoard = new RectF();

    public RectF getDstBoard() {
        return dstBoard;
    }

    public Board(float x, float y, Bitmap board) {
        this.x = x;
        this.y = y;
        this.board = board;
    }

    public void draw(Canvas canvas) {
        dstBoard.set(x - board.getWidth() / 2, y - board.getHeight() / 2,
                x + board.getWidth() / 2, y + board.getHeight() / 2);
        canvas.drawBitmap(board, null, dstBoard, paint);
    }
}
