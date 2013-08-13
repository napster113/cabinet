package com.afollestad.cabinet.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;
import com.afollestad.cabinet.R;
import com.afollestad.cabinet.file.File;
import com.afollestad.silk.Silk;
import com.afollestad.silk.adapters.SilkAdapter;

/**
 * The adapter for {@link File} objects, used by the {@link com.afollestad.cabinet.fragments.DirectoryFragment}.
 *
 * @author Aidan Follestad
 */
public class FileAdapter extends SilkAdapter<File> {

    public FileAdapter(Context context, ThumbnailClickListener thumbnailListener) {
        super(context);
        mThumbnailListener = thumbnailListener;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        showHidden = prefs.getBoolean("show_hidden_files", false);
        isTablet = Silk.isTablet(context);
    }

    public static interface ThumbnailClickListener {
        public void onThumbnailClicked(int index);
    }

    private final ThumbnailClickListener mThumbnailListener;

    private boolean showHidden;
    private int sortSetting;
    private final boolean isTablet;

    public boolean invalidate() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean show = prefs.getBoolean("show_hidden_files", false);
        int sort = Integer.parseInt(prefs.getString("file_sorting", "0"));
        boolean different = sort != sortSetting || show != showHidden;
        showHidden = show;
        sortSetting = sort;
        return different;
    }

    @Override
    public void add(File toAdd) {
        if (!showHidden && toAdd.isHidden()) return;
        super.add(toAdd);
    }

    @Override
    public int getLayout(int type) {
        return R.layout.list_item_file;
    }

    private int getMimeIcon(File file, String mime) {
        int attr;
        if (file.isDirectory()) {
            attr = R.attr.ic_folder;
        } else if (mime == null) {
            attr = R.attr.ic_file;
        } else if (mime.startsWith("image/")) {
            attr = R.attr.ic_picture;
        } else if (mime.startsWith("video/")) {
            attr = R.attr.ic_video;
        } else if (mime.startsWith("audio/") || mime.equals("application/ogg")) {
            attr = R.attr.ic_audio;
        } else {
            attr = R.attr.ic_file;
        }
        TypedArray a = getContext().getTheme().obtainStyledAttributes(new int[]{attr});
        return a.getResourceId(0, 0);
    }

    @Override
    public View onViewCreated(final int index, View recycled, File item) {
        ImageView image = (ImageView) recycled.findViewById(R.id.image);
        TextView title = (TextView) recycled.findViewById(R.id.title);
        TextView details = (TextView) recycled.findViewById(R.id.details);
        String mime = item.getMimeType();

        title.setText(item.getName());
        String detailsStr = item.getSizeString(getContext());
        if (!item.isDirectory()) {
            if (mime != null) {
                detailsStr += " — " + mime;
            } else if (item.getExtension() != null && !item.getExtension().trim().isEmpty()) {
                detailsStr += " — " + getContext().getString(R.string.manual_mime).replace("{extension}", item.getExtension());
            }
        }
        details.setText(detailsStr);

        ThumbnailTask.ViewHolder holder;
        if (recycled.getTag() != null) {
            holder = (ThumbnailTask.ViewHolder) recycled.getTag();
        } else {
            holder = new ThumbnailTask.ViewHolder();
            holder.thumbnail = image;
            recycled.setTag(holder);
        }

        holder.position = index;
        int mimeIcon = getMimeIcon(item, mime);
        if (mime != null && (mime.startsWith("image/") || mime.equals("application/vnd.android.package-archive")) &&
                getScrollState() != AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
            new ThumbnailTask(getContext(), index, holder).execute(item);
        } else {
            image.setImageResource(mimeIcon);
        }

        if (!isTablet) {
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mThumbnailListener.onThumbnailClicked(index);
                }
            });
        }

        return recycled;
    }
}
