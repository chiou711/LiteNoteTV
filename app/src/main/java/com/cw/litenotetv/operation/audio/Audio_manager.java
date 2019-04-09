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

package com.cw.litenotetv.operation.audio;

import android.support.v4.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.List;

import com.cw.litenotetv.db.DB_page;
import com.cw.litenotetv.main.MainAct;
import com.cw.litenotetv.tabs.TabsHost;
import com.cw.litenotetv.util.Util;

public class Audio_manager
{
	private static List<String> audioList;
	private static List<Integer> audioList_checked;

    private static int mAudioPlayMode;
    public final static int NOTE_PLAY_MODE = 0;
    public final static int PAGE_PLAY_MODE = 1;

    private static int mPlayerState;
    public static int PLAYER_AT_STOP = 0;
    public static int PLAYER_AT_PLAY = 1;
    public static int PLAYER_AT_PAUSE = 2;
    public static boolean isRunnableOn_note;
    public static boolean isRunnableOn_page;
    public static int mAudioPos; // index of current media to play


    // constructor
   Audio_manager()
   {
      audioList = new ArrayList<>();
      audioList_checked = new ArrayList<>();
   }

    /**
     * Setters and Getters
     *
     */
    // player state
    public static int getPlayerState() {
        return mPlayerState;
    }

    public static void setPlayerState(int playerState) {
        mPlayerState = playerState;
    }

    // Audio play mode
    public static int getAudioPlayMode() {
        return mAudioPlayMode;
    }

    public static void setAudioPlayMode(int audioPlayMode) {
        mAudioPlayMode = audioPlayMode;
    }

    /**
     * Stop audio
     */
    public static void stopAudioPlayer()
    {
        System.out.println("Audio_manager / _stopAudio");

        // stop media player
        if(BackgroundAudioService.mMediaPlayer != null) {
            if (BackgroundAudioService.mMediaPlayer.isPlaying()) {
                BackgroundAudioService.mMediaPlayer.pause();
                BackgroundAudioService.mMediaPlayer.stop();
            }
            BackgroundAudioService.mMediaPlayer.release();
            BackgroundAudioService.mMediaPlayer = null;
        }

        // stop handler and set flag to remove runnable
        if( AudioPlayer_page.mAudioHandler != null)
            Audio_manager.isRunnableOn_page = false;
        else if(AudioPlayer_note.mAudioHandler != null)
            Audio_manager.isRunnableOn_note = false;

        Audio_manager.setPlayerState(Audio_manager.PLAYER_AT_STOP);

        //hide notification
        NotificationManagerCompat.from(MainAct.mAct).cancel(BackgroundAudioService.id);
    }


   // Get audio files count
   static int getAudioFilesCount()
   {
	   int size = 0; 
	   if(audioList != null)
	   {
		  for(int i=0;i< audioList.size();i++)
		  {
			  if( !Util.isEmptyString(audioList.get(i)) && (getCheckedAudio(i) == 1) )
				  size++;
		  }
	   }
	   return size;
   }

   // Add audio to list
   private static void addAudio(String path)
   {
      audioList.add(path);
   }
   
   // Add audio with marking to list
   private static void addCheckedAudio(int i)
   {
	   audioList_checked.add(i);
   }   
   
   private static void setCheckedAudio(int index, int marking)
   {
	   audioList_checked.set(index,marking);
   }

   public static int getCheckedAudio(int index)
   {
	   return  audioList_checked.get(index);
   }
   
   // return String at position index
   public static String getAudioStringAt(int index)
   {
      if (index >= 0 && index < audioList.size())
         return audioList.get(index);
      else
         return null;
   }
   
	// Update audio info
	void updateAudioInfo()
	{
		DB_page db_page = new DB_page(MainAct.mAct, TabsHost.getCurrentPageTableId());
		
		db_page.open();
	 	// update media info 
	 	for(int i = 0; i< db_page.getNotesCount(false); i++)
	 	{
	 		String audioUri = db_page.getNoteAudioUri(i,false);
	 		
	 		// initialize
	 		addAudio(audioUri);
	 		addCheckedAudio(i);

	 		// set playable
	 		if( !Util.isEmptyString(audioUri)  &&
                (db_page.getNoteMarking(i,false) == 1) )
		 		setCheckedAudio(i,1);
	 		else
	 			setCheckedAudio(i,0);
	 	}
	 	db_page.close();
	}

	public static int getPlayingPage_notesCount()
    {
        int playingPageTableId = TabsHost.mTabsPagerAdapter.getItem(TabsHost.audioPlayTabPos).page_tableId;
        DB_page db_page = new DB_page(MainAct.mAct, playingPageTableId);
        return db_page.getNotesCount(true);
    }
	
}