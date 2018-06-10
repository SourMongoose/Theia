package com.chrisx.theia;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

class HexagonButton {
    private Bitmap bmp;
    private String text;
    private int angle;
    private float x, y;
    private Paint p;

    HexagonButton(Bitmap bmp, String text, float x, float y) {
        this.bmp = bmp;
        this.text = text;
        this.x = x;
        this.y = y;

        p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.BLACK);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(MainActivity.trench);
        p.setTextSize(bmp.getHeight()/2);
    }

    void draw() {
        Canvas c = MainActivity.canvas;

        c.save();
        c.translate(x, y);

        c.drawBitmap(bmp, -bmp.getWidth()/2, -bmp.getHeight()/2, null);
        c.drawText(text, 0, -(p.ascent()+p.descent())/2, p);

        c.restore();
    }
}
