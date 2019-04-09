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

package com.cw.litenotetv.util;
/**
 * This file provides simple End User License Agreement
 * It shows a simple dialog with the license text, and two buttons.
 * If user clicks on 'cancel' button, app closes and user will not be granted access to app.
 * If user clicks on 'accept' button, app access is allowed and this choice is saved in preferences
 * so next time this will not show, until next upgrade.
 */
 
import com.cw.litenotetv.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

public class Dialog_EULA {

    private String EULA_PREFIX = "appEULA";
    private AppCompatActivity mAct;
    private SharedPreferences prefs;
    private String eulaKey;
    public DialogInterface.OnClickListener clickListener_Ok;
    public DialogInterface.OnClickListener clickListener_No;

    private String title;
    private String message;

    public Dialog_EULA(AppCompatActivity act ){
        mAct = act;
        // EULA title
        title = mAct.getString(R.string.app_name) +
                " v" +
                getPackageInfo().versionName;

        // EULA text
        message = mAct.getString(R.string.EULA_string);
    }

    public boolean isEulaAlreadyAccepted() {
        PackageInfo versionInfo = getPackageInfo();
        prefs= PreferenceManager.getDefaultSharedPreferences(mAct);
        // The eulaKey changes every time you increment the version number in
        // the AndroidManifest.xml
        eulaKey = EULA_PREFIX + versionInfo.versionCode;

        return prefs.getBoolean(eulaKey, false);
    }

    private PackageInfo getPackageInfo() {
        PackageInfo info = null;
        try {
            info = mAct.getPackageManager().getPackageInfo(
                    mAct.getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info;
    }
 
    public void applyPreference()
    {
        // Mark this version as read.
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(eulaKey, true);
        editor.apply();
    }

    public void show()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(mAct)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.accept, clickListener_Ok)
                .setNegativeButton(android.R.string.cancel,clickListener_No);
        builder.create().show();
    }
}