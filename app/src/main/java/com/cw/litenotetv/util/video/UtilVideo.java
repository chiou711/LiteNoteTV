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

package com.cw.litenotetv.util.video;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

import com.cw.litenotetv.R;
import com.cw.litenotetv.note.NoteUi;
import com.cw.litenotetv.util.image.UtilImage;
import com.cw.litenotetv.util.Util;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ProgressBar;

public class UtilVideo 
{
	// Play mode setting, true:media control widget / false:custom control buttons
	public static boolean hasMediaControlWidget = false;// true;//      
	
	public static AppCompatActivity mAct;
	private static final String TAG_VIDEO = "UtilVideo";
	public final static int VIDEO_AT_STOP = 0;
	public final static int VIDEO_AT_PLAY = 1;
	public final static int VIDEO_AT_PAUSE = 2;

	public static VideoViewCustom mVideoView;
	public static int mVideoState;
	public static int mPlayVideoPosition;
	static String mPictureString;
	public static String currentPicturePath;
	public static View mCurrentPagerView;

	UtilVideo()	{}
	
	/***********************************************
	* init video view
	*
	initVideoView
		setVideoViewLayout
				getBitmapDrawableByPath
				branch _____ new AsyncTaskVideoBitmapPager
					   |____ setVideoViewDimensions()
						  |_ setBitmapDrawableToVideoView
		setVideoViewUI
	*/

	public static void initVideoView(ViewPager viewPager, final String strPicture, final AppCompatActivity act, int position)
    {
    	System.out.println("UtilVideo / _initVideoView / strPicture = " + strPicture);
		mAct = act;
		mPictureString = strPicture;

    	if(hasVideoExtension(mPictureString,mAct))
	  	{
        	System.out.println("UtilVideo / _initVideoView / has video extension");
        	setVideoViewLayout(mPictureString);

        	if(!hasMediaControlWidget) {
				NoteUi ui = new NoteUi(act,viewPager,position);
                NoteUi.updateVideoPlayButtonState(viewPager, NoteUi.getFocus_notePos());
				ui.tempShow_picViewUI(5006,strPicture);
			}
        	else
				playOrPauseVideo(viewPager,mPictureString);
  		}
    } // handle video entry
	
    public static BitmapDrawable mBitmapDrawable;
	
