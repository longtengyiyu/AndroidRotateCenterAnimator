package com.tangtang.rotatecenter;

import android.content.Context;
import android.util.Log;

import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

public class RotateCenterTransform extends BaseAgentTransform<RotateCenterView> implements Orientable {

    private static final String TAG = RotateCenterTransform.class.getSimpleName();
    private long duration = 800;

    private int orientation = RecyclerView.HORIZONTAL;

    private AgentDecor cacheEnterView;

    public RotateCenterTransform(){}

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    private boolean isHorizontal(){
        return orientation == RecyclerView.HORIZONTAL;
    }
    @Override
    public int orientation() {
        return orientation;
    }

    @Override
    protected void transforming(AgentDecor<RotateCenterView> view, float position) {
        if (view != null){
            Log.d(TAG, "transforming");
        }
        if(isHorizontal()){
            view.setTranslationX(-view.getWidth() * position);
        }else{
            view.setTranslationY(-view.getHeight() * position);
        }

        if(position > 1 || position <= -1){
            ViewCompat.setTranslationZ(view, -1f);
            view.clear();
        }else if(position <= 0){
            ViewCompat.setTranslationZ(view, 1f);
            if (cacheEnterView != null){
                Log.d(TAG, "play");
                view.play(
                        snapExit(view),
                        snapEnter(cacheEnterView),
                        view.getWidth() / 2,
                        view.getHeight() / 2,
                        isHorizontal(),
                        duration);
            }
        }else if(position <= 1){
            ViewCompat.setTranslationZ(view, 0f);
            view.clear();
            cacheEnterView = view;
            if (cacheEnterView != null){
                Log.d(TAG, "cacheEnterView");
            }
        }
    }

    @Override
    protected RotateCenterView createAgentView(Context context) {
        return new RotateCenterView(context);
    }
}