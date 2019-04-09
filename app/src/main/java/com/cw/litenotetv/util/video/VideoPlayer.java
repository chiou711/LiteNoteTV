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

import com.cw.litenotetv.note.Note;
import com.cw.litenotetv.note.NoteUi;
import com.cw.litenotetv.util.Util;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;

public class VideoPlayer 
{
	private static final String TAG_VIDEO = "VIDEO_PLAYER"; // error logging tag
	private static final int DURATION_1S = 1000; // 1000 = 1 second

	static Activity mAct;
    static ViewPager pager;
    static MediaController mMediaController;
	public static Handler mVideoHandler;
	private static String mPlayingPath;
    private static String mCurrentPicStr;

	public VideoPlayer(AppCompatActivity act, ViewPager pager, String picString)
	{
		mAct = act;
		mPlayingPath = picString;
		int state = UtilVideo.getVideoState();
		if(state == UtilVideo.VIDEO_AT_PLAY)
		{
			if(UtilVideo.mPlayVideoPosition == 0)
				startVideo(pager,picString);
			else if(UtilVideo.mPlayVideoPosition > 0)
				goOnVideo(pager);
		}
		else if(state == UtilVideo.VIDEO_AT_PAUSE)
		{
			goOnVideo(pager);
		}
	}

	private void startVideo(ViewPager viewPager,String picString)
	{
		System.out.println("VideoPlayer / _startVideo");
		pager = viewPager;
		mCurrentPicStr = picString;
		// remove call backs to make sure next toast will appear soon
		if(mVideoHandler != null)
		{
			mVideoHandler.removeCallbacks(mRunPlayVideo); 
		}
		
		// start a new handler
		mVideoHandler = new Handler();
		
		if(UtilVideo.mVideoView != null)
		{
			if(!UtilVideo.hasMediaControlWidget)
			{
				UtilVideo.setVideoPlayerListeners(viewPager,picString);
			}
			else
			{
				setMediaController(viewPager);
			}
		}
		mVideoHandler.post(mRunPlayVideo);
	}
	
	public static void stopVideo()
	{
		System.out.println("VideoPlayer / _stopVideo");
		
		if(mVideoHandler != null)
		{
			mVideoHandler.removeCallbacks(mRunPlayVideo); 
		}
		
		if((UtilVideo.mVideoView != null) && UtilVideo.mVideoView.isPlaying())
		{
			UtilVideo.mVideoView.stopPlayback();
		}

		UtilVideo.mVideoView = null;
		UtilVideo.mVideoPlayer = null;

		AsyncTaskVideoBitmapPager.mVideoUrl = null;
		UtilVideo.setVideoState(UtilVideo.VIDEO_AT_STOP);
	}	
	
	void goOnVideo(ViewPager pager)
	{
		System.out.println("VideoPlayer / _goOnVideo");
		if(UtilVideo.hasMediaControlWidget)
			setMediaController(pager);

		if(mVideoHandler != null)
			mVideoHandler.post(mRunPlayVideo);
	}
	
	//
	// Runnable for play video
	//
	public static Runnable mRunPlayVideo = new Runnable()
	{   @Override
		public void run()
		{
			// for remote video
		    String path = AsyncTaskVideoBitmapPager.mVideoUrl;
		    if(Util.isEmptyString(path))
		    	path = mPlayingPath;
		    	
//		    System.out.println("VideoPlayer / mRunPlayVideo / path = " + path);
			if(UtilVideo.mVideoView != null)
			{	
				try 
				{
					if(Util.isEmptyString(path) || (NoteUi.videoFileLength_inMilliSeconds ==0))
						Toast.makeText(mAct, "Video file URL/path is empty or video file is not playable",Toast.LENGTH_LONG).show();
					else 
					{
						// for key protect
						if(Note.mPlayVideoPositionOfInstance > 0)
							UtilVideo.mPlayVideoPosition = Note.mPlayVideoPositionOfInstance;
						// for view mode change
						else if(Note.mIsViewModeChanged)
							UtilVideo.mPlayVideoPosition = Note.mPositionOfChangeView;
						else
							UtilVideo.mPlayVideoPosition = UtilVideo.mVideoView.getCurrentPosition();

//						System.out.println("VideoPlayer / mRunPlayVideo/ UtilVideo.mPlayVideoPosition = " + UtilVideo.mPlayVideoPosition);
						
						// start processing video view
						processVideoView(pager,path);
						
						//
						if(!UtilVideo.hasMediaControlWidget)
						{
							if( NoteUi.showSeekBarProgress )
								NoteUi.primaryVideoSeekBarProgressUpdater(pager,NoteUi.getFocus_notePos(),UtilVideo.mPlayVideoPosition,mCurrentPicStr);

							// final play
							int diff = Math.abs(UtilVideo.mPlayVideoPosition - NoteUi.videoFileLength_inMilliSeconds);
							if( diff  <= 1000)
							{	
								System.out.println("VideoPlayer / mRunPlayVideo/ final play");
//								Note_view.mPlayVideoPositionOfInstance = 1; // for Pause at start
								UtilVideo.setVideoState(UtilVideo.VIDEO_AT_PAUSE);//
								mVideoHandler.postDelayed(mRunPlayVideo,DURATION_1S);
								//will call: UtilVideo / _setOnCompletionListener / _onCompletion
							}
							
							// delay and execute runnable
							if(UtilVideo.getVideoState() == UtilVideo.VIDEO_AT_PLAY)
								mVideoHandler.postDelayed(mRunPlayVideo,DURATION_1S);							
						}
					}
				} 
				catch (Exception e) 
				{
					Log.e(TAG_VIDEO, "VideoPlayer / mRunPlayVideo / error: " + e.getMessage(), e);
					VideoPlayer.stopVideo();
				}
			}
		} 
	};

