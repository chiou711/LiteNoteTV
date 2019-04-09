/*
 * Copyright (C) 2018 CW Chiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cw.litenotetv.util.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;


/**
 * Created by cw on 2017/10/11.
 */

public class Pref
{
    // set folder table id of focus view
    public static void setPref_focusView_folder_tableId(Activity act, int folderTableId )
    {
        SharedPreferences pref = act.getSharedPreferences("focus_view", 0);
        String keyName = "KEY_FOCUS_VIEW_FOLDER_TABLE_ID";
        pref.edit().putInt(keyName, folderTableId).apply();
    }

    // get folder table id of focus view
    public static int getPref_focusView_folder_tableId(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("focus_view", 0);
        String keyName = "KEY_FOCUS_VIEW_FOLDER_TABLE_ID";
        return pref.getInt(keyName, 1); // folder table Id: default is 1
    }

    // remove key of focus view for folder
    public static void removePref_focusView_folder_tableId_key(Activity act)
    {
        SharedPreferences pref = act.getSharedPreferences("focus_view", 0);
        String keyName = "KEY_FOCUS_VIEW_FOLDER_TABLE_ID";
        pref.edit().remove(keyName).apply();
    }

    // set page table id of focus view
    public static void setPref_focusView_page_tableId(Activity act, int pageTableId )
    {
        SharedPreferences pref = act.getSharedPreferences("focus_view", 0);
        String keyPrefix = "KEY_FOCUS_VIEW_PAGE_TABLE_ID_";
        int folderTableId = getPref_focusView_folder_tableId(act);
        String keyName = keyPrefix.concat(String.valueOf(folderTableId));
        pref.edit().putInt(keyName, pageTableId).apply();
    }

    // get page table id of focus view
    public static int getPref_focusView_page_tableId(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("focus_view", 0);
        String keyPrefix = "KEY_FOCUS_VIEW_PAGE_TABLE_ID_";
        int folderTableId = getPref_focusView_folder_tableId(context);
        String keyName = keyPrefix.concat(String.valueOf(folderTableId));
        // page table Id: default is 1
        return pref.getInt(keyName, 1);
    }

    // remove key of focus view for page table Id
    public static void removePref_focusView_page_tableId_key(Activity act, int folderTableId)
    {
        SharedPreferences pref = act.getSharedPreferences("focus_view", 0);
        String keyPrefix = "KEY_FOCUS_VIEW_PAGE_TABLE_ID_";
        String keyName = keyPrefix.concat(String.valueOf(folderTableId));
        pref.edit().remove(keyName).apply();
    }

    // Set list view first visible Index of focus view
    public static void setPref_focusView_list_view_first_visible_index(Activity act, int index )
    {
//		System.out.println("Pref / _setPref_focusView_list_view_first_visible_index / index = " + index);
        SharedPreferences pref = act.getSharedPreferences("focus_view", 0);
        String keyName = "KEY_LIST_VIEW_FIRST_VISIBLE_INDEX";
        String location = getCurrentListViewLocation(act);
        keyName = keyName.concat(location);
        pref.edit().putInt(keyName, index).apply();
    }

    // Get list view first visible Index of focus view
    public static Integer getPref_focusView_list_view_first_visible_index(Activity act)
    {
        SharedPreferences pref = act.getSharedPreferences("focus_view", 0);
        String keyName = "KEY_LIST_VIEW_FIRST_VISIBLE_INDEX";
        String location = getCurrentListViewLocation(act);
        keyName = keyName.concat(location);
        return pref.getInt(keyName, 0); // default scroll X is 0
    }

    // Set list view first visible index Top of focus view
    public static void setPref_focusView_list_view_first_visible_index_top(Activity act, int top )
    {
//        System.out.println("Pref / _setPref_focusView_list_view_first_visible_index_top / top = " + top);
        SharedPreferences pref = act.getSharedPreferences("focus_view", 0);
        String keyName = "KEY_LIST_VIEW_FIRST_VISIBLE_INDEX_TOP";
        String location = getCurrentListViewLocation(act);
        keyName = keyName.concat(location);
        pref.edit().putInt(keyName, top).apply();
    }

    // Get list view first visible index Top of focus view
    public static Integer getPref_focusView_list_view_first_visible_index_top(Activity act)
    {
        SharedPreferences pref = act.getSharedPreferences("focus_view", 0);
        String keyName = "KEY_LIST_VIEW_FIRST_VISIBLE_INDEX_TOP";
        String location = getCurrentListViewLocation(act);
        keyName = keyName.concat(location);
        return pref.getInt(keyName, 0);
    }

    // set key: will create default content
    public static void setPref_will_create_default_content(Activity act, boolean will)
    {
        SharedPreferences pref = act.getSharedPreferences("create_view", 0);
        String keyName = "KEY_WITH_DEFAULT_CONTENT";
        pref.edit().putBoolean(keyName, will).apply();
    }

