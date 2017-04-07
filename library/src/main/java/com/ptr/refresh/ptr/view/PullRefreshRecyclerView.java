package com.ptr.refresh.ptr.view;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.ptr.refresh.R;
import com.ptr.refresh.familiarrecyclerview.RecyclerViewUtils;
import com.ptr.refresh.ptr.Constant;
import com.ptr.refresh.ptr.DefaultLoadMoreUIHandler;
import com.ptr.refresh.ptr.DefaultPullRefreshHeader;
import com.ptr.refresh.ptr.ILoadMoreContainer;
import com.ptr.refresh.ptr.ILoadMoreUIHandler;
import com.ptr.refresh.ptr.IPrepareUIHandler;
import com.ptr.refresh.ptr.OnLoadMoreListener;
import com.ptr.refresh.ptr.OnRefreshListener;
import com.ptr.refresh.ptr.PtrLoadMoreLayout;

import in.srain.cube.views.ptr.PtrClassicDefaultHeader;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.PtrUIHandler;

/**
 * Created by wuchangyou on 2016/9/13.
 */
public class PullRefreshRecyclerView extends FrameLayout implements ILoadMoreContainer, IPrepareUIHandler,
        FooterRecyclerView.OnScrolledListener, FooterRecyclerView.OnAdapterSetListener {

    private View header;

    private PtrLoadMoreLayout ptrLayout;
    private FooterRecyclerView recyclerView;


    private ILoadMoreUIHandler uiHandler;
    private View loadMoreView;
    private FrameLayout emptyContainer;


    private int footerType = Constant.LOAD_SHOW_BY_CONTENT;
    private boolean footerShow;

    private RecyclerView.Adapter adapter;

    public PullRefreshRecyclerView(Context context) {
        super(context);
        init();
    }

    public PullRefreshRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PullRefreshRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.pull_refresh_recyclerview, this);
        loadMoreView = new DefaultLoadMoreUIHandler(getContext());
        uiHandler = (ILoadMoreUIHandler) loadMoreView;

        header = new DefaultPullRefreshHeader(getContext());
        ptrLayout = (PtrLoadMoreLayout) findViewById(R.id.ptr);

        recyclerView = (FooterRecyclerView) findViewById(R.id.rv_footer_recycler_view);
        recyclerView.setFooter(loadMoreView);
        recyclerView.setOnScrolledListener(this);
        recyclerView.setOnAdapterSetListener(this);

        ptrLayout.setHeaderView(header);
        ptrLayout.addPtrUIHandler((PtrUIHandler) header);
        ptrLayout.setLoadMoreUiHandler((ILoadMoreUIHandler) loadMoreView);
        ptrLayout.setPrepareUIHandler(this);
        ptrLayout.setScrollableView(recyclerView);


        emptyContainer = (FrameLayout) findViewById(R.id.empty_container);
        emptyContainer.setVisibility(GONE);
    }


    public View getLoadMoreView() {
        return loadMoreView;
    }

    @Override
    public void setLoadMoreView(View view) {
        loadMoreView = view;
        recyclerView.setFooter(view);
        checkFooterHideOrShow();
    }

    public View getHeader() {
        return header;
    }

    public void setHeaderView(View header) {
        this.header = header;
        ptrLayout.setHeaderView(header);
    }

    @Override
    public void setLoadMoreUIHandler(ILoadMoreUIHandler loadMoreUIHandler) {
        uiHandler = loadMoreUIHandler;
        ptrLayout.setLoadMoreUiHandler(uiHandler);
    }

    @Override
    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        ptrLayout.setOnLoadMoreListener(listener);
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        ptrLayout.setOnRefreshListener(listener);
    }

    @Override
    public boolean onPrepare() {
        //在最后一个item显示的时候即为prepare
        return RecyclerViewUtils.getLastCompletelyVisibleItemPos(recyclerView) >= recyclerView.getAdapter().getItemCount() - 2;
    }

    public void setHasMore(boolean hasMore) {
        if (uiHandler != null) {
            uiHandler.setHasMore(hasMore);
        }
    }

    @Override
    public void setLoadMoreCompleted(boolean success, boolean hasMore) {
        if (uiHandler != null) {
            uiHandler.onLoadFinish(success, hasMore);
        }
        ptrLayout.loadComplete();
    }

    public void setRefreshComplete() {
        ptrLayout.setRefreshComplete();
    }

    public void setDefaultHeaderLastUpdateTimeKey(String key) {
        if (this.header != null && header instanceof DefaultPullRefreshHeader) {
            DefaultPullRefreshHeader header = (DefaultPullRefreshHeader) this.header;
            header.setLastUpdateTimeKey(key);
        }

    }

    public void setDefaultHeaderLastUpdateTimeRelateObject(Object object) {
        if (this.header != null && header instanceof DefaultLoadMoreUIHandler) {
            DefaultPullRefreshHeader header = (DefaultPullRefreshHeader) this.header;
            header.setLastUpdateTimeRelateObject(object);
        }

    }

    public PtrLoadMoreLayout getPtrLayout() {
        return ptrLayout;
    }

    public void setLoadMoreType(int loadType) {
        footerType = loadType;
        checkFooterHideOrShow();
    }

    public void setLoadMoreStyle(int style) {
        ptrLayout.setLoadMoreStyle(style);
    }

    public void setCanLoadMore(boolean canLoadMore) {
        ptrLayout.setCanLoadMore(canLoadMore);
        if (canLoadMore) {
            checkShowFooter();
        } else {
            checkHideFooter();
        }
    }

    public boolean isCanLoadMore() {
        return ptrLayout.isCanLoadMore();
    }

    public void setCanPullToRefresh(boolean canPullToRefresh) {
        ptrLayout.setCanPullToRefresh(canPullToRefresh);
    }

    public boolean isCanPullToRefresh() {
        return ptrLayout.isCanPullToRefresh();
    }

    public void setEmptyView(View view) {
        emptyContainer.removeAllViews();
        if (view != null) {
            emptyContainer.addView(view);
        }
    }

    public void showEmptyView() {
        emptyContainer.setVisibility(VISIBLE);
    }

    public void hideEmptyView() {
        emptyContainer.setVisibility(GONE);
    }

    public FooterRecyclerView getRecyclerView() {
        return recyclerView;
    }

    @Override
    public void onAdapterSet(RecyclerView.Adapter adapter) {
        //如果是根据内容来显示或隐藏footer那么需要在数据集变化的时候判断是否需要隐藏footer
        // 而footer的显示则在数据滚动的时候来判断

        if (adapter != null && this.adapter != adapter) {
            if (this.adapter != null) {
                this.adapter.unregisterAdapterDataObserver(observer);
            }
            this.adapter = adapter;
            this.adapter.registerAdapterDataObserver(observer);
        }
    }

    @Override
    public void onScrolled(int dx, int dy) {
        checkShowFooter();
        checkFooterState();

    }

    private void checkShowFooter() {
        if (Constant.LOAD_SHOW_ALWAYS == footerType) {
            if (!footerShow) {
                recyclerView.showFooter();
                footerShow = true;
            }
        } else if (Constant.LOAD_SHOW_HIDE_ALWAYS == footerType) {
            if (footerShow) {
                recyclerView.hideFooter();
                footerShow = false;
            }
        } else {
            if (!footerShow) {
                int visibleItemCount = RecyclerViewUtils.getCompletelyVisibleItemCount(recyclerView);
                int itemCount = recyclerView.getAdapter().getItemCount();
                if (itemCount > 0 && visibleItemCount > 0 && visibleItemCount < itemCount) {
                    recyclerView.showFooter();
                    footerShow = true;
                }
            }
        }

    }

    private void checkHideFooter() {
        if (Constant.LOAD_SHOW_ALWAYS == footerType) {
            if (!footerShow) {
                recyclerView.showFooter();
                footerShow = true;
            }
        } else if (Constant.LOAD_SHOW_HIDE_ALWAYS == footerType) {
            if (footerShow) {
                recyclerView.hideFooter();
                footerShow = false;
            }
        } else {
            if (footerShow) {
                int visibleItemCount = RecyclerViewUtils.getCompletelyVisibleItemCount(recyclerView);
                int itemCount = recyclerView.getAdapter().getItemCount();
                int lp = RecyclerViewUtils.getLastCompletelyVisibleItemPos(recyclerView);
                if (visibleItemCount == 0 || visibleItemCount >= itemCount || lp == itemCount - 2) {
                    recyclerView.hideFooter();
                    footerShow = false;
                }
            }
        }
    }


    private void checkFooterHideOrShow() {
        if (Constant.LOAD_SHOW_ALWAYS == footerType) {
            if (!footerShow) {
                recyclerView.showFooter();
                footerShow = true;
            }
        } else if (Constant.LOAD_SHOW_HIDE_ALWAYS == footerType) {
            if (footerShow) {
                recyclerView.hideFooter();
                footerShow = false;
            }
        } else {
            if (footerShow) {
                int visibleItemCount = RecyclerViewUtils.getCompletelyVisibleItemCount(recyclerView);
                int itemCount = recyclerView.getAdapter().getItemCount();
                int lp = RecyclerViewUtils.getLastCompletelyVisibleItemPos(recyclerView);
                if (visibleItemCount == 0 || visibleItemCount >= itemCount || lp == itemCount - 2) {
                    recyclerView.hideFooter();
                    footerShow = false;
                }
            } else {
                int visibleItemCount = RecyclerViewUtils.getCompletelyVisibleItemCount(recyclerView);
                int itemCount = recyclerView.getAdapter().getItemCount();
                if (itemCount > 0 && visibleItemCount > 0 && visibleItemCount < itemCount) {
                    recyclerView.showFooter();
                    footerShow = true;
                }

            }
        }
    }

    private void checkFooterState() {
        if (uiHandler.hasMore() && onPrepare()) {
            ptrLayout.setOnPrepare();
        }

    }

    @Override
    public void onScrollStateChanged(int state) {
        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            boolean toBottom = RecyclerViewUtils.getLastCompletelyVisibleItemPos(recyclerView) == recyclerView.getAdapter().getItemCount() - 1;
            if (toBottom) {
                ptrLayout.setLoadMore();
            }
        }
    }

    //用于监听数据集变化时候是否显示footer
    private RecyclerView.AdapterDataObserver observer = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            checkHideFooter();
            checkShowEmptyView();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            checkHideFooter();
            checkShowEmptyView();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            checkHideFooter();
            checkShowEmptyView();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            checkHideFooter();
            checkShowEmptyView();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            checkHideFooter();
            checkShowEmptyView();
        }
    };

    private void checkShowEmptyView() {
        if (emptyContainer.getChildCount() < 1) {
            return;
        }
        if (footerShow && recyclerView.getAdapter().getItemCount() == 1) {
            showEmptyView();
        } else if (!footerShow && recyclerView.getAdapter().getItemCount() == 0) {
            showEmptyView();
        } else {
            hideEmptyView();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (adapter != null) {
            adapter.unregisterAdapterDataObserver(observer);
        }
    }
}
