package com.bupt.paragon.sound_wave;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Random;

/**
 * Created by paragon on 16/10/28.
 */
public class SoundWave extends View {
    private static final int[] DEFAULT_STYLE = R.styleable.SoundWave;
    private static final int VERTICAL = 0;
    private static final int HORIZON = 1;
    private static final int DEFAULT_RECT_NUMS = 3;
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 200;
    private static final int DEFAULT_BASELINE = DEFAULT_HEIGHT>>1;
    private static final int DEFALUT_RECT_WIDTH = 80;
    private static final int DEFAULT_RECT_HEIGHT = 150;
    private static final int DEFAULT_RECT_INTERVAL = 20;
    private static final int DEFAULT_PERIOD = 1000;
    private static final int DEFAULT_DELAY = 100;
    private static final int DEFAULT_COLOR = 0X00F5FF;
    private static final int DEFAULT_FRAMES = 30;
    private static final float DEFAULT_RECT_MIN_HEIGHT_RATIO = 0.5f;

    private static final String TAG = "SoundWave";

    private Context mContext;
    private Paint mPaint;
    private Canvas mCanvas;
    private Random mRandom;

    private int mLeft;
    private int mRight;
    private int mTop;
    private int mBottom;
    //整个View的宽度
    private int mWidth;
    //整个View的高度
    private int mHeight;
    private int mPaddingLeft;
    private int mPaddingRight;
    private int mPaddingTop;
    private int mPaddingBottom;
    //矩形绘制的方向
    private int mOrientition;
    //矩形数量
    private int mRectNums;
    //基准线距离底部高度,和padding有关
    private int mBaseLine;
    //矩形高度
    private int mRectHeight;
    //矩形最小高度
    private int mRectMinHeight;
    //矩形可变高度
    private int mVariableHeight;
    //矩形宽度
    private int mRectWidth;
    //每个矩形的间隔
    private int mRectInterval;
    //矩形动作循环周期
    private int mPeriod;
    private float mPI_Period;
    //每个矩形的背景/颜色
    private Drawable mRectBg;
    //每个矩形动作的延迟
    private int mRectDelay;
    //一个周期的帧率
    private int mFrames;
    //每帧间隔
    private int mFrameDelay;
    //当前动画进度
    private int mCurrentProgress;
    //根据周期计算进度步长,和帧率相关
    private long mStep;

    private int mColor;

    private boolean mPlaying;

    private boolean mPlayed;

    public SoundWave(Context context) {
        this(context, null);
        mContext = context;
    }

    public SoundWave(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
        mContext = context;
    }

    public SoundWave(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        TypedArray array = mContext.obtainStyledAttributes(attrs, DEFAULT_STYLE);
        resolveAttrs(array);
        initAttrs();
        initPaint();
    }

    private void resolveAttrs(TypedArray array){
        mRectHeight = array.getDimensionPixelSize(R.styleable.SoundWave_rect_height, DEFAULT_RECT_HEIGHT);
        mRectWidth = array.getDimensionPixelSize(R.styleable.SoundWave_rect_width, DEFALUT_RECT_WIDTH);
        mRectInterval = array.getDimensionPixelSize(R.styleable.SoundWave_rect_interval, DEFAULT_RECT_INTERVAL);
        mRectMinHeight = array.getDimensionPixelOffset(R.styleable.SoundWave_rect_min_height, DEFAULT_RECT_HEIGHT>>1);
        mRectNums = array.getInt(R.styleable.SoundWave_rect_number, DEFAULT_RECT_NUMS);
        mColor = array.getColor(R.styleable.SoundWave_rect_color, DEFAULT_COLOR);
        mPeriod = array.getInt(R.styleable.SoundWave_period, DEFAULT_PERIOD);
        mFrames = array.getInt(R.styleable.SoundWave_frames, DEFAULT_FRAMES);
        mOrientition = array.getInt(R.styleable.SoundWave_sound_orientation, HORIZON);
        mBaseLine = array.getDimensionPixelSize(R.styleable.SoundWave_baseline, mOrientition == HORIZON ? (mRectHeight >> 1) : (mRectWidth >> 1));
        mRectDelay = array.getInt(R.styleable.SoundWave_rect_delay, DEFAULT_DELAY);
    }

    protected void initAttrs(){
        mPaddingLeft = getPaddingLeft();
        mPaddingRight = getPaddingRight();
        mPaddingTop = getPaddingTop();
        mPaddingBottom = getPaddingBottom();
        adjustRectSize(1);
        mFrameDelay = mPeriod/mFrames;
        mVariableHeight = ((mRectHeight - mRectMinHeight)>>1);
        mRectMinHeight >>=1;
        mStep = mPeriod/mFrames;
        mPI_Period = (float) (Math.PI/mPeriod);
        mRandom = new Random(System.currentTimeMillis());
        if(mOrientition == VERTICAL){
            swapPaddings();
        }
    }

