package com.ptr.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.ptr.refresh.ptr.Constant;
import com.ptr.refresh.ptr.OnLoadMoreListener;
import com.ptr.refresh.ptr.OnRefreshListener;
import com.ptr.refresh.ptr.PtrLoadMoreLayout;
import com.ptr.refresh.ptr.view.PullRefreshRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private PullRefreshRecyclerView pullRefreshRecyclerView;
    private Adapter adapter;
    int pageNum = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pullRefreshRecyclerView = (PullRefreshRecyclerView) findViewById(R.id.ptr_layout);
//        pullRefreshRecyclerView.setRefreshEnable(false);
        setView();
    }

    private void setView() {
        PtrLoadMoreLayout loadMoreLayout = pullRefreshRecyclerView.getPtrLayout();
        loadMoreLayout.setLoadMoreStyle(Constant.LOAD_STYLE_OVER);
        RecyclerView recyclerView = pullRefreshRecyclerView.getRecyclerView();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));



        adapter = new Adapter(this);
        adapter.setData(getDdata());
        recyclerView.setAdapter(adapter);
        pullRefreshRecyclerView.setLoadMoreType(Constant.LOAD_SHOW_BY_CONTENT);

//        pullRefreshRecyclerView.setLoadMoreStyle(Constant.LOAD_STYLE_NORMAL);

        pullRefreshRecyclerView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullRefreshRecyclerView.setLoadMoreEnable(false);
                pageNum = 1;
                adapter.setData(getDdata());

                pullRefreshRecyclerView.refreshComplete();
                pullRefreshRecyclerView.setLoadMoreEnable(true);
                pageNum++;
            }
        });


        pullRefreshRecyclerView.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                pullRefreshRecyclerView.setRefreshEnable(false);
                adapter.addItems(getDdata());
                pageNum += 1;

                pullRefreshRecyclerView.onLoadMoreCompleted(true,true);
//                pullRefreshRecyclerView.setRefreshEnable(true);
            }
        });
    }

    private List<String> getDdata() {
        List<String> l = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            l.add(pageNum + " pos " + i);
        }
        return l;
    }
}
