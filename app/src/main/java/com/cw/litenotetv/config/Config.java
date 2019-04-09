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

package com.cw.litenotetv.config;


import java.io.File;

import com.cw.litenotetv.folder.FolderUi;
import com.cw.litenotetv.operation.audio.Audio_manager;
import com.cw.litenotetv.operation.audio.BackgroundAudioService;
import com.cw.litenotetv.tabs.TabsHost;
import com.cw.litenotetv.util.BaseBackPressedListener;
import com.cw.litenotetv.main.MainAct;
import com.cw.litenotetv.R;
import com.cw.litenotetv.db.DB_drawer;
import com.cw.litenotetv.util.ColorSet;
import com.cw.litenotetv.util.Util;
import com.cw.litenotetv.util.preferences.Pref;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class Config extends Fragment
{
	// style
	TextView mNewPageTVStyle;
	private int mStyle = 0;

	// vibration
	SharedPreferences mPref_vibration;
	TextView mTextViewVibration;

	private AlertDialog dialog;
	private Context mContext;
	private LayoutInflater mInflater;
	String[] mItemArray = new String[]{"1","2","3","4","5","6","7","8","9","10"};
	
	public Config(){}
	static View mRootView;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		System.out.println("================ Config / onCreateView ==================");

		mRootView = inflater.inflate(R.layout.config, container, false);

	    //Set text style
		setNewPageTextStyle();
		
		//Set Take Picture Option
		setTakeImageOption();

		//Set YouTube launch delay
		setYouTubeLaunchDelay();

		//Set slideshow switch time
		setSlideshowSwitchTime();

		//Set vibration time length
		setVibrationTimeLength();

		//delete DB
		deleteDB_button();

		//recover all settings to default
		recover_all_settings_button();

		// set Back pressed listener
		((MainAct)getActivity()).setOnBackPressedListener(new BaseBackPressedListener(MainAct.mAct));

		return mRootView;
	}   	

	/**
	 *  set take picture option
	 *  
	 */
	SharedPreferences mPref_takePicture;
	TextView mTextViewTakePicture;	
	void setTakeImageOption()
	{
		//  set current
		mPref_takePicture = getActivity().getSharedPreferences("takeImage", 0);
		View viewOption = mRootView.findViewById(R.id.takePictureOption);
		mTextViewTakePicture = (TextView)mRootView.findViewById(R.id.TakePictureOptionSetting);
		
		if(mPref_takePicture.getString("KEY_SHOW_CONFIRMATION_DIALOG","no").equalsIgnoreCase("yes"))		   
			mTextViewTakePicture.setText(getResources().getText(R.string.confirm_dialog_button_yes).toString());
		else
			mTextViewTakePicture.setText(getResources().getText(R.string.confirm_dialog_button_no).toString());

		// Select new 
		viewOption.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				selectTakePictureOptionDialog();
			}
		});
	}

	void selectTakePictureOptionDialog()
	{
		   final String[] items = new String[]{
				   getResources().getText(R.string.confirm_dialog_button_yes).toString(),
				   getResources().getText(R.string.confirm_dialog_button_no).toString()   };
		   AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		   
		   String strTakePicture = mPref_takePicture.getString("KEY_SHOW_CONFIRMATION_DIALOG","no");
		   
		   // add current selection
		   for(int i=0;i< items.length;i++)
		   {
			   if(strTakePicture.equalsIgnoreCase("yes"))
				   items[0] = getResources().getText(R.string.confirm_dialog_button_yes).toString() + " *";
			   else if(strTakePicture.equalsIgnoreCase("no"))
				   items[1] = getResources().getText(R.string.confirm_dialog_button_no).toString() + " *";
		   }
		   
		   DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
		   {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(which == 0)
					{
						mPref_takePicture.edit().putString("KEY_SHOW_CONFIRMATION_DIALOG","yes").apply();
						mTextViewTakePicture.setText(getResources().getText(R.string.confirm_dialog_button_yes).toString());
					}
					else if(which == 1)
					{
						mPref_takePicture.edit().putString("KEY_SHOW_CONFIRMATION_DIALOG","no").apply();
						mTextViewTakePicture.setText(getResources().getText(R.string.confirm_dialog_button_no).toString());
					}
					
					//end
					dialog.dismiss();
				}
		   };
		   builder.setTitle(R.string.config_confirm_taken_picture)
				  .setSingleChoiceItems(items, -1, listener)
				  .setNegativeButton(R.string.btn_Cancel, null)
				  .show();
	}

	/**
	 *  select style
	 *  
	 */
	void setNewPageTextStyle()
	{
		// Get current style
		mNewPageTVStyle = (TextView)mRootView.findViewById(R.id.TextViewStyleSetting);
		View mViewStyle = mRootView.findViewById(R.id.setStyle);
		int iBtnId = Util.getNewPageStyle(getActivity());
		
		// set background color with current style 
		mNewPageTVStyle.setBackgroundColor(ColorSet.mBG_ColorArray[iBtnId]);
		mNewPageTVStyle.setText(mItemArray[iBtnId]);
		mNewPageTVStyle.setTextColor(ColorSet.mText_ColorArray[iBtnId]);
		
		mViewStyle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectStyleDialog(v);
			}
		});
	}
	
	
	void selectStyleDialog(View view)
	{
		mContext = getActivity();
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		
		builder.setTitle(R.string.config_set_style_title)
			   .setPositiveButton(R.string.btn_OK, listener_ok)
			   .setNegativeButton(R.string.btn_Cancel, null);
		
		// inflate select style layout
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = mInflater.inflate(R.layout.select_style, null);
		RadioGroup RG_view = (RadioGroup)view.findViewById(R.id.radioGroup1);
		
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio0),0);
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio1),1);
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio2),2);
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio3),3);
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio4),4);
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio5),5);
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio6),6);
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio7),7);
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio8),8);
		setButtonColor((RadioButton)RG_view.findViewById(R.id.radio9),9);
		
		builder.setView(view);

		RadioGroup radioGroup = (RadioGroup) RG_view.findViewById(R.id.radioGroup1);
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(RadioGroup RG, int id) {
				mStyle = RG.indexOfChild(RG.findViewById(id));
		}});
		
		dialog = builder.create();
		dialog.show();
	}
	
    private void setButtonColor(RadioButton rBtn,int iBtnId)
    {
		rBtn.setBackgroundColor(ColorSet.mBG_ColorArray[iBtnId]);
		rBtn.setText(mItemArray[iBtnId]);
		rBtn.setTextColor(ColorSet.mText_ColorArray[iBtnId]);
		
		//set checked item
		if(iBtnId == Util.getNewPageStyle(mContext))
			rBtn.setChecked(true);
		else
			rBtn.setChecked(false);
    }
		   
    DialogInterface.OnClickListener listener_ok = new DialogInterface.OnClickListener()
   {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			SharedPreferences mPref_style = getActivity().getSharedPreferences("style", 0);
			mPref_style.edit().putInt("KEY_STYLE",mStyle).apply();
			// update the style selection directly
			mNewPageTVStyle.setBackgroundColor(ColorSet.mBG_ColorArray[mStyle]);
			mNewPageTVStyle.setText(mItemArray[mStyle]);
			mNewPageTVStyle.setTextColor(ColorSet.mText_ColorArray[mStyle]);
			//end
			dialog.dismiss();
		}
   };

	/**
	 *  set YouTube launch delay
	 *
	 */
	void setYouTubeLaunchDelay()
	{
		//  set current
		SharedPreferences pref_sw_time = getActivity().getSharedPreferences("youtube_launch_delay", 0);
		View swTimeView = mRootView.findViewById(R.id.youtube_launch_delay);
		TextView slideshow_text_view = (TextView)mRootView.findViewById(R.id.youtube_launch_delay_setting);
		String strSwTime = pref_sw_time.getString("KEY_YOUTUBE_LAUNCH_DELAY","10");
		slideshow_text_view.setText(strSwTime +"s");

		// switch time picker
		swTimeView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				youtubeLaunchDelayPickerDialog();
			}
		});
	}

	/**
	 *  set slideshow switch time
	 *
	 */
	void setSlideshowSwitchTime()
	{
		//  set current
		SharedPreferences pref_sw_time = getActivity().getSharedPreferences("slideshow_sw_time", 0);
		View swTimeView = mRootView.findViewById(R.id.slideshow_sw_time);
		TextView slideshow_text_view = (TextView)mRootView.findViewById(R.id.slideshow_sw_time_setting);
		String strSwTime = pref_sw_time.getString("KEY_SLIDESHOW_SW_TIME","5");
		slideshow_text_view.setText(strSwTime +"s");

		// switch time picker
		swTimeView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				slideshowSwTimePickerDialog();
			}
		});
	}

	/**
	 *  select vibration time length
	 *  
	 */
	void setVibrationTimeLength()
	{
		//  set current
		mPref_vibration = getActivity().getSharedPreferences("vibration", 0);
		View viewVibration = mRootView.findViewById(R.id.vibrationSetting);
		mTextViewVibration = (TextView)mRootView.findViewById(R.id.TextViewVibrationSetting);
	    String strVibTime = mPref_vibration.getString("KEY_VIBRATION_TIME","25");
		if(strVibTime.equalsIgnoreCase("00"))
			mTextViewVibration.setText(getResources().getText(R.string.config_status_disabled).toString());
		else
			mTextViewVibration.setText(strVibTime +"ms");

		// Select new 
		viewVibration.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				selectVibrationLengthDialog();
			}
		});
	}


	/**
	 * Dialog for setting youtube launch delay
	 */
	void youtubeLaunchDelayPickerDialog()
	{
		final AlertDialog.Builder d = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.config_slideshow_sw_time_picker, null);
		d.setTitle(R.string.config_set_slideshow_dlg_title);
		d.setMessage(R.string.config_set_slideshow_dlg_message);
		d.setView(dialogView);

		final SharedPreferences pref_sw_time = getActivity().getSharedPreferences("youtube_launch_delay", 0);
		final String strSwitchTime = pref_sw_time.getString("KEY_YOUTUBE_LAUNCH_DELAY","10");

		final NumberPicker numberPicker = (NumberPicker) dialogView.findViewById(R.id.dialog_number_picker);
		numberPicker.setMaxValue(20);
		numberPicker.setMinValue(1);
		numberPicker.setValue(Integer.valueOf(strSwitchTime));
		numberPicker.setWrapSelectorWheel(true);
		numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker numberPicker, int i, int i1) {
			}
		});
		d.setPositiveButton(R.string.btn_OK, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				int len = numberPicker.getValue();
				pref_sw_time.edit().putString("KEY_YOUTUBE_LAUNCH_DELAY",String.valueOf(len)).apply();
				TextView slideshow_text_view = (TextView)mRootView.findViewById(R.id.youtube_launch_delay_setting);
				slideshow_text_view.setText(len + "s");
			}
		});
		d.setNegativeButton(R.string.btn_Cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
			}
		});
		AlertDialog alertDialog = d.create();
		alertDialog.show();
	}



	/**
	 * Dialog for setting slideshow switch time
	 */
	void slideshowSwTimePickerDialog()
	{
		final AlertDialog.Builder d = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.config_slideshow_sw_time_picker, null);
		d.setTitle(R.string.config_set_slideshow_dlg_title);
		d.setMessage(R.string.config_set_slideshow_dlg_message);
		d.setView(dialogView);

		final SharedPreferences pref_sw_time = getActivity().getSharedPreferences("slideshow_sw_time", 0);
		final String strSwitchTime = pref_sw_time.getString("KEY_SLIDESHOW_SW_TIME","5");

		final NumberPicker numberPicker = (NumberPicker) dialogView.findViewById(R.id.dialog_number_picker);
		numberPicker.setMaxValue(120);
		numberPicker.setMinValue(1);
		numberPicker.setValue(Integer.valueOf(strSwitchTime));
		numberPicker.setWrapSelectorWheel(true);
		numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker numberPicker, int i, int i1) {
			}
		});
		d.setPositiveButton(R.string.btn_OK, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				int len = numberPicker.getValue();
				pref_sw_time.edit().putString("KEY_SLIDESHOW_SW_TIME",String.valueOf(len)).apply();
				TextView slideshow_text_view = (TextView)mRootView.findViewById(R.id.slideshow_sw_time_setting);
				slideshow_text_view.setText(len + "s");
			}
		});
		d.setNegativeButton(R.string.btn_Cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
			}
		});
		AlertDialog alertDialog = d.create();
		alertDialog.show();
	}


	void selectVibrationLengthDialog()
	{
		   final String[] items = new String[]{getResources().getText(R.string.config_status_disabled).toString(),
				   		    				"15ms","25ms","35ms","45ms"};
		   AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		   
		   String strVibTime = mPref_vibration.getString("KEY_VIBRATION_TIME","25");
		   
		   if(strVibTime.equalsIgnoreCase("00"))
		   {
			   items[0] = getResources().getText(R.string.config_status_disabled).toString() + " *";
		   }
		   else 
		   {
			   for(int i=1;i< items.length;i++)
			   {
				   if(strVibTime.equalsIgnoreCase((String) items[i].subSequence(0,2)))
					   items[i] += " *";
			   }
		   }
		   
		   DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
		   {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String len = null;
					
					if(which ==0)
						len = "00";
					else
						len = (String) items[which].subSequence(0,2);
					mPref_vibration.edit().putString("KEY_VIBRATION_TIME",len).apply();
					// change the length directly
					if(len.equalsIgnoreCase("00"))
						mTextViewVibration.setText(getResources().getText(R.string.config_status_disabled).toString());
					else
						mTextViewVibration.setText(len + "ms");					
					
					//end
					dialog.dismiss();
				}
		   };
		   builder.setTitle(R.string.config_set_vibration_title)
				  .setSingleChoiceItems(items, -1, listener)
				  .setNegativeButton(R.string.btn_Cancel, null)
				  .show();
	}

   
    /**
     * Delete DB
     *
     */
    public void deleteDB_button(){
	    View tvDelDB = mRootView.findViewById(R.id.SetDeleteDB);
	    tvDelDB.setOnClickListener(new OnClickListener() {
		   @Override
		   public void onClick(View v) {
			   confirmDeleteDB(v);
		   }
	   });
    }

	private void confirmDeleteDB(View view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.confirm_dialog_title)
	           .setMessage(R.string.config_delete_DB_confirm_content)
			   .setPositiveButton(R.string.btn_OK, listener_delete_DB)
			   .setNegativeButton(R.string.btn_Cancel, null)
			   .show();
	}

    DialogInterface.OnClickListener listener_delete_DB = new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) {
			DB_drawer db_drawer = new DB_drawer(getActivity());
			db_drawer.deleteDB();

			// stop audio player
			if(BackgroundAudioService.mMediaPlayer != null)
				Audio_manager.stopAudioPlayer();

			//set last tab Id to 0, otherwise TabId will not start from 0 when deleting all
			//reset tab Index to 0
			//fix: select tab over next import amount => clean all => import => export => error
			TabsHost.setFocus_tabPos(0);
			FolderUi.setFocus_folderPos(0);

			// remove focus view folder table Id key
			Pref.removePref_focusView_folder_tableId_key(getActivity());

			//todo Add initial condition?

			dialog.dismiss();

			getActivity().finish();
			Intent intent  = new Intent(getActivity(),MainAct.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			getActivity().startActivity(intent);
		}
    };

	/**
	 * recover all settings to default
	 */
	public void recover_all_settings_button(){
		View recoverDefault = mRootView.findViewById(R.id.RecoverAllSettings);
		recoverDefault.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				confirmRecoverDefault(v);
			}
		});
	}


	private void confirmRecoverDefault(View view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.confirm_dialog_title)
				.setMessage(R.string.config_recover_all_settings)
				.setPositiveButton(R.string.btn_OK, listener_recover_default)
				.setNegativeButton(R.string.btn_Cancel, null)
				.show();
	}

	DialogInterface.OnClickListener listener_recover_default = new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) {

			// stop audio player
			if(BackgroundAudioService.mMediaPlayer != null)
				Audio_manager.stopAudioPlayer();

			//remove preference
			clearSharedPreferencesForSettings(getActivity());

			dialog.dismiss();

            ((MainAct)getActivity()).getSupportFragmentManager()//??? warning
                    .beginTransaction()
                    .detach(Config.this)
                    .attach(Config.this)
                    .commit();
		}
	};

    public static void clearSharedPreferencesForSettings(Context context)
	{
		File dir = new File(context.getFilesDir().getParent() + "/shared_prefs/");

		String[] children = dir.list();

		for (int i = 0; i < children.length; i++) {
			System.out.println("original: " + children[i]);

            // EULA is using PreferenceManager.getDefaultSharedPreferences(MainAct.mAct)
            // it will create packageName_preferences.xml

			// clear each preferences XML file content, except default shared preferences file
            if(!children[i].contains("preferences")) {
                context.getSharedPreferences(children[i].replace(".xml", ""), Context.MODE_PRIVATE)
                        .edit().clear().apply();
                System.out.println("clear: " + children[i]);
            }
		}

		// Make sure it has enough time to save all the committed changes
		try { Thread.sleep(1000); } catch (InterruptedException e) {}

		for (int i = 0; i < children.length; i++) {
			// delete the files
            if(!children[i].contains("preferences")) {
                new File(dir, children[i]).delete();
                System.out.println("delete:" + " " + children[i]);
            }
		}
    }
    
}