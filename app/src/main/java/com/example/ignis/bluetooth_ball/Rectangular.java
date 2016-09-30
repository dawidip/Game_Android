package com.example.ignis.bluetooth_ball;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.Arrays;

/**
 * Created by Ignis on 21-May-16.
 */
class Rectangular {

    float width = 80;
    float height = 4;
    float x = 100;
    float y = 300;
    float vX = 60;
    private final RectF bounds;
    private final Paint paint;
    public static double[] gravity = new double[2];


    public Rectangular() {
        Arrays.fill(gravity, 0);
        bounds = new RectF();
        paint = new Paint();
        paint.setColor(android.graphics.Color.WHITE);
    }


    public void moveWithCollisionDetection(Box box) {
        vX = (float) (-1 * gravity[0] * 10);

        x += vX;

        if (x + width > box.xMax) {
            vX = -vX;
            x = box.xMax - width;
        } else if (x - width < box.xMin) {
            vX = -vX;
            x = box.xMin + width;
        }
    }

    public void draw(Canvas canvas) {
        canvas.drawRect(x - width, y + height, x + width, y - height, paint);
        canvas.drawOval(bounds, paint);
    }
}
