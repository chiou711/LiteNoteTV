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

package com.cw.litenotetv.page;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cw.litenotetv.R;
import com.cw.litenotetv.db.DB_drawer;
import com.cw.litenotetv.db.DB_folder;
import com.cw.litenotetv.db.DB_page;
import com.cw.litenotetv.folder.FolderUi;
import com.cw.litenotetv.main.MainAct;
import com.cw.litenotetv.note.Note;
import com.cw.litenotetv.note_edit.Note_edit;
import com.cw.litenotetv.operation.audio.Audio_manager;
import com.cw.litenotetv.operation.audio.AudioPlayer_page;
import com.cw.litenotetv.operation.audio.BackgroundAudioService;
import com.cw.litenotetv.page.item_touch_helper.ItemTouchHelperAdapter;
import com.cw.litenotetv.page.item_touch_helper.ItemTouchHelperViewHolder;
import com.cw.litenotetv.page.item_touch_helper.OnStartDragListener;
import com.cw.litenotetv.tabs.AudioUi_page;
import com.cw.litenotetv.tabs.TabsHost;
import com.cw.litenotetv.util.ColorSet;
import com.cw.litenotetv.util.CustomWebView;
import com.cw.litenotetv.util.Util;
import com.cw.litenotetv.util.audio.UtilAudio;
import com.cw.litenotetv.util.image.AsyncTaskAudioBitmap;
import com.cw.litenotetv.util.image.UtilImage;
import com.cw.litenotetv.util.image.UtilImage_bitmapLoader;
import com.cw.litenotetv.util.preferences.Pref;
import com.cw.litenotetv.util.uil.UilCommon;
import com.cw.litenotetv.util.video.UtilVideo;
import com.google.android.youtube.player.YouTubeIntents;

import static com.cw.litenotetv.db.DB_page.KEY_NOTE_AUDIO_URI;
import static com.cw.litenotetv.db.DB_page.KEY_NOTE_BODY;
import static com.cw.litenotetv.db.DB_page.KEY_NOTE_CREATED;
import static com.cw.litenotetv.db.DB_page.KEY_NOTE_DRAWING_URI;
import static com.cw.litenotetv.db.DB_page.KEY_NOTE_LINK_URI;
import static com.cw.litenotetv.db.DB_page.KEY_NOTE_MARKING;
import static com.cw.litenotetv.db.DB_page.KEY_NOTE_PICTURE_URI;
import static com.cw.litenotetv.db.DB_page.KEY_NOTE_TITLE;
import static com.cw.litenotetv.page.Page_recycler.mDb_page;
import static com.cw.litenotetv.page.Page_recycler.swapRows;
import static com.cw.litenotetv.util.Util.getYoutubeId;

