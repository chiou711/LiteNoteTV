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

package com.cw.litenotetv.main;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.cw.litenotetv.R;
import com.cw.litenotetv.config.About;
import com.cw.litenotetv.config.Config;
import com.cw.litenotetv.db.DB_folder;
import com.cw.litenotetv.db.DB_page;
import com.cw.litenotetv.drawer.Drawer;
import com.cw.litenotetv.folder.Folder;
import com.cw.litenotetv.folder.FolderUi;
import com.cw.litenotetv.note_add.Add_note_option;
import com.cw.litenotetv.operation.audio.Audio_manager;
import com.cw.litenotetv.operation.audio.AudioPlayer_page;
import com.cw.litenotetv.operation.audio.BackgroundAudioService;
import com.cw.litenotetv.operation.delete.DeleteFolders;
import com.cw.litenotetv.operation.delete.DeletePages;
import com.cw.litenotetv.operation.import_export.Import_fileView;
import com.cw.litenotetv.operation.import_export.Import_webAct;
import com.cw.litenotetv.page.Checked_notes_option;
import com.cw.litenotetv.page.PageUi;
import com.cw.litenotetv.page.Page_recycler;
import com.cw.litenotetv.tabs.AudioUi_page;
import com.cw.litenotetv.tabs.TabsHost;
import com.cw.litenotetv.util.DeleteFileAlarmReceiver;
import com.cw.litenotetv.operation.import_export.Export_toSDCardFragment;
import com.cw.litenotetv.operation.import_export.Import_filesList;
import com.cw.litenotetv.db.DB_drawer;
import com.cw.litenotetv.util.Dialog_EULA;
import com.cw.litenotetv.util.audio.UtilAudio;
import com.cw.litenotetv.operation.gallery.GalleryGridAct;
import com.cw.litenotetv.operation.slideshow.SlideshowInfo;
import com.cw.litenotetv.operation.slideshow.SlideshowPlayer;
import com.cw.litenotetv.util.image.UtilImage;
import com.cw.litenotetv.define.Define;
import com.cw.litenotetv.operation.mail.MailNotes;
import com.cw.litenotetv.util.OnBackPressedListener;
import com.cw.litenotetv.operation.mail.MailPagesFragment;
import com.cw.litenotetv.util.Util;
import com.cw.litenotetv.util.preferences.Pref;
import com.mobeta.android.dslv.DragSortListView;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;
import android.os.StrictMode;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O;

public class MainAct extends AppCompatActivity implements OnBackStackChangedListener
{
    public static CharSequence mFolderTitle;
    public static CharSequence mAppTitle;
    public Context mContext;
    public Config mConfigFragment;
    public About mAboutFragment;
    public static Menu mMenu;
    public static List<String> mFolderTitles;
    public static AppCompatActivity mAct;//TODO static issue
    public FragmentManager mFragmentManager;
    public static FragmentManager.OnBackStackChangedListener mOnBackStackChangedListener;
    public static int mLastOkTabId = 1;
    public static SharedPreferences mPref_show_note_attribute;
    OnBackPressedListener onBackPressedListener;
    public Drawer drawer;
    public static Folder mFolder;
    public static MainUi mMainUi;
    public static Toolbar mToolbar;

    public static MediaBrowserCompat mMediaBrowserCompat;
    public static MediaControllerCompat mMediaControllerCompat;
    public static int mCurrentState;
    public final static int STATE_PAUSED = 0;
    public final static int STATE_PLAYING = 1;
    public boolean bEULA_accepted;

	// Main Act onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        ///
//    	 StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//    	   .detectDiskReads()
//    	   .detectDiskWrites()
//    	   .detectNetwork() 
//    	   .penaltyLog()
//    	   .build());
//
//    	    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
////    	   .detectLeakedSqlLiteObjects() //??? unmark this line will cause strict mode error
//    	   .penaltyLog() 
//    	   .penaltyDeath()
//    	   .build());     	
        ///

        super.onCreate(savedInstanceState);

        /**
         * Set APP build mode
         * Note:
         *  1. for AdMob: it works after Google Play store release
         *  2. for assets mode: need to enable build.gradle assets.srcDirs = ['preferred/assets/']
         */
        /** 1 debug, initial */
//        Define.setAppBuildMode(Define.DEBUG_DEFAULT_BY_INITIAL);

        /** 2 debug, assets */
        Define.setAppBuildMode(Define.DEBUG_DEFAULT_BY_ASSETS);

        /** 3 debug, download */
//        Define.setAppBuildMode(Define.DEBUG_DEFAULT_BY_DOWNLOAD);

        /** 4 release, initial */
//        Define.setAppBuildMode(Define.RELEASE_DEFAULT_BY_INITIAL);

        /** 5 release, assets */
//        Define.setAppBuildMode(Define.RELEASE_DEFAULT_BY_ASSETS);

        /** 6 release, download */
//        Define.setAppBuildMode(Define.RELEASE_DEFAULT_BY_DOWNLOAD);

        // Release mode: no debug message
        if (Define.CODE_MODE == Define.RELEASE_MODE) {
            OutputStream nullDev = new OutputStream() {
                public void close() {}
                public void flush() {}
                public void write(byte[] b) {}
                public void write(byte[] b, int off, int len) {}
                public void write(int b) {}
            };
            System.setOut(new PrintStream(nullDev));
        }

        System.out.println("================start application ==================");
        System.out.println("MainAct / _onCreate");

        mAct = this;
        mAppTitle = getTitle();
        mMainUi = new MainUi();

        // File provider implementation is needed after Android version 24
        // if not, will encounter android.os.FileUriExposedException
        // cf. https://stackoverflow.com/questions/38200282/android-os-fileuriexposedexception-file-storage-emulated-0-test-txt-exposed

        // add the following to disable this requirement
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                // method 1
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);

                // method 2
