package com.afollestad.cabinet.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.afollestad.cabinet.file.CloudFile;
import com.afollestad.cabinet.file.File;
import com.afollestad.cabinet.file.LocalFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Has convenience methods for managing your navigation drawer directory shortcuts.
 *
 * @author Aidan Follestad (afollestad)
 */
public class Shortcuts {

    private final static String COMMA_ENTITY = "&#44;";

    public static void save(Context context, List<File> files) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String toSet = "";
        for (int i = 0; i < files.size(); i++) {
            if (i > 0) toSet += ",";
            toSet += files.get(i).toString().replace(",", COMMA_ENTITY);
        }
        prefs.edit().putString("shortcuts", toSet).commit();
    }

    public static void add(Context context, File dir) {
        List<File> files = getAll(context);
        files.add(dir);
        save(context, files);
    }

    public static List<File> getAll(Context context) {
        List<File> files = new ArrayList<File>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String shortcuts = prefs.getString("shortcuts", null);
        if (shortcuts == null) return files;

        String[] splitShortcuts = shortcuts.split(",");
        for (String item : splitShortcuts) {
            if (item.trim().isEmpty()) continue;
            item = item.replace(COMMA_ENTITY, ",");
            files.add(item.startsWith("REMOTE:") ? new CloudFile(context, item) : new LocalFile(item));
        }
        return files;
    }

    public static boolean contains(Context context, File dir) {
        if (dir.isStorageDirectory() || dir.isRootDirectory())
            return true;
        List<File> shortcuts = getAll(context);
        for (File fi : shortcuts) {
            if (fi.toString().equals(dir.toString())) return true;
        }
        return false;
    }

    public static void remove(Context context, int index) {
        List<File> files = getAll(context);
        files.remove(index);
        save(context, files);
    }
}