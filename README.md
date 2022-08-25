ViewPager切换实现立体旋转动效。效果如下：



![animators](animators.gif)



我们先来分析下思路

本身就动效实现来说很简单，当前视图沿着中轴旋转180度，达到90度时切换成目标视图，上代码：



```
/**
 * Author:    
 * Version    V1.0
 * Date:      2022/6/22
 * Description:
 * Modification  History:
 * Date         	Author        		Version        	Description
 * -----------------------------------------------------------------------------------
 * 2022/6/22                  1.0                    1.0
 * Why & What is modified:
 */
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
    //我们的View加上旋转效果，在移动的过程中，视图还会以XYZ轴为中心进行旋转。
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

  //折叠方式实现中轴旋转，效果目前看不是很友好
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
```

传统的Transform是由三个view变化（左，中，右），产生距离变化，无法实现我们需要的动效。动效有了，我们来自定义Transform：

```
/**
 * Author:    
 * Version    V1.0
 * Date:      2022/6/22
 * Description:
 * Modification  History:
 * Date         	Author        		Version        	Description
 * -----------------------------------------------------------------------------------
 * 2022/6/22                 1.0                    1.0
 * Why & What is modified:
 */
public class RotateCenterTransform extends BaseAgentTransform<RotateCenterView> implements Orientable {

  private long duration = 1500;

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
        if(!view.isPlaying()){
          view.play(
                  snapExit(view),
                  snapEnter(cacheEnterView),
                  view.getWidth() / 2,
                  view.getHeight() / 2,
                  isHorizontal(),
                  duration);
        }
      }
    }else if(position <= 1){
      ViewCompat.setTranslationZ(view, 0f);
      view.clear();
      cacheEnterView = view;
    }
  }

  @Override
  protected RotateCenterView createAgentView(Context context) {
    return new RotateCenterView(context);
  }
}
```

这里需要一个代理Transform，将自定义Transform代理为PageTransformer。