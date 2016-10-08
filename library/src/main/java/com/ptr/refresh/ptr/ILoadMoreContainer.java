package com.ptr.refresh.ptr;

import android.view.View;

/**
 * Created by changyou on 2016/4/6.
 */
public interface ILoadMoreContainer {
    void setLoadMoreView(View view);

    void setLoadMoreUIHandler(ILoadMoreUIHandler loadMoreUIHandler);

    void setOnLoadMoreListener(OnLoadMoreListener listener);

    void onLoadMoreCompleted(boolean success, boolean hasMore);

}
