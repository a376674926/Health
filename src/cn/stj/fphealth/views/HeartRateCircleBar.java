package cn.stj.fphealth.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import cn.stj.fphealth.R;
import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.util.DisplayUtil;
import cn.stj.fphealth.util.LogUtil;

public class HeartRateCircleBar extends View {

	private RectF mColorWheelRectangle = new RectF();// 圆圈的矩形范围
	private Paint mDefaultWheelPaint;// 绘制底部灰色圆圈的画笔
	private Paint mColorWheelPaint;// 绘制蓝色扇形的画笔
	private Paint textPaint;// 中间文字的画笔
	private Paint textPaint1;// 上下文字的画笔
	private float mColorWheelRadius;// 圆圈普通状态下的半径
	private float circleStrokeWidth;// 圆圈的线条粗细
	private float pressExtraStrokeWidth;// 按下状态下增加的圆圈线条增加的粗细
	private int mText;// 中间文字内容
	private int mCount;// 为了达到数字增加效果而添加的变量，他和mText其实代表一个意思
	private float mProgressAni;// 为了达到蓝色扇形增加效果而添加的变量，他和mProgress其实代表一个意思
	private float mProgress;// 扇形弧度
	private int mTextSize;// 中间文字大小
	private int mTextSize1;// 上下文字大小
	private int mDistance;// 上下文字的距离
	private int mDistance1;//圆形离父布局的距离
	BarAnimation anim;// 动画类
	private int mType;// 根据传入的数值判断应该显示的页面
	private int max = Constants.TARGET_STEP_NUM;// 圆形所代表最大的数值
	private static final int GREEN = Color.rgb(78, 181, 43);
	private static final int GRAY = Color.rgb(215, 220, 214);
	private String mMiddleText;
	private int mMiddleTextSize;

	public HeartRateCircleBar(Context context) {
		super(context);
		init();
	}

