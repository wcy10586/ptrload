package com.ptr.refresh.ptr;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Scroller;

import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.indicator.PtrIndicator;

/**
 * Created by wuchangyou on 2016/9/13.
 */
public class PtrLoadMoreLayout extends PtrFrameLayout implements View.OnClickListener{
    private ILoadMoreUIHandler uiHandler;
    private IPrepareUIHandler prepareUIHandler;
    private OnLoadMoreListener loadMoreListener;

    private boolean loadMoreEnable = true;
    private boolean isLoading;
    private boolean canPullToRefresh = true;

    private static final int NORMAL = 0;
    private static final int TOP_OFFSET = 1;
    private int mStatus = NORMAL;

    private int currentPos;

    private int loadMoreStyle = Constant.LOAD_STYLE_NORMAL;

    private boolean finishOverScroll;
    private boolean abortScroller;
    private boolean isOverScrollBottom;

    private boolean shouldSetScrollerStart;

    private float downY;
    private float oldY;
    private int dealtY;
    private Scroller mScroller;
    private float baseOverScrollLength;

    private boolean isMoving;

    private View scrollableView;

    public PtrLoadMoreLayout(Context context) {
        super(context);
        init();
    }

    public PtrLoadMoreLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PtrLoadMoreLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mScroller = new Scroller(getContext(), new OvershootInterpolator(0.75f));
        baseOverScrollLength = getContext().getResources().getDisplayMetrics().density * 120;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        int action = ev.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
                oldY = 0;
                break;
            case MotionEvent.ACTION_DOWN:
                isMoving = false;
                downY = ev.getY();
                oldY = 0;
                dealtY = mScroller.getCurrY();
                if (dealtY != 0) {
                    shouldSetScrollerStart = true;
                    abortScroller = true;
                    mScroller.abortAnimation();
                }

                if (isOverScrollBottom) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                isMoving = true;
                if (isLoading || TOP_OFFSET == mStatus) {
                    return super.dispatchTouchEvent(ev);
                }
                if (uiHandler == null) {
                    return super.dispatchTouchEvent(ev);
                }
                if (isOverScrollBottom) {
                    if (shouldSetScrollerStart) {
                        shouldSetScrollerStart = false;
                        mScroller.startScroll(0, dealtY, 0, 0);
                    }
                    if (oldY == 0) {
                        oldY = ev.getY();
                        return true;
                    }
                    dealtY += getDealt(oldY - ev.getY(), dealtY);
                    oldY = ev.getY();
                    if (isOverScrollBottom && dealtY < 0) {
                        dealtY = 0;
                    }
                    overScroll(0, dealtY);
                    return true;
                } else {
                    if (oldY == 0) {
                        oldY = ev.getY();
                        return true;
                    }
                    boolean tempOverScrollBottom = isBottomOverScroll(ev.getY());
                    if (!isOverScrollBottom && tempOverScrollBottom) {
                        if (canLoadMore()) {
                            if (loadMoreStyle == Constant.LOAD_STYLE_NORMAL) {
                                loadMore();
                                return super.dispatchTouchEvent(ev);
                            } else {
                                uiHandler.onWaitToLoadMore();
                            }
                        }
                        oldY = ev.getY();
                        isOverScrollBottom = tempOverScrollBottom;
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                        super.dispatchTouchEvent(ev);
                        return true;
                    }
                    isOverScrollBottom = tempOverScrollBottom;
                    oldY = ev.getY();
                }
                if (canLoadMore() && prepareUIHandler != null && prepareUIHandler.onPrepare()) {
                    uiHandler.onPrePareLoadMore();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                finishOverScroll = true;
                mSmoothScrollTo(0, 0);
                if (currentPos == 0) {
                    mStatus = NORMAL;
                }
                isMoving = false;
                break;
        }

