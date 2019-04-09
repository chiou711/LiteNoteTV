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

import com.cw.litenotetv.R;
import com.cw.litenotetv.util.image.UtilImage;
import com.cw.litenotetv.note.Note;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

//Video Async Task for applying MediaMetadataRetriever
//Note: setDataSource could hang up system for a long time when accessing remote content
public class AsyncTaskVideoBitmap extends AsyncTask<String,Integer,String>
{
	 Activity mAct;
	 String mPictureUri;
	 ImageView mImageView;
	 MediaMetadataRetriever mmr;
	 Bitmap bitmap;
	 ProgressBar mProgressBar;
	 
	 public AsyncTaskVideoBitmap(Activity act,String picString, ImageView view, ProgressBar progressBar)
	 {
		 mAct = act;
		 mPictureUri = picString;
		 mImageView = view;
		 mProgressBar = progressBar;
		 System.out.println("AsyncTaskVideoBitmap constructor");
	 }	 
	 
	 @Override
	 protected void onPreExecute() 
	 {
		 super.onPreExecute();
		 System.out.println("AsyncTaskVideoBitmap / _onPreExecute");
		 mImageView.setVisibility(View.GONE);
		 mProgressBar.setProgress(0);
		 mProgressBar.setVisibility(View.VISIBLE);
	 } 
	 
	 @Override
	 protected String doInBackground(String... params) 
	 {
		 System.out.println("AsyncTaskVideoBitmap / _doInBackground");
		 if(Note.isPagerActive)
		 {
			if(this != null)
			{
				System.out.println("NoteFragment.mVideoAsyncTask != null");
				
				if(this.isCancelled())
					System.out.println("NoteFragment.mVideoAsyncTask.isCancelled()");
				else
					System.out.println("NoteFragment.mVideoAsyncTask is not Cancelled()");
				
				 if( (this != null) && (!this.isCancelled()) )
				 {
					 System.out.println("    NoteFragment.mVideoAsyncTask cancel");
					 this.cancel(true);
					 return "cancel";
				 }				
			}
			else
				System.out.println("NoteFragment.mVideoAsyncTask = null");
		 }
		 
		 mmr = new MediaMetadataRetriever();
		 try
		 {
//			 System.out.println("AsyncTaskVideoBitmap / setDataSource start");
			 mmr.setDataSource(mAct,Uri.parse(mPictureUri));
//			 System.out.println("AsyncTaskVideoBitmap / setDataSource done");
			 bitmap = mmr.getFrameAtTime(-1);
			 bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true);
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
		 System.out.println("AsyncTaskVideoBitmap / _onPostExecute");
		 mProgressBar.setVisibility(View.GONE);
		 mImageView.setVisibility(View.VISIBLE);
		 
		 Bitmap bmVideoIcon = BitmapFactory.decodeResource(mAct.getResources(), R.drawable.ic_media_play);
		 bitmap = UtilImage.setIconOnThumbnail(bitmap,bmVideoIcon,50);
		 
		 if(bitmap != null)
		 {
			 mImageView.setImageBitmap(bitmap);
			 System.out.println("AsyncTaskVideoBitmap / set image bitmap");
		 }
	 }
}