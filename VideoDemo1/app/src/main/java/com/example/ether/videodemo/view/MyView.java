package com.example.ether.videodemo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.example.ether.videodemo.R;

public class MyView extends View {
    private Paint paint;
    private Bitmap bitmap;
    private int width, height;
    private Matrix matrix;
    private Bitmap scaleBitmap;

    public MyView(Context context) {
        super(context);
        init();
    }

    private void init() {
        paint = new Paint();
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
        matrix = new Matrix();
    }

    public MyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        matrix.postScale((float) width / (float) bitmap.getWidth(), (float) height / (float) bitmap.getHeight());
        scaleBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        super.onDraw(canvas);
        canvas.drawBitmap(scaleBitmap, 0, 0, paint);
        bitmap.recycle();
    }
}
