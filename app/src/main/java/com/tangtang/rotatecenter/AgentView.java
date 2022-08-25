package com.tangtang.rotatecenter;

import android.animation.Animator;
import android.graphics.Rect;
import android.view.View;

public interface AgentView {
    void play(Rect rect, Animator.AnimatorListener animatorListener, Object... args);

    View view();
}
