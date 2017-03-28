package com.shuang.meiZhi.android;


import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.orhanobut.logger.Logger;
import com.shuang.meiZhi.base.BaseFragment;
import com.shuang.meiZhi.R;
import com.shuang.meiZhi.adapter.AndroidItemViewBinder;
import com.shuang.meiZhi.constantPool.RefreshConstantField;
import com.shuang.meiZhi.entity.AndroidBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import me.drakeet.multitype.MultiTypeAdapter;

/**
 * @author feng
 * @Description: fragment 的基类
 * @date 2017/3/22
 */
public class AndroidFragment extends BaseFragment implements IAndroidContract.IAndroidView {
    private static final int PRELOAD_SIZE = 10;
    @BindView(R.id.srl_swipeRefresh)
    SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.rv_androidRecyclerView)
    RecyclerView androidRecyclerView;
    private IAndroidContract.IAndroidPresenter presenter;
    private MultiTypeAdapter androidAdapter;
    private boolean mIsFirstTimeTouchBottom = true;
    private onBottomScrollListener mOnBottomScrollListener;
    private int mPage = 1;
    private List<AndroidBean.ResultsBean> mAndroidBeenLists;
    private AndroidBean mAndroidBean;
    private LinearLayoutManager mLinearLayoutManager;

    @Override
    protected int getContentView() {
        return R.layout.fragment_android;
    }

    @Override
    protected void initView() {
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        androidRecyclerView.setLayoutManager(mLinearLayoutManager);
        androidAdapter = new MultiTypeAdapter();
        androidAdapter.register(AndroidBean.ResultsBean.class, new AndroidItemViewBinder());
        androidRecyclerView.setAdapter(androidAdapter);
        mOnBottomScrollListener = new onBottomScrollListener();
        androidRecyclerView.addOnScrollListener(mOnBottomScrollListener);
    }

    private class onBottomScrollListener extends RecyclerView.OnScrollListener {


        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            boolean isBottomed = mLinearLayoutManager.findLastVisibleItemPosition()
                    >= recyclerView.getAdapter().getItemCount() - 1;
            if (!swipeRefresh.isRefreshing() && isBottomed) {
                if (!mIsFirstTimeTouchBottom) {
                    Logger.d("----上啦加载");
                    if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_SETTLING) {
                        presenter.onObtainData(PRELOAD_SIZE, ++mPage);
                    }
                } else {
                    Logger.d("---第一次触摸底部");
                    mIsFirstTimeTouchBottom = false;
                }
            }
        }
    }

    @Override
    protected void initData() {
        mAndroidBeenLists = new ArrayList<>();
        if (null != presenter) {
            presenter.onObtainData(PRELOAD_SIZE, mPage);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (null != mAndroidBeenLists && mAndroidBeenLists.size() > 0) {
                    mAndroidBeenLists.clear();
                }
                presenter.onObtainData(PRELOAD_SIZE, mPage);
            }
        });
    }

    @Override
    public void setPresenter(IAndroidContract.IAndroidPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onRefresh(final int refresh) {
        if (swipeRefresh.isRefreshing()) {
            swipeRefresh.postDelayed(new Runnable() {
                @Override
                public void run() {
                    swipeRefresh.setRefreshing(refresh == RefreshConstantField.REFRESHING
                            ? true : false);
                }
            }, RefreshConstantField.REFRESHING_DELAY_MILLIS);
        } else {
            swipeRefresh.setRefreshing(refresh == RefreshConstantField.REFRESHING
                    ? true : false);
        }
    }

    @Override
    public void onResultSuccess(AndroidBean androidBean) {
        mAndroidBean = androidBean;
        if (androidAdapter != null) {
            mAndroidBeenLists.addAll(androidBean.getResults());
            androidAdapter.setItems(mAndroidBeenLists);
            androidAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onResultFail(Exception e) {

    }

    @Override
    public void onDestroy() {
        androidRecyclerView.removeOnScrollListener(mOnBottomScrollListener);
        super.onDestroy();

    }
}
