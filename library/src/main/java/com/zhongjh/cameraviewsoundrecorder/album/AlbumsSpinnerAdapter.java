package com.zhongjh.cameraviewsoundrecorder.album;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.zhongjh.cameraviewsoundrecorder.R;
import com.zhongjh.cameraviewsoundrecorder.album.entity.Album;
import com.zhongjh.cameraviewsoundrecorder.settings.SelectionSpec;

import java.io.File;

/**
 * 左上角的下拉框适配器
 * Created by zhongjh on 2018/8/29.
 */
public class AlbumsSpinnerAdapter extends CursorAdapter {

    private final Drawable mPlaceholder; // 默认图片

    public AlbumsSpinnerAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);

        TypedArray ta = context.getTheme().obtainStyledAttributes(
                new int[]{R.attr.album_thumbnail_placeholder});
        mPlaceholder = ta.getDrawable(0);
        ta.recycle();
    }

    public AlbumsSpinnerAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);

        TypedArray ta = context.getTheme().obtainStyledAttributes(
                new int[]{R.attr.album_thumbnail_placeholder});
        mPlaceholder = ta.getDrawable(0);
        ta.recycle();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_matiss_spinner_zjh, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Album album = Album.valueOf(cursor);
        ((TextView) view.findViewById(R.id.album_name)).setText(album.getDisplayName(context));
        ((TextView) view.findViewById(R.id.album_media_count)).setText(String.valueOf(album.getCount()));

        // do not need to load animated Gif
        SelectionSpec.getInstance().imageEngine.loadThumbnail(context, context.getResources().getDimensionPixelSize(R
                        .dimen.media_grid_size), mPlaceholder,
                view.findViewById(R.id.album_cover), Uri.fromFile(new File(album.getCoverPath())));
    }
}
