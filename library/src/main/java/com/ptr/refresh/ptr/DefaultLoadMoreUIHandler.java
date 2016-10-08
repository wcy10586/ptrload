package com.ptr.refresh.ptr;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ptr.refresh.R;


/**
 * Created by changyou on 2016/4/6.
 */
public class DefaultLoadMoreUIHandler extends RelativeLayout implements ILoadMoreUIHandler {
    private ProgressBar loadMoreProgress;
    private ImageView loadMoreNoDataImage;
    private TextView loadMoreTextView;

    private boolean hasMore = true;

    public DefaultLoadMoreUIHandler(Context context) {
        super(context);
        init();
    }

    public DefaultLoadMoreUIHandler(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DefaultLoadMoreUIHandler(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressWarnings("NewApi")
    public DefaultLoadMoreUIHandler(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.load_more_footer, this);
        loadMoreProgress = (ProgressBar) findViewById(R.id.load_more_progress);
        loadMoreTextView = (TextView) findViewById(R.id.load_more_text);
        loadMoreNoDataImage = (ImageView) findViewById(R.id.load_more_no_data_img);
    }

    @Override
    public void onLoading() {
        loadMoreTextView.setText(R.string.more_loading);
        loadMoreProgress.setVisibility(VISIBLE);
    }

    @Override
    public void onLoadFinish(boolean success, boolean hasMore) {
        this.hasMore = hasMore;
        loadMoreProgress.setVisibility(GONE);
        if (!hasMore) {
            loadMoreTextView.setText(R.string.no_more_data);
            loadMoreNoDataImage.setVisibility(VISIBLE);
        } else {
            if (success) {
                loadMoreTextView.setText(R.string.data_load_success);
            } else {
                loadMoreTextView.setText(R.string.data_load_tip);
            }

        }

    }

    @Override
    public void setHasMore(boolean hasMore) {
        if (this.hasMore != hasMore) {
            this.hasMore = hasMore;
            loadMoreProgress.setVisibility(GONE);
            if (!hasMore) {
                loadMoreTextView.setText(R.string.no_more_data);
                loadMoreNoDataImage.setVisibility(VISIBLE);
            } else {
                loadMoreTextView.setText(R.string.data_load_tip);
            }
        }
    }

    @Override
    public void onWaitToLoadMore() {
        loadMoreTextView.setText(R.string.data_load_tip);
        loadMoreProgress.setVisibility(GONE);
        loadMoreNoDataImage.setVisibility(GONE);
    }

    @Override
    public void onPrePareLoadMore() {
        onWaitToLoadMore();
    }

    @Override
    public boolean hasMore() {
        return hasMore;
    }

    @Override
    public View getLoadMoreView() {
        return this;
    }
}
