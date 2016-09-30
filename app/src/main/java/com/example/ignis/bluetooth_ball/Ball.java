package com.example.ignis.bluetooth_ball;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.Arrays;

/**
 * Created by Ignis on 21-May-16.
 */
class Ball {
    private final float r = 30;
    private float x = r + 80;
    private float y = r;
    private float vX = 30;
    private float vY = 25;
    private final RectF bounds;
    private final Paint paint;
    public static double[] gravity = new double[2];


    public Ball() {
        Arrays.fill(gravity, 0);
        bounds = new RectF();
        paint = new Paint();
        paint.setColor(android.graphics.Color.WHITE);
    }

    public Ball(float x, float y, float vX, float vY) {
        Arrays.fill(gravity, 0);
        bounds = new RectF();
        paint = new Paint();
        paint.setColor(android.graphics.Color.WHITE);
        this.x = x;
        this.y = Math.max(y, r + 4);
        this.vX = vX;
        this.vY = vY;
    }

    public float[] getPosition() {
        return new float[]{x, y, vX, vY};
    }


    public int moveWithCollisionDetection(Box box, Rectangular rectangular) {
        float air = (float) 0.95;
        x += vX * 0.99;
        y += vY;

        if (x - r < rectangular.x + rectangular.width && x + r > rectangular.x - rectangular.width && y + r > rectangular.y - rectangular.height && y < rectangular.y) {
            vY = -vY;
            vX += rectangular.vX;
            y = rectangular.y - r - rectangular.height;
        }

        if (x + r > box.xMax) {
            vX = -vX * air;
            x = box.xMax - r;
        } else if (x - r < box.xMin) {
            vX = -vX;
            x = box.xMin + r;
        }

        if (y + r > box.yMax) {
            vY = -vY * air;
            y = box.yMax - r;
            return 1;   //1
        } else if (y - r < box.yMin) {
            vY = -vY;
            y = box.yMin + r;
            return 2;
        }

        return 0;
    }

    public void draw(Canvas canvas) {
        bounds.set(x - r, y - r, x + r, y + r);
        canvas.drawOval(bounds, paint);
    }
}