    // set video view layout
    public static void setVideoViewLayout(String picStr)
    {
    	System.out.println("UtilVideo / _setVideoViewLayout");
		// set video view
      	mVideoView = (VideoViewCustom) mCurrentPagerView.findViewById(R.id.video_view);
      	ProgressBar spinner = (ProgressBar) mCurrentPagerView.findViewById(R.id.loading);
      	
      	mVideoView.setPlayPauseListener(new VideoViewCustom.PlayPauseListener() 
      	{
      	    @Override
      	    public void onPlay() {
      	    	setVideoState(VIDEO_AT_PLAY);
      	        System.out.println("UtilVideo / _setVideoViewLayout / setPlayPauseListener / Play!");
      	    }

      	    @Override
      	    public void onPause() {
      	    	setVideoState(VIDEO_AT_PAUSE);
      	        System.out.println("UtilVideo / _setVideoViewLayout / setPlayPauseListener / Pause!");
      	    }
      	});      	
      	
		mVideoView.setVisibility(View.VISIBLE);
		
//		System.out.println("UtilVideo / _setVideoViewLayout / video view h = " + mVideoView.getHeight());
//		System.out.println("UtilVideo / _setVideoViewLayout / video view w = " + mVideoView.getWidth());
		
		// get bitmap by path
      	mBitmapDrawable = getBitmapDrawableByPath(mAct,picStr);
      	
      	// if bitmap drawable is null, start an Async task
	  	if(mBitmapDrawable.getBitmap() == null)
	  	{
	  		System.out.println("UtilVideo / _setVideoViewLayout / mBitmapDrawable.getBitmap() == null");
	      	AsyncTaskVideoBitmapPager mPagerVideoAsyncTask;
			mPagerVideoAsyncTask = new AsyncTaskVideoBitmapPager(mAct,mPictureString,mVideoView,spinner); 
			mPagerVideoAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"Searching media ...");
	  	}
	  	else // if bitmap is not null, set bitmap drawable to video view directly
	  	{
	  		AsyncTaskVideoBitmapPager.mVideoUrl = null;
	  		
	  		System.out.println("UtilVideo / _setVideoViewLayout / mBitmapDrawable.getBitmap() != null");
	  		setVideoViewDimensions(mBitmapDrawable);
	  		
	  		if(!hasMediaControlWidget)
	  		{
		      	// set bitmap drawable to video view
		  		if(UtilVideo.mVideoView.getCurrentPosition() == 0)
		  			setBitmapDrawableToVideoView(mBitmapDrawable,mVideoView);
	  		}
	  	}

//		if(mVideoView != null)
//		{
//	    	System.out.println("UtilVideo / _setVideoViewLayout / mVideoView != null");
//	      	mVideoView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
//		}
		
    }    
    
    // Set video view dimensions
    public static void setVideoViewDimensions(BitmapDrawable bitmapDrawable)
    {
    	int screenHeight = UtilImage.getScreenHeight(mAct);
	    int screenWidth = UtilImage.getScreenWidth(mAct);
//  		System.out.println("UtilVideo / _setVideoViewDimensions / screenHeight = " + screenHeight + ", screenWidth = " + screenWidth);
	    
      	int bitmapHeight=0,bitmapWidth=0;
      	int config_orientation = mAct.getResources().getConfiguration().orientation;

      	Bitmap bitmap = bitmapDrawable.getBitmap();
      	boolean bitmapIsLandScape = false;
      	boolean	bitmapIsPortrait = false;
      	
      	if(bitmap != null)
      	{
      		bitmapHeight = bitmap.getHeight();
      		bitmapWidth = bitmap.getWidth();
      		System.out.println("UtilVideo / _setVideoViewDimensions / bitmapHeight = " + bitmapHeight + ", bitmapWidth = " + bitmapWidth);
          	bitmapIsLandScape = ( bitmapWidth > bitmapHeight )?true:false;
          	bitmapIsPortrait = ( bitmapHeight > bitmapWidth )?true:false;
//          	System.out.println("UtilVideo / _setVideoViewDimensions / bitmapIsLandScape 1 = " + bitmapIsLandScape);
//          	System.out.println("UtilVideo / _setVideoViewDimensions / bitmapIsPortrait 1 = " + bitmapIsPortrait);
      	}
		else
		{
			// for remote video which there is no way to get bitmap yet
			// default dimension
			bitmapWidth = screenWidth;
			bitmapHeight = screenHeight/2;
			// default orientation is landscape
			AsyncTaskVideoBitmapPager.mRotationStr = "0"; //
		}

      	String rotDeg = AsyncTaskVideoBitmapPager.mRotationStr;
      	if(rotDeg != null)
      	{
      		System.out.println("UtilVideo / _setVideoViewDimensions / rotDeg = " + rotDeg);
      		if( rotDeg.equalsIgnoreCase("0"))
      		{
      	       	bitmapIsLandScape = true;
      	       	bitmapIsPortrait = false;
      	    }
      		else if( rotDeg.equalsIgnoreCase("90"))
      		{
      			bitmapIsLandScape = false;
      			bitmapIsPortrait = true;
      		}
      	}
//      	System.out.println("UtilVideo / _setVideoViewDimensions / bitmapIsLandScape 2 = " + bitmapIsLandScape);
//      	System.out.println("UtilVideo / _setVideoViewDimensions / bitmapIsPortrait 2 = " + bitmapIsPortrait);
      	
      	int dimWidth = 0;
      	int dimHeight = 0;
      	// for landscape screen
  		if (config_orientation == Configuration.ORIENTATION_LANDSCAPE)
  		{
  			// for landscape bitmap
  			if(bitmapIsLandScape)
  			{
  	          	System.out.println("UtilVideo / _setVideoViewDimensions / L_scr L_bmp");
          		dimWidth = screenWidth;
          		dimHeight = screenHeight;
  			}// for portrait bitmap
  			else if (bitmapIsPortrait)
  			{
  	          	System.out.println("UtilVideo / _setVideoViewDimensions / L_scr P_bmp");
  				// set screen height to be constant, and set screen width by proportional
  	          	int propotionalWidth = 0;
  	          	if(bitmap != null)
  	          	{
  	          		propotionalWidth = (bitmapWidth > bitmapHeight)?
  							  		   Math.round(screenHeight * bitmapHeight/bitmapWidth) : 
  							  		   Math.round(screenHeight * bitmapWidth/bitmapHeight);
  	          	}
  	          	else
  	          		propotionalWidth = Math.round(screenHeight * screenHeight/screenWidth);
  	          	
          		dimWidth = propotionalWidth;
          		dimHeight = screenHeight;
  			}
  		}// for portrait screen
  		else if (config_orientation == Configuration.ORIENTATION_PORTRAIT)
  		{
  			// for landscape bitmap
  			if(bitmapIsLandScape)
  			{
  	          	System.out.println("UtilVideo / _setVideoViewDimensions / P_scr L_bmp");
	    		// set screen width to be constant, and set screen height by proportional
  	          	int propotiaonalHeight = 0;
  	          	if(bitmap != null)
  	          	{
  	          		
  	          		propotiaonalHeight = (bitmapWidth > bitmapHeight)?
  	          							  Math.round(screenWidth * bitmapHeight/bitmapWidth) : 
  	          							  Math.round(screenWidth * bitmapWidth/bitmapHeight);
  	          	}
  	          	else
  	          		propotiaonalHeight = Math.round(screenWidth * screenWidth/screenHeight );
  	          	
          		dimWidth = screenWidth;
          		dimHeight = propotiaonalHeight;
  			}// for portrait bitmap
  			else if (bitmapIsPortrait)
  			{
  	          	System.out.println("UtilVideo / _setVideoViewDimensions / P_scr P_bmp");
  	          	
          		dimWidth = screenWidth;
          		dimHeight = screenHeight;
  			}
  		}
  		
  		// set dimensions
    	if(UtilVideo.mVideoView != null)
    	{
    		UtilVideo.mVideoView.setDimensions(dimWidth, dimHeight);
    		UtilVideo.mVideoView.getHolder().setFixedSize(dimWidth, dimHeight);
    		System.out.println("UtilVideo / _setVideoViewDimensions / dim Width = " + dimWidth + ", dim Height = " + dimHeight);
    	}

  		
  	} //setVideoViewDimensions
    
    
    // on global layout listener
