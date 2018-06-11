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

    static Bitmap theia;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    static Typeface trench;

    private boolean paused = false;
    private long frameCount = 0;

    private String menu = "start";

    //frame data
    private static final int FRAMES_PER_SECOND = 60;
    private long nanosecondsPerFrame;

    private int TRANSITION_MAX = FRAMES_PER_SECOND * 2 / 3;
    private int transition = TRANSITION_MAX / 2;

    private float downX, downY;

    private Paint title;

    private HexagonButton[] levels;

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
        theia = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.theia),
                Math.round(h()*1200/1600),Math.round(h()),false);

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
                goToMenu("levels");
            }
        } else if (menu.equals("levels")) {
            if (action == MotionEvent.ACTION_DOWN) {
                goToMenu("start");
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

    private long getHighScore() {
        return sharedPref.getInt("high_score", 0);
    }

    private double toRad(double deg) {
        return Math.PI/180*deg;
    }

    private void goToMenu(String s) {
        transition = TRANSITION_MAX;

        menu = s;
    }

    private void drawTitleMenu() {
        canvas.drawBitmap(theia,w()/2-theia.getWidth()/2,0,null);
    }

    private void drawLevels() {
    }
}
