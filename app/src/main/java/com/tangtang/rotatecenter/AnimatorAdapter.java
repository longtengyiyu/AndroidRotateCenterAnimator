package com.tangtang.rotatecenter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Random;

public class AnimatorAdapter extends RecyclerView.Adapter<AnimatorAdapter.LayoutViewHolder> {

    protected int mCount;
    private BaseAgentTransform transform;

    public AnimatorAdapter(int count, BaseAgentTransform transform) {
        mCount = count;
        this.transform = transform;
    }

    @NonNull
    @Override
    public LayoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_animator, parent, false);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER;
        FrameLayout layout = transform.createDecor(parent.getContext());
        layout.setLayoutParams(lp);
        layout.addView(view);
        return LayoutViewHolder.createViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull LayoutViewHolder holder, int position) {
        holder.bindView(position);
    }

    @Override
    public int getItemCount() {
        return mCount;
    }

    static class LayoutViewHolder extends RecyclerView.ViewHolder {

        private final Random random = new Random();

        static LayoutViewHolder createViewHolder(View view) {
            return new LayoutViewHolder(view);
        }

        LayoutViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void bindView(int position) {
            FrameLayout layout = (FrameLayout) itemView;
            layout.setBackgroundColor(0xff000000 | random.nextInt(0x00ffffff));
        }
    }
}