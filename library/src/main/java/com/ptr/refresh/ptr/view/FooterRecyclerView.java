package com.ptr.refresh.ptr.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.ptr.refresh.familiarrecyclerview.ExStaggeredGridLayoutManager;
import com.ptr.refresh.familiarrecyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.ptr.refresh.familiarrecyclerview.HeaderSpanSizeLookup;
import com.ptr.refresh.familiarrecyclerview.RecyclerViewUtils;

/**
 * Created by wuchangyou on 2016/9/13.
 */
public class FooterRecyclerView extends RecyclerView {

    private View footer;
    private OnScrolledListener l;

    private OnAdapterSetListener listener;

    private boolean isFooterShow;

    public FooterRecyclerView(Context context) {
        super(context);
    }

    public FooterRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FooterRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onScrolled(int dx, int dy) {
        if (l != null) {
            l.onScrolled(dx, dy);
        }
    }

    @Override
    public void onScrollStateChanged(int state) {
        if (l != null) {
            l.onScrollStateChanged(state);
        }
    }

    public void setOnScrolledListener(OnScrolledListener listener) {
        l = listener;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (adapter != null) {
            HeaderAndFooterRecyclerViewAdapter mHeaderAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(adapter);
            super.setAdapter(mHeaderAndFooterRecyclerViewAdapter);
            if (listener != null) {
                listener.onAdapterSet(adapter);
            }
        }
    }

    public void setOnAdapterSetListener(OnAdapterSetListener listener) {
        this.listener = listener;
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        if (layout instanceof GridLayoutManager) {
            GridLayoutManager manager = (GridLayoutManager) layout;
            manager.setSpanSizeLookup(new HeaderSpanSizeLookup((HeaderAndFooterRecyclerViewAdapter) getAdapter(), manager.getSpanCount()));
            super.setLayoutManager(manager);
        } else if (layout instanceof ExStaggeredGridLayoutManager) {
            ExStaggeredGridLayoutManager manager = (ExStaggeredGridLayoutManager) layout;
            manager.setSpanSizeLookup(new HeaderSpanSizeLookup((HeaderAndFooterRecyclerViewAdapter) getAdapter(), manager.getSpanCount()));
            super.setLayoutManager(manager);
        } else {
            super.setLayoutManager(layout);
        }
    }


    public void setFooter(View footer) {
        if (this.footer != footer) {
            this.footer = footer;
            hideFooter();
        }
    }

    public void showFooter() {
        isFooterShow = true;
        RecyclerViewUtils.setFooterView(this, footer);
    }

    public void hideFooter() {
        isFooterShow = false;
        RecyclerViewUtils.removeFooterView(this);
    }

    public interface OnScrolledListener {
        void onScrolled(int dxx, int dy);

        void onScrollStateChanged(int state);
    }

    public interface OnAdapterSetListener {
        void onAdapterSet(Adapter adapter);
    }

}
