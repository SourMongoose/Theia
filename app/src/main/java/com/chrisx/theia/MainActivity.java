package com.chrisx.theia;

/**
 * Organized in order of priority:
 * @TODO everything
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
    private Bitmap bmp;
    static Canvas canvas;
    private LinearLayout ll;
    private float scaleFactor;

    static Bitmap theia, back, reset, table, on, off;
    static Bitmap[] icons_1;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    static Typeface trench;

    private boolean paused = false;
    private long frameCount = 0;

    private String menu = "start";
    private int floor, icon;

    //frame data
    private static final int FRAMES_PER_SECOND = 60;
    private long nanosecondsPerFrame;

    private int TRANSITION_MAX = FRAMES_PER_SECOND * 2 / 3;
    private int transition = TRANSITION_MAX / 2;

    private float downX, downY;

    private Paint title;

    private Puzzle puzzle;
    private String[][] puzzles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //creates the bitmap
        //note: Star 4.5 is 480x854
        int targetW = 480,
                wpx = getAppUsableScreenSize(this).x,
                hpx = getAppUsableScreenSize(this).y;
        scaleFactor = Math.min(1,(float)targetW/wpx);
        bmp = Bitmap.createBitmap(Math.round(wpx*scaleFactor),Math.round(hpx*scaleFactor),Bitmap.Config.ARGB_8888);

        //creates canvas
        canvas = new Canvas(bmp);

        ll = (LinearLayout) findViewById(R.id.draw_area);
        ll.setBackgroundDrawable(new BitmapDrawable(bmp));

        Resources res = getResources();
        Bitmap theia_tmp = BitmapFactory.decodeResource(res, R.drawable.theia);
        if (h()/w() > 1.*theia_tmp.getHeight()/theia_tmp.getWidth()) //screen is thinner
            theia = Bitmap.createScaledBitmap(theia_tmp,
                    Math.round(h()*theia_tmp.getWidth()/theia_tmp.getHeight()),Math.round(h()),false);
        else //screen is wider
            theia = Bitmap.createScaledBitmap(theia_tmp,
                    Math.round(w()),Math.round(w()*theia_tmp.getHeight()/theia_tmp.getWidth()),false);

        back = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.back),
                Math.round(c480(100)),Math.round(c480(100)),false);
        reset = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.reset),
                Math.round(c480(100)),Math.round(c480(100)),false);

        Bitmap table_tmp = BitmapFactory.decodeResource(res, R.drawable.table);
        table = Bitmap.createScaledBitmap(table_tmp,
                Math.round(w()),Math.round(w()*table_tmp.getHeight()/table_tmp.getWidth()),false);

        on = BitmapFactory.decodeResource(res, R.drawable.on);
        off = BitmapFactory.decodeResource(res, R.drawable.off);

        icons_1 = new Bitmap[9];
        for (int i = 0; i < icons_1.length; i++)
            icons_1[i] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.icon_1_1+i),
                    Math.round(w()/3),Math.round(w()/3),false);

        //initializes SharedPreferences
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        nanosecondsPerFrame = (long)1e9 / FRAMES_PER_SECOND;

        //initialize fonts
        trench = Typeface.createFromAsset(getAssets(), "fonts/Trench.ttf");

        canvas.drawColor(Color.BLACK);

        //pre-defined paints
        title = newPaint(Color.WHITE);
        title.setTextAlign(Paint.Align.CENTER);
        title.setTextSize(c480(200));

        puzzles = new String[][]{
                {
                        "0110100110010110",
                        "1111100110011111",
                        "1011001111001101",
                        "1001011001101001",
                        "1110010110100111",
                        "0010100000010100",
                        "1011100000011101",
                        "1000110000110001",
                        "1011010110101101",
                        "011110111010100111111001010111011110"
                },
                {
                        "1000101010101010101010001",
                        "0111010001101011000101110",
                        "0100111000001000001110010",
                        "0010001010101010101000100",
                        "0101011111011101111101010",
                        "0001110110101010110111000",
                        "1101110101011101010111011",
                        "1010101110000000111010101",
                        "0111010101101010000011011",
                        "001100011110111111110011100001110011"
                },
                {
                        "011011100011101100001101110001110110",
                        "100011100100010000000010001001110001",
                        "011110100001001100001100100001011110",
                        "001100000010100101101001010000001100",
                        "001000011010000011110000010110000100",
                        "101101011110110011110011011110101101",
                        "100110000001011001100110100000011001",
                        "001100010100111101101111001010001100",
                        "001100101101100001111111010010011110",
                        "001100011110010010001100011110100001"
                }
        };


        final Handler handler = new Handler();

        //Update thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                //draw loop
                while (!menu.equals("quit")) {
                    final long startTime = System.nanoTime();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!paused) {
                                //fading transition effect
                                if (transition > 0) {
                                    transition--;
                                }
                            }
                        }
                    });

                    frameCount++;

                    //wait until frame is done
                    while (System.nanoTime() - startTime < nanosecondsPerFrame);
                }
            }
        }).start();

        //UI thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                //draw loop
                while (!menu.equals("quit")) {
                    final long startTime = System.nanoTime();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!paused) {
                                if (transition < TRANSITION_MAX / 2) {
                                    if (menu.equals("start")) {
                                        drawTitleMenu();
                                    } else if (menu.equals("levels")) {
                                        drawLevels();
                                    } else if (menu.equals("puzzle")) {
                                        drawPuzzle();
                                    }
                                }

                                //fading transition effect
                                if (transition > 0) {
                                    int t = TRANSITION_MAX / 2, alpha;
                                    if (transition > t) {
                                        alpha = 255 - 255 * (transition - t) / t;
                                    } else {
                                        alpha = 255 - 255 * (t - transition) / t;
                                    }
                                    canvas.drawColor(Color.argb(alpha, 20, 20, 20));
                                }

                                //update canvas
                                ll.invalidate();
                            }
                        }
                    });

                    //wait until frame is done
                    while (System.nanoTime() - startTime < nanosecondsPerFrame);
                }
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        paused = false;
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    //handles touch events
    public boolean onTouchEvent(MotionEvent event) {
        float X = event.getX()*scaleFactor;
        float Y = event.getY()*scaleFactor;
        int action = event.getAction();

        if (menu.equals("start")) {
            if (action == MotionEvent.ACTION_DOWN) {
                float left, top;
                if (h()/w() > 1.*theia.getHeight()/theia.getWidth()) { //thinner
                    left = w()/2 - theia.getWidth()/2;
                    top = 0;
                } else { //wider
                    left = 0;
                    top = h()/2 - theia.getHeight()/2;
                }

                //hitbox for play button
                double play[] = {780./1200, 1340./1800, 950./1200, 1510./1800};
                if (X > left+play[0]*theia.getWidth() && X < left+play[2]*theia.getWidth()
                        && Y > top+play[1]*theia.getHeight() && Y < top+play[3]*theia.getHeight()) {
                    goToMenu("levels");
                }
            }
        } else if (menu.equals("levels")) {
            if (action == MotionEvent.ACTION_DOWN) {
                for (int i = 0; i < 9; i++) {
                    float top = h()/2-w()*2/3+(i/3)*w()/3,
                            left = (i%3)*w()/3;
                    float bottom = top+w()/3,
                            right = left+w()/3;

                    if (X > left && X < right && Y > top && Y < bottom) {
                        icon = i;
                        goToMenu("puzzle");
                    }
                }
            }
        } else if (menu.equals("puzzle")) {
            if (action == MotionEvent.ACTION_DOWN) {
                //back button
                if (X < c480(100) && Y < c480(100)) {
                    goToMenu("levels");
                }

                //reset button
                if (X > w()-c480(100) && Y < c480(100)) {
                    puzzle.reset();
                }

                RectF rf = puzzle.getRectF();
                float w = (rf.right - rf.left) / puzzle.getW();
                for (int i = 0; i < puzzle.getW(); i++) {
                    for (int j = 0; j < puzzle.getW(); j++) {
                        if (X > rf.left+j*w && X < rf.left+(j+1)*w
                                && Y > rf.top+i*w && Y < rf.top+(i+1)*w)
                            puzzle.flip(i,j);
                    }
                }
            }
        }

        return true;
    }

    private Point getAppUsableScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    //shorthand for w() and h()
    static float w() {
        return canvas.getWidth();
    }
    static float h() {
        return canvas.getHeight();
    }

    //creates an instance of Paint set to a given color
    private Paint newPaint(int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setTypeface(trench);

        return p;
    }

    static float c480(float f) {
        return w() / (480 / f);
    }
    static float c854(float f) {
        return h() / (854 / f);
    }

    private boolean completed(int floor, int icon) {
        return sharedPref.getBoolean("completed_"+floor+"_"+icon, false);
    }

    private double toRad(double deg) {
        return Math.PI/180*deg;
    }

    private void goToMenu(String s) {
        transition = TRANSITION_MAX;

        if (s.equals("levels")) {
            floor = 0;
            outer: for (int i = 1; i <= 3; i++) {
                for (int j = 1; j <= 9; j++) {
                    if (!completed(i, j)) {
                        floor = i;
                        break outer;
                    }
                }
            }
        } else if (s.equals("puzzle")) {
            puzzle = new Puzzle(puzzles[floor-1][icon],0,h()-w(),w(),h());
        }

        menu = s;
    }

    private void drawTitleMenu() {
        if (h()/w() > 1.*theia.getHeight()/theia.getWidth()) //thinner
            canvas.drawBitmap(theia,w()/2-theia.getWidth()/2,0,null);
        else //wider
            canvas.drawBitmap(theia,0,h()/2-theia.getHeight()/2,null);
    }

    private void drawLevels() {
        if (floor == 1) {
            float top = h()/2 - table.getHeight()/2;
            canvas.drawBitmap(table,0,top,null);

            for (int i = 0; i < 9; i++) {
                canvas.drawBitmap(icons_1[i],(i%3)*w()/3,h()/2-w()*2/3+(i/3)*w()/3,null);
            }
        }
    }

    private void drawPuzzle() {
        //back button
        canvas.drawBitmap(back,0,0,null);
        //reset button
        canvas.drawBitmap(reset,w()-c480(100),0,null);

        puzzle.drawGrid();
    }
}
