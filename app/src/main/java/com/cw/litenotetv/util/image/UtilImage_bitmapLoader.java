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

package com.cw.litenotetv.util.image;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.cw.litenotetv.R;
import com.cw.litenotetv.util.video.AsyncTaskVideoBitmap;
import com.cw.litenotetv.util.video.UtilVideo;
import com.cw.litenotetv.util.uil.UilCommon;
import com.cw.litenotetv.util.Util;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;


public class UtilImage_bitmapLoader
{
	private Bitmap thumbnail;
	private AsyncTaskVideoBitmap mVideoAsyncTask;
	private SimpleImageLoadingListener mSimpleUilListener, mSimpleUilListenerForVideo;
	private ImageLoadingProgressListener mUilProgressListener;
	private ProgressBar mProgressBar;
	private ImageView mPicImageView;
  

	public UtilImage_bitmapLoader(ImageView picImageView,
								  String mPictureUriInDB,
								  final ProgressBar progressBar,
								  DisplayImageOptions options,
								  Activity mAct )
	{
 	    setLoadingListeners();
	    mPicImageView = picImageView;
	    mProgressBar = progressBar;
	    
		Bitmap bmVideoIcon = BitmapFactory.decodeResource(mAct.getResources(), R.drawable.ic_media_play);
		Uri imageUri = Uri.parse(mPictureUriInDB);
		String pictureUri = imageUri.toString();
//		System.out.println("UtilImage_bitmapLoader / _constructor / pictureUri = " + pictureUri);
		
		// 1 for image check
		if (UtilImage.hasImageExtension(pictureUri,mAct)) 
		{
//			System.out.println("UtilImage_bitmapLoader constructor / has image extension");
			UilCommon.imageLoader
					 .displayImage(	pictureUri,
									mPicImageView,
									options,
									mSimpleUilListener,
									mUilProgressListener);
		}
		// 2 for video check
		else if (UtilVideo.hasVideoExtension(pictureUri,mAct)) 
		{
//			System.out.println("UtilImage_bitmapLoader / _constructor / has video extension");
			Uri uri = Uri.parse(pictureUri);
			String path = uri.getPath();
			
			// check if content is local or remote
			if(Util.getUriScheme(pictureUri).equals("content"))
			{
				path = Util.getLocalRealPathByUri(mAct,uri);
			}

			// for local
			if(!pictureUri.startsWith("http") && (path != null))
			{
				System.out.println("UtilImage_bitmapLoader constructor / local");
				thumbnail = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MICRO_KIND);

				// check if video thumb nail exists
				if (thumbnail != null) 
				{
					// add video play icon overlay
					thumbnail = UtilImage.setIconOnThumbnail(thumbnail,	bmVideoIcon, 50);
					UilCommon.imageLoader
							 .displayImage( "drawable://" + R.drawable.ic_media_play,
									 mPicImageView,
									 options,
									 mSimpleUilListenerForVideo,
									 mUilProgressListener);
				}
				// video file is not found
				else 
				{
					UilCommon.imageLoader
				 			 .displayImage( "drawable://" + R.drawable.ic_not_found,
				 					 mPicImageView,
				 					 options,
									 mSimpleUilListener,
									 mUilProgressListener);
	
				}
			}
			else // for remote
			{
				System.out.println("UtilImage_bitmapLoader constructor / remote");
				// refer to
				// http://android-developers.blogspot.tw/2009/05/painless-threading.html
				mVideoAsyncTask = new AsyncTaskVideoBitmap(mAct, pictureUri, mPicImageView, mProgressBar);
				mVideoAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"Searching media ...");
			}
		}
		//??? add some code when content is scheme?
//		else
//		{
//			System.out.println("UtilImage_bitmapLoader constructor / can not decide image and video");
//			mPicImageView.setVisibility(View.GONE);
//		}
	}

	private  void setLoadingListeners()
    {
        // set image loading listener
        mSimpleUilListener = new SimpleImageLoadingListener() 
        {
      	    @Override
      	    public void onLoadingStarted(String imageUri, View view)
      	    {
      		    mPicImageView.setVisibility(View.GONE);
      		    mProgressBar.setProgress(0);
      		    mProgressBar.setVisibility(View.VISIBLE);
      	    }

      	    @Override
      	    public void onLoadingFailed(String imageUri, View view, FailReason failReason)
      	    {
      		    mProgressBar.setVisibility(View.GONE);
      		    mPicImageView.setVisibility(View.VISIBLE);

				String message = null;
				switch (failReason.getType()) {
					case IO_ERROR:
//				        message = "Input/Output error";
//						message = mAct.getResources().getString(R.string.file_not_found);
						break;
					case DECODING_ERROR:
						message = "Image can't be decoded";
						break;
					case NETWORK_DENIED:
						message = "Downloads are denied";
						break;
					case OUT_OF_MEMORY:
						message = "Out Of Memory error";
						break;
					case UNKNOWN:
						message = "Unknown error";
						break;
				}
//				Toast.makeText(mAct, message, Toast.LENGTH_SHORT).show();
      	    }

      	    @Override
      	    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
      	    {
      		    super.onLoadingComplete(imageUri, view, loadedImage);
      		    mProgressBar.setVisibility(View.GONE);
      		    mPicImageView.setVisibility(View.VISIBLE);
      	    }
  		};

  		// set image loading listener for video
  		mSimpleUilListenerForVideo = new SimpleImageLoadingListener() 
  		{
  			@Override
  			public void onLoadingStarted(String imageUri, View view) 
  			{
  				mPicImageView.setVisibility(View.GONE);
  				mProgressBar.setProgress(0);
  				mProgressBar.setVisibility(View.VISIBLE);
  			}

  			@Override
  			public void onLoadingFailed(String imageUri, View view, FailReason failReason) 
  			{
  				mProgressBar.setVisibility(View.GONE);
  				mPicImageView.setVisibility(View.VISIBLE);

  			}

  			@Override
  			public void onLoadingComplete(final String imageUri, View view, Bitmap loadedImage) 
  			{
  				super.onLoadingComplete(imageUri, view, loadedImage);
  				mProgressBar.setVisibility(View.GONE);
  				mPicImageView.setVisibility(View.VISIBLE);
  				// set thumb nail bitmap instead of video play icon
				mPicImageView.setImageBitmap(UtilImage.getRoundedCornerBitmap(thumbnail,10));
  			}
  		};

  		// Set image loading process listener
  		mUilProgressListener = new ImageLoadingProgressListener() 
  		{
  			@Override
  			public void onProgressUpdate(String imageUri, View view, int current, int total) 
  			{
  				mProgressBar.setProgress(Math.round(100.0f * current / total));
  			}
  		};
    }
    
}