    // get key: will create default content
    public static boolean getPref_will_create_default_content(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("create_view", 0);
        String keyName = "KEY_WITH_DEFAULT_CONTENT";
        return pref.getBoolean(keyName, false);
    }

    // location about drawer table Id and page table Id
    static String getCurrentListViewLocation(Activity act)
    {
        String strLocation = "";
        // folder
        int folderTableId = getPref_focusView_folder_tableId(act);
        String strFolderTableId = String.valueOf(folderTableId);
        // page
        int pageTableId = getPref_focusView_page_tableId(act);
        String strPageTableId = String.valueOf(pageTableId);
        strLocation = "_" + strFolderTableId + "_" + strPageTableId;
        return strLocation;
    }

    // Get YouTube auto play in note view
    public static boolean getPref_is_autoPlay_YouTubeApi(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("show_note_attribute", 0);
        String keyName = "KEY_IS_AUTO_PLAY_YOUTUBE_API";
        return pref.getBoolean(keyName, false);
    }

    // Set YouTube auto play in note view
    public static void setPref_is_autoPlay_YouTubeApi(Context context, boolean isAuto)
    {
        SharedPreferences pref = context.getSharedPreferences("show_note_attribute", 0);
        String keyName = "KEY_IS_AUTO_PLAY_YOUTUBE_API";
        pref.edit().putBoolean(keyName, isAuto).apply();
    }

    // set drawing line width
    public static void setPref_drawing_line_width(Activity act, int width )
    {
        SharedPreferences pref = act.getSharedPreferences("drawing", 0);
        String key = "KEY_DRAWING_LINE_WIDTH";
        pref.edit().putInt(key, width).apply();
    }

    // get drawing line width
    public static Integer getPref_drawing_line_width(Activity act)
    {
        SharedPreferences pref = act.getSharedPreferences("drawing", 0);
        String key = "KEY_DRAWING_LINE_WIDTH";
        return pref.getInt(key, 5);
    }

    // set drawing line alpha
    public static void setPref_drawing_line_color_alpha(Activity act, int alpha )
    {
        SharedPreferences pref = act.getSharedPreferences("drawing", 0);
        String key = "KEY_DRAWING_LINE_COLOR_ALPHA";
        pref.edit().putInt(key, alpha).apply();
    }

    // get drawing line alpha
    public static Integer getPref_drawing_line_color_alpha(Activity act)
    {
        SharedPreferences pref = act.getSharedPreferences("drawing", 0);
        String key = "KEY_DRAWING_LINE_COLOR_ALPHA";
        return pref.getInt(key, 255);
    }

    // set drawing line red
    public static void setPref_drawing_line_color_red(Activity act, int red )
    {
        SharedPreferences pref = act.getSharedPreferences("drawing", 0);
        String key = "KEY_DRAWING_LINE_COLOR_RED";
        pref.edit().putInt(key, red).apply();
    }

    // get drawing line red
    public static Integer getPref_drawing_line_color_red(Activity act)
    {
        SharedPreferences pref = act.getSharedPreferences("drawing", 0);
        String key = "KEY_DRAWING_LINE_COLOR_RED";
        return pref.getInt(key, 0);
    }

    // set drawing line green
    public static void setPref_drawing_line_color_green(Activity act, int green )
    {
        SharedPreferences pref = act.getSharedPreferences("drawing", 0);
        String key = "KEY_DRAWING_LINE_COLOR_GREEN";
        pref.edit().putInt(key, green).apply();
    }

    // get drawing line green
    public static Integer getPref_drawing_line_color_green(Activity act)
    {
        SharedPreferences pref = act.getSharedPreferences("drawing", 0);
        String key = "KEY_DRAWING_LINE_COLOR_GREEN";
        return pref.getInt(key, 0);
    }

    // set drawing line blue
    public static void setPref_drawing_line_color_blue(Activity act, int blue )
    {
        SharedPreferences pref = act.getSharedPreferences("drawing", 0);
        String key = "KEY_DRAWING_LINE_COLOR_BLUE";
        pref.edit().putInt(key, blue).apply();
    }

    // get drawing line blue
    public static Integer getPref_drawing_line_color_blue(Activity act)
    {
        SharedPreferences pref = act.getSharedPreferences("drawing", 0);
        String key = "KEY_DRAWING_LINE_COLOR_BLUE";
        return pref.getInt(key, 0);
    }

    // set recording quality
    public static void setPref_recorder_high_quality(Activity act, boolean set )
    {
        SharedPreferences pref = act.getSharedPreferences("audio_recorder_quality", 0);
        String key = "KEY_PREF_HIGH_QUALITY";
        pref.edit().putBoolean(key, set).apply();
    }

    // get recording quality
    public static boolean getPref_recorder_high_quality(Activity act)
    {
        SharedPreferences pref = act.getSharedPreferences("audio_recorder_quality", 0);
        String key = "KEY_PREF_HIGH_QUALITY";
        return pref.getBoolean(key, false);
    }
}