//                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//                StrictMode.setVmPolicy(builder.build());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Show Api version
        if (Define.CODE_MODE == Define.DEBUG_MODE)
            Toast.makeText(this, mAppTitle + " " + "API_" + Build.VERSION.SDK_INT, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, mAppTitle, Toast.LENGTH_SHORT).show();

        //Log.d below can be disabled by applying proguard
        //1. enable proguard-android-optimize.txt in project.properties
        //2. be sure to use newest version to avoid build error
        //3. add the following in proguard-project.txt
        /*-assumenosideeffects class android.util.Log {
        public static boolean isLoggable(java.lang.String, int);
        public static int v(...);
        public static int i(...);
        public static int w(...);
        public static int d(...);
        public static int e(...);
        }
        */
        UtilImage.getDefaultScaleInPercent(MainAct.this);

        // EULA
        Dialog_EULA dialog_EULA = new Dialog_EULA(this);
        bEULA_accepted = dialog_EULA.isEulaAlreadyAccepted();

        // Show dialog of EULA
        if (!bEULA_accepted)
        {
            // Ok button listener
            dialog_EULA.clickListener_Ok = (DialogInterface dialog, int i) -> {

                dialog_EULA.applyPreference();

                // dialog: with default content
                if( (Define.DEFAULT_CONTENT == Define.BY_ASSETS) ||
                    (Define.DEFAULT_CONTENT == Define.BY_DOWNLOAD) )
                {
                    // Click Yes
                    DialogInterface.OnClickListener click_Yes = (DialogInterface dlg, int j) -> {
                        // Close dialog
                        dialog.dismiss();

                        // check build version for permission request (starts from API 23)
                        if(Build.VERSION.SDK_INT >= 23)
                            checkPermission(savedInstanceState, Util.PERMISSIONS_REQUEST_STORAGE_WITH_DEFAULT_CONTENT_YES);
                        else {
                            if (Define.DEFAULT_CONTENT == Define.BY_DOWNLOAD) {
                                createDefaultContent_byDownload();
                            }
                            else {
                                Pref.setPref_will_create_default_content(this, true);
                                recreate();
                            }
                        }
                    };

                    // Click No
                    DialogInterface.OnClickListener click_No = (DialogInterface dlg, int j) -> {
                        // Close dialog
                        dialog.dismiss();

                        // check build version for permission request
                        if(Build.VERSION.SDK_INT >= 23)
                            checkPermission(savedInstanceState, Util.PERMISSIONS_REQUEST_STORAGE_WITH_DEFAULT_CONTENT_NO);
                        else {
                            Pref.setPref_will_create_default_content(this, false);
                            recreate();
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(mAct)
                            .setTitle(R.string.sample_notes_title)
                            .setMessage(R.string.sample_notes_message)
                            .setCancelable(false)
                            .setPositiveButton(R.string.confirm_dialog_button_yes, click_Yes)
                            .setNegativeButton(R.string.confirm_dialog_button_no, click_No);
                    builder.create().show();
                }
                else if((Define.DEFAULT_CONTENT == Define.BY_INITIAL_TABLES) && (Define.INITIAL_FOLDERS_COUNT > 0))
                {
                    if(Build.VERSION.SDK_INT >= 23)
                        checkPermission(savedInstanceState, Util.PERMISSIONS_REQUEST_STORAGE_WITH_DEFAULT_CONTENT_YES);
                    else
                    {
                        Pref.setPref_will_create_default_content(this, true);
                        recreate();
                    }
                    // Close dialog
                    dialog.dismiss();
                }
            };

            // No button listener
            dialog_EULA.clickListener_No = (DialogInterface dialog, int which) -> {
                    // Close the activity as they have declined
                    // the EULA
                    dialog.dismiss();
                    mAct.finish();
            };

            dialog_EULA.show();
        }
        else
            doCreate(savedInstanceState);

    }

    // check permission dialog
    void checkPermission(Bundle savedInstanceState,int permissions_request)
    {
        // check permission first time, request all necessary permissions
        if(Build.VERSION.SDK_INT >= M)//API23
        {
            int permissionWriteExtStorage = ActivityCompat.checkSelfPermission(mAct, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if(permissionWriteExtStorage != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(mAct,
                                                  new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                               Manifest.permission.READ_EXTERNAL_STORAGE },
                                                  permissions_request);
            }
            else
                doCreate(savedInstanceState);
        }
        else
            doCreate(savedInstanceState);
    }

    // Do major create operation
    void doCreate(Bundle savedInstanceState)
    {
        System.out.println("MainAct / _doCreate");

        // Will create default contents: by assets or by initial tables
        if(Pref.getPref_will_create_default_content(this)) {
            if (Define.DEFAULT_CONTENT == Define.BY_ASSETS)
                createDefaultContent_byAssets();
            else if ((Define.DEFAULT_CONTENT == Define.BY_INITIAL_TABLES) && (Define.INITIAL_FOLDERS_COUNT > 0))
                createDefaultContent_byInitialTables();
        }

        mFolderTitles = new ArrayList<>();

//		Context context = getApplicationContext();

        //Add note with the link which got from other App
        String intentLink = mMainUi.addNote_IntentLink(getIntent(), mAct);
        if (!Util.isEmptyString(intentLink)) {
            finish(); // LiteNote not running at first, keep closing
            return;
        } else {
            // check DB
            final boolean ENABLE_DB_CHECK = false;//true;//false
            if (ENABLE_DB_CHECK) {
                // list all folder tables
                FolderUi.listAllFolderTables(mAct);

                // recover focus
                DB_folder.setFocusFolder_tableId(Pref.getPref_focusView_folder_tableId(this));
                DB_page.setFocusPage_tableId(Pref.getPref_focusView_page_tableId(this));
            }//if(ENABLE_DB_CHECK)

            // get focus folder table Id, default folder table Id: 1
            DB_drawer dB_drawer = new DB_drawer(this);
            dB_drawer.open();
            if (savedInstanceState == null) {
                for (int i = 0; i < dB_drawer.getFoldersCount(false); i++) {
                    if (dB_drawer.getFolderTableId(i, false) == Pref.getPref_focusView_folder_tableId(this)) {
                        FolderUi.setFocus_folderPos(i);
                        System.out.println("MainAct / _mainAction / FolderUi.getFocus_folderPos() = " + FolderUi.getFocus_folderPos());
                    }
                }
                Audio_manager.setPlayerState(Audio_manager.PLAYER_AT_STOP);
            }
            dB_drawer.close();

            // enable ActionBar app icon to behave as action to toggle nav drawer
//	        getActionBar().setDisplayHomeAsUpEnabled(true);
//	        getActionBar().setHomeButtonEnabled(true);
//			getActionBar().setBackgroundDrawable(new ColorDrawable(ColorSet.getBarColor(mAct)));

            mContext = getBaseContext();

            // add on back stack changed listener
            mFragmentManager = getSupportFragmentManager();
            mOnBackStackChangedListener = this;
            mFragmentManager.addOnBackStackChangedListener(mOnBackStackChangedListener);
        }

        isAddedOnNewIntent = false;

        // Register Bluetooth device receiver
        if (Build.VERSION.SDK_INT < 21) {
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
            this.registerReceiver(bluetooth_device_receiver, filter);
        } else // if(Build.VERSION.SDK_INT >= 21)
        {
            // Media session: to receive media button event of bluetooth device
            // new media browser instance and create BackgroundAudioService instance: support notification
            if (mMediaBrowserCompat == null) {
                mMediaBrowserCompat = new MediaBrowserCompat(mAct,
                        new ComponentName(mAct, BackgroundAudioService.class),
                        mMediaBrowserCompatConnectionCallback,
                        mAct.getIntent().getExtras());

                if (!mMediaBrowserCompat.isConnected())
                    mMediaBrowserCompat.connect();//cf: https://stackoverflow.com/questions/43169875/mediabrowser-subscribe-doesnt-work-after-i-get-back-to-activity-1-from-activity

                mCurrentState = STATE_PAUSED;
            }
        }

        // init audio parameters
        MainAct.mPlaying_folderPos = -1;
        Audio_manager.setPlayerState(Audio_manager.PLAYER_AT_STOP);
        TabsHost.audioPlayTabPos = -1;
    }


    /**
     *  Create default content
     */
    void createDefaultContent_byAssets()
    {
        System.out.println("MainAct / _createDefaultContent_byAssets");

        String fileName;
        File xmlFile = null;
        // will create database first
        DB_drawer dB_drawer = new DB_drawer(this);

        // create asset files
        // default image
        String imageFileName = "local.jpg";
        Util.createAssetsFile(this, imageFileName);

        // default video
        String videoFileName = "local.mp4";
        Util.createAssetsFile(this, videoFileName);

        // default audio
        String audioFileName = "local.mp3";
        Util.createAssetsFile(this, audioFileName);

        fileName = "default_content_by_assets.xml";

        // By assets file
        xmlFile = Util.createAssetsFile(this,fileName);

        // import content
        if(xmlFile.exists()) {
            //todo Could halt on this ?
            Import_fileView.importDefaultContentByXml(this, xmlFile);

            //set default position to 0
            int folderTableId = dB_drawer.getFolderTableId(0, true);
            Pref.setPref_focusView_folder_tableId(this, folderTableId);
            DB_folder.setFocusFolder_tableId(folderTableId);
        }


        // already has preferred tables
        Pref.setPref_will_create_default_content(this, false);

        //workaround: fix blank page after adding default page (due to no TabsHost onPause/onResume cycles, but why?)
        recreate();
    }

    /**
     * Create initial tables
     */
    void createDefaultContent_byInitialTables()
    {
        DB_drawer dB_drawer = new DB_drawer(this);

        for(int i = 1; i<= Define.INITIAL_FOLDERS_COUNT; i++)
        {
            // Create initial folder tables
            System.out.println("MainAct / _createInitialTables / folder id = " + i);
            String folderTitle = getResources().getString(R.string.default_folder_name).concat(String.valueOf(i));
            dB_drawer.insertFolder(i, folderTitle, true); // Note: must set false for DB creation stage
            dB_drawer.insertFolderTable( i, true);

            // Create initial page tables
            if(Define.INITIAL_PAGES_COUNT > 0)
            {
                // page tables
                for(int j = 1; j<= Define.INITIAL_PAGES_COUNT; j++)
                {
                    System.out.println("MainAct / _createInitialTables / page id = " + j);
                    DB_folder db_folder = new DB_folder(this,i);
                    db_folder.insertPageTable(db_folder, i, j, true);

                    String DB_FOLDER_TABLE_PREFIX = "Folder";
                    String folder_table = DB_FOLDER_TABLE_PREFIX.concat(String.valueOf(i));
                    db_folder.open();
                    db_folder.insertPage(db_folder.mSqlDb ,
                            folder_table,
                            Define.getTabTitle(this,j),
                            1,
                            Define.STYLE_DEFAULT);//Define.STYLE_PREFER
                    db_folder.close();
                    //db_folder.insertPage(sqlDb,folder_table,"N2",2,1);
                }
            }
        }

        recreate();
        Pref.setPref_will_create_default_content(this,false);
    }

    Intent intentReceive;
    //The BroadcastReceiver that listens for bluetooth broadcasts
    private BroadcastReceiver bluetooth_device_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("MainAct / _BroadcastReceiver / onReceive");
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Device is now connected
                Toast.makeText(getApplicationContext(), "ACTION_ACL_CONNECTED: device is " + device, Toast.LENGTH_LONG).show();
            }

            intentReceive = intent;
            KeyEvent keyEvent = (KeyEvent) intentReceive.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if(keyEvent != null)
                onKeyDown( keyEvent.getKeyCode(),keyEvent);
        }
    };


    // key event: 1 from bluetooth device 2 when notification bar dose not shown
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        System.out.println("MainAct / _onKeyDown / keyCode = " + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS: //88
                if(TabsHost.audioUi_page != null)
                    TabsHost.audioUi_page.audioPanel_previous_btn.performClick();
                return true;

            case KeyEvent.KEYCODE_MEDIA_NEXT: //87
                if(TabsHost.audioUi_page != null)
                    TabsHost.audioUi_page.audioPanel_next_btn.performClick();

                return true;

            case KeyEvent.KEYCODE_MEDIA_PLAY: //126
                if(TabsHost.audioUi_page != null)
                    TabsHost.audioUi_page.audioPanel_play_button.performClick();
                else
                    playFirstAudio();
                return true;

            case KeyEvent.KEYCODE_MEDIA_PAUSE: //127
                if(TabsHost.audioUi_page != null)
                    TabsHost.audioUi_page.audioPanel_play_button.performClick();
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


    // callback of granted permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        System.out.println("MainAct / _onRequestPermissionsResult / grantResults.length =" + grantResults.length);

        if ( (grantResults.length > 0) &&
             ( (grantResults[0] == PackageManager.PERMISSION_GRANTED) &&
               (grantResults[1] == PackageManager.PERMISSION_GRANTED)   ) )
        {
            switch (requestCode)
            {
                case Util.PERMISSIONS_REQUEST_STORAGE_WITH_DEFAULT_CONTENT_YES:
                    if(Define.DEFAULT_CONTENT == Define.BY_DOWNLOAD)
                        createDefaultContent_byDownload();
                    else {
                        Pref.setPref_will_create_default_content(this, true);
                        recreate();
                    }
                break;

                case Util.PERMISSIONS_REQUEST_STORAGE_WITH_DEFAULT_CONTENT_NO:
                    Pref.setPref_will_create_default_content(this, false);
                    recreate();
                break;
            }
        }
        else
        {
            Pref.setPref_will_create_default_content(this, false);
            recreate();
        }

        //normally, will go to _resume
    }

    //  Download XML file from Google drive
    void createDefaultContent_byDownload()
    {

        /**
         * LiteNote_default_content.xml
         * Unit: folder
         */
        // LiteNote_default_content.xml
        String srcUrl = "https://drive.google.com/uc?authuser=0&id=1qAfMUJ9DMsciVkb7hEQAwLrmcyfN95sF&export=download";

        Async_default_byDownload async = new Async_default_byDownload(mAct,srcUrl);
        async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"Downloading file ...");


        ///
        // download txt file from Web site
