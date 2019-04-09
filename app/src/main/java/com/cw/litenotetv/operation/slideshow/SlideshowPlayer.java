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

package com.cw.litenotetv.operation.slideshow;

import com.cw.litenotetv.main.MainAct;
import com.cw.litenotetv.R;
import com.cw.litenotetv.note.Note;
import com.cw.litenotetv.util.image.UtilImage;
import com.cw.litenotetv.util.uil.UilCommon;
import com.cw.litenotetv.util.Util;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SlideshowPlayer extends FragmentActivity
{
	private String STATE_SLIDE_INDEX = "STATE_SLIDE_INDEX";
	private String STATE_SLIDE_ENABLE = "STATE_SLIDE_ENABLE";
	private int currIndex; // current index of image to display
	private static int switch_time;
	private ImageView imageView; // displays the current image
	View itemView;
	TextView titleView; // displays the current title
	TextView textView; // displays the current text

	private SlideshowInfo showInfo; // slide show being played
	private Handler slideHandler; // used to update the slide show
	private BroadcastReceiver mReceiver;
   
	// initializes the SlideshowPlayer Activity
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.slideshow_player);
      
		UilCommon.init();
		Util.setFullScreen(this);
		
		// disable screen saving
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

		// disable key guard
		getWindow().addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD);
      
		System.out.println("SlideshowPlayer / _onCreate ");
		if (savedInstanceState == null) // Activity starting
		{
			currIndex = -1; // default
			enableSlide = true;
		}

		// get SlideshowInfo for showInfo to play
		showInfo = MainAct.slideshowInfo;
   	  	slideHandler = new Handler(); // create handler to control showInfo

		// intent filter
//   	  	IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
//   	  	filter.addAction(Intent.ACTION_SCREEN_OFF);
//   	  	mReceiver = new SlideshowScreenReceiver();
//   	  	registerReceiver(mReceiver, filter);

		// preference of switch time
		SharedPreferences pref_sw_time = MainAct.mAct.getSharedPreferences("slideshow_sw_time", 0);
		switch_time = Integer.valueOf(pref_sw_time.getString("KEY_SLIDESHOW_SW_TIME","5"));
	}
   
	@Override
	protected void onRestart()
	{
		System.out.println("SlideshowPlayer / _onRestart ");
		super.onRestart();
	}   

   
	// called after onCreate and sometimes onStop
	@Override
	protected void onStart()
	{
		System.out.println("SlideshowPlayer / _onStart ");
		super.onStart();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		System.out.println("SlideshowPlayer / _onRestoreInstanceState ");
		super.onRestoreInstanceState(savedInstanceState);

        // restore index
		currIndex = savedInstanceState.getInt(STATE_SLIDE_INDEX);

        // restore showInfo state
        enableSlide = savedInstanceState.getBoolean(STATE_SLIDE_ENABLE,true);

        // restore paused view
        if(!enableSlide)
		{
			itemView = findViewById(R.id.show_item);
			imageView = (ImageView) findViewById(R.id.show_image);

//			imageView.setOnClickListener(new View.OnClickListener() {
			itemView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					enableSlide = !enableSlide;
                    showToast(enableSlide);
                    if(enableSlide)
                    {
                        slideHandler.removeCallbacks(runSlideshow);
                        slideHandler.postDelayed(runSlideshow, 500);
                    }
				}
			});

