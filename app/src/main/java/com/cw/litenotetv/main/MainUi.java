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

package com.cw.litenotetv.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import com.cw.litenotetv.R;
import com.cw.litenotetv.db.DB_drawer;
import com.cw.litenotetv.db.DB_folder;
import com.cw.litenotetv.db.DB_page;
import com.cw.litenotetv.page.Page_recycler;
import com.cw.litenotetv.tabs.TabsHost;
import com.cw.litenotetv.util.CustomWebView;
import com.cw.litenotetv.util.Util;
import com.cw.litenotetv.util.preferences.Pref;

import java.util.Date;

/**
 * Created by cw on 2017/10/7.
 */

public class MainUi {

    MainUi(){}

    /**
     * Add note with Intent link
     */
    String title;
    String addNote_IntentLink(Intent intent,final AppCompatActivity act)
    {
        Bundle extras = intent.getExtras();
        String pathOri = null;
        String path;
        if(extras != null)
            pathOri = extras.getString(Intent.EXTRA_TEXT);
        else
            System.out.println("MainUi / _addNote_IntentLink / extras == null");

        path = pathOri;

        if(!Util.isEmptyString(pathOri))
        {
            System.out.println("MainUi / _addNote_IntentLink / pathOri = " + pathOri);
            // for SoundCloud case, path could contain other strings before URI path
            if(pathOri.contains("http"))
            {
                String[] str = pathOri.split("http");

                for(int i=0;i< str.length;i++)
                {
                    if(str[i].contains("://"))
                        path = "http".concat(str[i]);
                }
            }

            DB_drawer db_drawer = new DB_drawer(act);
            DB_folder db_folder = new DB_folder(act, Pref.getPref_focusView_folder_tableId(MainAct.mAct));
            if((db_drawer.getFoldersCount(true) == 0) || (db_folder.getPagesCount(true) == 0))
            {
                Toast.makeText(act,"No folder or no page yet, please add a new one in advance.",Toast.LENGTH_LONG).show();
                return null;
            }

            System.out.println("MainUi / _addNote_IntentLink / path = " + path);
            DB_page dB_page = new DB_page(act,Pref.getPref_focusView_page_tableId(MainAct.mAct));
            dB_page.open();
            dB_page.insertNote("", "", "", "", path, "", 0, (long) 0);// add new note, get return row Id
            dB_page.close();

            // save to top or to bottom
            final String link =path;
            int count = dB_page.getNotesCount(true);
            SharedPreferences pref_show_note_attribute = act.getSharedPreferences("add_new_note_option", 0);

            // swap if new position is top
            boolean isAddedToTop = pref_show_note_attribute.getString("KEY_ADD_NEW_NOTE_TO","bottom").equalsIgnoreCase("top");
            if( isAddedToTop && (count > 1) )
            {
                Page_recycler.swap(dB_page);
            }

            // update title: YouTube
            if( Util.isYouTubeLink(path))
            {
                title = Util.getYouTubeTitle(path);

                if(pref_show_note_attribute
                        .getString("KEY_ENABLE_LINK_TITLE_SAVE", "yes")
                        .equalsIgnoreCase("yes"))
                {
                    Date now = new Date();

                    long row_id;
                    if(isAddedToTop)
                        row_id = dB_page.getNoteId(0,true);
                    else
                        row_id = dB_page.getNoteId(count-1,true);

                    dB_page.updateNote(row_id, title, "", "", "", path, "", 0, now.getTime(), true); // update note
                }

                Toast.makeText(act,
                        act.getResources().getText(R.string.add_new_note_option_title) + title,
                        Toast.LENGTH_SHORT)
                        .show();
            }
            // update title: Web page
            else if(!Util.isEmptyString(path) &&
                    path.startsWith("http")   &&
                    !Util.isYouTubeLink(path)   )
            {
//                System.out.println("MainUi / _addNote_IntentLink / Web page");
                title = path; //set default
                final CustomWebView web = new CustomWebView(act);
                web.loadUrl(path);
                web.setVisibility(View.INVISIBLE);

                web.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public void onReceivedTitle(WebView view, String titleReceived) {
                        super.onReceivedTitle(view, titleReceived);
//                        System.out.println("MainUi / _addNote_IntentLink / Web page / onReceivedTitle");
                        if (!TextUtils.isEmpty(titleReceived) &&
                           !titleReceived.equalsIgnoreCase("about:blank"))
                        {
                            SharedPreferences pref_show_note_attribute = act.getSharedPreferences("add_new_note_option", 0);
                            if(pref_show_note_attribute
                                    .getString("KEY_ENABLE_LINK_TITLE_SAVE", "yes")
                                    .equalsIgnoreCase("yes"))
                            {
                                Date now = new Date();
                                DB_page dB_page = new DB_page(act, Pref.getPref_focusView_page_tableId(act));
                                long row_id;
                                if(isAddedToTop)
                                    row_id = dB_page.getNoteId(0,true);
                                else
                                    row_id = dB_page.getNoteId(count-1,true);

                                dB_page.updateNote(row_id, titleReceived, "", "", "", link, "", 0, now.getTime(), true); // update note
                            }

                            Toast.makeText(act,
                                    act.getResources().getText(R.string.add_new_note_option_title) + titleReceived,
                                    Toast.LENGTH_SHORT)
                                    .show();
                            CustomWebView.pauseWebView(web);
                            CustomWebView.blankWebView(web);

                            title = titleReceived;
                        }
                    }
                });
            }
            else // other
            {
                title = pathOri;
                if (pref_show_note_attribute.getString("KEY_ADD_NEW_NOTE_TO", "bottom").equalsIgnoreCase("top") &&
                        (count > 1)) {
                    Page_recycler.swap(dB_page);
                }

                Toast.makeText(act,
                        act.getResources().getText(R.string.add_new_note_option_title) + title,
                        Toast.LENGTH_SHORT)
                        .show();
            }

            return title;
        }
        else
            return null;
    }


    /****************************
     *          YouTube
     *
     ****************************/
    /**
     *  get YouTube link
     */
    String getYouTubeLink(AppCompatActivity act,int pos)
    {
        DB_page dB_page = new DB_page(act, TabsHost.getCurrentPageTableId());

        dB_page.open();
        int count = dB_page.getNotesCount(false);
        dB_page.close();

        if(pos >= count)
        {
            pos = 0;
            Page_recycler.mCurrPlayPosition = 0;
        }

        String linkStr="";
        if(pos < count)
            linkStr =dB_page.getNoteLinkUri(pos,true);

        return linkStr;
    }

    /**
     *  launch next YouTube intent
     */
    void launchNextYouTubeIntent(AppCompatActivity act, Handler handler, Runnable runCountDown)
    {
        String link = getYouTubeLink(act,TabsHost.getCurrentPage().mCurrPlayPosition);
        if( Util.isYouTubeLink(link) )
        {
            Util.openLink_YouTube(act, link);
            cancelYouTubeHandler(handler,runCountDown);
        }
    }

    /**
     *  cancel YouTube Handler
     */
    void cancelYouTubeHandler(Handler handler,Runnable runCountDown)
    {
        if(handler != null) {
            handler.removeCallbacks(runCountDown);
//            handler = null;
        }
    }

}