//                    Thread thread = new Thread(new Runnable()
//                    {
//                        @Override
//                        public void run()
//                        {
//                            try {
//                                //cf https://www.sample-videos.com/download-sample-text-file.php
//                                URL url = new URL("https://www.sample-videos.com/text/Sample-text-file-10kb.txt");
//
//                                //create https connection
//                                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
//                                urlConnection.setRequestMethod("GET");
//                                urlConnection.setDoOutput(true);
//
//                                //connect
//                                urlConnection.connect();
//
//                                //output path
//                                String dirString = Environment.getExternalStorageDirectory().toString() +
//                                        "/" +
//                                        Util.getStorageDirName(MainAct.mAct);
//
//                                File storageRoot = new File(dirString);
//                                File file = new File(storageRoot, "default_content_by_download.xml");
//
//                                FileOutputStream fileOutput = null;
//                                fileOutput = new FileOutputStream(file);
//
//                                // input path
//                                InputStream inputStream = null;
//                                inputStream = urlConnection.getInputStream();
//
//                                int totalSize = urlConnection.getContentLength();
//                                int downloadedSize = 0;
//
//                                //create buffer
//                                byte[] buffer = new byte[1024];
//                                int bufferLength = 0;
//                                bufferLength = inputStream.read(buffer);
//
//                                while ( bufferLength  > 0 )
//                                {
//                                    // write
//                                    try {
//                                        fileOutput.write(buffer, 0, bufferLength);
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
//
//                                    downloadedSize += bufferLength;
//
//                                    // progress
//                                    int progress=(int)(downloadedSize*100/totalSize);
//
//                                    try {
//                                        bufferLength = inputStream.read(buffer);
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//
//                                //close
//                                fileOutput.close();
//
//                                while(!file.exists() || (file.length() == 0) )
//                                {
//                                    System.out.println("MainAct / _onRequestPermissionsResult / downloading ! Waiting...");
//                                }
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//                    thread.start();
        ///
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
    }

    /**
     * initialize action bar
     */
