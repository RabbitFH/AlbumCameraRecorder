package com.zhongjh.cameraviewsoundrecorder.album.ui.mediaselection;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhongjh.cameraviewsoundrecorder.R;
import com.zhongjh.cameraviewsoundrecorder.album.MatissFragment;
import com.zhongjh.cameraviewsoundrecorder.album.entity.Album;
import com.zhongjh.cameraviewsoundrecorder.album.entity.Item;
import com.zhongjh.cameraviewsoundrecorder.settings.SelectionSpec;
import com.zhongjh.cameraviewsoundrecorder.album.model.AlbumMediaCollection;
import com.zhongjh.cameraviewsoundrecorder.album.model.SelectedItemCollection;
import com.zhongjh.cameraviewsoundrecorder.album.ui.mediaselection.adapter.AlbumMediaAdapter;
import com.zhongjh.cameraviewsoundrecorder.album.utils.UIUtils;
import com.zhongjh.cameraviewsoundrecorder.album.widget.MediaGridInset;

/**
 * 相册 界面
 * Created by zhongjh on 2018/8/30.
 */
public class MediaSelectionFragment extends Fragment implements AlbumMediaCollection.AlbumMediaCallbacks,
        AlbumMediaAdapter.CheckStateListener, AlbumMediaAdapter.OnMediaClickListener {

    public static final String EXTRA_ALBUM = "extra_album";     // 专辑数据

    private final AlbumMediaCollection mAlbumMediaCollection = new AlbumMediaCollection();
    private RecyclerView mRecyclerView;
    private AlbumMediaAdapter mAdapter;
    private SelectionProvider mSelectionProvider; // 选择接口事件
    private AlbumMediaAdapter.CheckStateListener mCheckStateListener;       // 单选事件
    private AlbumMediaAdapter.OnMediaClickListener mOnMediaClickListener;   // 点击事件

    /**
     * 实例化
     *
     * @param album 专辑
     */
    public static MediaSelectionFragment newInstance(Album album) {
        MediaSelectionFragment fragment = new MediaSelectionFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_ALBUM, album);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * 生命周期 onAttach() - onCreate() - onCreateView() - onActivityCreated()
     *
     * @param context 上下文
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // 旧版的知乎是用Activity，而这边则使用Fragments的获取到MatissFragment
        Fragment matissFragment = null;
        for (Fragment fragment :getFragmentManager().getFragments()) {
            if (fragment instanceof MatissFragment){
                matissFragment = fragment;
            }
        }
        if (matissFragment == null)
            throw new IllegalStateException("matissFragment 不能为null");
        if (matissFragment instanceof SelectionProvider) {
            mSelectionProvider = (SelectionProvider) matissFragment;
        }else {
            // 并没有实现这个接口
            throw new IllegalStateException("Context must implement SelectionProvider.");
        }
        if (matissFragment instanceof AlbumMediaAdapter.CheckStateListener) {
            mCheckStateListener = (AlbumMediaAdapter.CheckStateListener) matissFragment;
        }
        if (matissFragment instanceof AlbumMediaAdapter.OnMediaClickListener) {
            mOnMediaClickListener = (AlbumMediaAdapter.OnMediaClickListener) matissFragment;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_media_selection_zjh, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(R.id.recyclerview);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Album album = getArguments().getParcelable(EXTRA_ALBUM);

        mAdapter = new AlbumMediaAdapter(getContext(),
                mSelectionProvider.provideSelectedItemCollection(), mRecyclerView);
        mAdapter.registerCheckStateListener(this);
        mAdapter.registerOnMediaClickListener(this);
        mRecyclerView.setHasFixedSize(true);

        // 设置recyclerView的布局
        int spanCount;
        SelectionSpec selectionSpec = SelectionSpec.getInstance();
        if (selectionSpec.gridExpectedSize > 0) {
            spanCount = UIUtils.spanCount(getContext(), selectionSpec.gridExpectedSize);
        } else {
            spanCount = selectionSpec.spanCount;
        }
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));

        // 加载线，recyclerView加载数据
        int spacing = getResources().getDimensionPixelSize(R.dimen.media_grid_spacing);
        mRecyclerView.addItemDecoration(new MediaGridInset(spanCount, spacing, false));
        mRecyclerView.setAdapter(mAdapter);
        mAlbumMediaCollection.onCreate(getActivity(), this);
        mAlbumMediaCollection.load(album, selectionSpec.capture);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAlbumMediaCollection.onDestroy();
    }

    /**
     * 刷新数据源
     */
    public void refreshMediaGrid() {
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 刷新所能看到的选择
     */
    public void refreshSelection() {
        mAdapter.refreshSelection();
    }

    /**
     * 加载数据完毕
     *
     * @param cursor 光标数据
     */
    @Override
    public void onAlbumMediaLoad(Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    /**
     * 当一个已创建的加载器被重置从而使其数据无效时，此方法被调用
     */
    @Override
    public void onAlbumMediaReset() {
        // 此处是用于上面的onLoadFinished()的游标将被关闭时执行，我们需确保我们不再使用它
        mAdapter.swapCursor(null);
    }

    @Override
    public void onUpdate() {
        // 通知外部活动检查状态改变
        if (mCheckStateListener != null) {
            mCheckStateListener.onUpdate();
        }
    }

    @Override
    public void onMediaClick(Album album, Item item, int adapterPosition) {
        if (mOnMediaClickListener != null) {
            mOnMediaClickListener.onMediaClick(getArguments().getParcelable(EXTRA_ALBUM),
                    item, adapterPosition);
        }
    }

    /**
     * 点击接口
     */
    public interface SelectionProvider {
        SelectedItemCollection provideSelectedItemCollection();
    }

}
