# ptrload
一款可以自定义header以及footer的下拉刷新和上拉加载库，可以实现对任意View的下拉刷新和上拉加载，目前仅实现了RecyclerView。
是满足 list、gride以及staggred形式的。
  这是一款基于 Ptr 实现的下拉加载和上拉刷新功能的控件库，自定义性强，可以根据自己的需要自定义刷新的头部，以及加载得footer。
  同时 加载的footer支持两种风格和三中模式。两种风格：1.类似于QQ的，当footer显示了之后  继续往上拖动 才会加载。2.当footer显示后立即加载。
  三中模式：1.加载的footer无论在数据是否满屏情况下都会显示，2.加载的footer只有在数据满屏之后才会显示，3.加载的footer一直都不显示。
  
  
  ##使用方式
    <com.ptr.refresh.ptr.view.PullRefreshRecyclerView
        android:id="@+id/ptr_layout"
        android:layout_width="match_parent"
        android:layout_height="match_parent"></com.ptr.refresh.ptr.view.PullRefreshRecyclerView>
        
        
        PtrLoadMoreLayout loadMoreLayout = pullRefreshRecyclerView.getPtrLayout();
        loadMoreLayout.setLoadMoreStyle(Constant.LOAD_STYLE_OVER);
        RecyclerView recyclerView = pullRefreshRecyclerView.getRecyclerView();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        pullRefreshRecyclerView.setLoadMoreType(Constant.LOAD_SHOW_BY_CONTENT);

        pullRefreshRecyclerView.setLoadMoreStyle(Constant.LOAD_STYLE_OVER);//默认是LOAD_STYLE_NORMAL

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
                adapter.addItems(getDdata());
                pageNum += 1;

                pullRefreshRecyclerView.onLoadMoreCompleted(true,true);
            }
        });
        
          pullRefreshRecyclerView.setRefreshEnable(false);//设置是否可以下拉刷新，默认是true
           pullRefreshRecyclerView.setLoadMoreEnable(true);//设置是否可以加载更多，默认是true

        
        在自定义Header时候可以参考 DefaultPullRefreshHeader；自定义footer时候可以参考 DefaultLoadMoreUIHandler；
        
         pullRefreshRecyclerView.setEmptyView(View view); 可以设置一个数据无数据状态的View,无数据的View会根据Adapter中数据的变化而自动显示或者隐藏。
         
         
        