//    void initActionBar(Menu mMenu,Drawer drawer)
//    {
//        mMenu.setGroupVisible(R.id.group_notes, true);
//        getActionBar().setDisplayShowHomeEnabled(true);
//        getActionBar().setDisplayHomeAsUpEnabled(true);
//        drawer.drawerToggle.setDrawerIndicatorEnabled(true);
//    }

    void initActionBar()
    {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            mToolbar.setNavigationIcon(R.drawable.ic_drawer);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Drawer.drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }
    }

    // set action bar for fragment
    void initActionBar_home()
    {
        drawer.drawerToggle.setDrawerIndicatorEnabled(false);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
            getSupportActionBar().setDisplayShowHomeEnabled(false);//false: no launcher icon
        }

        mToolbar.setNavigationIcon(R.drawable.ic_menu_back);
        mToolbar.getChildAt(1).setContentDescription(getResources().getString(R.string.btn_back));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("MainAct / _initActionBar_home / click to popBackStack");

                // check if DB is empty
                DB_drawer db_drawer = new DB_drawer(mAct);
                int focusFolder_tableId = Pref.getPref_focusView_folder_tableId(mAct);
                DB_folder db_folder = new DB_folder(mAct,focusFolder_tableId);
                if((db_drawer.getFoldersCount(true) == 0) ||
                   (db_folder.getPagesCount(true) == 0)      )
                {
                    finish();
                    Intent intent  = new Intent(mAct,MainAct.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                else
                    getSupportFragmentManager().popBackStack();
            }
        });

    }


    /*********************************************************************************
     *
     *                                      Life cycle
     *
     *********************************************************************************/

    boolean isAddedOnNewIntent;
    // if one LiteNote Intent is already running, call it again in YouTube or Browser will run into this
    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        if(!isAddedOnNewIntent)
        {
            String intentTitle = mMainUi.addNote_IntentLink(intent, mAct);
//            if (!Util.isEmptyString(intentTitle) && intentTitle.startsWith("http")) {
//                Page.itemAdapter.notifyDataSetChanged();
//            }

            if (!Util.isEmptyString(intentTitle))
                TabsHost.reloadCurrentPage();

            if(Build.VERSION.SDK_INT >= O)//API26
                isAddedOnNewIntent = true; // fix 2 times _onNewIntent on API26
        }
    }

    // for Rotate screen
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
       super.onSaveInstanceState(outState);
        System.out.println("MainAct / onSaveInstanceState / getFocus_folderPos() = " + FolderUi.getFocus_folderPos());
        outState.putInt("NowFolderPosition", FolderUi.getFocus_folderPos());
        outState.putInt("Playing_pageId", mPlaying_pagePos);
        outState.putInt("Playing_folderPos", mPlaying_folderPos);
        outState.putInt("SeekBarProgress", AudioUi_page.mProgress);
        outState.putInt("AudioInfo_state", Audio_manager.getPlayerState());
        if(FolderUi.mHandler != null)
            FolderUi.mHandler.removeCallbacks(FolderUi.mTabsHostRun);
        FolderUi.mHandler = null;
    }

    // for After Rotate
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        System.out.println("MainAct / _onRestoreInstanceState ");
        if(savedInstanceState != null)
        {
            FolderUi.setFocus_folderPos(savedInstanceState.getInt("NowFolderPosition"));
            mPlaying_pagePos = savedInstanceState.getInt("Playing_pageId");
            mPlaying_folderPos = savedInstanceState.getInt("Playing_folderPos");
            Audio_manager.setPlayerState(savedInstanceState.getInt("AudioInfo_state"));
            AudioUi_page.mProgress = savedInstanceState.getInt("SeekBarProgress");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        bluetooth_device_receiver.abortBroadcast();//todo better place?
        System.out.println("MainAct / _onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
    	System.out.println("MainAct / _onResume");

    	mAct = this;

        configLayoutView(); //createAssetsFile inside

        // Sync the toggle state after onRestoreInstanceState has occurred.
        if(bEULA_accepted)
            drawer.drawerToggle.syncState();
    }


    @Override
    protected void onResumeFragments() {
        System.out.println("MainAct / _onResumeFragments ");
        super.onResumeFragments();

//		// fix: home button failed after power off/on in Config fragment
        if(bEULA_accepted) {
            mFragmentManager.popBackStack();

            if(!mAct.isDestroyed()) {
	            System.out.println("MainAct / _onResumeFragments / mAct is not Destroyed()");
	            openFolder();
            }
            else
	            System.out.println("MainAct / _onResumeFragments / mAct is Destroyed()");
        }
    }

    // open folder
    public static void openFolder()
    {
        System.out.println("MainAct / _openFolder");
        DB_drawer dB_drawer = new DB_drawer(mAct);
        if (dB_drawer.getFoldersCount(true) > 0) {
            System.out.println("MainAct / _openFolder / getFocus_folderPos() = " + FolderUi.getFocus_folderPos());

            int focus_folderPos = FolderUi.getFocus_folderPos();
            FolderUi.selectFolder(mAct, focus_folderPos);

            if (mAct.getSupportActionBar() != null)
                mAct.getSupportActionBar().setTitle(mFolderTitle);
        }
    }


    @Override
    protected void onDestroy()
    {
        System.out.println("MainAct / onDestroy");

        if(bluetooth_device_receiver != null)
        {
            try
            {
                unregisterReceiver(bluetooth_device_receiver);
            }
            catch (Exception e)
            {
            }
            bluetooth_device_receiver = null;
        }

        // stop audio player
        if(BackgroundAudioService.mMediaPlayer != null)
            Audio_manager.stopAudioPlayer();

        // disconnect MediaBrowserCompat
        if( (mMediaBrowserCompat != null) && mMediaBrowserCompat.isConnected())
            mMediaBrowserCompat.disconnect();

        //hide notification
        NotificationManagerCompat.from(MainAct.mAct).cancel(BackgroundAudioService.id);

        mMediaBrowserCompat = null;

        super.onDestroy();
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        System.out.println("MainAct / _onConfigurationChanged");

        configLayoutView();

        // Pass any configuration change to the drawer toggles
        drawer.drawerToggle.onConfigurationChanged(newConfig);

		drawer.drawerToggle.syncState();

        FolderUi.startTabsHostRun();
    }


    /**
     *  on Back button pressed
     */
    @Override
    public void onBackPressed()
    {
        System.out.println("MainAct / _onBackPressed");
        doBackKeyEvent();
    }

    void doBackKeyEvent()
    {
        if (onBackPressedListener != null)
        {
            DB_drawer dbDrawer = new DB_drawer(this);
            int foldersCnt = dbDrawer.getFoldersCount(true);

            if(foldersCnt == 0)
            {
                finish();
                Intent intent  = new Intent(this,MainAct.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            else {
                onBackPressedListener.doBack();
            }
        }
        else
        {
            if((drawer != null) && drawer.isDrawerOpen())
                drawer.closeDrawer();
            else
                super.onBackPressed();
        }

    }


    @Override
    public void onBackStackChanged() {
        int backStackEntryCount = mFragmentManager.getBackStackEntryCount();
        System.out.println("MainAct / _onBackStackChanged / backStackEntryCount = " + backStackEntryCount);

        if(backStackEntryCount == 1) // fragment
        {
            System.out.println("MainAct / _onBackStackChanged / fragment");
            initActionBar_home();
        }
        else if(backStackEntryCount == 0) // init
        {
            System.out.println("MainAct / _onBackStackChanged / init");
            onBackPressedListener = null;

            if(mFolder.adapter!=null)
                mFolder.adapter.notifyDataSetChanged();

            configLayoutView();

            drawer.drawerToggle.syncState(); // make sure toggle icon state is correct
        }
    }

    public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
    }

    /**
     * on Activity Result
     */
    AlertDialog.Builder builder;
    AlertDialog alertDlg;
    Handler handler;
    int count;
    String countStr;
    String nextLinkTitle;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        System.out.println("MainAct / _onActivityResult ");
        String stringFileName[] = null;

        // mail
        if((requestCode== MailNotes.EMAIL) || (requestCode== MailPagesFragment.EMAIL_PAGES)) {
            if (requestCode == MailNotes.EMAIL)
                stringFileName = MailNotes.mAttachmentFileName;
            else if (requestCode == MailPagesFragment.EMAIL_PAGES)
                stringFileName = MailPagesFragment.mAttachmentFileName;

            Toast.makeText(mAct, R.string.mail_exit, Toast.LENGTH_SHORT).show();

            // note: result code is always 0 (cancel), so it is not used
            new DeleteFileAlarmReceiver(mAct,
                    System.currentTimeMillis() + 1000 * 60 * 5, // formal: 300 seconds
//					System.currentTimeMillis() + 1000 * 10, // test: 10 seconds
                    stringFileName);
        }

        // YouTube
        if(requestCode == Util.YOUTUBE_LINK_INTENT)
        {
            // preference of delay
            SharedPreferences pref_delay = getSharedPreferences("youtube_launch_delay", 0);
            count = Integer.valueOf(pref_delay.getString("KEY_YOUTUBE_LAUNCH_DELAY","10"));

            builder = new AlertDialog.Builder(this);

            do
            {
                TabsHost.getCurrentPage().mCurrPlayPosition++;
                if(Page_recycler.mCurrPlayPosition >= TabsHost.getCurrentPage().getNotesCountInPage(mAct))
                    TabsHost.getCurrentPage().mCurrPlayPosition = 0; //back to first index

                nextLinkTitle = mMainUi.getYouTubeLink(this,TabsHost.getCurrentPage().mCurrPlayPosition);
            }
            while (!Util.isYouTubeLink(nextLinkTitle));

            countStr = getResources().getString(R.string.message_continue_or_stop_YouTube_message);
            countStr = countStr.replaceFirst("[0-9]",String.valueOf(count));

            builder.setTitle(R.string.message_continue_or_stop_YouTube_title)
                    .setMessage(nextLinkTitle +"\n\n" + countStr)
                    .setNegativeButton(R.string.confirm_dialog_button_no, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog1, int which1)
                        {
                            alertDlg.dismiss();
                            mMainUi.cancelYouTubeHandler(handler,runCountDown);
                        }
                    })
                    .setPositiveButton(R.string.confirm_dialog_button_yes, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog1, int which1) {
                            alertDlg.dismiss();
                            mMainUi.cancelYouTubeHandler(handler,runCountDown);
                            mMainUi.launchNextYouTubeIntent(mAct,handler,runCountDown);
                        }
                    });

            alertDlg = builder.create();

            // set listener for selection
            alertDlg.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dlgInterface) {
                    handler = new Handler();
                    handler.postDelayed(runCountDown,1000);
                }
            });
            alertDlg.show();
        }

        // make sure main activity is still executing
        if(requestCode == Util.YOUTUBE_ADD_NEW_LINK_INTENT)
        {
            System.out.println("MainAct / _onActivityResult /YOUTUBE_ADD_NEW_LINK_INTENT");

            if(Build.VERSION.SDK_INT >= O)//API26
                isAddedOnNewIntent = false;
        }
    }

    /**
     * runnable for counting down
     */
    Runnable runCountDown = new Runnable() {
        public void run() {
            // show count down
            TextView messageView = (TextView) alertDlg.findViewById(android.R.id.message);
            count--;
            countStr = getResources().getString(R.string.message_continue_or_stop_YouTube_message);
            countStr = countStr.replaceFirst("[0-9]",String.valueOf(count));
            messageView.setText(nextLinkTitle + "\n\n" +countStr);

            if(count>0)
                handler.postDelayed(runCountDown,1000);
            else
            {
                // launch next intent
                alertDlg.dismiss();
                mMainUi.cancelYouTubeHandler(handler,runCountDown);
                System.out.println("MainAct / _runCountDown /alertDlg  dismiss ");
                mMainUi.launchNextYouTubeIntent(mAct,handler,runCountDown);
            }
        }
    };



    /***********************************************************************************
     *
     *                                          Menu
     *
     ***********************************************************************************/

    /****************************************************
     *  On Prepare Option menu :
     *  Called whenever we call invalidateOptionsMenu()
     ****************************************************/
    @Override
    public boolean onPrepareOptionsMenu(android.view.Menu menu) {
        System.out.println("MainAct / _onPrepareOptionsMenu");

        if((drawer == null) || (drawer.drawerLayout == null) || (!bEULA_accepted))
            return false;

        DB_drawer db_drawer = new DB_drawer(this);
        int foldersCnt = db_drawer.getFoldersCount(true);

        /**
         * Folder group
         */
        // If the navigation drawer is open, hide action items related to the content view
        if(drawer.isDrawerOpen())
        {
            // for landscape: the layout file contains folder menu
            if(Util.isLandscapeOrientation(mAct)) {
                mMenu.setGroupVisible(R.id.group_folders, true);
                // set icon for folder draggable: landscape
                if(MainAct.mPref_show_note_attribute != null)
                {
                    if (MainAct.mPref_show_note_attribute.getString("KEY_ENABLE_FOLDER_DRAGGABLE", "no")
                            .equalsIgnoreCase("yes"))
                        mMenu.findItem(R.id.ENABLE_FOLDER_DRAG_AND_DROP).setIcon(R.drawable.btn_check_on_holo_light);
                    else
                        mMenu.findItem(R.id.ENABLE_FOLDER_DRAG_AND_DROP).setIcon(R.drawable.btn_check_off_holo_light);
                }
            }

//            mMenu.findItem(R.id.DELETE_FOLDERS).setVisible(foldersCnt >0);
//            mMenu.findItem(R.id.ENABLE_FOLDER_DRAG_AND_DROP).setVisible(foldersCnt >1);

            mMenu.setGroupVisible(R.id.group_pages_and_more, false);
            mMenu.setGroupVisible(R.id.group_notes, false);
        }
        else if(!drawer.isDrawerOpen())
        {
            if(Util.isLandscapeOrientation(mAct))
                mMenu.setGroupVisible(R.id.group_folders, false);

            /**
             * Page group and more
             */
            mMenu.setGroupVisible(R.id.group_pages_and_more, foldersCnt >0);

            if(foldersCnt>0)
            {
                getSupportActionBar().setTitle(mFolderTitle);

                // pages count
                int pgsCnt = FolderUi.getFolder_pagesCount(this,FolderUi.getFocus_folderPos());

                // notes count
                int notesCnt = 0;
                int pageTableId = Pref.getPref_focusView_page_tableId(this);

                if(pageTableId > 0) {
                    DB_page dB_page = new DB_page(this, pageTableId);
                    if (dB_page != null) {
                        try {
                            notesCnt = dB_page.getNotesCount(true);
                        } catch (Exception e) {
                            System.out.println("MainAct / _onPrepareOptionsMenu / dB_page.getNotesCount() error");
                            notesCnt = 0;
                        }
                    }
                }

                // change page color
                mMenu.findItem(R.id.CHANGE_PAGE_COLOR).setVisible(pgsCnt >0);

                // pages order
                mMenu.findItem(R.id.SHIFT_PAGE).setVisible(pgsCnt >1);

                // delete pages
                mMenu.findItem(R.id.DELETE_PAGES).setVisible(pgsCnt >0);

                // note operation
                mMenu.findItem(R.id.note_operation).setVisible( (pgsCnt >0) && (notesCnt>0) );

                // EXPORT TO SD CARD
                mMenu.findItem(R.id.EXPORT_TO_SD_CARD).setVisible(pgsCnt >0);

                // SEND PAGES
                mMenu.findItem(R.id.SEND_PAGES).setVisible(pgsCnt >0);

                /**
                 *  Note group
                 */
                // group of notes
                mMenu.setGroupVisible(R.id.group_notes, pgsCnt > 0);

                // play
                mMenu.findItem(R.id.PLAY).setVisible( (pgsCnt >0) && (notesCnt>0) );

                // HANDLE CHECKED NOTES
                mMenu.findItem(R.id.HANDLE_CHECKED_NOTES).setVisible( (pgsCnt >0) && (notesCnt>0) );
            }
            else if(foldersCnt==0)
            {
                /**
                 *  Note group
                 */
                mMenu.setGroupVisible(R.id.group_notes, false);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /*************************
     * onCreate Options Menu
     *
     *************************/
    public static MenuItem mSubMenuItemAudio;
    MenuItem playOrStopMusicButton;
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu)
    {
//		System.out.println("MainAct / _onCreateOptionsMenu");
        mMenu = menu;

        // inflate menu
        getMenuInflater().inflate(R.menu.main_menu, menu);

        playOrStopMusicButton = menu.findItem(R.id.PLAY_OR_STOP_MUSIC);

        // enable drag note
        mPref_show_note_attribute = getSharedPreferences("show_note_attribute", 0);
        if(mPref_show_note_attribute.getString("KEY_ENABLE_DRAGGABLE", "yes").equalsIgnoreCase("yes"))
            menu.findItem(R.id.ENABLE_NOTE_DRAG_AND_DROP)
                    .setIcon(R.drawable.btn_check_on_holo_light)
                    .setTitle(R.string.drag_note) ;
        else
            menu.findItem(R.id.ENABLE_NOTE_DRAG_AND_DROP)
                    .setIcon(R.drawable.btn_check_off_holo_light)
                    .setTitle(R.string.drag_note) ;

        // enable show body
        mPref_show_note_attribute = getSharedPreferences("show_note_attribute", 0);
        if(mPref_show_note_attribute.getString("KEY_SHOW_BODY", "yes").equalsIgnoreCase("yes"))
            menu.findItem(R.id.SHOW_BODY)
                    .setIcon(R.drawable.btn_check_on_holo_light)
                    .setTitle(R.string.preview_note_body) ;
        else
            menu.findItem(R.id.SHOW_BODY)
                .setIcon(R.drawable.btn_check_off_holo_light)
                .setTitle(R.string.preview_note_body) ;


        //
        // Group 1 sub_menu for drawer operation
        //

        // add sub_menu item: add folder drag setting
//    	if(mPref_show_note_attribute.getString("KEY_ENABLE_FOLDER_DRAGGABLE", "no")
//    								.equalsIgnoreCase("yes"))
//			menu.findItem(R.id.ENABLE_FOLDER_DRAG_AND_DROP)
//				.setIcon(R.drawable.btn_check_on_holo_light)
//				.setTitle(R.string.drag_folder) ;
//    	else
//			menu.findItem(R.id.ENABLE_FOLDER_DRAG_AND_DROP)
//				.setIcon(R.drawable.btn_check_off_holo_light)
//				.setTitle(R.string.drag_folder) ;

        return super.onCreateOptionsMenu(menu);
    }

    /******************************
     * on options item selected
     *
     ******************************/
    public static SlideshowInfo slideshowInfo;
    public static FragmentTransaction mFragmentTransaction;
    public static int mPlaying_pageTableId;
    public static int mPlaying_pagePos;
    public static int mPlaying_folderPos;
    public static int mPlaying_folderTableId;

    static int mMenuUiState;

    public static void setMenuUiState(int mMenuState) {
        mMenuUiState = mMenuState;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        //System.out.println("MainAct / _onOptionsItemSelected");
        setMenuUiState(item.getItemId());
        DB_drawer dB_drawer = new DB_drawer(this);
        DB_folder dB_folder = new DB_folder(this, Pref.getPref_focusView_folder_tableId(this));
        DB_page dB_page = new DB_page(this,Pref.getPref_focusView_page_tableId(this));

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Go back: check if Configure fragment now
        if( (item.getItemId() == android.R.id.home ))
        {

            System.out.println("MainAct / _onOptionsItemSelected / Home key of Config is pressed / mFragmentManager.getBackStackEntryCount() =" +
            mFragmentManager.getBackStackEntryCount());

            if(mFragmentManager.getBackStackEntryCount() > 0 )
            {
                int foldersCnt = dB_drawer.getFoldersCount(true);
                System.out.println("MainAct / _onOptionsItemSelected / Home key of Config is pressed / foldersCnt = " + foldersCnt);

                if(foldersCnt == 0)
                {
                    finish();
                    Intent intent  = new Intent(this,MainAct.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                else
                {
                    mFragmentManager.popBackStack();

                    initActionBar();

                    mFolderTitle = dB_drawer.getFolderTitle(FolderUi.getFocus_folderPos(),true);
                    setTitle(mFolderTitle);
                    drawer.closeDrawer();
                }
                return true;
            }
        }


        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (drawer.drawerToggle.onOptionsItemSelected(item))
        {
            System.out.println("MainAct / _onOptionsItemSelected / drawerToggle.onOptionsItemSelected(item) == true ");
            return true;
        }

        switch (item.getItemId())
        {
            case MenuId.ADD_NEW_FOLDER:
                FolderUi.renewFirstAndLast_folderId();
                FolderUi.addNewFolder(this, FolderUi.mLastExist_folderTableId +1, mFolder.getAdapter());
                return true;

            case MenuId.ENABLE_FOLDER_DRAG_AND_DROP:
                if(MainAct.mPref_show_note_attribute.getString("KEY_ENABLE_FOLDER_DRAGGABLE", "no")
                        .equalsIgnoreCase("yes"))
                {
                    mPref_show_note_attribute.edit().putString("KEY_ENABLE_FOLDER_DRAGGABLE","no")
                            .apply();
                    DragSortListView listView = (DragSortListView) this.findViewById(R.id.drawer_listview);
                    listView.setDragEnabled(false);
                    Toast.makeText(this,getResources().getString(R.string.drag_folder)+
                                    ": " +
                                    getResources().getString(R.string.set_disable),
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mPref_show_note_attribute.edit().putString("KEY_ENABLE_FOLDER_DRAGGABLE","yes")
                            .apply();
                    DragSortListView listView = (DragSortListView) this.findViewById(R.id.drawer_listview);
                    listView.setDragEnabled(true);
                    Toast.makeText(this,getResources().getString(R.string.drag_folder) +
                                    ": " +
                                    getResources().getString(R.string.set_enable),
                            Toast.LENGTH_SHORT).show();
                }
                mFolder.getAdapter().notifyDataSetChanged();
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                return true;

            case MenuId.DELETE_FOLDERS:
                mMenu.setGroupVisible(R.id.group_folders, false);

                if(dB_drawer.getFoldersCount(true)>0)
                {
                    drawer.closeDrawer();
                    mMenu.setGroupVisible(R.id.group_notes, false); //hide the menu
                    DeleteFolders delFoldersFragment = new DeleteFolders();
                    mFragmentTransaction = mFragmentManager.beginTransaction();
                    mFragmentTransaction.setCustomAnimations(R.anim.fragment_slide_in_left, R.anim.fragment_slide_out_left, R.anim.fragment_slide_in_right, R.anim.fragment_slide_out_right);
                    mFragmentTransaction.replace(R.id.content_frame, delFoldersFragment).addToBackStack("delete_folders").commit();
                }
                else
                {
                    Toast.makeText(this, R.string.config_export_none_toast, Toast.LENGTH_SHORT).show();
                }
                return true;

            case MenuId.ADD_NEW_NOTE:
                if(Build.VERSION.SDK_INT >= M)//api23
                {
                    // check permission
                    int permissionWriteExtStorage = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if(permissionWriteExtStorage == PackageManager.PERMISSION_GRANTED)
                        Add_note_option.createSelection(this,true);
                    else
                        Add_note_option.createSelection(this,false);
                }
                else
                    Add_note_option.createSelection(this,true);
                return true;

            case MenuId.OPEN_PLAY_SUBMENU:
                // new play instance: stop button is off
                if( (BackgroundAudioService.mMediaPlayer != null) &&
                    (Audio_manager.getPlayerState() != Audio_manager.PLAYER_AT_STOP))
                {
                    // show Stop
                    playOrStopMusicButton.setTitle(R.string.menu_button_stop_audio);
                    playOrStopMusicButton.setIcon(R.drawable.ic_media_stop);
                }
                else
                {
                    // show Play
                    playOrStopMusicButton.setTitle(R.string.menu_button_play_audio);
                    playOrStopMusicButton.setIcon(R.drawable.ic_media_play);
                }
                return true;

            case MenuId.PLAY_OR_STOP_AUDIO:
                if( (BackgroundAudioService.mMediaPlayer != null) &&
                    (Audio_manager.getPlayerState() != Audio_manager.PLAYER_AT_STOP))
                {
                    Audio_manager.stopAudioPlayer();

                    // remove audio panel
                    TabsHost.audioPlayer_page.page_runnable.run();

                    // refresh
                    TabsHost.reloadCurrentPage();

                    return true; // just stop playing, wait for user action
                }
                else // play first audio
                {
                    playFirstAudio();
                }
                return true;

            case MenuId.SLIDE_SHOW:
                slideshowInfo = new SlideshowInfo();
                // add images for slide show
                dB_page.open();
                int count = dB_page.getNotesCount(false);
                for(int position = 0; position < count ; position++)
                {
                    if(dB_page.getNoteMarking(position,false) == 1)
                    {
                        String pictureUri = dB_page.getNotePictureUri(position,false);
                        String drawingUri = dB_page.getNoteDrawingUri(position,false);
                        String linkUri = dB_page.getNoteLinkUri(position,false);

                        // replace picture path
                        if(Util.isEmptyString(pictureUri) && UtilImage.hasImageExtension(linkUri,this))
                            pictureUri = linkUri;
                        else if(UtilImage.hasImageExtension(drawingUri,this))
                            pictureUri = drawingUri;

                        String title = dB_folder.getCurrentPageTitle();
                        title = title.concat(" " + "(" + (position+1) + "/" + count + ")");
                        String text = dB_page.getNoteTitle(position,false);

                        if(!Util.isEmptyString(dB_page.getNoteBody(position,false)))
                            text += " : " + dB_page.getNoteBody(position,false);

                        if( (!Util.isEmptyString(pictureUri) && UtilImage.hasImageExtension(pictureUri,this)) ||
                            !(Util.isEmptyString(text)) 														) // skip empty
                        {
                            slideshowInfo.addShowItem(title,pictureUri,text,position);
                        }
                    }
                }
                dB_page.close();

                if(slideshowInfo.showItemsSize() > 0)
                {
                    // create new Intent to launch the slideShow player Activity
                    Intent playSlideshow = new Intent(this, SlideshowPlayer.class);
                    startActivity(playSlideshow);
                }
                else
                    Toast.makeText(mContext,R.string.file_not_found,Toast.LENGTH_SHORT).show();
                return true;

            case MenuId.GALLERY:
                Intent i_browsePic = new Intent(this, GalleryGridAct.class);
                startActivity(i_browsePic);
                return true;

            case MenuId.CHECKED_OPERATION:
                Checked_notes_option op = new Checked_notes_option(this);
                op.open_option_grid(this);
                return true;

            case MenuId.ADD_NEW_PAGE:

                // get current Max page table Id
                int currentMaxPageTableId = 0;
                int pgCnt = FolderUi.getFolder_pagesCount(this,FolderUi.getFocus_folderPos());
                DB_folder db_folder = new DB_folder(this,DB_folder.getFocusFolder_tableId());

                for(int i=0;i< pgCnt;i++)
                {
                    int id = db_folder.getPageTableId(i,true);
                    if(id >currentMaxPageTableId)
                        currentMaxPageTableId = id;
                }

                PageUi.addNewPage(this, currentMaxPageTableId + 1);
                return true;

            case MenuId.CHANGE_PAGE_COLOR:
                PageUi.changePageColor(this);
                return true;

            case MenuId.SHIFT_PAGE:
                PageUi.shiftPage(this);
            return true;

            case MenuId.DELETE_PAGES:
                if(dB_folder.getPagesCount(true)>0)
                {
                    mMenu.setGroupVisible(R.id.group_notes, false); //hide the menu
                    DeletePages delPgsFragment = new DeletePages();
                    mFragmentTransaction = mFragmentManager.beginTransaction();
                    mFragmentTransaction.setCustomAnimations(R.anim.fragment_slide_in_left, R.anim.fragment_slide_out_left, R.anim.fragment_slide_in_right, R.anim.fragment_slide_out_right);
                    mFragmentTransaction.replace(R.id.content_frame, delPgsFragment).addToBackStack("delete_pages").commit();
                }
                else
                {
                    Toast.makeText(this, R.string.no_page_yet, Toast.LENGTH_SHORT).show();
                }
            return true;

            case MenuId.ENABLE_NOTE_DRAG_AND_DROP:
                mPref_show_note_attribute = mContext.getSharedPreferences("show_note_attribute", 0);
                if(mPref_show_note_attribute.getString("KEY_ENABLE_DRAGGABLE", "yes").equalsIgnoreCase("yes")) {
                    mPref_show_note_attribute.edit().putString("KEY_ENABLE_DRAGGABLE", "no").apply();
                    Toast.makeText(this,getResources().getString(R.string.drag_note)+
                                        ": " +
                                        getResources().getString(R.string.set_disable),
                                   Toast.LENGTH_SHORT).show();
                }
                else {
                    mPref_show_note_attribute.edit().putString("KEY_ENABLE_DRAGGABLE", "yes").apply();
                    Toast.makeText(this,getResources().getString(R.string.drag_note) +
                                        ": " +
                                        getResources().getString(R.string.set_enable),
                                   Toast.LENGTH_SHORT).show();
                }
                invalidateOptionsMenu();
                TabsHost.reloadCurrentPage();
                return true;

            case MenuId.SHOW_BODY:
                mPref_show_note_attribute = mContext.getSharedPreferences("show_note_attribute", 0);
                if(mPref_show_note_attribute.getString("KEY_SHOW_BODY", "yes").equalsIgnoreCase("yes")) {
                    mPref_show_note_attribute.edit().putString("KEY_SHOW_BODY", "no").apply();
                    Toast.makeText(this,getResources().getString(R.string.preview_note_body) +
                                        ": " +
                                        getResources().getString(R.string.set_disable),
                                    Toast.LENGTH_SHORT).show();
                }
                else {
                    mPref_show_note_attribute.edit().putString("KEY_SHOW_BODY", "yes").apply();
                    Toast.makeText(this,getResources().getString(R.string.preview_note_body) +
                                        ": " +
                                        getResources().getString(R.string.set_enable),
                                   Toast.LENGTH_SHORT).show();
                }
                invalidateOptionsMenu();
                TabsHost.reloadCurrentPage();
                return true;

            // sub menu for backup
            case MenuId.IMPORT_FROM_WEB:
                Intent import_web = new Intent(this,Import_webAct.class);
                startActivityForResult(import_web,8000);
                return true;

            case MenuId.IMPORT_FROM_SD_CARD:
                //hide the menu
                mMenu.setGroupVisible(R.id.group_notes, false);
                mMenu.setGroupVisible(R.id.group_pages_and_more, false);
                // replace fragment
                Import_filesList importFragment = new Import_filesList();
                transaction.setCustomAnimations(R.anim.fragment_slide_in_left, R.anim.fragment_slide_out_left, R.anim.fragment_slide_in_right, R.anim.fragment_slide_out_right);
                transaction.replace(R.id.content_frame, importFragment,"import").addToBackStack(null).commit();
                return true;

            case MenuId.EXPORT_TO_SD_CARD:
                //hide the menu
                mMenu.setGroupVisible(R.id.group_notes, false);
                mMenu.setGroupVisible(R.id.group_pages_and_more, false);
                if(dB_folder.getPagesCount(true)>0)
                {
                    Export_toSDCardFragment exportFragment = new Export_toSDCardFragment();
                    transaction.setCustomAnimations(R.anim.fragment_slide_in_left, R.anim.fragment_slide_out_left, R.anim.fragment_slide_in_right, R.anim.fragment_slide_out_right);
                    transaction.replace(R.id.content_frame, exportFragment,"export").addToBackStack(null).commit();
                }
                else
                {
                    Toast.makeText(this, R.string.no_page_yet, Toast.LENGTH_SHORT).show();
                }
                return true;

            case MenuId.SEND_PAGES:
                mMenu.setGroupVisible(R.id.group_notes, false); //hide the menu

                if(dB_folder.getPagesCount(true)>0)
                {
                    MailPagesFragment mailFragment = new MailPagesFragment();
                    transaction.setCustomAnimations(R.anim.fragment_slide_in_left, R.anim.fragment_slide_out_left, R.anim.fragment_slide_in_right, R.anim.fragment_slide_out_right);
                    transaction.replace(R.id.content_frame, mailFragment,"mail").addToBackStack(null).commit();
                }
                else
                {
                    Toast.makeText(this, R.string.no_page_yet, Toast.LENGTH_SHORT).show();
                }
                return true;

            case MenuId.CONFIG:
                mMenu.setGroupVisible(R.id.group_notes, false); //hide the menu
                mMenu.setGroupVisible(R.id.group_pages_and_more, false);
                setTitle(R.string.settings);

                mConfigFragment = new Config();
                mFragmentTransaction = mFragmentManager.beginTransaction();
                mFragmentTransaction.setCustomAnimations(R.anim.fragment_slide_in_left, R.anim.fragment_slide_out_left, R.anim.fragment_slide_in_right, R.anim.fragment_slide_out_right);
                mFragmentTransaction.replace(R.id.content_frame, mConfigFragment).addToBackStack("config").commit();
                return true;

            case MenuId.ABOUT:
                mMenu.setGroupVisible(R.id.group_notes, false); //hide the menu
                mMenu.setGroupVisible(R.id.group_pages_and_more, false);
                setTitle(R.string.about_title);

                mAboutFragment = new About();
                mFragmentTransaction = mFragmentManager.beginTransaction();
                mFragmentTransaction.setCustomAnimations(R.anim.fragment_slide_in_left, R.anim.fragment_slide_out_left, R.anim.fragment_slide_in_right, R.anim.fragment_slide_out_right);
                mFragmentTransaction.replace(R.id.content_frame, mAboutFragment).addToBackStack("about").commit();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void playFirstAudio()
    {
        Audio_manager.setPlayerState(Audio_manager.PLAYER_AT_PLAY);

        Audio_manager.setAudioPlayMode(Audio_manager.PAGE_PLAY_MODE);
        Audio_manager.mAudioPos = 0;

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

        // initial
        BackgroundAudioService.mMediaPlayer = null;//for first

        Page_recycler page = TabsHost.getCurrentPage();
        TabsHost.audioUi_page = new AudioUi_page(this,page.recyclerView);
        TabsHost.audioUi_page.initAudioBlock(this);

        TabsHost.audioPlayer_page = new AudioPlayer_page(this,TabsHost.audioUi_page);
        TabsHost.audioPlayer_page.prepareAudioInfo();
        TabsHost.audioPlayer_page.runAudioState();

        // update audio play position
        TabsHost.audioPlayTabPos = TabsHost.getFocus_tabPos();
        TabsHost.mTabsPagerAdapter.notifyDataSetChanged();

        UtilAudio.updateAudioPanel(TabsHost.audioUi_page.audioPanel_play_button,
                TabsHost.audioUi_page.audio_panel_title_textView);

        // update playing page position
        mPlaying_pagePos = TabsHost.getFocus_tabPos();

        // update playing page table Id
        mPlaying_pageTableId = TabsHost.getCurrentPageTableId();

        // update playing folder position
        mPlaying_folderPos = FolderUi.getFocus_folderPos();

        DB_drawer dB_drawer = new DB_drawer(this);
        MainAct.mPlaying_folderTableId = dB_drawer.getFolderTableId(MainAct.mPlaying_folderPos,true);
    }

    // configure layout view
    void configLayoutView()
    {
        System.out.println("MainAct / _configLayoutView");

        setContentView(R.layout.drawer);
        initActionBar();

        // new drawer
        drawer = new Drawer(this);
        drawer.initDrawer();

        // new folder
        mFolder = new Folder(this);

        openFolder();
    }


    // callback: media browser connection
    public static MediaBrowserCompat.ConnectionCallback mMediaBrowserCompatConnectionCallback = new MediaBrowserCompat.ConnectionCallback() {
        @Override
        public void onConnected() {
            super.onConnected();

            System.out.println("MainAct / MediaBrowserCompat.Callback / _onConnected");
            try {
                mMediaControllerCompat = new MediaControllerCompat(mAct, mMediaBrowserCompat.getSessionToken());
                mMediaControllerCompat.registerCallback(mMediaControllerCompatCallback);
                MediaControllerCompat.setMediaController(mAct,mMediaControllerCompat);
            } catch( RemoteException e ) {
                System.out.println("MainAct / MediaBrowserCompat.Callback / RemoteException");
            }
        }
    };

    // callback: media controller
    public static MediaControllerCompat.Callback mMediaControllerCompatCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
//            System.out.println("MainAct / _MediaControllerCompat.Callback / _onPlaybackStateChanged / state = " + state);
            if( state == null ) {
                return;
            }

            switch( state.getState() ) {
                case STATE_PLAYING: {
                    mCurrentState = STATE_PLAYING;
                    break;
                }
                case STATE_PAUSED: {
                    mCurrentState = STATE_PAUSED;
                    break;
                }
            }
        }
    };

}