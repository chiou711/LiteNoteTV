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

package com.cw.litenotetv.note;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cw.litenotetv.R;
import com.cw.litenotetv.db.DB_page;
import com.cw.litenotetv.operation.mail.MailNotes;
import com.cw.litenotetv.operation.youtube.SearchYouTube;
import com.cw.litenotetv.tabs.TabsHost;
import com.cw.litenotetv.util.Util;
import com.cw.litenotetv.util.preferences.Pref;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cw on 2017/10/7.
 */
public class View_note_option {
    int option_id;
    int option_drawable_id;
    int option_string_id;

    View_note_option(int id, int draw_id, int string_id)
    {
        this.option_id = id;
        this.option_drawable_id = draw_id;
        this.option_string_id = string_id;
    }

    /**
     *
     * 	Add new note
     *
     */
    static List<View_note_option> option_list;

    private final static int ID_OPTION_MAIL = 0;
    private final static int ID_OPTION_AUTO_PLAY = 1;
    private final static int ID_OPTION_SEARCH_YOUTUBE = 2;
    private final static int ID_OPTION_BACK = 9;
    static long noteId;
    static GridIconAdapter mGridIconAdapter;

    public static void note_option(final AppCompatActivity act, long _noteId)
    {
        AbsListView gridView;
        noteId = _noteId;
        // get layout inflater
        View rootView = act.getLayoutInflater().inflate(R.layout.option_grid, null);

        option_list = new ArrayList<>();

        // mail
        option_list.add(new View_note_option(ID_OPTION_MAIL ,
                        android.R.drawable.ic_menu_send,
                        R.string.mail_notes_btn));

        // auto play
        if(Pref.getPref_is_autoPlay_YouTubeApi(act))
            option_list.add(new View_note_option(ID_OPTION_AUTO_PLAY,
                            R.drawable.btn_radio_on_holo_dark,
                            R.string.view_note_auto_play));
        else
            option_list.add(new View_note_option(ID_OPTION_AUTO_PLAY,
                            R.drawable.btn_radio_off_holo_dark,
                            R.string.view_note_auto_play));

        // search youtube with keyword
        option_list.add(new View_note_option(ID_OPTION_SEARCH_YOUTUBE ,
                R.drawable.ic_youtube,
                R.string.search_youtube));

        // Back
        option_list.add(new View_note_option(ID_OPTION_BACK,
                R.drawable.ic_menu_back,
                R.string.btn_back));

        gridView = (GridView) rootView.findViewById(R.id.option_grid_view);

        // check if directory is created AND not empty
        if( (option_list != null  ) && (option_list.size() > 0))
        {
            mGridIconAdapter = new GridIconAdapter(act);
            gridView.setAdapter(mGridIconAdapter);
        }
        else
        {
            Toast.makeText(act,R.string.gallery_toast_no_file, Toast.LENGTH_SHORT).show();
            act.finish();
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("View_note_option / _note_option / _OnItemClickListener / position = " + position +" id = " + id);
                startAddNoteActivity(act, option_list.get(position).option_id);
            }
        });

        // set view to dialog
        AlertDialog.Builder builder1 = new AlertDialog.Builder(act);
        builder1.setView(rootView);
        dlgAddNew = builder1.create();
        dlgAddNew.show();
    }
    private static AlertDialog dlgAddNew;

    private static void startAddNoteActivity(AppCompatActivity act,int optionId)
    {
        System.out.println("View_note_option / _startAddNoteActivity / optionId = " + optionId);

        switch (optionId) {
            case ID_OPTION_MAIL:
            {
				// set Sent string Id
				String sentString = Util.getStringWithXmlTag(TabsHost.getFocus_tabPos(),noteId);
				sentString = Util.addXmlTag(sentString);

                DB_page dB_page = new DB_page(act, TabsHost.getCurrentPageTableId());

                // picture first priority
                String picFile = dB_page.getNotePictureUri_byId(noteId);

				// then drawing
				if(Util.isEmptyString(picFile))
                    picFile = dB_page.getNoteDrawingUri_byId(noteId);

                System.out.println("-> picFile = " + picFile);

				String[] picFileArray = null;
				if( (picFile != null) &&
						(picFile.length() > 0) )
				{
					picFileArray = new String[]{picFile};
				}
				new MailNotes(act,sentString,picFileArray);
            }
            break;

            case ID_OPTION_AUTO_PLAY:
            {
                // toggle setting
                if(Pref.getPref_is_autoPlay_YouTubeApi(act)) {
                    Pref.setPref_is_autoPlay_YouTubeApi(act,false);
                    option_list.set(optionId,
                            new View_note_option(ID_OPTION_AUTO_PLAY,
                                    R.drawable.btn_radio_off_holo_dark,
                                    R.string.view_note_auto_play));
                }
                else {
                    Pref.setPref_is_autoPlay_YouTubeApi(act,true);
                    option_list.set(optionId,
                            new View_note_option(ID_OPTION_AUTO_PLAY,
                                    R.drawable.btn_radio_on_holo_dark,
                                    R.string.view_note_auto_play));
                }

                mGridIconAdapter.notifyDataSetChanged();
            }
            break;

            case ID_OPTION_SEARCH_YOUTUBE:
            {
                dlgAddNew.dismiss();

                DB_page dB_page = new DB_page(act, TabsHost.getCurrentPageTableId());
                String keyWord = dB_page.getNoteTitle_byId(noteId);
                Intent intent = new Intent(act,SearchYouTube.class);
                intent.putExtra("search_keywords", keyWord );
                act.startActivity(intent);
            }

            break;

            case ID_OPTION_BACK:
            {
                dlgAddNew.dismiss();
            }
            break;

            // default
            default:
                break;
        }

    }


    /**
     * Created by cw on 2017/10/7.
     */
    static class GridIconAdapter extends BaseAdapter {
        private AppCompatActivity act;
        GridIconAdapter(AppCompatActivity fragAct){act = fragAct;}

        @Override
        public int getCount() {
            return option_list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            View view = convertView;
            if (view == null) {
                view = act.getLayoutInflater().inflate(R.layout.add_note_grid_item, parent, false);
                holder = new ViewHolder();
                assert view != null;
                holder.imageView = (ImageView) view.findViewById(R.id.grid_item_image);
                holder.text = (TextView) view.findViewById(R.id.grid_item_text);
                holder.text.setTextColor(Color.WHITE);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            Drawable drawable = act.getResources().getDrawable(option_list.get(position).option_drawable_id);
            holder.imageView.setImageDrawable(drawable);
            holder.text.setText(option_list.get(position).option_string_id);
            return view;
        }

        private class ViewHolder {
            ImageView imageView;
            TextView text;
        }
    }
}
