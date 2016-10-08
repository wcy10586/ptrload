package com.ptr.refresh.ptr;

import android.view.View;

/**
 * Created by changyou on 2016/4/6.
 */
public interface ILoadMoreUIHandler {
    void onLoading();

    void onLoadFinish(boolean success, boolean hasMore);

    void onWaitToLoadMore();

    void onPrePareLoadMore();

    boolean hasMore();

    void setHasMore(boolean hasMore);

    View getLoadMoreView();
}