// Pager adapter
public class PageAdapter_recycler extends RecyclerView.Adapter<PageAdapter_recycler.ViewHolder>
        implements ItemTouchHelperAdapter
{
	private AppCompatActivity mAct;
	private Cursor cursor;
	private int count;
	private static String linkUri;
	private static int style;
    DB_folder dbFolder;
	private int page_pos;
    private final OnStartDragListener mDragStartListener;

    PageAdapter_recycler(Cursor _cursor, int _page_pos, OnStartDragListener dragStartListener) {
        cursor = _cursor;
        page_pos = _page_pos;

        if(_cursor != null)
            count = _cursor.getCount();
        else
            count = 0;

        System.out.println("PageAdapter_recycler / _constructor / count = " + count);
        System.out.println("PageAdapter_recycler / _constructor / page_pos = " + page_pos);

        // add this for fixing java.lang.IllegalStateException: attempt to re-open an already-closed object
        mDb_page.open();
        mDb_page.close();

        mAct = MainAct.mAct;

        mDragStartListener = dragStartListener;

        dbFolder = new DB_folder(mAct,Pref.getPref_focusView_folder_tableId(mAct));
    }

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        ImageView btnMarking;
        ImageView btnViewNote;
        ImageView btnEditNote;
        ImageView btnPlayAudio;
        ImageView btnPlayYouTube;
        ImageView btnPlayWeb;
		TextView rowId;
		View audioBlock;
		ImageView iconAudio;
		TextView audioName;
		TextView textTitle;
		TextView textBody;
		TextView textTime;
        ImageViewCustom btnDrag;
		View thumbBlock;
		ImageView thumbPicture;
		ImageView thumbAudio;
		CustomWebView thumbWeb;
		ProgressBar progressBar;

        public ViewHolder(View v) {
            super(v);

            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

            textTitle = (TextView) v.findViewById(R.id.row_title);
            rowId= (TextView) v.findViewById(R.id.row_id);
            audioBlock = v.findViewById(R.id.audio_block);
            iconAudio = (ImageView) v.findViewById(R.id.img_audio);
            audioName = (TextView) v.findViewById(R.id.row_audio_name);
            btnMarking = (ImageView) v.findViewById(R.id.btn_marking);
            btnViewNote = (ImageView) v.findViewById(R.id.btn_view_note);
            btnEditNote = (ImageView) v.findViewById(R.id.btn_edit_note);
            btnPlayAudio = (ImageView) v.findViewById(R.id.btn_play_audio);
            btnPlayYouTube = (ImageView) v.findViewById(R.id.btn_play_youtube);
            btnPlayWeb = (ImageView) v.findViewById(R.id.btn_play_web);
            thumbBlock = v.findViewById(R.id.row_thumb_nail);
            thumbPicture = (ImageView) v.findViewById(R.id.thumb_picture);
            thumbAudio = (ImageView) v.findViewById(R.id.thumb_audio);
            thumbWeb = (CustomWebView) v.findViewById(R.id.thumb_web);
            btnDrag = (ImageViewCustom) v.findViewById(R.id.btn_drag);
            progressBar = (ProgressBar) v.findViewById(R.id.thumb_progress);
            textTitle = (TextView) v.findViewById(R.id.row_title);
            textBody = (TextView) v.findViewById(R.id.row_body);
            textTime = (TextView) v.findViewById(R.id.row_time);
        }

        public TextView getTextView() {
            return textTitle;
        }

        @Override
        public void onItemSelected() {
//            itemView.setBackgroundColor(Color.LTGRAY);
            ((CardView)itemView).setCardBackgroundColor(MainAct.mAct.getResources().getColor(R.color.button_color));
        }

        @Override
        public void onItemClear() {
            ((CardView)itemView).setCardBackgroundColor(ColorSet.mBG_ColorArray[style]);
        }

    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.page_view_card, viewGroup, false);

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

