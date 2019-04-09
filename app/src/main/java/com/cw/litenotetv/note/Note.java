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

import com.cw.litenotetv.note_edit.Note_edit;
import com.cw.litenotetv.R;
import com.cw.litenotetv.db.DB_folder;
import com.cw.litenotetv.db.DB_page;
import com.cw.litenotetv.main.MainAct;
import com.cw.litenotetv.operation.audio.Audio_manager;
import com.cw.litenotetv.operation.audio.BackgroundAudioService;
import com.cw.litenotetv.page.PageAdapter_recycler;
import com.cw.litenotetv.tabs.TabsHost;
import com.cw.litenotetv.util.CustomWebView;
import com.cw.litenotetv.util.DeleteFileAlarmReceiver;
import com.cw.litenotetv.util.audio.UtilAudio;
import com.cw.litenotetv.util.image.UtilImage;
import com.cw.litenotetv.util.preferences.Pref;
import com.cw.litenotetv.util.video.AsyncTaskVideoBitmapPager;
import com.cw.litenotetv.util.video.UtilVideo;
import com.cw.litenotetv.util.video.VideoPlayer;
import com.cw.litenotetv.operation.mail.MailNotes;
import com.cw.litenotetv.util.uil.UilCommon;
import com.cw.litenotetv.util.Util;

import android.R.color;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Note extends AppCompatActivity
{
    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    public ViewPager viewPager;
    public static boolean isPagerActive;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    public static PagerAdapter mPagerAdapter;

    // DB
    public DB_page mDb_page;
    public static Long mNoteId;
    int mEntryPosition;
    int EDIT_CURRENT_VIEW = 5;
    int MAIL_CURRENT_VIEW = 6;
    static int mStyle;
    
    static SharedPreferences mPref_show_note_attribute;

    Button editButton;
    Button optionButton;
    Button backButton;

	public static String mAudioUriInDB;

    public AppCompatActivity act;
    public static int mPlayVideoPositionOfInstance;
    public AudioUi_note audioUi_note;

	public static int mCurrentState;
	public final static int STATE_PAUSED = 0;
	public final static int STATE_PLAYING = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        System.out.println("Note / _onCreate");

		// set current selection
		mEntryPosition = getIntent().getExtras().getInt("POSITION");
		NoteUi.setFocus_notePos(mEntryPosition);

		// init video
		UtilVideo.mPlayVideoPosition = 0;   // not played yet
		mPlayVideoPositionOfInstance = 0;
		AsyncTaskVideoBitmapPager.mRotationStr = null;

		Audio_manager.isRunnableOn_note = false;

		act = this;

        MainAct.mMediaBrowserCompat = null;

	} //onCreate end