	public HeartRateCircleBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.circlebar);
		mMiddleText = ta.getString(R.styleable.circlebar_middleText);
		mMiddleTextSize = ta.getInt(R.styleable.circlebar_middleTextSize,30);
		mMiddleTextSize = DisplayUtil.sp2px(getContext(), mMiddleTextSize);
        ta.recycle();
		init();
	}

	public HeartRateCircleBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {

		circleStrokeWidth = DisplayUtil.dip2px(getContext(), 8);// 圆圈的线条粗细20
		mTextSize = DisplayUtil.sp2px(getContext(), 80);// 中间文字大小
		mTextSize1 = DisplayUtil.sp2px(getContext(), 20);// 上下文字大小
		mDistance = DisplayUtil.dip2px(getContext(), 50);// 文字间的距离
		mDistance1 = DisplayUtil.dip2px(getContext(), 5);//圆圈离父布局的距离

		// 绘制蓝色扇形的画笔
		mColorWheelPaint = new Paint();
		mColorWheelPaint.setAntiAlias(true);// 抗锯齿
		mColorWheelPaint.setColor(Color.parseColor("#FFFFFF"));// 设置颜色0xFF29a6f6
		mColorWheelPaint.setStyle(Paint.Style.STROKE);// 设置空心
		mColorWheelPaint.setStrokeCap(Paint.Cap.ROUND);//设置圆形画笔
		mColorWheelPaint.setStrokeWidth(circleStrokeWidth);// 设置圆圈粗细

		// 绘制底部灰色圆圈的画笔
		mDefaultWheelPaint = new Paint();
		mDefaultWheelPaint.setAntiAlias(true);
		mDefaultWheelPaint.setColor(GRAY);//0xFFeeefef
		mDefaultWheelPaint.setStyle(Paint.Style.STROKE);
		mDefaultWheelPaint.setAlpha(50);
		mDefaultWheelPaint.setStrokeWidth(circleStrokeWidth);

		// 中间文字的画笔
		textPaint = new Paint(Paint.LINEAR_TEXT_FLAG);
		textPaint.setAntiAlias(true);
		textPaint.setColor(Color.parseColor("#FFFFFF"));//Color.parseColor("#6DCAEC")
		textPaint.setStyle(Style.FILL_AND_STROKE);
		textPaint.setTextAlign(Align.LEFT);
		textPaint.setTextSize(mTextSize);

		// 上下文字的画笔
		textPaint1 = new Paint(Paint.LINEAR_TEXT_FLAG);
		textPaint1.setAntiAlias(true);
		textPaint1.setColor(Color.parseColor("#FFFFFF"));
		textPaint1.setStyle(Style.FILL_AND_STROKE);
		textPaint1.setTextAlign(Align.LEFT);
		textPaint1.setAlpha(120);
		textPaint1.setTextSize(mTextSize1);

		// 中间文字内容
		mText = 0;
		// 扇形弧度
		mProgress = 0;

		// 动画类
		anim = new BarAnimation();
		anim.setDuration(1000);

	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		int halfHeight = getHeight() / 2;
		int halfWidth = getWidth() / 2;
		int radius = halfHeight < halfWidth ? halfHeight : halfWidth;
		// 圆圈的矩形范围 绘制底部灰色圆圈的画笔
		canvas.drawCircle(halfWidth, halfHeight, radius - mDistance1,
				mDefaultWheelPaint);

		// canvas.drawArc(mColorWheelRectangle, -90, 360, false,
		// mDefaultWheelPaint);
		// 为了达到蓝色扇形增加效果而添加的变量，他和mSweepAngle其实代表一个意思

		// mColorWheelRectangle是绘制蓝色扇形的画笔
		mColorWheelRectangle.top = halfHeight - radius + mDistance1;
		mColorWheelRectangle.bottom = halfHeight + radius - mDistance1;
		mColorWheelRectangle.left = halfWidth - radius + mDistance1;
		mColorWheelRectangle.right = halfWidth + radius - mDistance1;
		// 根据mProgressAni（角度）画扇形
		canvas.drawArc(mColorWheelRectangle, -90, mProgressAni, false,
				mColorWheelPaint);
		Rect bounds = new Rect();
		String middleText = "";// 中间的文字
		String upText = "";// 上面文字
		String downText = "";// 底部文字

		if (this.mType == 1) {// 第一个页面
			downText = getResources().getString(R.string.heart_rate_unit);
			middleText = String.valueOf(mCount);
			textPaint.setTextSize(mTextSize);
			textPaint.getTextBounds(middleText, 0, middleText.length(), bounds);
	        // drawText各个属性的意思(文字,x坐标,y坐标,画笔)
	        canvas.drawText(middleText, (mColorWheelRectangle.centerX())
	                - (textPaint.measureText(middleText) / 2),
	                mColorWheelRectangle.centerY() + bounds.height() / 3, textPaint);
		} else{
		    middleText = TextUtils.isEmpty(mMiddleText)?"":mMiddleText;
		    textPaint.setTextSize(mMiddleTextSize);
		    textPaint.getTextBounds(middleText, 0, middleText.length(), bounds);
	        // drawText各个属性的意思(文字,x坐标,y坐标,画笔)
	        canvas.drawText(middleText, (mColorWheelRectangle.centerX())
	                - (textPaint.measureText(middleText) / 2),
	                mColorWheelRectangle.centerY() + bounds.height() / 2, textPaint);
		}
		
		textPaint1.getTextBounds(upText, 0, upText.length(), bounds);
		canvas.drawText(
				upText,
				(mColorWheelRectangle.centerX())
						- (textPaint1.measureText(upText) / 2),
				mColorWheelRectangle.centerY() + bounds.height() / 2
						- mDistance, textPaint1);
		textPaint1.getTextBounds(downText, 0, downText.length(), bounds);
		canvas.drawText(downText, (mColorWheelRectangle.centerX())
				- (textPaint1.measureText(downText) / 2),
				mColorWheelRectangle.centerY() + bounds.height() / 2
						+ mDistance, textPaint1);
	}

	// 测量父布局的大小
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int height = getDefaultSize(getSuggestedMinimumHeight(),
				heightMeasureSpec);
		int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		int min = Math.min(width, height);
		setMeasuredDimension(min, min);
		mColorWheelRadius = min - circleStrokeWidth - pressExtraStrokeWidth;

		// set方法的参数意思：left,top,right,bottom
		mColorWheelRectangle.set(circleStrokeWidth + pressExtraStrokeWidth,
				circleStrokeWidth + pressExtraStrokeWidth, mColorWheelRadius,
				mColorWheelRadius);
	}

	// 对外的一个接口，用来开启动画
	public void startCustomAnimation() {
		this.startAnimation(anim);
	}

	// 设置最大数值
	public void setMax(int max) {
		this.max = max;
	}

	// 设置圆圈的进度和圆圈所显示的第几个页面
	public void setProgress(int progress, int mType) {
		if (this.mType != mType) {
			this.startCustomAnimation();
			mProgress = (Float.valueOf(progress) / Float.valueOf(max)) * 360;
			mText = progress;
			this.mType = mType;
		} else {
//			this.mCount = progress;
			this.mProgressAni = (Float.valueOf(progress) / Float.valueOf(max)) * 360;
		}

		this.postInvalidate();// 可以用子线程更新视图的方法调用。
	}
	
	public void setRateCount(int rate,int mType){
	    this.mCount = rate;
	    this.mType = mType;
	    this.postInvalidate();// 可以用子线程更新视图的方法调用。
	}

	/**
	 * 继承animation的一个动画类
	 * 
	 * @author liyachao
	 *
	 */
	public class BarAnimation extends Animation {
		/**
		 * Initializes expand collapse animation, has two types, collapse (1)
		 * and expand (0).
		 * 
		 * @param view
		 *            The view to animate
		 * @param type
		 *            The type of animation: 0 will expand from gone and 0 size
		 *            to visible and layout size defined in xml. 1 will collapse
		 *            view and set to gone
		 */
		@Override
		protected void applyTransformation(float interpolatedTime,
				Transformation t) {
			super.applyTransformation(interpolatedTime, t);
			if (interpolatedTime < 1.0f) {
				mProgressAni = interpolatedTime * mProgress;
				mCount = (int) (interpolatedTime * mText);
			} else {
				mProgressAni = mProgress;
				mCount = mText;
			}
			postInvalidate();

		}
	}
	
	public void reset(){
	    mCount = 0;
	    mProgress = 0;
	    this.mType = -1;
	    this.mProgressAni = 0;
	    this.postInvalidate();
	}

}