//        System.out.println("PageAdapter_recycler / _onBindViewHolder / position = " + position);

        // style
        style = dbFolder.getPageStyle(page_pos, true);

        ((CardView)holder.itemView).setCardBackgroundColor(ColorSet.mBG_ColorArray[style]);

	    ((CardView)holder.itemView).setClickable(true);
	    ((CardView)holder.itemView).setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
			    if(cursor.moveToPosition(position)) {
				    String idStr = getYoutubeId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_LINK_URI)));
				    Intent intent = YouTubeIntents.createPlayVideoIntentWithOptions(MainAct.mAct, idStr, false/*fullscreen*/, true/*finishOnEnd*/);
				    MainAct.mAct.startActivity(intent);
			    }
		    }
	    });

	    ((CardView)holder.itemView).setFocusable(true);
	    ((CardView)holder.itemView).setOnFocusChangeListener(new View.OnFocusChangeListener() {
		    @Override
		    public void onFocusChange(View v, boolean hasFocus) {
			    if(hasFocus) {
				    ((CardView)holder.itemView).setCardBackgroundColor(Color.rgb(0xf0,0xf0,0xc0));
			    }
			    else {
				    ((CardView)holder.itemView).setCardBackgroundColor(ColorSet.mBG_ColorArray[style]);
			    }

			    float mBaseElevation = ((CardView)holder.itemView).getCardElevation();

			    if(hasFocus) {
				    ((CardView)holder.itemView).setMaxCardElevation(mBaseElevation* (0.1f));// Color(Color.rgb(0xf0,0xa0,70));
			    }
			    else {
				    ((CardView)holder.itemView).setMaxCardElevation(mBaseElevation);
			    }
		    }
	    });

        // get DB data
        String strTitle = null;
        String strBody = null;
        String pictureUri = null;
        String audioUri = null;
        String drawingUri = null;
        Long timeCreated = null;
        linkUri = null;
        int marking = 0;

		SharedPreferences pref_show_note_attribute = MainAct.mAct.getSharedPreferences("show_note_attribute", 0);

        if(cursor.moveToPosition(position)) {
            strTitle = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_TITLE));
            strBody = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_BODY));
            pictureUri = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_PICTURE_URI));
            audioUri = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_AUDIO_URI));
            linkUri = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_LINK_URI));
            drawingUri = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_DRAWING_URI));
            marking = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NOTE_MARKING));
            timeCreated = cursor.getLong(cursor.getColumnIndex(KEY_NOTE_CREATED));
        }

        /**
         *  control block
         */
        // show row Id
        holder.rowId.setText(String.valueOf(position+1));
        holder.rowId.setTextColor(ColorSet.mText_ColorArray[style]);


        // show marking check box
        if(marking == 1)
        {
            holder.btnMarking.setBackgroundResource(style % 2 == 1 ?
                    R.drawable.btn_check_on_holo_light :
                    R.drawable.btn_check_on_holo_dark);
        }
        else
        {
            holder.btnMarking.setBackgroundResource(style % 2 == 1 ?
                    R.drawable.btn_check_off_holo_light :
                    R.drawable.btn_check_off_holo_dark);
        }

        // show drag button
        if(pref_show_note_attribute.getString("KEY_ENABLE_DRAGGABLE", "yes").equalsIgnoreCase("yes"))
            holder.btnDrag.setVisibility(View.VISIBLE);
        else
            holder.btnDrag.setVisibility(View.GONE);

        // show audio button
        if( !Util.isEmptyString(audioUri) && (marking == 1) && Util.isUriExisted(audioUri,mAct))
            holder.btnPlayAudio.setVisibility(View.VISIBLE);
        else
            holder.btnPlayAudio.setVisibility(View.GONE);

        // show/hide play YouTube button, on play Web button
        if(!Util.isEmptyString(linkUri) &&
           linkUri.startsWith("http")      )
        {
            if(Util.isYouTubeLink(linkUri))
            {
                // YouTube
                holder.btnPlayYouTube.setVisibility(View.VISIBLE);
                holder.btnPlayWeb.setVisibility(View.GONE);
            }
            else
            {
                // Web
                holder.btnPlayYouTube.setVisibility(View.GONE);
                holder.btnPlayWeb.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            holder.btnPlayYouTube.setVisibility(View.GONE);
            holder.btnPlayWeb.setVisibility(View.GONE);
        }

        // set audio name
        String audio_name = null;
        if(!Util.isEmptyString(audioUri))
            audio_name = Util.getDisplayNameByUriString(audioUri, mAct);

        // show audio name
        if(Util.isUriExisted(audioUri, mAct))
            holder.audioName.setText(audio_name);
        else
            holder.audioName.setText(R.string.file_not_found);

//			holder.audioName.setTextSize(12.0f);

        if(!Util.isEmptyString(audioUri))
            holder.audioName.setTextColor(ColorSet.mText_ColorArray[style]);

        // show audio highlight if audio is not at Stop
        if( PageUi.isAudioPlayingPage() &&
            (marking !=0) &&
            (position == Audio_manager.mAudioPos)  &&
            (Audio_manager.getPlayerState() != Audio_manager.PLAYER_AT_STOP) &&
            (Audio_manager.getAudioPlayMode() == Audio_manager.PAGE_PLAY_MODE) 	)
        {
//            System.out.println("PageAdapter / _getView / show highlight / position = " + position);
            TabsHost.getCurrentPage().mHighlightPosition = position;
            holder.audioBlock.setBackgroundResource(R.drawable.bg_highlight_border);
            holder.audioBlock.setVisibility(View.VISIBLE);

            // set type face
//			holder.audioName.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            holder.audioName.setTextColor(ColorSet.getHighlightColor(mAct));

            // set icon
            holder.iconAudio.setVisibility(View.VISIBLE);
            holder.iconAudio.setImageResource(R.drawable.ic_audio);

            // set animation
//			Animation animation = AnimationUtils.loadAnimation(mContext , R.anim.right_in);
//			holder.audioBlock.startAnimation(animation);
        }
        else
        {

//			System.out.println("PageAdapter / _getView / not show highlight ");
            holder.audioBlock.setBackgroundResource(R.drawable.bg_gray_border);
            holder.audioBlock.setVisibility(View.VISIBLE);

            // set type face
//			holder.audioName.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);

            // set icon
            holder.iconAudio.setVisibility(View.VISIBLE);
            if(style % 2 == 0)
                holder.iconAudio.setImageResource(R.drawable.ic_audio_off_white);
            else
                holder.iconAudio.setImageResource(R.drawable.ic_audio_off_black);
        }

        // show audio icon and block
        if(Util.isEmptyString(audioUri))
        {
            holder.iconAudio.setVisibility(View.GONE);
            holder.audioBlock.setVisibility(View.GONE);
        }

		// show text title
		if( Util.isEmptyString(strTitle) )
		{
			if(Util.isYouTubeLink(linkUri)) {
				strTitle = Util.getYouTubeTitle(linkUri);
				holder.textTitle.setVisibility(View.VISIBLE);
				holder.textTitle.setText(strTitle);
				holder.textTitle.setTextColor(Color.GRAY);
			}
			else if( (linkUri != null) && (linkUri.startsWith("http")))
			{
				holder.textTitle.setVisibility(View.VISIBLE);
				Util.setHttpTitle(linkUri, mAct,holder.textTitle);
			}
			else
			{
				// make sure empty title is empty after scrolling
				holder.textTitle.setVisibility(View.VISIBLE);
				holder.textTitle.setText("");
			}
		}
		else
		{
			holder.textTitle.setVisibility(View.VISIBLE);
			holder.textTitle.setText(strTitle);
			holder.textTitle.setTextColor(ColorSet.mText_ColorArray[style]);
		}

		// set YouTube thumb nail if picture Uri is none and YouTube link exists
		if(Util.isEmptyString(pictureUri) &&
		   Util.isYouTubeLink(linkUri)      )
		{
			pictureUri = "http://img.youtube.com/vi/"+ getYoutubeId(linkUri)+"/0.jpg";
		}
		else if(UtilImage.hasImageExtension(drawingUri, mAct ))
            pictureUri = drawingUri;

		// case 1: show thumb nail if picture Uri exists
		if(UtilImage.hasImageExtension(pictureUri, mAct ) ||
		   UtilVideo.hasVideoExtension(pictureUri, mAct )   )
		{
			holder.thumbBlock.setVisibility(View.VISIBLE);
			holder.thumbPicture.setVisibility(View.VISIBLE);
			holder.thumbAudio.setVisibility(View.GONE);
			holder.thumbWeb.setVisibility(View.GONE);
			// load bitmap to image view
			try
			{
				new UtilImage_bitmapLoader(holder.thumbPicture,
										   pictureUri,
										   holder.progressBar,
                                           UilCommon.optionsForFadeIn,
										   mAct);
			}
			catch(Exception e)
			{
				Log.e("PageAdapter_recycler", "UtilImage_bitmapLoader error");
				holder.thumbBlock.setVisibility(View.GONE);
				holder.thumbPicture.setVisibility(View.GONE);
				holder.thumbAudio.setVisibility(View.GONE);
				holder.thumbWeb.setVisibility(View.GONE);
			}
		}
		// case 2: show audio thumb nail if picture Uri is none and audio Uri exists
		else if((Util.isEmptyString(pictureUri) && UtilAudio.hasAudioExtension(audioUri) ) )
		{
			holder.thumbBlock.setVisibility(View.VISIBLE);
			holder.thumbPicture.setVisibility(View.GONE);
			holder.thumbAudio.setVisibility(View.VISIBLE);
			holder.thumbWeb.setVisibility(View.GONE);

            try {
                AsyncTaskAudioBitmap audioAsyncTask;
                audioAsyncTask = new AsyncTaskAudioBitmap(mAct,
                        audioUri,
                        holder.thumbAudio,
                        holder.progressBar,
                        false);
                audioAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "Searching media ...");
            } catch (Exception e) {
                Log.e("PageAdapter", "AsyncTaskAudioBitmap error");
                holder.thumbBlock.setVisibility(View.GONE);
                holder.thumbPicture.setVisibility(View.GONE);
                holder.thumbAudio.setVisibility(View.GONE);
                holder.thumbWeb.setVisibility(View.GONE);
            }
		}
		// case 3: set web title and web view thumb nail of link if no title content
		else if(!Util.isEmptyString(linkUri) &&
                linkUri.startsWith("http")   &&
				!Util.isYouTubeLink(linkUri)   )
		{
			// reset web view
			CustomWebView.pauseWebView(holder.thumbWeb);
			CustomWebView.blankWebView(holder.thumbWeb);

			holder.thumbBlock.setVisibility(View.VISIBLE);
			holder.thumbWeb.setInitialScale(50);
			holder.thumbWeb.getSettings().setJavaScriptEnabled(true);//Using setJavaScriptEnabled can introduce XSS vulnerabilities
			holder.thumbWeb.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT );
			holder.thumbWeb.loadUrl(linkUri);
			holder.thumbWeb.setVisibility(View.VISIBLE);

			holder.thumbPicture.setVisibility(View.GONE);
			holder.thumbAudio.setVisibility(View.GONE);

			//Add for non-stop showing of full screen web view
			holder.thumbWeb.setWebViewClient(new WebViewClient() {
				@Override
			    public boolean shouldOverrideUrlLoading(WebView view, String url)
			    {
			        view.loadUrl(url);
			        return true;
			    }
			});


			if (Util.isEmptyString(strTitle)) {

				holder.thumbWeb.setWebChromeClient(new WebChromeClient() {
					@Override
					public void onReceivedTitle(WebView view, String title) {
						super.onReceivedTitle(view, title);
						if (!TextUtils.isEmpty(title) &&
								!title.equalsIgnoreCase("about:blank")) {
							holder.textTitle.setVisibility(View.VISIBLE);
							holder.rowId.setText(String.valueOf(position + 1));
							holder.rowId.setTextColor(ColorSet.mText_ColorArray[style]);

						}
					}
				});
			}
		}
		else
		{
			holder.thumbBlock.setVisibility(View.GONE);
			holder.thumbPicture.setVisibility(View.GONE);
			holder.thumbAudio.setVisibility(View.GONE);
			holder.thumbWeb.setVisibility(View.GONE);
		}

		// Show text body
	  	if(pref_show_note_attribute.getString("KEY_SHOW_BODY", "yes").equalsIgnoreCase("yes"))
	  	{
	  		// test only: enabled for showing picture path
//            String strBody = cursor.getString(cursor.getColumnIndex(KEY_NOTE_BODY));
	  		if(!Util.isEmptyString(strBody)){
				//normal: do nothing
			}
	  		else if(!Util.isEmptyString(pictureUri)) {
//				strBody = pictureUri;//show picture Uri
			}
	  		else if(!Util.isEmptyString(linkUri)) {
//				strBody = linkUri; //show link Uri
			}

			holder.textBody.setText(strBody);
//			holder.textBody.setTextSize(12);

//			holder.rowDivider.setVisibility(View.VISIBLE);
			holder.textBody.setTextColor(ColorSet.mText_ColorArray[style]);
			// time stamp
            holder.textTime.setText(Util.getTimeString(timeCreated));
			holder.textTime.setTextColor(ColorSet.mText_ColorArray[style]);
	  	}
	  	else
	  	{
            holder.textBody.setVisibility(View.GONE);
            holder.textTime.setVisibility(View.GONE);
	  	}