        return super.dispatchTouchEvent(ev);
    }


    private float getDealt(float dealt, float distance) {
        if (dealt * distance < 0)
            return dealt;
        //x 为0的时候 y 一直为0, 所以当x==0的时候,给一个0.1的最小值
        float x = (float) Math.min(Math.max(Math.abs(distance), 0.1) / Math.abs(baseOverScrollLength), 1);
        float y = Math.min(new AccelerateInterpolator(0.15f).getInterpolation(x), 1);
        return dealt * (1 - y);
    }

    private void overScroll(int dealtX, int dealtY) {
        mSmoothScrollTo(dealtX, dealtY);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int scrollerY = mScroller.getCurrY();
            scrollTo(mScroller.getCurrX(), scrollerY);
            postInvalidate();
        } else {
            if (abortScroller) {
                abortScroller = false;
                return;
            }
            if (finishOverScroll) {
                if (isOverScrollBottom && uiHandler != null && canLoadMore()) {
                    loadMore();
                }
                finishOverScroll = false;
                isOverScrollBottom = false;
            }
        }
    }

    private void loadMore() {
        if (loadMoreListener != null) {
            isLoading = true;
            uiHandler.onLoading();
            loadMoreListener.onLoadMore();
        }
    }

    protected void mSmoothScrollTo(int fx, int fy) {
        int dx = fx - mScroller.getFinalX();
        int dy = fy - mScroller.getFinalY();
        mSmoothScrollBy(dx, dy);
    }


    protected void mSmoothScrollBy(int dx, int dy) {
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx, dy);
        invalidate();
    }

    @Override
    protected void onPositionChange(boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) {
        currentPos = ptrIndicator.getCurrentPosY();
        if (currentPos > 0) {
            mStatus = TOP_OFFSET;
        } else if (currentPos == 0 && !isUnderTouch) {
            mStatus = NORMAL;
        }
    }
    private boolean isBottomOverScroll(float currentY) {
        if (isOverScrollBottom) {
            return true;
        }

        float dealtY = oldY - currentY;
        return dealtY > 0 && !canChildScrollDown();
    }

    private boolean canChildScrollDown() {
        if (scrollableView == null) {
            return false;
        }
        return ViewCompat.canScrollVertically(scrollableView, 1);

    }

    public void setScrollableView(View scrollableView) {
        this.scrollableView = scrollableView;
    }

    public void loadComplete() {
        isLoading = false;
        super.setCanPullToRefresh(true && canPullToRefresh);
    }

    public void setLoadMoreUiHandler(ILoadMoreUIHandler uiHandler) {
        this.uiHandler = uiHandler;
        if (this.uiHandler != null) {
            uiHandler.getLoadMoreView().setOnClickListener(this);
        }
    }

    public void setPrepareUIHandler(IPrepareUIHandler uiHandler) {
        this.prepareUIHandler = uiHandler;
    }

    @Override
    public void setCanPullToRefresh(boolean canPullToRefresh) {
        super.setCanPullToRefresh(canPullToRefresh);
        this.canPullToRefresh = canPullToRefresh;
    }

    public void setLoadMoreEnable(boolean enable) {
        this.loadMoreEnable = enable;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        loadMoreListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (!canLoadMore()) {
            return;
        }

        if (uiHandler != null) {
            uiHandler.onLoading();
            loadMore();
        }


    }

    private boolean canLoadMore() {
        if (uiHandler != null && uiHandler.hasMore()) {
            return loadMoreEnable && !isLoading && uiHandler.hasMore();
        }
        return false;
    }


    public void setLoadMoreStyle(int style) {
        this.loadMoreStyle = style;
    }

    public void setOnPrepare() {
        if (uiHandler != null && canLoadMore()) {
            uiHandler.onPrePareLoadMore();
        }
    }

    public void setLoadMore() {
        if (isMoving || loadMoreStyle == Constant.LOAD_STYLE_OVER) {
            return;
        }
        if (uiHandler != null && canLoadMore()) {
            loadMore();
        }
    }

}
