package com.ptr.refresh.familiarrecyclerview;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

/**
 * Created by changyou on 2015/10/22.
 * <p/>
 * RecyclerView设置Header/Footer所用到的工具类
 */
public class RecyclerViewUtils {

    /**
     * 设置HeaderView
     *
     * @param recyclerView
     * @param view
     */
    public static void setHeaderView(RecyclerView recyclerView, View view) {
        RecyclerView.Adapter outerAdapter = recyclerView.getAdapter();

        if (outerAdapter == null || !(outerAdapter instanceof HeaderAndFooterRecyclerViewAdapter)) {
            return;
        }

        HeaderAndFooterRecyclerViewAdapter headerAndFooterAdapter = (HeaderAndFooterRecyclerViewAdapter) outerAdapter;
        if (headerAndFooterAdapter.getHeaderViewsCount() == 0) {
            headerAndFooterAdapter.addHeaderView(view);
        }
    }

    /**
     * 设置FooterView
     *
     * @param recyclerView
     * @param view
     */
    public static void setFooterView(RecyclerView recyclerView, View view) {
        RecyclerView.Adapter outerAdapter = recyclerView.getAdapter();

        if (outerAdapter == null || !(outerAdapter instanceof HeaderAndFooterRecyclerViewAdapter)) {
            return;
        }

        HeaderAndFooterRecyclerViewAdapter headerAndFooterAdapter = (HeaderAndFooterRecyclerViewAdapter) outerAdapter;
        if (headerAndFooterAdapter.getFooterViewsCount() == 0) {
            headerAndFooterAdapter.addFooterView(view);
        }
    }

    /**
     * 移除FooterView
     *
     * @param recyclerView
     */
    public static void removeFooterView(RecyclerView recyclerView) {

        RecyclerView.Adapter outerAdapter = recyclerView.getAdapter();

        if (outerAdapter != null && outerAdapter instanceof HeaderAndFooterRecyclerViewAdapter) {

            int footerViewCounter = ((HeaderAndFooterRecyclerViewAdapter) outerAdapter).getFooterViewsCount();
            if (footerViewCounter > 0) {
                View footerView = ((HeaderAndFooterRecyclerViewAdapter) outerAdapter).getFooterView();
                ((HeaderAndFooterRecyclerViewAdapter) outerAdapter).removeFooterView(footerView);
            }
        }
    }

    /**
     * 移除HeaderView
     *
     * @param recyclerView
     */
    public static void removeHeaderView(RecyclerView recyclerView) {

        RecyclerView.Adapter outerAdapter = recyclerView.getAdapter();

        if (outerAdapter != null && outerAdapter instanceof HeaderAndFooterRecyclerViewAdapter) {

            int headerViewCounter = ((HeaderAndFooterRecyclerViewAdapter) outerAdapter).getHeaderViewsCount();
            if (headerViewCounter > 0) {
                View headerView = ((HeaderAndFooterRecyclerViewAdapter) outerAdapter).getHeaderView();
                ((HeaderAndFooterRecyclerViewAdapter) outerAdapter).removeFooterView(headerView);
            }
        }
    }

    /**
     * 请使用本方法替代RecyclerView.ViewHolder的getLayoutPosition()方法
     *
     * @param recyclerView
     * @param holder
     * @return
     */
    public static int getLayoutPosition(RecyclerView recyclerView, RecyclerView.ViewHolder holder) {
        RecyclerView.Adapter outerAdapter = recyclerView.getAdapter();
        if (outerAdapter != null && outerAdapter instanceof HeaderAndFooterRecyclerViewAdapter) {

            int headerViewCounter = ((HeaderAndFooterRecyclerViewAdapter) outerAdapter).getHeaderViewsCount();
            if (headerViewCounter > 0) {
                return holder.getLayoutPosition() - headerViewCounter;
            }
        }

        return holder.getLayoutPosition();
    }