//        setBindViewHolder_listeners(holder,position);
    }


    /**
     * Set bind view holder listeners
     * @param viewHolder
     * @param position
     */
    void setBindViewHolder_listeners(ViewHolder viewHolder, final int position)
    {

//        System.out.println("PageAdapter_recycler / setBindViewHolder_listeners / position = " + position);
        /**
         *  control block
         */
        // on mark note
        viewHolder.btnMarking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("PageAdapter / _getView / btnMarking / _onClick");
                // toggle marking
                toggleNoteMarking(mAct,position);

                // Stop if unmarked item is at playing state
                if(Audio_manager.mAudioPos == position) {
                    UtilAudio.stopAudioIfNeeded();
                }

                //Toggle marking will resume page, so do Store v scroll
                RecyclerView listView = TabsHost.mTabsPagerAdapter.fragmentList.get(TabsHost.getFocus_tabPos()).recyclerView;
                TabsHost.store_listView_vScroll(listView);
                TabsHost.isDoingMarking = true;

                TabsHost.reloadCurrentPage();
                TabsHost.showFooter(MainAct.mAct);

                // update audio info
                if(PageUi.isAudioPlayingPage()) {
                    System.out.println("PageAdapter / _getView / btnMarking / is AudioPlayingPage");
                    AudioPlayer_page.prepareAudioInfo();
                }
            }
        });

        // on view note
        viewHolder.btnViewNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TabsHost.getCurrentPage().mCurrPlayPosition = position;
                DB_page db_page = new DB_page(mAct,TabsHost.getCurrentPageTableId());
                int count = db_page.getNotesCount(true);
                if(position < count)
                {
                    // apply Note class
                    Intent intent;
                    intent = new Intent(mAct, Note.class);
                    intent.putExtra("POSITION", position);
                    mAct.startActivity(intent);
                }
            }
        });

        // on edit note
        viewHolder.btnEditNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DB_page db_page = new DB_page(mAct, TabsHost.getCurrentPageTableId());
                Long rowId = db_page.getNoteId(position,true);

                Intent i = new Intent(mAct, Note_edit.class);
                i.putExtra("list_view_position", position);
                i.putExtra(DB_page.KEY_NOTE_ID, rowId);
                i.putExtra(DB_page.KEY_NOTE_TITLE, db_page.getNoteTitle_byId(rowId));
                i.putExtra(DB_page.KEY_NOTE_PICTURE_URI , db_page.getNotePictureUri_byId(rowId));
                i.putExtra(DB_page.KEY_NOTE_DRAWING_URI , db_page.getNoteDrawingUri_byId(rowId));
                i.putExtra(DB_page.KEY_NOTE_AUDIO_URI , db_page.getNoteAudioUri_byId(rowId));
                i.putExtra(DB_page.KEY_NOTE_LINK_URI , db_page.getNoteLinkUri_byId(rowId));
                i.putExtra(DB_page.KEY_NOTE_BODY, db_page.getNoteBody_byId(rowId));
                i.putExtra(DB_page.KEY_NOTE_CREATED, db_page.getNoteCreatedTime_byId(rowId));
                mAct.startActivity(i);
            }
        });

        // on play audio
        viewHolder.btnPlayAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TabsHost.reloadCurrentPage();// after Drag and drop: this is needed to update thumb nail and title

                Audio_manager.setAudioPlayMode(Audio_manager.PAGE_PLAY_MODE);
                DB_page db_page = new DB_page(mAct, TabsHost.getCurrentPageTableId());
                int notesCount = db_page.getNotesCount(true);
                if(position >= notesCount) //end of list
                    return ;

                int marking = db_page.getNoteMarking(position,true);
                String uriString = db_page.getNoteAudioUri(position,true);

                boolean isAudioUri = false;
                if( !Util.isEmptyString(uriString) && (marking == 1))
                    isAudioUri = true;

                if(position < notesCount) // avoid footer error
                {
                    if(isAudioUri)
                    {
                        // cancel playing
                        if(BackgroundAudioService.mMediaPlayer != null)
                        {
                            if(BackgroundAudioService.mMediaPlayer.isPlaying())
                                BackgroundAudioService.mMediaPlayer.pause();

                            if((AudioPlayer_page.mAudioHandler != null) &&
                                    (TabsHost.audioPlayer_page != null)        ){
                                AudioPlayer_page.mAudioHandler.removeCallbacks(TabsHost.audioPlayer_page.page_runnable);
                            }
                            BackgroundAudioService.mMediaPlayer.release();
                            BackgroundAudioService.mMediaPlayer = null;
                        }

                        Audio_manager.setPlayerState(Audio_manager.PLAYER_AT_PLAY);

                        // create new Intent to play audio
                        Audio_manager.mAudioPos = position;
                        Audio_manager.setAudioPlayMode(Audio_manager.PAGE_PLAY_MODE);

                        TabsHost.audioUi_page = new AudioUi_page(mAct, TabsHost.getCurrentPage().recyclerView);
                        TabsHost.audioUi_page.initAudioBlock(MainAct.mAct);

                        TabsHost.audioPlayer_page = new AudioPlayer_page(mAct,TabsHost.audioUi_page);
                        AudioPlayer_page.prepareAudioInfo();
                        TabsHost.audioPlayer_page.runAudioState();

                        // update audio play position
                        TabsHost.audioPlayTabPos = page_pos;

                        // update audio panel
                        UtilAudio.updateAudioPanel(TabsHost.audioUi_page.audioPanel_play_button,
                                TabsHost.audioUi_page.audio_panel_title_textView);

                        // update playing page position
                        MainAct.mPlaying_pagePos = TabsHost.getFocus_tabPos();

                        // update playing page table Id
                        MainAct.mPlaying_pageTableId = TabsHost.getCurrentPageTableId();

                        // update playing folder position
                        MainAct.mPlaying_folderPos = FolderUi.getFocus_folderPos();

                        // update playing folder table Id
                        DB_drawer dB_drawer = new DB_drawer(mAct);
                        MainAct.mPlaying_folderTableId = dB_drawer.getFolderTableId(MainAct.mPlaying_folderPos,true);

                        TabsHost.mTabsPagerAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        // on play YouTube
        viewHolder.btnPlayYouTube.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                TabsHost.getCurrentPage().mCurrPlayPosition = position;
                DB_page db_page = new DB_page(mAct, TabsHost.getCurrentPageTableId());
                db_page.open();
                int count = db_page.getNotesCount(false);
                String linkStr = db_page.getNoteLinkUri(position, false);
                db_page.close();

                if (position < count) {
                    if (Util.isYouTubeLink(linkStr)) {
                        Audio_manager.stopAudioPlayer();

                        // apply native YouTube
                        Util.openLink_YouTube(mAct, linkStr);
                    }
                }
            }
        });

        // on play Web
        viewHolder.btnPlayWeb.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                DB_page db_page = new DB_page(mAct, TabsHost.getCurrentPageTableId());
                linkUri = db_page.getNoteLinkUri(position, true);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkUri));
                MainAct.mAct.startActivity(intent);
            }
        });

        // Start a drag whenever the handle view it touched
        viewHolder.btnDrag.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked())
                {
                    case MotionEvent.ACTION_DOWN:
                        mDragStartListener.onStartDrag(viewHolder);
                        System.out.println("PageAdapter_recycler / onTouch / ACTION_DOWN");
                        return true;
                    case MotionEvent.ACTION_UP:
                        v.performClick();
                        return true;
                }
                return false;
            }


        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return cursor.getCount();//mDataSet.length; //todo bug: Attempt to invoke interface method 'int android.database.Cursor.getCount()' on a null object reference
    }

    // toggle mark of note
    public static int toggleNoteMarking(AppCompatActivity mAct, int position)
    {
        int marking = 0;
		DB_page db_page = new DB_page(mAct,TabsHost.getCurrentPageTableId());
        db_page.open();
        int count = db_page.getNotesCount(false);
        if(position >= count) //end of list
        {
            db_page.close();
            return marking;
        }

        String strNote = db_page.getNoteTitle(position,false);
        String strPictureUri = db_page.getNotePictureUri(position,false);
        String strAudioUri = db_page.getNoteAudioUri(position,false);
        String strDrawingUri = db_page.getNoteDrawingUri(position,false);
        String strLinkUri = db_page.getNoteLinkUri(position,false);
        String strNoteBody = db_page.getNoteBody(position,false);
        Long idNote =  db_page.getNoteId(position,false);

        // toggle the marking
        if(db_page.getNoteMarking(position,false) == 0)
        {
            db_page.updateNote(idNote, strNote, strPictureUri, strAudioUri, strDrawingUri, strLinkUri, strNoteBody, 1, 0, false);
            marking = 1;
        }
        else
        {
            db_page.updateNote(idNote, strNote, strPictureUri, strAudioUri, strDrawingUri, strLinkUri, strNoteBody, 0, 0, false);
            marking = 0;
        }
        db_page.close();

        System.out.println("PageAdapter_recycler / _toggleNoteMarking / position = " + position + ", marking = " + db_page.getNoteMarking(position,true));
        return  marking;
    }

    @Override
    public void onItemDismiss(int position) {
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPos, int toPos) {
//        System.out.println("PageAdapter_recycler / _onItemMove / fromPos = " +
//                        fromPos + ", toPos = " + toPos);

        notifyItemMoved(fromPos, toPos);

        int oriStartPos = fromPos;
        int oriEndPos = toPos;

        mDb_page = new DB_page(mAct, TabsHost.getCurrentPageTableId());
        if(fromPos >= mDb_page.getNotesCount(true)) // avoid footer error
            return false;

        //reorder data base storage
        int loop = Math.abs(fromPos-toPos);
        for(int i=0;i< loop;i++)
        {
            swapRows(mDb_page, fromPos,toPos);
            if((fromPos-toPos) >0)
                toPos++;
            else
                toPos--;
        }

        if( PageUi.isAudioPlayingPage() &&
                (BackgroundAudioService.mMediaPlayer != null)				   )
        {
            if( (Page_recycler.mHighlightPosition == oriEndPos)  && (oriStartPos > oriEndPos))
            {
                Page_recycler.mHighlightPosition = oriEndPos+1;
            }
            else if( (Page_recycler.mHighlightPosition == oriEndPos) && (oriStartPos < oriEndPos))
            {
                Page_recycler.mHighlightPosition = oriEndPos-1;
            }
            else if( (Page_recycler.mHighlightPosition == oriStartPos)  && (oriStartPos > oriEndPos))
            {
                Page_recycler.mHighlightPosition = oriEndPos;
            }
            else if( (Page_recycler.mHighlightPosition == oriStartPos) && (oriStartPos < oriEndPos))
            {
                Page_recycler.mHighlightPosition = oriEndPos;
            }
            else if(  (Page_recycler.mHighlightPosition < oriEndPos) &&
                    (Page_recycler.mHighlightPosition > oriStartPos)   )
            {
                Page_recycler.mHighlightPosition--;
            }
            else if( (Page_recycler.mHighlightPosition > oriEndPos) &&
                    (Page_recycler.mHighlightPosition < oriStartPos)  )
            {
                Page_recycler.mHighlightPosition++;
            }

            Audio_manager.mAudioPos = Page_recycler.mHighlightPosition;
            AudioPlayer_page.prepareAudioInfo();
        }

        // update footer
        TabsHost.showFooter(mAct);
        return true;
    }

    @Override
    public void onItemMoved(RecyclerView.ViewHolder sourceViewHolder, int fromPos, RecyclerView.ViewHolder targetViewHolder, int toPos) {
        System.out.println("PageAdapter_recycler / _onItemMoved");
        ((TextView)sourceViewHolder.itemView.findViewById(R.id.row_id)).setText(String.valueOf(toPos+1));
        ((TextView)targetViewHolder.itemView.findViewById(R.id.row_id)).setText(String.valueOf(fromPos+1));

        setBindViewHolder_listeners((ViewHolder)sourceViewHolder,toPos);
        setBindViewHolder_listeners((ViewHolder)targetViewHolder,fromPos);
    }

}
