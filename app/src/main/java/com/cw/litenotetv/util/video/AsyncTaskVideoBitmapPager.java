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


import java.io.IOException;

import com.cw.litenotetv.util.image.UtilImage;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

// pager video async task
public class AsyncTaskVideoBitmapPager extends AsyncTask<String,Integer,String>
{
	AppCompatActivity mAct;
	 String mPictureUri;
	 VideoViewCustom mVideoView;
	 MediaMetadataRetriever mmr;
	 Bitmap bitmap;
	 static String mVideoUrl;
	 public static String mRotationStr = null;
	 ProgressBar mProgressBar;
	 
	 public AsyncTaskVideoBitmapPager(AppCompatActivity act, String mPictureString, VideoViewCustom view, ProgressBar spinner)
	 {
		 mAct = act;
		 mPictureUri = mPictureString;
		 System.out.println("AsyncTaskVideoBitmapPager constructor / mPictureUri = " + mPictureUri);
		 mVideoView = view;
		 mProgressBar = spinner;
	 }

	@Override
	 protected void onPreExecute() 
	 {
		 super.onPreExecute();
		 mVideoView.setVisibility(View.INVISIBLE);
		 mProgressBar.setProgress(0);
		 mProgressBar.setVisibility(View.VISIBLE);
	 } 
	 
	 @Override
	 protected String doInBackground(String... params) 
	 {
		 // remote video samples
//		 String strPath = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
//		 String strPath = "http://mgalaxy.net/load/Videos/Funny_Videos/Talking_Dog.3gp";
//		 String strPath = "http://techslides.com/demos/sample-videos/small.mp4";

	     try {
			mVideoUrl = UtilVideo.getVideoDataSource(mPictureUri);
	     } catch (IOException e1) {
			e1.printStackTrace();
	     }
		 
		 mmr = new MediaMetadataRetriever();
		 try
		 {
//			 System.out.println("AsyncTaskVideoBitmapPager / setDataSource start / mPictureUri = " + mPictureUri);
			 mmr.setDataSource(mAct,Uri.parse(mPictureUri));
			 bitmap = mmr.getFrameAtTime(-1);
			 bitmap = Bitmap.createScaledBitmap(bitmap, UtilImage.getScreenWidth(mAct), UtilImage.getScreenHeight(mAct), true);
			 
			 if (Build.VERSION.SDK_INT >= 17) {
				 mRotationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
				 System.out.println("PagerVideoAsyncTask / Rotation = " + mRotationStr);
			 }
			 
			 mmr.release();
		 }
		 catch(Exception e)
		 { }

		 return null;
	 }
	
	 @Override
	 protected void onProgressUpdate(Integer... progress) 
	 { 
	     super.onProgressUpdate(progress);
	 }
	 
	 // This is executed in the context of the main GUI thread
	 protected void onPostExecute(String result)
	 {
		 System.out.println("AsyncTaskVideoBitmapPager / _onPostExecute / mVideoUrl = " + mVideoUrl);
		 mProgressBar.setVisibility(View.GONE);
		 mVideoView.setVisibility(View.VISIBLE);

		 BitmapDrawable bitmapDrawable = new BitmapDrawable(mAct.getResources(),bitmap);

		 UtilVideo.setVideoViewDimensions(bitmapDrawable);

		 if((!UtilVideo.hasMediaControlWidget) &&  (bitmapDrawable != null))
		 {
			 if(mVideoView.getCurrentPosition() == 0)
				 UtilVideo.setBitmapDrawableToVideoView(bitmapDrawable,mVideoView);
		 }
	 }
}