//	// callback of granted permission
//	@Override
//	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
//	{
//		System.out.println("Note / _onRequestPermissionsResult / grantResults.length =" + grantResults.length);
//		switch (requestCode)
//		{
//			case Util.PERMISSIONS_REQUEST_PHONE:
//			{
//				// If request is cancelled, the result arrays are empty.
//				if ( (grantResults.length > 0) && ( (grantResults[0] == PackageManager.PERMISSION_GRANTED) ))
//					UtilAudio.setPhoneListener(this);
//			}
//			break;
//		}
//	}

	// Add to prevent resizing full screen picture,
	// when popup menu shows up at picture mode
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		System.out.println("Note / _onWindowFocusChanged");
		if (hasFocus && isPictureMode() )
			Util.setFullScreen(act);
	}

	// key event: 1 from bluetooth device 2 when notification bar dose not shown
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		int newPos;
		System.out.println("Note / _onKeyDown / keyCode = " + keyCode);
		switch (keyCode) {
			case KeyEvent.KEYCODE_MEDIA_PREVIOUS: //88
				if(viewPager.getCurrentItem() == 0)
                    newPos = mPagerAdapter.getCount() - 1;//back to last one
				else
					newPos = NoteUi.getFocus_notePos()-1;

				NoteUi.setFocus_notePos(newPos);
				viewPager.setCurrentItem(newPos);

				BackgroundAudioService.mIsPrepared = false;
				BackgroundAudioService.mMediaPlayer = null;
				Audio_manager.isRunnableOn_page = false;
				findViewById(R.id.pager_btn_audio_play).performClick();
				return true;

			case KeyEvent.KEYCODE_MEDIA_NEXT: //87
				if(viewPager.getCurrentItem() == (mPagerAdapter.getCount() - 1))
					newPos = 0;
				else
					newPos = NoteUi.getFocus_notePos() + 1;

				NoteUi.setFocus_notePos(newPos);
				viewPager.setCurrentItem(newPos);

				BackgroundAudioService.mIsPrepared = false;
				BackgroundAudioService.mMediaPlayer = null;
				Audio_manager.isRunnableOn_page = false;
				AudioUi_note.mPager_audio_play_button.performClick();
				return true;

			case KeyEvent.KEYCODE_MEDIA_PLAY: //126
				AudioUi_note.mPager_audio_play_button.performClick();
				return true;

			case KeyEvent.KEYCODE_MEDIA_PAUSE: //127
				AudioUi_note.mPager_audio_play_button.performClick();
				return true;

			case KeyEvent.KEYCODE_BACK:
                onBackPressed();
				return true;

			case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
				return true;

			case KeyEvent.KEYCODE_MEDIA_REWIND:
				return true;

			case KeyEvent.KEYCODE_MEDIA_STOP:
				return true;
		}
		return false;
	}



	void setLayoutView()
	{
        System.out.println("Note / _setLayoutView");

		if( UtilVideo.mVideoView != null)
			UtilVideo.mPlayVideoPosition = UtilVideo.mVideoView.getCurrentPosition();

		// video view will be reset after _setContentView
		if(Util.isLandscapeOrientation(this))
			setContentView(R.layout.note_view_landscape);
		else
			setContentView(R.layout.note_view_portrait);

		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		mPref_show_note_attribute = getSharedPreferences("show_note_attribute", 0);

		UilCommon.init();

		// DB
		DB_folder dbFolder = new DB_folder(act,Pref.getPref_focusView_folder_tableId(act));
		mStyle = dbFolder.getPageStyle(TabsHost.getFocus_tabPos(), true);

		mDb_page = new DB_page(act, TabsHost.getCurrentPageTableId());

		// Instantiate a ViewPager and a PagerAdapter.
		viewPager = (ViewPager) findViewById(R.id.tabs_pager);
		mPagerAdapter = new Note_adapter(viewPager,this);
		viewPager.setAdapter(mPagerAdapter);
		viewPager.setCurrentItem(NoteUi.getFocus_notePos());

		// tab style
//		if(TabsHost.mDbFolder != null)
//			TabsHost.mDbFolder.close();

		if(mDb_page != null) {
			mNoteId = mDb_page.getNoteId(NoteUi.getFocus_notePos(), true);
			mAudioUriInDB = mDb_page.getNoteAudioUri_byId(mNoteId);
		}

        if(UtilAudio.hasAudioExtension(mAudioUriInDB) ||
		   UtilAudio.hasAudioExtension(Util.getDisplayNameByUriString(mAudioUriInDB, act))) {
            audioUi_note = new AudioUi_note(this, mAudioUriInDB);
            audioUi_note.init_audio_block();
        }


		// Note: if viewPager.getCurrentItem() is not equal to mEntryPosition, _onPageSelected will
		//       be called again after rotation
		viewPager.setOnPageChangeListener(onPageChangeListener);//todo deprecated

		// edit note button
		editButton = (Button) findViewById(R.id.view_edit);
		editButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_edit, 0, 0, 0);
		editButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				Intent intent = new Intent(Note.this, Note_edit.class);
				intent.putExtra(DB_page.KEY_NOTE_ID, mNoteId);
				intent.putExtra(DB_page.KEY_NOTE_TITLE, mDb_page.getNoteTitle_byId(mNoteId));
				intent.putExtra(DB_page.KEY_NOTE_AUDIO_URI , mDb_page.getNoteAudioUri_byId(mNoteId));
				intent.putExtra(DB_page.KEY_NOTE_PICTURE_URI , mDb_page.getNotePictureUri_byId(mNoteId));
				intent.putExtra(DB_page.KEY_NOTE_DRAWING_URI , mDb_page.getNoteDrawingUri_byId(mNoteId));
				intent.putExtra(DB_page.KEY_NOTE_LINK_URI , mDb_page.getNoteLinkUri_byId(mNoteId));
				intent.putExtra(DB_page.KEY_NOTE_BODY, mDb_page.getNoteBody_byId(mNoteId));
				intent.putExtra(DB_page.KEY_NOTE_CREATED, mDb_page.getNoteCreatedTime_byId(mNoteId));
				startActivityForResult(intent, EDIT_CURRENT_VIEW);
			}
		});

		// send note button
		optionButton = (Button) findViewById(R.id.view_option);
		optionButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_more, 0, 0, 0);
		optionButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				View_note_option.note_option(act,mNoteId);
			}
		});

		// back button
		backButton = (Button) findViewById(R.id.view_back);
		backButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_back, 0, 0, 0);
		backButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view) {
				if(isTextMode())
				{
					// back to view all mode
					setViewAllMode();
					setOutline(act);
				}
				else //view all mode
				{
					stopAV();
					finish();
				}
			}
		});
	}

	// on page change listener
	ViewPager.SimpleOnPageChangeListener onPageChangeListener = new ViewPager.SimpleOnPageChangeListener()
	{
		@Override
		public void onPageSelected(int nextPosition)
		{
			if(Audio_manager.getAudioPlayMode()  == Audio_manager.NOTE_PLAY_MODE)
                Audio_manager.stopAudioPlayer();

			NoteUi.setFocus_notePos(viewPager.getCurrentItem());
			System.out.println("Note / _onPageSelected");
//			System.out.println("    NoteUi.getFocus_notePos() = " + NoteUi.getFocus_notePos());
//			System.out.println("    nextPosition = " + nextPosition);

			mIsViewModeChanged = false;

			// show audio name
			mNoteId = mDb_page.getNoteId(nextPosition,true);
			System.out.println("Note / _onPageSelected / mNoteId = " + mNoteId);
			mAudioUriInDB = mDb_page.getNoteAudioUri_byId(mNoteId);
			System.out.println("Note / _onPageSelected / mAudioUriInDB = " + mAudioUriInDB);

			if(UtilAudio.hasAudioExtension(mAudioUriInDB)) {
                audioUi_note = new AudioUi_note(Note.this, mAudioUriInDB);
                audioUi_note.init_audio_block();
                audioUi_note.showAudioBlock();
            }

			// stop video when changing note
			String pictureUriInDB = mDb_page.getNotePictureUri_byId(mNoteId);
			if(UtilVideo.hasVideoExtension(pictureUriInDB,act)) {
				VideoPlayer.stopVideo();
				NoteUi.cancel_UI_callbacks();
			}

            setOutline(act);
		}
	};

	public static int getStyle() {
		return mStyle;
	}

	public void setStyle(int style) {
		mStyle = style;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		System.out.println("Note / _onActivityResult ");
        if((requestCode==EDIT_CURRENT_VIEW) || (requestCode==MAIL_CURRENT_VIEW))
        {
			stopAV();
        }
		else if(requestCode == MailNotes.EMAIL)
		{
			Toast.makeText(act,R.string.mail_exit,Toast.LENGTH_SHORT).show();
			// note: result code is always 0 (cancel), so it is not used
			new DeleteFileAlarmReceiver(act,
					                    System.currentTimeMillis() + 1000 * 60 * 5, // formal: 300 seconds
//						    		    System.currentTimeMillis() + 1000 * 10, // test: 10 seconds
					                    MailNotes.mAttachmentFileName);
		}

		// show current item
		if(requestCode == Util.YOUTUBE_LINK_INTENT)
        	viewPager.setCurrentItem(viewPager.getCurrentItem());

	    // check if there is one note at least in the pager
		if( viewPager.getAdapter().getCount() > 0 )
			setOutline(act);
		else
			finish();
	}

    /** Set outline for selected view mode
    *
    *   Determined by view mode: all, picture, text
    *
    *   Controlled factor:
    *   - action bar: hide, show
    *   - full screen: full, not full
    */
	public static void setOutline(AppCompatActivity act)
	{
        // Set full screen or not, and action bar
		if(isViewAllMode() || isTextMode())
		{
			Util.setFullScreen_noImmersive(act);
            if(act.getSupportActionBar() != null)
			    act.getSupportActionBar().show();
		}
		else if(isPictureMode())
		{
			Util.setFullScreen(act);
            if(act.getSupportActionBar() != null)
    			act.getSupportActionBar().hide();
		}

        // renew pager
        showSelectedView();

		LinearLayout buttonGroup = (LinearLayout) act.findViewById(R.id.view_button_group);
        // button group
        if(Note.isPictureMode() )
            buttonGroup.setVisibility(View.GONE);
        else
            buttonGroup.setVisibility(View.VISIBLE);

		TextView audioTitle = (TextView) act.findViewById(R.id.pager_audio_title);
        // audio title
        if(!Note.isPictureMode())
        {
            if(!Util.isEmptyString(audioTitle.getText().toString()) )
                audioTitle.setVisibility(View.VISIBLE);
            else
                audioTitle.setVisibility(View.GONE);
        }

        // renew options menu
        act.invalidateOptionsMenu();
	}


    //Refer to http://stackoverflow.com/questions/4434027/android-videoview-orientation-change-with-buffered-video
	/***************************************************************
	video play spec of Pause and Rotate:
	1. Rotate: keep pause state
	 pause -> rotate -> pause -> play -> continue

	2. Rotate: keep play state
	 play -> rotate -> continue play

	3. Key guard: enable pause
	 play -> key guard on/off -> pause -> play -> continue

	4. Key guard and Rotate: keep pause
	 play -> key guard on/off -> pause -> rotate -> pause
	 ****************************************************************/	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    System.out.println("Note / _onConfigurationChanged");

		// dismiss popup menu
		if(NoteUi.popup != null)
		{
			NoteUi.popup.dismiss();
			NoteUi.popup = null;
		}

		NoteUi.cancel_UI_callbacks();

        setLayoutView();

        if(canShowFullScreenPicture())
            Note.setPictureMode();
        else
            Note.setViewAllMode();

        // Set outline of view mode
        setOutline(act);
	}

	@Override
	protected void onStart() {
		super.onStart();
		System.out.println("Note / _onStart");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		System.out.println("Note / _onResume");

		setLayoutView();

		isPagerActive = true;

        if(canShowFullScreenPicture())
            Note.setPictureMode();
        else
            Note.setViewAllMode();

		setOutline(act);

		// Register Bluetooth device receiver
		if(Build.VERSION.SDK_INT < 21)
		{
			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
			this.registerReceiver(mReceiver, filter);
		}
		else // Build.VERSION.SDK_INT >= 21
		{
			// Media session: to receive media button event of bluetooth device
			// new media browser instance and create BackgroundAudioService instance: support notification

			if(MainAct.mMediaBrowserCompat == null) {
				MainAct.mMediaBrowserCompat = new MediaBrowserCompat(act,
						new ComponentName(act, BackgroundAudioService.class),
						MainAct.mMediaBrowserCompatConnectionCallback,
						act.getIntent().getExtras());
			}

			if(!MainAct.mMediaBrowserCompat.isConnected())
				MainAct.mMediaBrowserCompat.connect();

			MainAct.mCurrentState = MainAct.STATE_PAUSED;
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		System.out.println("Note / _onPause");

		isPagerActive = false;

		// set pause when key guard is ON
		if( UtilVideo.mVideoView != null)
		{
			UtilVideo.mPlayVideoPosition = UtilVideo.mVideoView.getCurrentPosition();

			// keep play video position
			mPlayVideoPositionOfInstance = UtilVideo.mPlayVideoPosition;
			System.out.println("Note / _onPause / mPlayVideoPositionOfInstance = " + mPlayVideoPositionOfInstance);

			if(UtilVideo.mVideoPlayer != null)
				VideoPlayer.stopVideo();
		}

		// to stop YouTube web view running
    	String tagStr = "current"+ viewPager.getCurrentItem()+"webView";
    	CustomWebView webView = (CustomWebView) viewPager.findViewWithTag(tagStr);
    	CustomWebView.pauseWebView(webView);
    	CustomWebView.blankWebView(webView);

		// to stop Link web view running
    	tagStr = "current"+ viewPager.getCurrentItem()+"linkWebView";
    	CustomWebView linkWebView = (CustomWebView) viewPager.findViewWithTag(tagStr);
    	CustomWebView.pauseWebView(linkWebView);
    	CustomWebView.blankWebView(linkWebView);

		NoteUi.cancel_UI_callbacks();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		System.out.println("Note / _onStop");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.out.println("Note / _onDestroy");

		if(Audio_manager.isRunnableOn_note) {
			BackgroundAudioService.mIsPrepared = false;
			BackgroundAudioService.mMediaPlayer = null;
			Audio_manager.isRunnableOn_note = false;
		}

        // disconnect MediaBrowserCompat
		if(Build.VERSION.SDK_INT >= 21) {
			if (MainAct.mMediaBrowserCompat.isConnected())
				MainAct.mMediaBrowserCompat.disconnect();
		}
	}

	// avoid exception: has leaked window android.widget.ZoomButtonsController
	@Override
	public void finish() {
		System.out.println("Note / _finish");
		if(mPagerHandler != null)
			mPagerHandler.removeCallbacks(mOnBackPressedRun);		
	    
		ViewGroup view = (ViewGroup) getWindow().getDecorView();
	    view.setBackgroundColor(getResources().getColor(color.background_dark)); // avoid white flash
	    view.removeAllViews();

		super.finish();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		System.out.println("Note / _onSaveInstanceState");
	}

	Menu mMenu;
	// On Create Options Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        super.onCreateOptionsMenu(menu);
//		System.out.println("Note / _onCreateOptionsMenu");

		// inflate menu
		getMenuInflater().inflate(R.menu.pager_menu, menu);
		mMenu = menu;

		// menu item: checked status
		// get checked or not
		int isChecked = mDb_page.getNoteMarking(NoteUi.getFocus_notePos(),true);
		if( isChecked == 0)
			menu.findItem(R.id.VIEW_NOTE_CHECK).setIcon(R.drawable.btn_check_off_holo_dark);
		else
			menu.findItem(R.id.VIEW_NOTE_CHECK).setIcon(R.drawable.btn_check_on_holo_dark);

		// menu item: view mode
   		markCurrentSelected(menu.findItem(R.id.VIEW_ALL),"ALL");
		markCurrentSelected(menu.findItem(R.id.VIEW_PICTURE),"PICTURE_ONLY");
		markCurrentSelected(menu.findItem(R.id.VIEW_TEXT),"TEXT_ONLY");

	    // menu item: previous
		MenuItem itemPrev = menu.findItem(R.id.ACTION_PREVIOUS);
		itemPrev.setEnabled(viewPager.getCurrentItem() > 0);
		itemPrev.getIcon().setAlpha(viewPager.getCurrentItem() > 0?255:30);

		// menu item: Next or Finish
		MenuItem itemNext = menu.findItem(R.id.ACTION_NEXT);
		itemNext.setTitle((viewPager.getCurrentItem() == mPagerAdapter.getCount() - 1)	?
									R.string.view_note_slide_action_finish :
									R.string.view_note_slide_action_next                  );

        // set Disable and Gray for Last item
		boolean isLastOne = (viewPager.getCurrentItem() == (mPagerAdapter.getCount() - 1));
        if(isLastOne)
        	itemNext.setEnabled(false);

        itemNext.getIcon().setAlpha(isLastOne?30:255);

        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	// called after _onCreateOptionsMenu
        return true;
    }  
    
    // for menu buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            	if(isTextMode())
            	{
        			// back to view all mode
            		setViewAllMode();
					setOutline(act);
            	}
            	else if(isViewAllMode())
            	{
					stopAV();
	            	finish();
            	}
                return true;

            case R.id.VIEW_NOTE_MODE:
            	return true;

			case R.id.VIEW_NOTE_CHECK:
				int markingNow = PageAdapter_recycler.toggleNoteMarking(this,NoteUi.getFocus_notePos());

				// update marking
				if(markingNow == 1)
					mMenu.findItem(R.id.VIEW_NOTE_CHECK).setIcon(R.drawable.btn_check_on_holo_dark);
				else
					mMenu.findItem(R.id.VIEW_NOTE_CHECK).setIcon(R.drawable.btn_check_off_holo_dark);

				return true;

            case R.id.VIEW_ALL:
        		setViewAllMode();
				setOutline(act);
            	return true;
            	
            case R.id.VIEW_PICTURE:
        		setPictureMode();
				setOutline(act);
            	return true;

            case R.id.VIEW_TEXT:
        		setTextMode();
				setOutline(act);
            	return true;
            	
            case R.id.ACTION_PREVIOUS:
                // Go to the previous step in the wizard. If there is no previous step,
                // setCurrentItem will do nothing.
            	NoteUi.setFocus_notePos(NoteUi.getFocus_notePos()-1);
            	viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                return true;

            case R.id.ACTION_NEXT:
                // Advance to the next step in the wizard. If there is no next step, setCurrentItem
                // will do nothing.
				NoteUi.setFocus_notePos(NoteUi.getFocus_notePos()+1);
            	viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    //