	private static int mCount = 0;
	// process video view
	private static void processVideoView(ViewPager pager,String path) throws IOException
	{
		int state = UtilVideo.getVideoState();

//		mCount++;
//        String prefix = "VideoPlayer / _processVideoView / to state = ";
//		if(state == 1) {
//			System.out.println(prefix + "VIDEO_AT_STOP");
//			mCount = 0;
//		}
//		else if(state == 2)
//			System.out.println(prefix + "VIDEO_AT_PLAY " + mCount);
//		else if(state == 3) {
//			System.out.println(prefix + "VIDEO_AT_PAUSE");
//			mCount = 0;
//		}


		// To Play state
		if(state ==  UtilVideo.VIDEO_AT_PLAY)     
		{
			// after view mode is changed
			if(Note.mIsViewModeChanged)
			{
				System.out.println("VideoPlayer / _processVideoView/ isViewModeChanged/ to Play");
				UtilVideo.currentPicturePath = path;
				UtilVideo.mVideoView.setVideoPath(UtilVideo.getVideoDataSource(path));
				UtilVideo.mVideoView.seekTo(UtilVideo.mPlayVideoPosition);
				UtilVideo.mVideoView.start();
				UtilVideo.mVideoView.requestFocus();

				Note.mIsViewModeChanged = false;
			}
			// from Stop to Play
			else if(UtilVideo.mPlayVideoPosition == 0) 
			{
				System.out.println("VideoPlayer / _processVideoView/ from Stop to Play: normal start video");
				UtilVideo.currentPicturePath = path;
				UtilVideo.mVideoView.setVideoPath(UtilVideo.getVideoDataSource(path));
                UtilVideo.mVideoView.start();
				UtilVideo.mVideoView.requestFocus();

				if(!UtilVideo.hasMediaControlWidget)
					NoteUi.updateVideoPlayButtonState(pager, NoteUi.getFocus_notePos());
			}
			// from Pause to Play
			else if((UtilVideo.mPlayVideoPosition > 0) &&
				     path.equals(UtilVideo.currentPicturePath) &&
				     !UtilVideo.mVideoView.isPlaying()            )
			{
                System.out.println("VideoPlayer / _processVideoView / from Pause to Play");
				UtilVideo.mVideoView.start();
				UtilVideo.mVideoView.requestFocus();

				if(!UtilVideo.hasMediaControlWidget)
					NoteUi.updateVideoPlayButtonState(pager, NoteUi.getFocus_notePos());
			}
		}
		
		// To Pause state
		if(state ==  UtilVideo.VIDEO_AT_PAUSE)     
		{
			if(Note.mIsViewModeChanged || (Note.mPlayVideoPositionOfInstance > 0) )
			{
                System.out.println("VideoPlayer / mRunPlayVideo/ mIsViewModeChanged / to Pause");
				UtilVideo.currentPicturePath = path;
				UtilVideo.mVideoView.setVideoPath(UtilVideo.getVideoDataSource(path));
				UtilVideo.mVideoView.seekTo(UtilVideo.mPlayVideoPosition);
				UtilVideo.mVideoView.pause();
				UtilVideo.mVideoView.requestFocus();

				// reset
				Note.mIsViewModeChanged = false;
				Note.mPlayVideoPositionOfInstance = 0;
			}
			// from Play to Pause
			else if(UtilVideo.mVideoView.isPlaying())
			{
                System.out.println("VideoPlayer / mRunPlayVideo/ from Play to Pause");
				UtilVideo.mVideoView.pause();
				UtilVideo.mVideoView.requestFocus();
				if(!UtilVideo.hasMediaControlWidget)
					NoteUi.updateVideoPlayButtonState(pager, NoteUi.getFocus_notePos());
			}
			// keep pausing
			else if(!UtilVideo.mVideoView.isPlaying())
			{
				// do nothing
			}
		}		
	}
	
	// set media controller
	private void setMediaController(final ViewPager pager)
	{
		System.out.println("VideoPlayer / _setMediaController");
		//MediaController
		mMediaController = new MediaController(mAct);
		mMediaController.setVisibility(View.VISIBLE);
		mMediaController.setAnchorView(UtilVideo.mVideoView);
		UtilVideo.mVideoView.setMediaController(mMediaController);			
		
		mMediaController.setPrevNextListeners(
		    // for next
			new View.OnClickListener() 
			{
				public void onClick(View v) 
				{
					NoteUi.setFocus_notePos(NoteUi.getFocus_notePos()+1);
	            	if(NoteUi.getFocus_notePos() >= Note.mPagerAdapter.getCount())
						NoteUi.setFocus_notePos(NoteUi.getFocus_notePos()-1);
	            	else
						Note.changeToNext(pager);
				}
			}, 
			// for previous
			new View.OnClickListener() 
			{
				public void onClick(View v) 
				{
					NoteUi.setFocus_notePos(NoteUi.getFocus_notePos()-1);
            	
	            	if(NoteUi.getFocus_notePos() <0 )
						NoteUi.setFocus_notePos(NoteUi.getFocus_notePos()+1);
	            	else
						Note.changeToPrevious(pager);
				}
			});			
	}
	
	public static void cancelMediaController()
	{
		if(mMediaController != null)
		{
			mMediaController.cancelPendingInputEvents();
			mMediaController = null;
		}
	}
}