package com.example.ignis.bluetooth_ball;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by Ignis on 21-May-16.
 */
class Box {
    int xMin, xMax, yMin, yMax;
    private final Paint paint;
    private final Rect bounds;

    public Box() {
        int color = 0xff00004d;
        paint = new Paint();
        paint.setColor(color);
        bounds = new Rect();
    }

    public void set(int width, int height) {
        xMin = 0;
        xMax = width - 1;
        yMin = 0;
        yMax = height - 1;
        bounds.set(xMin, yMin, xMax, yMax);
    }

    public void draw(Canvas canvas) {
        canvas.drawRect(bounds, paint);
    }
}
