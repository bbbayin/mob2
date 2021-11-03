package com.mob.sms3.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by admin on 2019/10/9.
 */

public class MyItemDecoration extends RecyclerView.ItemDecoration {
    private int space;
    private int color;
    private Paint mPaint;

    /**
     * 默认的，垂直方向 横纵1px 的分割线 颜色透明
     */
    public MyItemDecoration() {
        this(1);
    }

    /**
     * 自定义宽度的透明分割线
     *
     * @param space 指定宽度
     */
    public MyItemDecoration(int space) {
        this(space, Color.TRANSPARENT);
    }

    /**
     * 自定义宽度，并指定颜色的分割线
     *
     * @param space 指定宽度
     * @param color 指定颜色
     */

    public MyItemDecoration(int space, int color) {
        this.space = space;
        this.color = color;
        initPaint();
    }


    private void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(space);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        GridLayoutManager manager = (GridLayoutManager) parent.getLayoutManager();
        int childSize = parent.getChildCount();
        int span = manager.getSpanCount();
        //为了Item大小均匀，将设定分割线平均分给左右两边Item各一半
        int offset = space / 2;
        //得到View的位置
        int childPosition = parent.getChildAdapterPosition(view);
        //第一排，顶部
        if (childPosition  < span) {
            //最左边的
            if (childPosition  % span == 0) {
                outRect.set(space, space, offset, 0);
                //最右边的
            } else if (childPosition  % span == span - 1) {
                outRect.set(offset, space, space, 0);
                //中间的
            } else {
                outRect.set(offset, space, offset, 0);
            }
        } else {
            //上下的分割线，就从第二排开始
            //最左边的
            if (childPosition  % span == 0) {
                outRect.set(space, space, offset, 0);
                //最右边的
            } else if (childPosition  % span == span - 1) {
                outRect.set(offset, space, space, 0);
                //中间的
            } else {
                outRect.set(offset, space, offset, 0);
            }
        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
    }
}