//    // Open link of YouTube
//    //
//    // Due to "AdWords or copyright" server limitation, for some URI,
//    // "video is not available" message could show up.
//    // At this case, one solution is to switch current mobile website to desktop website by browser setting.
//    // So, base on URI key words to decide "YouTube App" or "browser" launch.
//    public void openLink_YouTube(String linkUri)
//    {
//        // by YouTube App
//        if(linkUri.contains("youtu.be"))
//        {
//            // stop audio and video if playing
//            stopAV();
//
//            String id = Util.getYoutubeId(linkUri);
//            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + id));
//            act.startActivity(intent);
//        }
//        // by Chrome browser
//        else if(linkUri.contains("youtube.com"))
//        {
//            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(linkUri));
//            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            i.setPackage("com.android.chrome");
//
//            try
//            {
//                act.startActivity(i);
//            }
//            catch (ActivityNotFoundException e)
//            {
//                // Chrome is probably not installed
//                // Try with the default browser
//                i.setPackage(null);
//                act.startActivity(i);
//            }
//        }
//    }

    // on back pressed
    @Override
    public void onBackPressed() {
		System.out.println("Note / _onBackPressed");
    	// web view can go back
    	String tagStr = "current"+ viewPager.getCurrentItem()+"linkWebView";
    	CustomWebView linkWebView = (CustomWebView) viewPager.findViewWithTag(tagStr);
        if (linkWebView.canGoBack()) 
        {
        	linkWebView.goBack();
        }
        else if(isPictureMode())
    	{
            // dispatch touch event to show buttons
            long downTime = SystemClock.uptimeMillis();
            long eventTime = SystemClock.uptimeMillis() + 100;
            float x = 0.0f;
            float y = 0.0f;
            // List of meta states found here: developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
            int metaState = 0;
            MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP,
                                                    x, y,metaState);
            dispatchTouchEvent(event);
            event.recycle();

            // in order to make sure ImageViewBackButton is effective to be clicked
            mPagerHandler = new Handler();
            mPagerHandler.postDelayed(mOnBackPressedRun, 500);
        }
    	else if(isTextMode())
    	{
			// back to view all mode
    		setViewAllMode();
			setOutline(act);
    	}
    	else
    	{
    		System.out.println("Note / _onBackPressed / view all mode");
			stopAV();
        	finish();
    	}
    }
    
    static Handler mPagerHandler;
	Runnable mOnBackPressedRun = new Runnable()
	{   @Override
		public void run()
		{
            String tagStr = "current"+ NoteUi.getFocus_notePos() +"pictureView";
            ViewGroup pictureGroup = (ViewGroup) viewPager.findViewWithTag(tagStr);
            System.out.println("Note / _showPictureViewUI / tagStr = " + tagStr);

            Button picView_back_button;
            if(pictureGroup != null)
            {
                picView_back_button = (Button) (pictureGroup.findViewById(R.id.image_view_back));
                picView_back_button.performClick();
            }

			if(Note_adapter.mIntentView != null)
				Note_adapter.mIntentView = null;
		}
	};
    
    // get current picture string
    public String getCurrentPictureString()
    {
		return mDb_page.getNotePictureUri(NoteUi.getFocus_notePos(),true);
    }

    // Mark current selected
    void markCurrentSelected(MenuItem subItem, String str)
    {
        if(mPref_show_note_attribute.getString("KEY_PAGER_VIEW_MODE", "ALL")
                .equalsIgnoreCase(str))
            subItem.setIcon(R.drawable.btn_radio_on_holo_dark);
        else
            subItem.setIcon(R.drawable.btn_radio_off_holo_dark);
    }

    // Show selected view
    static void showSelectedView()
    {
   		mIsViewModeChanged = false;

		if(!Note.isTextMode())
   		{
	   		if(UtilVideo.mVideoView != null)
	   		{
	   	   		// keep current video position for NOT text mode
				mPositionOfChangeView = UtilVideo.mPlayVideoPosition;
	   			mIsViewModeChanged = true;

	   			if(VideoPlayer.mVideoHandler != null)
	   			{
					System.out.println("Note / _showSelectedView / just remove callbacks");
	   				VideoPlayer.mVideoHandler.removeCallbacks(VideoPlayer.mRunPlayVideo);
	   				if(UtilVideo.hasMediaControlWidget)
	   					VideoPlayer.cancelMediaController();
	   			}
	   		}
   			Note_adapter.mLastPosition = -1;
   		}

    	if(mPagerAdapter != null)
    		mPagerAdapter.notifyDataSetChanged(); // will call Note_adapter / _setPrimaryItem
    }
    
    public static int mPositionOfChangeView;
    public static boolean mIsViewModeChanged;
    
    static void setViewAllMode()
    {
		 mPref_show_note_attribute.edit()
		   						  .putString("KEY_PAGER_VIEW_MODE","ALL")
		   						  .apply();
    }
    
    static void setPictureMode()
    {
		 mPref_show_note_attribute.edit()
		   						  .putString("KEY_PAGER_VIEW_MODE","PICTURE_ONLY")
		   						  .apply();
    }
    
    static void setTextMode()
    {
		 mPref_show_note_attribute.edit()
		   						  .putString("KEY_PAGER_VIEW_MODE","TEXT_ONLY")
		   						  .apply();
    }
    
    
    public static boolean isPictureMode()
    {
	  	return mPref_show_note_attribute.getString("KEY_PAGER_VIEW_MODE", "ALL")
										.equalsIgnoreCase("PICTURE_ONLY");
    }
    
    public static boolean isViewAllMode()
    {
	  	return mPref_show_note_attribute.getString("KEY_PAGER_VIEW_MODE", "ALL")
										.equalsIgnoreCase("ALL");
    }

    public static boolean isTextMode()
    {
	  	return mPref_show_note_attribute.getString("KEY_PAGER_VIEW_MODE", "ALL")
										.equalsIgnoreCase("TEXT_ONLY");
    }

	static NoteUi picUI_touch;
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int maskedAction = event.getActionMasked();
        switch (maskedAction) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
    			 System.out.println("Note / _dispatchTouchEvent / MotionEvent.ACTION_UP / viewPager.getCurrentItem() =" + viewPager.getCurrentItem());
				 //1st touch to turn on UI
				 if(picUI_touch == null) {
				 	picUI_touch = new NoteUi(act, viewPager, viewPager.getCurrentItem());
				 	picUI_touch.tempShow_picViewUI(5000,getCurrentPictureString());
				 }
				 //2nd touch to turn off UI
				 else
					 setTransientPicViewUI();

				 //1st touch to turn off UI (primary)
				 if(Note_adapter.picUI_primary != null)
					 setTransientPicViewUI();
    	  	  	 break;

	        case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
	        case MotionEvent.ACTION_CANCEL: 
	        	 break;
        }

        return super.dispatchTouchEvent(event);
    }

	/**
	 * Set delay for transient picture view UI
	 *
	 */
    void setTransientPicViewUI()
    {
        NoteUi.cancel_UI_callbacks();
        picUI_touch = new NoteUi(act, viewPager, viewPager.getCurrentItem());

        // for video
        String pictureUriInDB = mDb_page.getNotePictureUri_byId(mNoteId);
        if(UtilVideo.hasVideoExtension(pictureUriInDB,act) &&
                (UtilVideo.mVideoView != null) &&
                (UtilVideo.getVideoState() != UtilVideo.VIDEO_AT_STOP) )
        {
            if (!NoteUi.showSeekBarProgress)
                picUI_touch.tempShow_picViewUI(110, getCurrentPictureString());
            else
                picUI_touch.tempShow_picViewUI(1110, getCurrentPictureString());
        }
        // for image
        else
            picUI_touch.tempShow_picViewUI(111,getCurrentPictureString());
    }

	public static void stopAV()
	{
		if(Audio_manager.getAudioPlayMode() == Audio_manager.NOTE_PLAY_MODE)
            Audio_manager.stopAudioPlayer();

		VideoPlayer.stopVideo();
	}

	public static void changeToNext(ViewPager mPager)
	{
		mPager.setCurrentItem(mPager.getCurrentItem() + 1);
	}

	public static void changeToPrevious(ViewPager mPager)
	{
		mPager.setCurrentItem(mPager.getCurrentItem() + 1);
	}

    // Show full screen picture when device orientation and image orientation are the same
    boolean canShowFullScreenPicture()
    {
        String pictureStr = mDb_page.getNotePictureUri(NoteUi.getFocus_notePos(),true);
		System.out.println(" Note / _canShowFullPicture / pictureStr = " +pictureStr);
//		System.out.println(" Note / _canShowFullPicture / Util.isLandscapeOrientation(act) = " +Util.isLandscapeOrientation(act));
//		System.out.println(" Note / _canShowFullPicture / UtilImage.isLandscapePicture(pictureStr) = " +UtilImage.isLandscapePicture(pictureStr));
        if( !Util.isEmptyString(pictureStr) &&
            ( (Util.isLandscapeOrientation(act) && UtilImage.isLandscapePicture(pictureStr) ) ||
            (Util.isPortraitOrientation(act) && !UtilImage.isLandscapePicture(pictureStr))  ) )
            return true;
        else
            return false;
    }

	//The BroadcastReceiver that listens for bluetooth broadcasts
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("MainAct / _BroadcastReceiver / onReceive");
			String action = intent.getAction();
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

			if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
				//Device is now connected
				Toast.makeText(getApplicationContext(), "ACTION_ACL_CONNECTED: device is " + device, Toast.LENGTH_LONG).show();
			}

			Intent intentReceive = intent;
			KeyEvent keyEvent = (KeyEvent) intentReceive.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			if(keyEvent != null)
				onKeyDown( keyEvent.getKeyCode(),keyEvent);
		}
	};

}
