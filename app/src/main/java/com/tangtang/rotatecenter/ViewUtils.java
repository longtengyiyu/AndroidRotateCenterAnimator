package com.tangtang.rotatecenter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

public class ViewUtils {
    public static Bitmap snapshot(View v){
        return snapshot(v, v.getWidth(), v.getHeight());
    }

    private static Bitmap snapshot(View v, int width, int height){
        if(width <= 0 || height <= 0){ return null; }

        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        if(b == null){ return null; }
        Canvas c = new Canvas(b);
        synchronized (c){
            c.setBitmap(b);
            v.draw(c);
            c.setBitmap(null);
        }
        return b;
    }
}
