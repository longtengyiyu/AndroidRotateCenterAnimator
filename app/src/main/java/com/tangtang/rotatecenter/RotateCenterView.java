package com.tangtang.rotatecenter;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import androidx.annotation.Nullable;

public class RotateCenterView extends View implements AgentView, ValueAnimator.AnimatorUpdateListener{
    private static final String TAG = RotateCenterView.class.getSimpleName();
    public static final Byte ROTATE_X_AXIS = 0x00; //x轴旋转
    public static final Byte ROTATE_Y_AXIS = 0x01; //y轴旋转
    public static final Byte ROTATE_Z_AXIS = 0x02;//z轴旋转
    private final float fromDegrees = 0;
    private final float toDegrees = 180;
    private float centerX;
    private float centerY;
    private Camera camera;
    private Byte rotateAxis;  // 0：X轴  1：Y轴  2：Z轴
    private float progress = 0; // 进度 0-1

    private Bitmap frontImage; //前景
    private Bitmap backImage;  //后景
    private Paint paint;
    private ValueAnimator rotateCenterAnimator;

    public RotateCenterView(Context context) {
        this(context, null);
    }

    public RotateCenterView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public RotateCenterView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        setWillNotDraw(false);
        camera = new Camera();
        paint = new Paint();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        progress = valueAnimator.getAnimatedFraction();
        invalidate();
    }

    @Override
    public void play(Rect rect, Animator.AnimatorListener animatorListener, Object... args) {
        centerX = (int) args[2];
        centerY = (int) args[3];
        boolean isHorizontal = (boolean) args[4];
        long duration = (long) args[5];
        rotateAxis = isHorizontal ? ROTATE_Y_AXIS : ROTATE_X_AXIS;
        frontImage = isHorizontal ? getRevertBitmap((Bitmap) args[0], isHorizontal) : (Bitmap) args[0];
        backImage = isHorizontal ? (Bitmap) args[1] : getRevertBitmap((Bitmap) args[1], isHorizontal);
        if (rotateCenterAnimator != null && rotateCenterAnimator.isRunning()){
            rotateCenterAnimator.removeAllUpdateListeners();
            rotateCenterAnimator.cancel();
            rotateCenterAnimator = null;
        }
        rotateCenterAnimator = ValueAnimator.ofFloat(0f, 1f);
        rotateCenterAnimator.setDuration(duration);
        rotateCenterAnimator.setInterpolator(new AccelerateInterpolator());
        rotateCenterAnimator.addListener(animatorListener);
        rotateCenterAnimator.addUpdateListener(this);
        rotateCenterAnimator.start();
    }

    @Override
    public View view() {
        return this;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);
        final Matrix matrix = new Matrix();
        drawAndRotate(canvas, matrix);
    }

    private void drawAndRotate(Canvas canvas, Matrix matrix){
        startRotateAxis(canvas, matrix);
    }

    private void startRotateAxis(Canvas canvas, Matrix matrix){
        if (progress > 1.0f) return;
        float degrees = (1- progress) * toDegrees;
//    drawBitmap(canvas, progress <= 0.5f ? backImage : frontImage, progress);
        //将当前的摄像头位置保存下来，以便变换进行完成后恢复成原位
        camera.save();
        camera.translate(0.0f, 0.0f, 0.0f);
        //是给我们的View加上旋转效果，在移动的过程中，视图还会以XYZ轴为中心进行旋转。
        if (ROTATE_X_AXIS.equals(rotateAxis)) {
            degrees = progress * toDegrees;
            camera.rotateX(degrees);
        } else if (ROTATE_Y_AXIS.equals(rotateAxis)) {
            camera.rotateY(degrees);
        } else {
            camera.rotateZ(degrees);
        }
        //这个是将我们刚才定义的一系列变换应用到变换矩阵上面，调用完这句之后，我们就可以将camera的位置恢复了，以便下一次再使用。
        camera.getMatrix(matrix);
        //camera位置恢复
        camera.restore();
        //下面两句是为了动画是以View中心为旋转点
        matrix.preTranslate(-centerX, -centerY);
        matrix.postTranslate(centerX, centerY);
        drawBitmap(canvas, matrix, progress <= 0.5f ? frontImage : backImage);
    }

    //折叠方式实现中轴旋转
    private void drawBitmap(Canvas canvas, Bitmap bitmap, float progress) {
        int bw = bitmap.getWidth();
        int bh = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float[] src = {0, 0, 0, bh, bw, bh, bw, 0};

        int DX = (int) ((progress <= 0.5f )
                ? (bw / 2 * (progress / 0.5f))
                : (bw / 2 * ((1 - progress) / 0.5f)));

        int h = (int) (bh * 0.1f);
        int DY = (int) ((progress <= 0.5f )
                ? (h * (progress / 0.5f))
                : (h * ((1 - progress) / 0.5f)));
        float[] dst = {
                DX, DY,
                DX, bh-DY,
                bw-DX, bh+DY,
                bw-DX, -DY
        };
        //折叠视图
        matrix.setPolyToPoly(src, 0, dst, 0, 4);
        float scale = (progress <= 0.5f )
                ? (1 - 0.4f * (progress / 0.5f))
                : (1 - 0.4f * ((1 - progress) / 0.5f));
        matrix.postScale(1f, 1f);
        float xOffset = bw * (1f - scale) / 2;
        float yOffset = bh * (1f - scale) / 2;
        matrix.postTranslate(xOffset, yOffset);
        canvas.drawBitmap(bitmap, matrix, paint);
    }

    //镜像翻转
    private Bitmap getRevertBitmap(Bitmap bitmap, boolean isHorizontal){
        Matrix newMatrix = new Matrix();
        if (isHorizontal){
            newMatrix.postScale(-1f, 1f);
        }else{
            newMatrix.postScale(1f, -1f);
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), newMatrix, true);
    }

    //翻转视图
    private Bitmap getHorizontalRevertBitmap(Bitmap bitmap){
        Matrix newMatrix = new Matrix();
        newMatrix.postScale(-1f, 1f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), newMatrix, true);
    }

    //翻转视图
    private Bitmap getVerticalRevertBitmap(Bitmap bitmap){
        Matrix newMatrix = new Matrix();
        newMatrix.postScale(1f, -1f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), newMatrix, true);
    }

    //绘制
    private void drawBitmap(Canvas canvas, Matrix matrix, Bitmap bitmap){
        canvas.drawBitmap(bitmap, matrix, paint);
    }

    //绘制前景
    private void drawFront(Canvas canvas, Matrix matrix){
        canvas.drawBitmap(frontImage, matrix, paint);
    }

    //绘制后景
    private void drawBack(Canvas canvas, Matrix matrix){
        canvas.drawBitmap(backImage, matrix, paint);
    }
}
