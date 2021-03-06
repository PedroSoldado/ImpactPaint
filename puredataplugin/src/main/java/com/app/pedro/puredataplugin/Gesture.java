package com.app.pedro.puredataplugin;

import android.util.Pair;

/**
 * Created by Pedro on 06-04-2015.
 */
public class Gesture {

    private Touch touch;
    private Hit hit;

    protected Gesture(int x, int y, float instrument, float vel, float colorTemp, float intensity) {

        touch = new Touch(x, y);
        hit = new Hit(instrument,vel,colorTemp, intensity);

    }

    protected Gesture(Touch t, Hit h) {

        touch = t;
        hit = h;

    }

    protected Hit getHit(){
        return hit;
    }

    protected Touch getTouch(){
        return touch;
    }

    public float getIntensity() {
        return hit.getVelocity();
    }
}