    protected void initPaint(){
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mColor);
    }

    public boolean isPlaying(){
        return mPlaying;
    }

    public void startPlay(){
        Log.d(TAG,"startPlay");
        mPlaying = true;
        postInvalidate();
    }

    public void stopPlay(){
        Log.d(TAG,"stopPlay");
        mPlaying = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if(widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST){
            setMeasuredDimension(getMinWidth(widthSize), getMinHeight(heightSize));
        }else if(widthMode == MeasureSpec.AT_MOST){
            setMeasuredDimension(getMinWidth(widthSize), heightSize);
        }else if(heightMode == MeasureSpec.AT_MOST){
            setMeasuredDimension(widthSize, getMinHeight(heightSize));
        }else{
            setMeasuredDimension(widthSize, heightSize);
        }
        mWidth = getWidth();
        mHeight = getHeight();
    }

    private int getMinWidth(int parentWidth){
        int width = getPaddingLeft() + getPaddingRight() + mRectNums * (mRectInterval + mRectWidth);
        if(width <= parentWidth){
            return width;
        }else{
            adjustWidth(width/(float)parentWidth);
            return parentWidth;
        }
    }

    private int getMinHeight(int parentHeight){
        int height = getPaddingTop() + getPaddingBottom() + mRectHeight;
        if(height <= parentHeight){
            return height;
        }else{
            adjustHeight(height/(float)parentHeight);
            return parentHeight;
        }
    }

    private void adjustWidth(float ratio){
        mPaddingLeft *= ratio;
        mPaddingRight *= ratio;
        mRectWidth *= ratio;
        mRectInterval *= ratio;
    }

    private void adjustHeight(float ratio){
        mPaddingTop *= ratio;
        mPaddingBottom *= ratio;
        adjustRectSize(ratio);
    }

    private void adjustRectSize(float ratio){
        if(mRectHeight > mHeight){
            if(mRectMinHeight < mRectHeight){
                mRectMinHeight *= ratio;
            }else{
                mRectMinHeight *= DEFAULT_RECT_MIN_HEIGHT_RATIO;
            }
            mRectHeight *= ratio;
        }else{
            if(mRectMinHeight > mRectHeight){
                mRectMinHeight = (int) (mRectHeight * DEFAULT_RECT_MIN_HEIGHT_RATIO);
            }
        }
    }

    //根据方向转换Rect的尺寸
    protected void swapPaddings(){
        mPaddingLeft ^= mPaddingTop;
        mPaddingTop ^= mPaddingLeft;
        mPaddingLeft ^= mPaddingTop;

        mPaddingRight ^= mPaddingBottom;
        mPaddingBottom ^= mPaddingRight;
        mPaddingRight ^= mPaddingBottom;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mLeft = left;
        mTop = top;
        mRight = right;
        mBottom = bottom;
        Log.d(TAG,"onLayout left:"+mLeft+" top:"+mTop+" right:"+mRight+" bottom:"+mBottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "postDelayed mRectWidth:"+mRectWidth+" mRectHeight:"+mRectHeight);
        if(mOrientition == VERTICAL){
            drawRectsVertical(canvas);
        }else{
            drawRectsHorizon(canvas);
        }
        if(mPlaying){
            postInvalidateDelayed(mFrameDelay);
        }
    }

    //只需要绘制纵向的,需要绘制横向的时候只需要转化高度,然后将Canvas顺时针旋转90度即可
    protected void drawRectsHorizon(Canvas canvas){
        int left = mPaddingLeft + mRectInterval/2;
        int real_baseLine = mBaseLine + mPaddingBottom;
        for(int i = 1; i < mRectNums+1; i++){
            int l = left + (i-1)*(mRectWidth + mRectInterval);
            int r = l + mRectWidth;
            float fraction = getReceVariable(mRectDelay*(i-1));
            int variable = (int) (mRectMinHeight + mVariableHeight*fraction);
            int t = real_baseLine - variable;
            int b = real_baseLine + variable;
            Log.d(TAG,"drawRects fraction"+fraction+" :left:"+l+" right:"+r+" top:"+t+" bottom:"+b);
            canvas.drawRect(l, t, r, b, mPaint);
        }
        mCurrentProgress += mStep;
        if(mCurrentProgress > mPeriod){
            mCurrentProgress = 0;
        }
    }

    protected void drawRectsVertical(Canvas canvas){
        int t = mPaddingTop + mRectInterval/2;
        int real_baseLine = mBaseLine + mPaddingLeft;
        for(int i = 1; i < mRectNums+1; i++){
            int top = t + (i-1)*(mRectWidth + mRectInterval);
            int bottom = top + mRectWidth;
            float fraction = getReceVariable(mRectDelay*(i-1));
            int variable = (int) (mRectMinHeight + mVariableHeight*fraction);
            int left = real_baseLine - variable;
            int right = real_baseLine + variable;
            Log.d(TAG,"drawRects fraction"+fraction+" :left:"+left+" right:"+right+" top:"+top+" bottom:"+bottom);
            canvas.drawRect(left, top, right, bottom, mPaint);
        }
        mCurrentProgress += mStep;
        if(mCurrentProgress > mPeriod){
            mCurrentProgress = 0;
        }
    }

    protected float getFraction(int delay){
        return (mCurrentProgress+delay)/(float)mPeriod;
    }

    //纵向:获得Rect的高度,横向:获得Rect的宽度
    protected float getReceVariable(int delay){
        //Todo:后期支持用户自定义插值器,默认使用Sin
        return getSinFraction(delay);
    }

    protected float getSinFraction(int delay){
        return (float) Math.abs(Math.sin(mPI_Period*(mCurrentProgress+ delay)));
    }

    protected float getRandomFraction(int delay){
        return mRandom.nextFloat();
    }

    //绘制开始动画前的默认画面
    protected void drawDefault(Canvas canvas){
        int left = mRectInterval/2;
        Rect rect = new Rect();
        for(int i = 1; i < mRectNums+1; i++){
            rect.left = left + (i-1)*(mRectWidth + mRectInterval);
            rect.right  = rect.left + mRectWidth;
            int height = (int) (mRectMinHeight * Math.sin(Math.PI*i/mRectNums));
            rect.top = mBaseLine + height;
            rect.bottom = mBaseLine - height;
            Log.d(TAG,"drawDefault left:"+rect.left+" right:"+rect.right+" top:"+rect.top+" bottom:"+rect.bottom);
            canvas.drawRect(rect.left, rect.top, rect.right, rect.bottom, mPaint);
        }
    }

}
