package com.afollestad.cabinet.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.SparseBooleanArray;
import android.view.*;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;
import com.afollestad.cabinet.App;
import com.afollestad.cabinet.File;
import com.afollestad.cabinet.R;
import com.afollestad.cabinet.adapters.FileAdapter;
import com.afollestad.cabinet.ui.MainActivity;
import com.afollestad.silk.adapters.SilkAdapter;
import com.afollestad.silk.fragments.SilkListFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DirectoryFragment extends SilkListFragment<File> {

    public DirectoryFragment(File dir) {
        mPath = dir;
    }

    private final File mPath;

    public File getPath() {
        return mPath;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setSelector(R.drawable.selectable_background_cabinet);
        setupCab(getListView());
        java.io.File[] contents = mPath.listFiles();
        Arrays.sort(contents, new File.Comparator());
        getAdapter().clear();
        for (java.io.File fi : contents)
            getAdapter().add(new File(fi));
    }

    private void setupCab(ListView listView) {
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            private List<File> getSelectedFiles() {
                List<File> files = new ArrayList<File>();
                int len = getListView().getCount();
                SparseBooleanArray checked = getListView().getCheckedItemPositions();
                for (int i = 0; i < len; i++) {
                    if (checked.get(i)) files.add(getAdapter().getItem(i));
                }
                return files;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                mode.invalidate();
                int count = getListView().getCheckedItemCount();
                if (count == 1)
                    mode.setTitle(getString(R.string.one_file_selected));
                else mode.setTitle(getString(R.string.x_files_selected).replace("{X}", count + ""));
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return DirectoryCAB.handleAction(DirectoryFragment.this, item.getItemId(), getSelectedFiles(), mode);
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.contextual_ab_file, menu);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                List<File> selectedFiles = getSelectedFiles();
                boolean hasFolders = false;
                boolean hasFiles = false;
                for (File fi : selectedFiles) {
                    if (fi.isDirectory()) hasFolders = true;
                    else hasFiles = true;
                    if (hasFiles && hasFolders) break;
                }
                menu.findItem(R.id.add_shortcut).setVisible(!hasFiles);
                menu.findItem(R.id.share).setVisible(!hasFolders);
                return true;
            }
        });
    }

    @Override
    public int getEmptyText() {
        return R.string.no_files;
    }

    @Override
    protected SilkAdapter<File> initializeAdapter() {
        return new FileAdapter(getActivity(), new FileAdapter.ThumbnailClickListener() {
            @Override
            public void onThumbnailClicked(int index) {
                getListView().setItemChecked(index, !getListView().isItemChecked(index));
            }
        });
    }

    @Override
    public void onItemTapped(int index, File item, View view) {
        if (item.isDirectory()) {
            ((MainActivity) getActivity()).navigate(item, true);
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW).setDataAndType(Uri.fromFile(item), item.getMimeType());
            startActivity(Intent.createChooser(intent, getString(R.string.open_with)));
        }
    }

    @Override
    public boolean onItemLongTapped(int index, File item, View view) {
        getListView().setItemChecked(index, !getListView().isItemChecked(index));
        return true;
    }

    @Override
    public void onVisibilityChange(boolean visible) {
        // Makes sure title gets updated when you go back in the fragment back stack
        if (mPath.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath()))
            getActivity().setTitle(R.string.app_name);
        else getActivity().setTitle(mPath.getName());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_directory, menu);
        menu.findItem(R.id.paste).setVisible(App.get(getActivity()).getClipboard().canPaste(mPath));
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_shortcut:
                ((MainActivity) getActivity()).addShortcut(mPath);
                Toast.makeText(getActivity(), R.string.shorts_updated, Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
