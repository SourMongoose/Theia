package com.chrisx.theia;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

class Puzzle {
    private boolean[][] grid, target;
    private int w;
    private float left, top, right, bottom;

    Puzzle(String s, float l, float t, float r, float b) {
        w = (int)Math.round(Math.sqrt(s.length()));
        grid = new boolean[w][w];
        target = new boolean[w][w];

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '1') target[i/w][i%w] = true;
        }

        left = l;
        top = t;
        right = r;
        bottom = b;
    }

    public int getW() {
        return w;
    }
    public RectF getRectF() {
        return new RectF(left,top,right,bottom);
    }

    public boolean complete() {
        for (int i = 0; i < w; i++)
            for (int j = 0; j < w; j++)
                if (grid[i][j] != target[i][j]) return false;
        return true;
    }

    public void flip(int i, int j) {
        //flip surrounding squares
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (i+dx >= 0 && i+dx < w && j+dy >= 0 && j+dy < w)
                    grid[i+dx][j+dy] = !grid[i+dx][j+dy];
            }
        }
    }

    public void reset() {
        for (int i = 0; i < w; i++)
            for (int j = 0; j < w; j++)
                grid[i][j] = false;
    }

    public void drawGrid() {
        Canvas c = MainActivity.canvas;
        Bitmap on = MainActivity.on, off = MainActivity.off;

        float boxW = (right - left) / w;
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < w; j++) {
                if (grid[i][j])
                    c.drawBitmap(on,null,new RectF(left+j*boxW,top+i*boxW,left+(j+1)*boxW,top+(i+1)*boxW),null);
                else
                    c.drawBitmap(off,null,new RectF(left+j*boxW,top+i*boxW,left+(j+1)*boxW,top+(i+1)*boxW),null);
            }
        }
    }

    public void drawIcon(float left, float top, float right, float bottom) {
        Canvas c = MainActivity.canvas;
    }
}
