package com.ptr.refresh.ptr;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AbsListView;
import android.widget.OverScroller;
import android.widget.Scroller;

import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.indicator.PtrIndicator;

/**
 * Created by wuchangyou on 2016/9/13.
 */
public class PtrLoadMoreLayout extends PtrFrameLayout implements View.OnClickListener {
    private ILoadMoreUIHandler uiHandler;
    private IPrepareUIHandler prepareUIHandler;
    private OnLoadMoreListener loadMoreListener;

    private ViewConfiguration configuration;

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
    private boolean isOverScrollTop;

    private boolean shouldSetScrollerStart;

    private float oldY;
    private int dealtY;
    private Scroller mScroller;
    private float baseOverScrollLength;

    private boolean isMoving;

    private View scrollableView;

    private static final int LOAD_STATUS_NORMAL = 1;
    private static final int LOAD_STATUS_WAITING = 2;
    private static final int LOAD_STATUS_PREPARE = 3;
    private static final int LOAD_STATUS_LOADING = 4;

    private int loadStatus = LOAD_STATUS_NORMAL;

    private GestureDetector detector;

    private FlingRunnable flingRunnable;
    private OverScroller flingScroller;
    private OverScrollRunnable overScrollRunnable;

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
        configuration = ViewConfiguration.get(getContext());
        mScroller = new Scroller(getContext(), new OvershootInterpolator(0.75f));
        flingRunnable = new FlingRunnable();
        overScrollRunnable = new OverScrollRunnable();
        flingScroller = new OverScroller(getContext());
        detector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (isOverScrollTop || isOverScrollBottom) {
                    return false;
                }
                flingRunnable.start(velocityX, velocityY);
                return false;
            }
        });

    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        detector.onTouchEvent(ev);
        int action = ev.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
                oldY = 0;
                break;
            case MotionEvent.ACTION_DOWN:
                isMoving = false;
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

                if (canLoadMore() && prepareUIHandler.onPrepare() && loadStatus == LOAD_STATUS_NORMAL) {
                    loadStatus = LOAD_STATUS_PREPARE;
                    uiHandler.onPrePareLoadMore();
                }


                if (isOverScrollTop || isOverScrollBottom) {

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
                    if (isOverScrollTop && dealtY > 0) {
                        dealtY = 0;
                    }
                    if (isOverScrollBottom && dealtY < 0) {
                        dealtY = 0;
                    }
                    overScroll(0, dealtY);
                    if ((isOverScrollTop && dealtY == 0 && !isOverScrollBottom) ||
                            (isOverScrollBottom && dealtY == 0 && !isOverScrollTop)) {
                        oldY = 0;
                        isOverScrollTop = false;
                        isOverScrollBottom = false;
                        if (!isChildCanScrollVertical()) {
                            return true;
                        }
                        return super.dispatchTouchEvent(resetVertical(ev));
                    }
                    return true;
                } else {
                    if (oldY == 0) {
                        oldY = ev.getY();
                        return true;
                    }
                    boolean tempOverScrollTop = isTopOverScroll(ev.getY());
                    if (!isOverScrollTop && tempOverScrollTop) {
                        oldY = ev.getY();
                        isOverScrollTop = tempOverScrollTop;
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                        super.dispatchTouchEvent(ev);
                        return true;
                    }
                    isOverScrollTop = tempOverScrollTop;
                    boolean tempOverScrollBottom = isBottomOverScroll(ev.getY());
                    if (!isOverScrollBottom && tempOverScrollBottom) {

                        if (canLoadMore()) {
                            if (loadMoreStyle == Constant.LOAD_STYLE_NORMAL) {
                                if (loadStatus == LOAD_STATUS_NORMAL) {
                                    loadStatus = LOAD_STATUS_PREPARE;
                                    uiHandler.onPrePareLoadMore();
                                } else if (loadStatus == LOAD_STATUS_PREPARE) {
                                    loadMore();
                                }
                                return super.dispatchTouchEvent(ev);
                            } else {
                                if (loadStatus == LOAD_STATUS_PREPARE) {
                                    loadStatus = LOAD_STATUS_WAITING;
                                    uiHandler.onWaitToLoadMore();
                                }
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


    private MotionEvent resetVertical(MotionEvent event) {
        oldY = 0;
        dealtY = 0;
        event.setAction(MotionEvent.ACTION_DOWN);
        super.dispatchTouchEvent(event);
        event.setAction(MotionEvent.ACTION_MOVE);
        return event;
    }

    private float getDealt(float dealt, float distance) {
        if (dealt * distance < 0)
            return dealt;
        if (baseOverScrollLength == 0){
            baseOverScrollLength = scrollableView.getMeasuredHeight();
        }
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
                if (isOverScrollBottom && uiHandler != null && canLoadMore() && loadStatus == LOAD_STATUS_WAITING) {
                    loadMore();
                }
                finishOverScroll = false;
                isOverScrollTop = false;
                isOverScrollBottom = false;
            }
        }
    }

    private void loadMore() {
        if (loadMoreListener != null) {
            loadStatus = LOAD_STATUS_LOADING;
            isLoading = true;
            uiHandler.onLoading();
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadMoreListener.onLoadMore();
                }
            },100);

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

    private boolean isTopOverScroll(float currentY) {
        if (getHeaderView() != null && isCanPullToRefresh()) {
            return false;
        }

        if (isOverScrollTop) {
            return true;
        }
        float dealtY = oldY - currentY;
        return dealtY < 0 && !canChildScrollUp();
    }


    private boolean isBottomOverScroll(float currentY) {
        if (isOverScrollBottom) {
            return true;
        }

        float dealtY = oldY - currentY;
        return dealtY > 0 && !canChildScrollDown();
    }


    private boolean isChildCanScrollVertical() {
        return canChildScrollDown() || canChildScrollUp();
    }

    private boolean canChildScrollUp() {

        return ViewCompat.canScrollVertically(scrollableView, -1);

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
        loadStatus = LOAD_STATUS_NORMAL;
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
        return uiHandler != null && uiHandler.hasMore() && loadMoreEnable && !isLoading;
    }


    public void setLoadMoreStyle(int style) {
        this.loadMoreStyle = style;
    }

    public void setOnPrepare() {
        if (uiHandler != null && canLoadMore() && loadStatus == LOAD_STATUS_NORMAL) {
            loadStatus = LOAD_STATUS_PREPARE;
            uiHandler.onPrePareLoadMore();
        }
    }

    public void setLoadMore() {
        if (isMoving || loadMoreStyle == Constant.LOAD_STYLE_OVER) {
            return;
        }
        if (uiHandler != null && canLoadMore() && loadStatus == LOAD_STATUS_PREPARE) {
            loadMore();
        }
    }


    private class OverScrollRunnable implements Runnable {

        private static final long DELAY_TIME = 20;
        private long duration = 160;
        private float speedX, speedY;
        private long timePass;
        private long startTime;
        private int distanceX, distanceY;
        private int times;

        public void start(float speedX, float speedY) {
            this.speedX = speedX;
            this.speedY = speedY;
            startTime = System.currentTimeMillis();
            times = 1;
            run();
        }

        @Override
        public void run() {
            timePass = System.currentTimeMillis() - startTime;
            if (timePass < duration) {
                distanceY = (int) (DELAY_TIME * speedY / times);
                distanceX = (int) (DELAY_TIME * speedX / times);
                times++;
                mSmoothScrollBy(distanceX, distanceY);
                postDelayed(this, DELAY_TIME);
            } else {
                removeCallbacks(this);
                mSmoothScrollTo(0, 0);
            }
        }
    }

    private void startOverScrollAim(float currVelocity) {
        float speed = currVelocity / configuration.getScaledMaximumFlingVelocity() + 0.5f;
        if (!canChildScrollUp()) {
            overScrollRunnable.start(0, -speed);
        } else {
            overScrollRunnable.start(0, speed);
        }
    }


    private class FlingRunnable implements Runnable {
        private static final long DELAY_TIME = 40;
        private boolean abort;
        private int mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();

        public void start(float velocityX, float velocityY) {
            abort = false;
            float velocity = velocityY;
            flingScroller.fling(0, 0, 0, (int) velocity, 0, 0,
                    Integer.MIN_VALUE, Integer.MAX_VALUE);
            postDelayed(this, 40);
        }

        @Override
        public void run() {
            if (!abort && flingScroller.computeScrollOffset()) {
                boolean scrollEnd = !canChildScrollDown() || !canChildScrollUp();

                float currVelocity = flingScroller.getCurrVelocity();
                if (scrollEnd) {
                    if (currVelocity > mMinimumFlingVelocity) {
                        startOverScrollAim(currVelocity);
                    }
                } else {
                    if (currVelocity > mMinimumFlingVelocity) {
                        postDelayed(this, DELAY_TIME);
                    }
                }

            }


        }

        public void abort() {
            abort = true;
        }
    }

}