    /**
     * 请使用本方法替代RecyclerView.ViewHolder的getAdapterPosition()方法
     *
     * @param recyclerView
     * @param holder
     * @return
     */
    public static int getAdapterPosition(RecyclerView recyclerView, RecyclerView.ViewHolder holder) {
        RecyclerView.Adapter outerAdapter = recyclerView.getAdapter();
        if (outerAdapter != null && outerAdapter instanceof HeaderAndFooterRecyclerViewAdapter) {

            int headerViewCounter = ((HeaderAndFooterRecyclerViewAdapter) outerAdapter).getHeaderViewsCount();
            if (headerViewCounter > 0) {
                return holder.getAdapterPosition() - headerViewCounter;
            }
        }

        return holder.getAdapterPosition();
    }

    public static boolean checkToTop(RecyclerView recyclerView) {

        return ViewCompat.canScrollVertically(recyclerView, -1);
    }

    public static boolean checkToBottom(RecyclerView recyclerView) {

        return ViewCompat.canScrollVertically(recyclerView, 1);
    }

    public static int getFirstVisiblePos(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager mGridLayoutManager = (GridLayoutManager) layoutManager;
            return mGridLayoutManager.findFirstVisibleItemPosition();
        } else if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager mLinearLayoutManager = (LinearLayoutManager) layoutManager;
            return mLinearLayoutManager.findFirstVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager mStaggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
            int[] mStaggeredFirstPositions = new int[mStaggeredGridLayoutManager.getSpanCount()];
            mStaggeredGridLayoutManager.findFirstVisibleItemPositions(mStaggeredFirstPositions);
            return mStaggeredFirstPositions[0];
        }
        return 0;
    }

    public static int getLastVisiblePos(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager mGridLayoutManager = (GridLayoutManager) layoutManager;
            return mGridLayoutManager.findLastVisibleItemPosition();
        } else if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager mLinearLayoutManager = (LinearLayoutManager) layoutManager;
            return mLinearLayoutManager.findLastVisibleItemPosition();

        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager mStaggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
            int[] mStaggeredLastPositions = new int[mStaggeredGridLayoutManager.getSpanCount()];
            mStaggeredGridLayoutManager.findLastVisibleItemPositions(mStaggeredLastPositions);
            int lastCompletelyVisibleItemPos = 0;
            for (int i = 0; i < mStaggeredLastPositions.length; i++) {
                if (lastCompletelyVisibleItemPos < mStaggeredLastPositions[i]) {
                    lastCompletelyVisibleItemPos = mStaggeredLastPositions[i];
                } else {
                    break;
                }
            }
            return lastCompletelyVisibleItemPos;
        }
        return 0;
    }


    public static int getLastCompletelyVisibleItemPos(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            return ((GridLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();
        } else if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int lastCompletelyVisibleItemPos = RecyclerView.NO_POSITION;
            StaggeredGridLayoutManager mStaggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
            int[] mStaggeredLastPositions = new int[mStaggeredGridLayoutManager.getSpanCount()];
            mStaggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(mStaggeredLastPositions);
            for (int i = 0; i < mStaggeredLastPositions.length; i++) {
                if (lastCompletelyVisibleItemPos < mStaggeredLastPositions[i]) {
                    lastCompletelyVisibleItemPos = mStaggeredLastPositions[i];
                } else {
                    break;
                }
            }
            return lastCompletelyVisibleItemPos;

        }
        return RecyclerView.NO_POSITION;
    }

    public static int getFirstCompletelyVisibleItemPos(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            return ((GridLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
        } else if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager mStaggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
            int[] mStaggeredFirstPositions = new int[mStaggeredGridLayoutManager.getSpanCount()];
            mStaggeredGridLayoutManager.findFirstCompletelyVisibleItemPositions(mStaggeredFirstPositions);
            return mStaggeredFirstPositions[0];

        }
        return RecyclerView.NO_POSITION;
    }

    public static int getVisibleItemCount(RecyclerView recyclerView) {
        int fp = getFirstVisiblePos(recyclerView);
        int lp = getLastVisiblePos(recyclerView);
        if (fp == lp) {
            return 0;
        }
        return lp - fp + 1;
    }

    public static int getCompletelyVisibleItemCount(RecyclerView recyclerView) {
        int fp = getFirstCompletelyVisibleItemPos(recyclerView);
        int lp = getLastCompletelyVisibleItemPos(recyclerView);
        if (fp == lp) {
            return 0;
        }
        return lp - fp + 1;
    }

}