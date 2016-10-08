# ptrload
一款可以自定义header以及footer的下拉刷新和上拉加载库，可以实现对任意View的下拉刷新和上拉加载，目前仅实现了RecyclerView。
是满足 list、gride以及staggred形式的。
  这是一款基于 Ptr 实现的下拉加载和上拉刷新功能的控件库，自定义性强，可以根据自己的需要自定义刷新的头部，以及加载得footer。
  同时 加载的footer支持两种风格和三中模式。两种风格：1.类似于QQ的，当footer显示了之后  继续往上拖动 才会加载。2.当footer显示后立即加载。
  三中模式：1.加载的footer无论在数据是否满屏情况下都会显示，2.加载的footer只有在数据满屏之后才会显示，3.加载的footer一直都不显示。