//			imageView.setOnLongClickListener(new View.OnLongClickListener(){
			itemView.setOnLongClickListener(new View.OnLongClickListener(){
                @Override
                public boolean onLongClick(View v) {
                    slideHandler.removeCallbacks(runSlideshow);
                    finish();

                    Intent intent;
					viewHolder = showInfo.getShowItem(currIndex);
					Integer position = viewHolder.position;
                    intent = new Intent(SlideshowPlayer.this, Note.class);
                    intent.putExtra("POSITION", position);
                    startActivity(intent);
                    return false;
                }
            });
            populateItemView(currIndex);
		}
	}
   
	// called after onStart or onPause
	@Override
	protected void onResume()
	{
		System.out.println("SlideshowPlayer / _onResume ");
		super.onResume();
		if(enableSlide)
   	  		slideHandler.post(runSlideshow); // post updateSlideshow to execute
	}

	// called when the Activity is paused
	@Override
	protected void onPause()
	{
		System.out.println("SlideshowPlayer / _onPause ");
		super.onPause();
   	  	slideHandler.removeCallbacks(runSlideshow);
	}

	// save slide show state so it can be restored in onRestoreInstanceState
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		System.out.println("SlideshowPlayer / _onSaveInstanceState ");
		super.onSaveInstanceState(outState);

		// for restarting current item after rotation
		if(enableSlide)
			currIndex--;

		// save index
		outState.putInt(STATE_SLIDE_INDEX, currIndex);

        // save showInfo state
		outState.putBoolean(STATE_SLIDE_ENABLE, enableSlide);
	}
   
	// called when the Activity stops
	@Override
	protected void onStop()
	{
		System.out.println("SlideshowPlayer / _onStop ");
		super.onStop();
	}

	// called when the Activity is destroyed
	@Override
	protected void onDestroy()
	{
		System.out.println("SlideshowPlayer / _onDestroy ");
		super.onDestroy();
//		unregisterReceiver(mReceiver);
	}

    boolean enableSlide = true;
	SlideshowInfo.ViewHolder viewHolder;
    /**
     * Runnable: runSlideshow
     *
     */
	private Runnable runSlideshow = new Runnable()
	{
		@Override
		public void run()
		{
			//change index from current to next
			if(!enableSlide)
				return;

			if ((currIndex+1) >= showInfo.showItemsSize())
				currIndex = 0; //over the size, back to 0
			else
				currIndex++;

			viewHolder = showInfo.getShowItem(currIndex);
			System.out.println("SlideshowPlayer / _Runnable runSlideshow / currIndex = " + currIndex);

			// check if Uri exists
			boolean uriOK;
			String path = viewHolder.imagePath;
			if (UtilImage.hasImageExtension(path, SlideshowPlayer.this))
				uriOK = Util.isUriExisted(path, SlideshowPlayer.this);
			else
				uriOK = false;
			System.out.println("SlideshowPlayer / _Runnable runSlideshow / uriOK = " + uriOK);

			String text = viewHolder.text;
			if((!uriOK) && Util.isEmptyString(text))
			{
				// post instantly
				slideHandler.post(runSlideshow); // go to display next instantly
				return;
			}

			itemView = findViewById(R.id.show_item);
			imageView = (ImageView) findViewById(R.id.show_image);

			//on Click listener
//			imageView.setOnClickListener(new View.OnClickListener() {
			itemView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					enableSlide = !enableSlide;
					showToast(enableSlide);
					if (enableSlide) {
						slideHandler.removeCallbacks(runSlideshow);
						slideHandler.postDelayed(runSlideshow, 500);
					}
				}
			});

			//on Long Click listener
//			imageView.setOnLongClickListener(new View.OnLongClickListener() {
			itemView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					viewHolder = showInfo.getShowItem(currIndex);
					Integer position = viewHolder.position;
					slideHandler.removeCallbacks(runSlideshow);
					finish();

					Intent intent;
					intent = new Intent(SlideshowPlayer.this, Note.class);
					intent.putExtra("POSITION", position);
					startActivity(intent);
					return false;
				}
			});

			populateItemView(currIndex);

			// post
			slideHandler.postDelayed(runSlideshow, switch_time * 1000);
		}
	};

    Toast toast;
    /**
     * Show toast for Pause or Play
     *
     * @param bEnableSlide
     */
	void showToast(boolean bEnableSlide) {
        if (!bEnableSlide) {
            if ((toast != null) && toast.getView().isShown())
                toast.cancel();

            toast = Toast.makeText(SlideshowPlayer.this, R.string.toast_pause, Toast.LENGTH_SHORT);
            toast.show();
        } else {
            if ((toast != null) && toast.getView().isShown())
                toast.cancel();

            toast = Toast.makeText(SlideshowPlayer.this, R.string.toast_play, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Populate image view and text view
     *
     * @param slideIndex
     */
	void populateItemView(int slideIndex) {
        // image
		SlideshowInfo.ViewHolder holder = showInfo.getShowItem(slideIndex);
		String path = holder.imagePath;

		// title
		titleView = (TextView) findViewById(R.id.show_title);
		if (!Util.isEmptyString(holder.title)) {
			titleView.setVisibility(View.VISIBLE);
			titleView.setText(holder.title);
		} else
			titleView.setVisibility(View.GONE);

		// image
		if(Util.isEmptyString(path))
			imageView.setVisibility(View.GONE);
		else
		{
			imageView.setVisibility(View.VISIBLE);
			UilCommon.imageLoader.displayImage(path,
					imageView,
					UilCommon.optionsForFadeIn,
					UilCommon.animateFirstListener);
		}

        // text
        textView = (TextView) findViewById(R.id.show_text);
        if (!Util.isEmptyString(holder.text)) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(holder.text);
        } else
            textView.setVisibility(View.GONE);
    }

}