//	static OnGlobalLayoutListener onGlobalLayoutListener = new OnGlobalLayoutListener() 
//	{
//		@SuppressWarnings("deprecation")
//		@Override
//		public void onGlobalLayout() 
//		{
//			//action bar height is 120(landscape),144(portrait)
//			System.out.println("OnGlobalLayoutListener / getActionBar().getHeight() = " + mAct.getActionBar().getHeight());
//
//	      	// get bitmap by path
//	      	BitmapDrawable bitmapDrawable = getBitmapDrawableByPath(mAct,mPictureString);
//	      	
//		  	if(bitmapDrawable.getBitmap() == null)
//		  	{
//		      	PagerVideoAsyncTask mPagerVideoAsyncTask = null;
//				mPagerVideoAsyncTask = new PagerVideoAsyncTask(mAct,mPictureString,mVideoView);
//				mPagerVideoAsyncTask.execute("Searching media ...");
//		  	}
//		  	else
//		      	// set bitmap drawable to video view
//		  		setBitmapDrawableToVideoView(bitmapDrawable,mVideoView);
//		} 
//	};	
	
	// Set Bitmap Drawable to Video View
	public static void setBitmapDrawableToVideoView(BitmapDrawable bitmapDrawable, VideoViewCustom videoView)
  	{
		//set bitmap drawable to video view
		System.out.println("UtilVideo / _setBitmapDrawableToVideoView / mPlayVideoPosition = " + mPlayVideoPosition);
		if(Build.VERSION.SDK_INT >= 16)
		{
			if(mPlayVideoPosition == 0)
				videoView.setBackground(bitmapDrawable);
		}
		else
		{
			if(mPlayVideoPosition == 0)
				videoView.setBackgroundDrawable(bitmapDrawable);
		}
  	}
	
	// get bitmap drawable by path
	static BitmapDrawable getBitmapDrawableByPath(Activity mAct,String picPathStr)
	{
		String path = Uri.parse(picPathStr).getPath();
		Bitmap bmThumbnail = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
		BitmapDrawable bitmapDrawable = new BitmapDrawable(mAct.getResources(),bmThumbnail);
		return bitmapDrawable;
	}
	
	public static VideoPlayer mVideoPlayer;
	// Play or Pause video
	public static void playOrPauseVideo(ViewPager pager,String picString)
	{
		System.out.println("UtilVideo / _playOrPauseVideo / picString = " + picString);
		
		if( mVideoView!= null)
			System.out.println("UtilVideo / _playOrPauseVideo / mVideoView != null");
		else
			System.out.println("UtilVideo / _playOrPauseVideo / mVideoView == null");

		if( mVideoView!= null)
		{
			if(!mVideoView.isPlaying())
			{
				System.out.println("UtilVideo / _playOrPauseVideo / mVideoView is not playing");
				if(Build.VERSION.SDK_INT >= 16)
					mVideoView.setBackground(null);
				else
					mVideoView.setBackgroundDrawable(null);

				mVideoView.setVisibility(View.VISIBLE);

				//start a new Video player instance
				mVideoPlayer = new VideoPlayer(mAct,pager,picString);
			}
			else if(mVideoPlayer != null)
			{
				System.out.println("UtilVideo / _playOrPauseVideo / mVideoPlayer is not null");
				mVideoPlayer.goOnVideo(pager);
			}
		}
	}

	// change video state
	public static void changeVideoState()
	{
		int state = UtilVideo.getVideoState();
		if (state == UtilVideo.VIDEO_AT_PLAY)
			UtilVideo.setVideoState(UtilVideo.VIDEO_AT_PAUSE);
		else if ((state == UtilVideo.VIDEO_AT_PAUSE) || (state == UtilVideo.VIDEO_AT_STOP))
			UtilVideo.setVideoState(UtilVideo.VIDEO_AT_PLAY);
	}

	// get video data source path
	public static String getVideoDataSource(String path) throws IOException
	{
		if (!URLUtil.isNetworkUrl(path)) 
		{
			return path;
		} 
		else 
		{
			URL url = new URL(path);
			URLConnection cn = url.openConnection();
			cn.connect();
			InputStream stream = cn.getInputStream();
			
			if (stream == null)
				throw new RuntimeException("stream is null");
			
			File temp = File.createTempFile("mediaplayertmp", "dat");
			temp.deleteOnExit();
			String tempPath = temp.getAbsolutePath();
			FileOutputStream out = new FileOutputStream(temp);
			byte buf[] = new byte[128];
			
			do
			{
				System.setProperty("http.keepAlive", "false");//??? need this?
				int numread = stream.read(buf);
				if (numread <= 0)
					break;
				out.write(buf, 0, numread);
			} while (true);
			
			try 
			{
				stream.close();
				out.close();
			} catch (IOException ex) 
			{
				Log.e(TAG_VIDEO, "error: " + ex.getMessage(), ex);
			}
			System.out.println("UtilVideo / _getVideoDataSource / tempPath " + tempPath);
			return tempPath;
		}
	}	
	
    // check if file has video extension
    // refer to http://developer.android.com/intl/zh-tw/guide/appendix/media-formats.html
    public static boolean hasVideoExtension(File file)
    {
    	boolean isVideo = false;
    	String fn = file.getName().toLowerCase(Locale.getDefault());
    	if(	fn.endsWith("3gp") || fn.endsWith("mp4") ||
    		fn.endsWith("ts") || fn.endsWith("webm") || fn.endsWith("mkv")  ) 
	    	isVideo = true;
	    
    	return isVideo;
    } 
    
    // check if string has video extension
    public static boolean hasVideoExtension(String string, Activity act)
    {
    	boolean hasVideo = false;
    	if(!Util.isEmptyString(string))
    	{
	    	String fn = string.toLowerCase(Locale.getDefault());
//	    	System.out.println("UtilVideo / _hasVideoExtension / fn 1 = " + fn);
	    	if(	fn.endsWith("3gp") || fn.endsWith("mp4") ||
	    		fn.endsWith("ts") || fn.endsWith("webm") || fn.endsWith("mkv")  ) 
		    	hasVideo = true;
    	}
		else
			return hasVideo;
    	
    	if(!hasVideo)
    	{
    		String fn = Util.getDisplayNameByUriString(string, act);
	    	fn = fn.toLowerCase(Locale.getDefault());
//	    	System.out.println("UtilVideo / _hasVideoExtension / fn 2 = " + fn);
	    	if(	fn.endsWith("3gp") || fn.endsWith("mp4") ||
	    		fn.endsWith("ts") || fn.endsWith("webm") || fn.endsWith("mkv")  ) 
		    	hasVideo = true;    		
    	}
    	
    	return hasVideo;
    } 
    
    // Set video player listeners
	static void setVideoPlayerListeners(final ViewPager pager,final String picString)
	{
		// on complete listener
		mVideoView.setOnCompletionListener(new OnCompletionListener()
		{
			@Override
			public void onCompletion(MediaPlayer mp)
			{
				System.out.println("UtilVideo / _setOnCompletionListener / _onCompletion");
				mPlayVideoPosition = 0;
				setVideoState(VIDEO_AT_PAUSE);
				
				if(!hasMediaControlWidget)
					NoteUi.updateVideoPlayButtonState(pager,mPlayVideoPosition);
			}
		});					
		
		// on prepared listener
		mVideoView.setOnPreparedListener(new OnPreparedListener() 
		{
			@Override
			public void onPrepared(MediaPlayer mp) 
			{
				System.out.println("UtilVideo / _setOnPreparedListener");
				
				if(!hasMediaControlWidget)
				{
					NoteUi.updateVideoPlayButtonState(pager,mPlayVideoPosition);
					NoteUi.primaryVideoSeekBarProgressUpdater(pager,NoteUi.getFocus_notePos(),UtilVideo.mPlayVideoPosition,picString);
					mp.setOnSeekCompleteListener(new OnSeekCompleteListener()
					{
						@Override
						public void onSeekComplete(MediaPlayer mp) 
						{}
					});
				}
			}
		});
	}
    
	public static int getVideoState() {
		return mVideoState;
	}

	public static void setVideoState(int videoState) {
		System.out.print("UtilVideo / _setVideoState / set state to be = ");

		if(videoState == VIDEO_AT_STOP)
			System.out.println("VIDEO_AT_STOP");
		else if(videoState == VIDEO_AT_PLAY)
			System.out.println("VIDEO_AT_PLAY");
		else if(videoState == VIDEO_AT_PAUSE)
			System.out.println("VIDEO_AT_PAUSE");

		mVideoState = videoState;
	}    
}

