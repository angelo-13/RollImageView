package com.huruwo.rollimageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by thijs on 08-06-15.
 */
public class RollImageView extends View {

    private float speed;
    private int width, height, initialState, sceneLength, resourceId;
    private Bitmap bitmap;
    private int x = 0, j = 0;

    public RollImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ParallaxView, 0, 0);
        for (int i = 0; i < a.getIndexCount(); i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.ParallaxView_initialState) {
                initialState = a.getInt(R.styleable.ParallaxView_initialState, 0);
            } else if (attr == R.styleable.ParallaxView_speed) {
                speed = a.getDimension(R.styleable.ParallaxView_speed, 10);

            } else if (attr == R.styleable.ParallaxView_sceneLength) {
                sceneLength = a.getInt(R.styleable.ParallaxView_sceneLength, 1000);

            } else if (attr == R.styleable.ParallaxView_src) {
                resourceId = a.getResourceId(R.styleable.ParallaxView_src, 0);
            }
        }
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
        if (bitmap == null) {
            bitmap = loadBitmap(getContext(), resourceId);
        }
        drawBitmap(bitmap, canvas);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    public Bitmap loadBitmap(Context context, int resourceId) {
        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true; //预读模式 只获取宽高

        BitmapFactory.decodeResource(context.getResources(), resourceId, options);

        options.inSampleSize = 4;

        //CalculateInSampleSize(options, height, width)

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeResource(context.getResources(), resourceId, options);
    }

    /**
     * 计算图片压缩比例
     * @param options
     * @param reqW
     * @param reqH
     * @return
     */
    private int CalculateInSampleSize(BitmapFactory.Options options, int reqW, int reqH) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqH || width > reqW) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqH && (halfWidth / inSampleSize) >= reqW) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }


    /**
     * 水平无限滚动
     * @param bitmap
     * @param canvas
     */
    private void drawBitmap(Bitmap bitmap, Canvas canvas) {



        if(x>=0&&x<=bitmap.getWidth()-(int) getSmallWidth(bitmap, width, height)){
            //第一阶段 单个图移动
            Rect mSrcRect = new Rect(x, 0, (int) getSmallWidth(bitmap, width, height) + x, bitmap.getHeight());//截取图片
            Rect mDestRect = new Rect(0, 0, this.getMeasuredWidth(), this.getMeasuredHeight()); //放到控件中
            canvas.drawBitmap(bitmap, mSrcRect, mDestRect, null);
            x = x + 5;
            invalidate();
        }
        else if(x>bitmap.getWidth()-(int) getSmallWidth(bitmap, width, height)&&x<=bitmap.getWidth()){
            //第二阶段 一个出去 一个接上来
            Rect mSrcRect = new Rect(bitmap.getWidth()-(int) getSmallWidth(bitmap, width, height) + j, 0, (int) getSmallWidth(bitmap, width, height) + bitmap.getWidth()-(int) getSmallWidth(bitmap, width, height), bitmap.getHeight());//截取图片
            Rect mDestRect = new Rect(0, 0, (int) getMinWidth((int) getSmallWidth(bitmap, width, height) - j, bitmap.getHeight(), this.getMeasuredHeight()), this.getMeasuredHeight()); //放到控件中
            canvas.drawBitmap(bitmap, mSrcRect, mDestRect, null);

            //同时这里有一张图片接上
            Rect mSrcRect2 = new Rect(0, 0, j, bitmap.getHeight());//截取图片
            Rect mDestRect2 = new Rect(mDestRect.right, 0, getMeasuredWidth(), getMeasuredHeight()); //放到控件中
            canvas.drawBitmap(bitmap, mSrcRect2, mDestRect2, null);

            x=x+5;
            j = j + 5;
            invalidate();
        }
        else {
            //回到第一阶段 这里要补一帧 防止闪烁
            x=0;
            j=0;

            Rect mSrcRect = new Rect(x, 0, (int) getSmallWidth(bitmap, width, height) + x, bitmap.getHeight());//截取图片
            Rect mDestRect = new Rect(0, 0, this.getMeasuredWidth(), this.getMeasuredHeight()); //放到控件中
            canvas.drawBitmap(bitmap, mSrcRect, mDestRect, null);


            invalidate();
        }

    }

    private float getSmallWidth(Bitmap bitmap, int width, int height) {
        return ((float) bitmap.getHeight()) * ((float) width) / ((float) height);
    }

    private float getMinWidth(int mixW, int mixH, int height) {
        return ((float) mixW) * ((float) height) / ((float) mixH);
    }
}
