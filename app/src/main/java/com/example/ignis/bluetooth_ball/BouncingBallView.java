package com.example.ignis.bluetooth_ball;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by Ignis on 21-May-16.
 */
public class BouncingBallView extends View {
    private Ball ball = null;
    private final Box box;
    private final Rectangular rectangular;
    private final MainActivity.ConnectedThread connectedThread;
    private boolean lost = false;

    public BouncingBallView(Context context, MainActivity.ConnectedThread connectedThread, boolean withBall) {
        super(context);

        box = new Box();
        if (withBall)
            ball = new Ball();
        rectangular = new Rectangular();
        this.connectedThread = connectedThread;
    }

    public void setBall(float x, float y, float vX, float vY) {
        ball = new Ball(x, y, vX, vY);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        box.draw(canvas);
        if (ball != null) {
            ball.draw(canvas);
            int status = ball.moveWithCollisionDetection(box, rectangular);
            if (status != 0) {
                if (status == 2) {  //send_position
                    String tab = Arrays.toString(ball.getPosition());
                    connectedThread.write(tab.getBytes(Charset.forName("UTF-8")));
                    ball = null;

                } else {    // game over
                    ball = null;
                    lost = true;
                }

            }
        } else if (lost) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lost = false;
            ball = new Ball();
        }

        rectangular.draw(canvas);
        rectangular.moveWithCollisionDetection(box);

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
        }
        invalidate();
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        rectangular.y = h - 80;
        box.set(w, h);
    }
}