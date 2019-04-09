package com.cw.litenotetv.operation.audio;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;

import com.cw.litenotetv.R;
import com.cw.litenotetv.main.MainAct;
import com.cw.litenotetv.note.AudioUi_note;
import com.cw.litenotetv.tabs.TabsHost;
import com.cw.litenotetv.util.Util;
import com.cw.litenotetv.util.audio.UtilAudio;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

// AudioManager.OnAudioFocusChangeListener: added in API level 8
public class BackgroundAudioService extends MediaBrowserServiceCompat implements AudioManager.OnAudioFocusChangeListener  {

    public static final String COMMAND_EXAMPLE = "command_example";

    public static MediaPlayer mMediaPlayer;
    public static MediaSessionCompat mMediaSessionCompat;
    public static boolean mIsPrepared;
    public static boolean mIsCompleted;
    final public static int id = 99;

    BroadcastReceiver audioNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {

            if (android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction()))
            {
                if((BackgroundAudioService.mMediaPlayer != null) && BackgroundAudioService.mMediaPlayer.isPlaying() )
                {
                    System.out.println("BackgroundAudioService / audioNoisyReceiver / _onReceive / play -> pause");
                    // when phone jack is unplugged
                    if( mMediaPlayer != null  ) {
                        if(mMediaPlayer.isPlaying())
                            mMediaPlayer.pause();

                        setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);

                        initMediaSessionMetadata();
                        showPausedNotification();
                    }

                    // update panel status: pause
                    Audio_manager.setPlayerState(Audio_manager.PLAYER_AT_PAUSE);

                    //update audio panel button in Page view
                    if(Audio_manager.getAudioPlayMode() == Audio_manager.PAGE_PLAY_MODE)
                        UtilAudio.updateAudioPanel(TabsHost.audioUi_page.audioPanel_play_button,TabsHost.audioUi_page.audio_panel_title_textView);

                    //update audio play button in Note view
                    if( (AudioUi_note.mPager_audio_play_button != null) &&
                         AudioUi_note.mPager_audio_play_button.isShown()    )
                    {
                        AudioUi_note.mPager_audio_play_button.setImageResource(R.drawable.ic_media_play);
                    }
                }
            }

        }
    };

    private MediaSessionCompat.Callback mMediaSessionCallback = new MediaSessionCompat.Callback() {

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            System.out.println("BackgroundAudioService / mMediaSessionCallback / _onSkipToNext");

            setMediaPlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT);
            mMediaSessionCompat.setActive(true);
            TabsHost.audioUi_page.audioPanel_next_btn.performClick();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();

            System.out.println("BackgroundAudioService / mMediaSessionCallback / _onSkipToPrevious");
            setMediaPlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS);
            mMediaSessionCompat.setActive(true);
            TabsHost.audioUi_page.audioPanel_previous_btn.performClick();
        }

        @Override
        public void onPlay() {
            super.onPlay();
            System.out.println("BackgroundAudioService / mMediaSessionCallback / _onPlay");

            if( !successfullyRetrievedAudioFocus() ) {
                return;
            }

            mMediaSessionCompat.setActive(true);
            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);

            initMediaSessionMetadata();
            showPlayingNotification();

            if(mMediaPlayer != null)
                mMediaPlayer.start();

            // update panel status: play
            Audio_manager.setPlayerState(Audio_manager.PLAYER_AT_PLAY);
            TabsHost.audioUi_page.audioPanel_play_button.setImageResource(R.drawable.ic_media_pause);
        }

        @Override
        public void onPause() {
            super.onPause();
            System.out.println("BackgroundAudioService / mMediaSessionCallback / _onPause");

            if( mMediaPlayer != null  ) {
                if(mMediaPlayer.isPlaying())
                    mMediaPlayer.pause();

                setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);

                initMediaSessionMetadata();
                showPausedNotification();
            }

            // update panel status: pause
            Audio_manager.setPlayerState(Audio_manager.PLAYER_AT_PAUSE);
            TabsHost.audioUi_page.audioPanel_play_button.setImageResource(R.drawable.ic_media_play);
        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            super.onPlayFromUri(uri, extras);
            System.out.println("BackgroundAudioService / mMediaSessionCallback / _onPlayFromUri / uri = " + uri);

            mIsPrepared = false;

            if(mMediaPlayer == null)
                initMediaPlayer();

            try {
                mMediaPlayer.setDataSource(MainAct.mAct, uri);
            } catch (IOException e) {
                e.printStackTrace();
            }


            try {
                mMediaPlayer.prepare();
                mIsPrepared = false;
                mIsCompleted = false;

                setAudioPlayerListeners();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Set audio player listeners
         */
        void setAudioPlayerListeners()
        {
            System.out.println("BackgroundAudioService / _setAudioPlayerListeners");

            // on prepared
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {

                    System.out.println("BackgroundAudioService / _setAudioPlayerListeners / onPrepared");
                    if (Audio_manager.getAudioPlayMode() == Audio_manager.PAGE_PLAY_MODE)
                    {
                        mMediaPlayer.seekTo(0);
                    }
                    mIsPrepared = true;
                }
            });

            // on completed
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    System.out.println("BackgroundAudioService / _setAudioPlayerListeners / onCompleted");

                    if(mMediaPlayer != null) {
                        mMediaPlayer.release();
                        // disconnect media browser
                        if( MediaControllerCompat.getMediaController(MainAct.mAct).getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING ) {
                            MediaControllerCompat.getMediaController(MainAct.mAct).getTransportControls().stop();// .pause();
                        }
                    }

                    mMediaPlayer = null;
                    mIsCompleted = true;
                }
            });

            // on error
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    // more than one error when playing an index
                    System.out.println("BackgroundAudioService / _setAudioPlayerListeners / _onError / what = " + what + " , extra = " + extra);
                    return false;
                }
            });

            // on buffering update
            BackgroundAudioService.mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    System.out.println("BackgroundAudioService / _setAudioPlayerListeners / _onBufferingUpdate");
                    if (TabsHost.getCurrentPage().seekBarProgress != null)
                        TabsHost.getCurrentPage().seekBarProgress.setSecondaryProgress(percent);
                }
            });
        }

        @Override
        public void onCommand(String command, Bundle extras, ResultReceiver cb) {
            super.onCommand(command, extras, cb);
            if( COMMAND_EXAMPLE.equalsIgnoreCase(command) ) {
                //Custom command here
            }
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
        }

    };

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("BackgroundAudioService / _onCreate");
        initMediaPlayer();
        initMediaSession();
        initNoisyReceiver();
    }

    private void initNoisyReceiver() {
        //Handles headphones coming unplugged. cannot be done through a manifest receiver
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(audioNoisyReceiver, filter);
    }

    @Override
    public void onDestroy() {
        System.out.println("BackgroundAudioService / _onDestroy");

        super.onDestroy();
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.abandonAudioFocus(this);
        unregisterReceiver(audioNoisyReceiver);
        mMediaSessionCompat.release();
        NotificationManagerCompat.from(this).cancel(id);
    }

    private void initMediaPlayer() {
        System.out.println("BackgroundAudioService / _initMediaPlayer");
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setVolume(1.0f, 1.0f);
    }

    private void showPlayingNotification() {
        System.out.println("BackgroundAudioService / _showPlayingNotification");

        NotificationCompat.Builder builder = MediaStyleHelper.from(this, mMediaSessionCompat);
        if( builder == null ) {
            return;
        }

        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_previous,
                "Previous",
                MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)));
        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_pause,
                "Pause",
                MediaButtonReceiver.buildMediaButtonPendingIntent(this,PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_next,
                "Next",
                MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)));
        builder.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0).setMediaSession(mMediaSessionCompat.getSessionToken()));
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setShowWhen(false);
        NotificationManagerCompat.from(this).notify(id, builder.build());
    }

    private void showPausedNotification() {
        System.out.println("BackgroundAudioService / _showPausedNotification");
        NotificationCompat.Builder builder = MediaStyleHelper.from(this, mMediaSessionCompat);
        if( builder == null ) {
            return;
        }

        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_previous,
                "Previous",
                MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)));
        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_play,
                "Play",
                MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_next,
                "Next",
                MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)));
        builder.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0).setMediaSession(mMediaSessionCompat.getSessionToken()));
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setShowWhen(false);
        NotificationManagerCompat.from(this).notify(id, builder.build());
    }


    private void initMediaSession() {
        System.out.println("BackgroundAudioService / _initMediaSession");
        ComponentName mediaButtonReceiver = new ComponentName(getApplicationContext(), MediaButtonReceiver.class);
        mMediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "Tag", mediaButtonReceiver, null);

        mMediaSessionCompat.setCallback(mMediaSessionCallback);
        mMediaSessionCompat.setFlags( MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS );

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
        mMediaSessionCompat.setMediaButtonReceiver(pendingIntent);

        setSessionToken(mMediaSessionCompat.getSessionToken());
    }

    private void setMediaPlaybackState(int state) {
        System.out.println("BackgroundAudioService / _setMediaPlaybackState / state = " + state);
        PlaybackStateCompat.Builder playbackstateBuilder = new PlaybackStateCompat.Builder();
        if( state == PlaybackStateCompat.STATE_PLAYING ) {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                            PlaybackStateCompat.ACTION_PAUSE |
                                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        }
        else {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                            PlaybackStateCompat.ACTION_PLAY|
                                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        }

        playbackstateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);
        mMediaSessionCompat.setPlaybackState(playbackstateBuilder.build());
    }

    private void initMediaSessionMetadata() {
        System.out.println("BackgroundAudioService / _initMediaSessionMetadata");

        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();

        //Notification icon in card
//        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
//        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));

        String audioStr = Audio_manager.getAudioStringAt(Audio_manager.mAudioPos);
        String displayName = Util.getDisplayNameByUriString(audioStr, MainAct.mAct);
        String[] displayItems={"",""};

        if(displayName.contains(" / "))
            displayItems = displayName.split(" / ");
        else
            displayItems[0] = displayName;

        // prepare bit map
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        Bitmap bitmap = null;
        try
        {
            mmr.setDataSource(MainAct.mAct,Uri.parse(audioStr));

            byte[] artBytes =  mmr.getEmbeddedPicture();
            if(artBytes != null)
            {
                InputStream is = new ByteArrayInputStream(mmr.getEmbeddedPicture());
                bitmap = BitmapFactory.decodeStream(is);
            }
            mmr.release();
        }
        catch(Exception e)
        {
            Log.e("BackgroundAudioService", "setDataSource / illegal argument");
        }

        //lock screen icon for pre lollipop
//        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, BitmapFactory.decodeResource(MainAct.mAct.getResources(), R.drawable.ic_launcher));
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bitmap);
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, displayItems[0]);
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, displayItems[1]);
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, 1);
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, 1);

        mMediaSessionCompat.setMetadata(metadataBuilder.build());
    }

    private boolean successfullyRetrievedAudioFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int result = audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        return result == AudioManager.AUDIOFOCUS_GAIN;
    }


    //Not important for general audio service, required for class
    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        if(TextUtils.equals(clientPackageName, getPackageName())) {
            return new BrowserRoot(getString(R.string.app_name), null);
        }

        return null;
    }

    //Not important for general audio service, required for class
    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }

    boolean isManualPause;
    @Override
    public void onAudioFocusChange(int focusChange) {
        System.out.println("BackgroundAudioService / _onAudioFocusChange");

        switch( focusChange ) {
            case AudioManager.AUDIOFOCUS_LOSS: {
                System.out.println("BackgroundAudioService / _onAudioFocusChange / AudioManager.AUDIOFOCUS_LOSS");
                Audio_manager.stopAudioPlayer();
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                // when phone call is coming in
                System.out.println("BackgroundAudioService / _onAudioFocusChange / AudioManager.AUDIOFOCUS_LOSS_TRANSIENT");

                if(!mMediaPlayer.isPlaying())
                    isManualPause = true;
                else
                    isManualPause = false;

                mMediaPlayer.pause();
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                System.out.println("BackgroundAudioService / _onAudioFocusChange / AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                if( mMediaPlayer != null ) {
                    mMediaPlayer.setVolume(0.3f, 0.3f);
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_GAIN: {
                System.out.println("BackgroundAudioService / _onAudioFocusChange / AudioManager.AUDIOFOCUS_GAIN");
                // when phone call is off line
                if( mMediaPlayer != null ) {
                    if( (!mMediaPlayer.isPlaying()) && (!isManualPause) ) {
                        mMediaPlayer.start();
                    }
                    mMediaPlayer.setVolume(1.0f, 1.0f);
                }
                break;
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mMediaSessionCompat, intent);
        return super.onStartCommand(intent, flags, startId);
    }
}