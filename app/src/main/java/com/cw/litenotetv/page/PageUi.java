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

package com.cw.litenotetv.page;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Space;
import android.widget.TextView;

import com.cw.litenotetv.R;
import com.cw.litenotetv.db.DB_folder;
import com.cw.litenotetv.db.DB_page;
import com.cw.litenotetv.folder.FolderUi;
import com.cw.litenotetv.main.MainAct;
import com.cw.litenotetv.define.Define;
import com.cw.litenotetv.tabs.TabsHost;
import com.cw.litenotetv.util.TouchableEditText;
import com.cw.litenotetv.util.Util;
import com.cw.litenotetv.util.preferences.Pref;

import java.lang.reflect.Field;

// implement lambda expressions
public class PageUi
{
	public PageUi(){}

    /*
	 * Change Page Color
	 *
	 */
	public static void changePageColor(final AppCompatActivity act)
	{
		// set color
		final Builder builder = new Builder(act);
		builder.setTitle(R.string.edit_page_color_title)
	    	   .setPositiveButton(R.string.edit_page_button_ignore, (DialogInterface dialog, int which) ->
	                 {/*cancel*/}
	            	);
		// inflate select style layout
		LayoutInflater mInflater= (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if(mInflater == null)
		    return;

		View view = mInflater.inflate(R.layout.select_style, null);
		RadioGroup RG_view = view.findViewById(R.id.radioGroup1);

		Util.setButtonColor(RG_view.findViewById(R.id.radio0),0);
		Util.setButtonColor(RG_view.findViewById(R.id.radio1),1);
		Util.setButtonColor(RG_view.findViewById(R.id.radio2),2);
		Util.setButtonColor(RG_view.findViewById(R.id.radio3),3);
		Util.setButtonColor(RG_view.findViewById(R.id.radio4),4);
		Util.setButtonColor(RG_view.findViewById(R.id.radio5),5);
		Util.setButtonColor(RG_view.findViewById(R.id.radio6),6);
		Util.setButtonColor(RG_view.findViewById(R.id.radio7),7);
		Util.setButtonColor(RG_view.findViewById(R.id.radio8),8);
		Util.setButtonColor(RG_view.findViewById(R.id.radio9),9);

		// set current selection
		for(int i=0;i< Util.getStyleCount();i++)
		{
			if(Util.getCurrentPageStyle(TabsHost.getFocus_tabPos()) == i)
			{
				RadioButton button = (RadioButton) RG_view.getChildAt(i);
		    	if(i%2 == 0)
		    		button.setButtonDrawable(R.drawable.btn_radio_on_holo_dark);
		    	else
		    		button.setButtonDrawable(R.drawable.btn_radio_on_holo_light);
			}
		}

		builder.setView(view);

		RadioGroup radioGroup = (RadioGroup) RG_view.findViewById(R.id.radioGroup1);

		final AlertDialog dlg = builder.create();
	    dlg.show();

		radioGroup.setOnCheckedChangeListener( (anyName, id) -> {
				DB_folder db = new DB_folder(MainAct.mAct,DB_folder.getFocusFolder_tableId());
				int style = radioGroup.indexOfChild(radioGroup.findViewById(id));
                int pos = TabsHost.getFocus_tabPos();
				db.updatePage(db.getPageId(pos, true),
							  db.getPageTitle(pos, true),
							  db.getPageTableId(pos, true),
							  style,
                              true);
	 			dlg.dismiss();
				FolderUi.startTabsHostRun();
		});
	}

	final static int LEFTMOST = -1;
	final static int MIDDLE = 0;
	final static int RIGHTMOST = 1;

	/**
	 * shift page right or left
     *
	 * If style parent is Theme.AppCompat.Light.NoActionBar
	 *      left to right order: NeutralButton (button 3), NegativeButton(button 2), PositiveButton(button 1)
	 * If style parent is @android:style/Theme.Holo
     *      left to right order: NegativeButton(button 2), NeutralButton(button 3), PositiveButton(button 1)
	 *
	 */
	public static void shiftPage(final AppCompatActivity act)
	{
	    Builder builder = new Builder(act);
	    builder.setTitle(R.string.rearrange_page_title)
	      	   .setMessage(null)
//				.setNegativeButton(R.string.rearrange_page_left, null)
//				.setNeutralButton(R.string.edit_note_button_back, null)
               .setNeutralButton(R.string.rearrange_page_left, null)
			   .setNegativeButton(R.string.edit_note_button_back, null)
	           .setPositiveButton(R.string.rearrange_page_right,null)
	           .setIcon(R.drawable.ic_dragger_horizontal);
	    final AlertDialog dlg = builder.create();

        // set center for negative button
        dlg.setOnShowListener( (DialogInterface dialog) -> {
            Button negativeButton = dlg.getButton(AlertDialog.BUTTON_NEUTRAL);
            LinearLayout layoutView = (LinearLayout) negativeButton.getParent();
            Space space= (Space) layoutView.getChildAt(1);
            space.setVisibility(View.GONE);
        });

	    // disable dim background
		dlg.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		dlg.show();


//        inal int dividerWidth = act.getResources().getDrawable(R.drawable.ic_tab_divider).getMinimumWidth();
//        final int screenWidth = UtilImage.getScreenWidth(act);

		// Shift to left
		dlg.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener((View v) -> {
			//change to OK
			Button mButton=(Button)dlg.findViewById(android.R.id.button2);
			mButton.setText(R.string.btn_Finish);
			mButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_finish , 0, 0, 0);
			DB_folder db = new DB_folder(act,Pref.getPref_focusView_folder_tableId(act));
		    int focus_tabPos = 	TabsHost.getFocus_tabPos();
			if(getTabPositionState() != LEFTMOST)
			{
				Pref.setPref_focusView_page_tableId(act, db.getPageTableId(focus_tabPos, true));
				swapPage(focus_tabPos,
						focus_tabPos -1);

				// shift left when audio playing
				if(MainAct.mPlaying_folderPos == FolderUi.getFocus_folderPos()) {
					// target is playing index
					if (focus_tabPos == MainAct.mPlaying_pagePos)
						MainAct.mPlaying_pagePos--;
					// target is at right side of playing index
					else if ((focus_tabPos - MainAct.mPlaying_pagePos) == 1)
						MainAct.mPlaying_pagePos++;
				}
				FolderUi.startTabsHostRun();
				TabsHost.setFocus_tabPos(focus_tabPos-1);
				updateButtonState(dlg);
			}
	    });

	    // done
		dlg.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener( (View v)-> dlg.dismiss() );

	    // Shift to right
	    dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((View v)-> {
			// middle button text: change to OK
//			    dlg.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
//			    Button mButton=(Button)dlg.findViewById(android.R.id.button3);
			dlg.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);
			Button mButton=(Button)dlg.findViewById(android.R.id.button2);

			mButton.setText(R.string.btn_Finish);
			mButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_finish , 0, 0, 0);

			DB_folder db = new DB_folder(act,Pref.getPref_focusView_folder_tableId(act));
			int focus_tabPos = 	TabsHost.getFocus_tabPos();
			if(getTabPositionState() != RIGHTMOST)
			{
				Pref.setPref_focusView_page_tableId(act, db.getPageTableId(focus_tabPos, true));
				swapPage(focus_tabPos, focus_tabPos +1);

				// shift right when audio playing
				if(MainAct.mPlaying_folderPos == FolderUi.getFocus_folderPos()) {
					// target is playing index
					if (focus_tabPos== MainAct.mPlaying_pagePos)
						MainAct.mPlaying_pagePos++;
					// target is at left side of plying index
					else if ((MainAct.mPlaying_pagePos - focus_tabPos) == 1)
						MainAct.mPlaying_pagePos--;
				}
				FolderUi.startTabsHostRun();
				TabsHost.setFocus_tabPos(TabsHost.getFocus_tabPos()+1);
				updateButtonState(dlg);
			}
	    });


        updateButtonState(dlg);

		((Button)dlg.findViewById(android.R.id.button2))
	              .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);
	}

	private static int getTabPositionState()
	{
		int pos = TabsHost.getFocus_tabPos();

		DB_folder db = new DB_folder(MainAct.mAct,Pref.getPref_focusView_folder_tableId(MainAct.mAct));
		int count = db.getPagesCount(true);

		if( pos == 0 )
			return LEFTMOST;
		else if(pos == (count-1))
			return RIGHTMOST;
		else
			return MIDDLE;
	}

	private static void updateButtonState(AlertDialog dlg)
    {
        if(getTabPositionState() == LEFTMOST )
        {
            // android.R.id.button1 for positive: next
            dlg.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            ((Button)dlg.findViewById(android.R.id.button1))
                    .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_forward, 0, 0, 0);

            // android.R.id.button3 for neutral: previous
			dlg.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);
            ((Button)dlg.findViewById(android.R.id.button3))
                    .setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        else if(getTabPositionState() == RIGHTMOST)
        {
            // android.R.id.button1 for positive: next
            dlg.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            ((Button)dlg.findViewById(android.R.id.button1))
                    .setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

            // android.R.id.button3 for neutral: previous
			dlg.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(true);//avoid long time toast
            ((Button)dlg.findViewById(android.R.id.button3))
                    .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_back, 0, 0, 0);

        }
        else if(getTabPositionState() == MIDDLE)
        {
            // android.R.id.button1 for positive: next
            dlg.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            ((Button)dlg.findViewById(android.R.id.button1))
                    .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_forward, 0, 0, 0);

            // android.R.id.button3 for neutral: previous
			dlg.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(true);
			((Button)dlg.findViewById(android.R.id.button3))
                    .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_back, 0, 0, 0);
        }
    }

	/**
	 * swap page
	 *
	 */
	private static void swapPage(int start, int end)
	{
		System.out.println("PageUi / _swapPage / start = " + start + " , end = " + end);
		DB_folder db = new DB_folder(MainAct.mAct,Pref.getPref_focusView_folder_tableId(MainAct.mAct));

        db.open();

        // start
        int startPageId = db.getPageId(start,false);
        String startPageTitle = db.getPageTitle(start,false);
        int startPageTableId = db.getPageTableId(start,false);
        int startPageStyle = db.getPageStyle(start, false);

        // end
        int endPageId = db.getPageId(end,false);
		String endPageTitle = db.getPageTitle(end,false);
		int endPageTableId = db.getPageTableId(end,false);
		int endPageStyle = db.getPageStyle(end, false);

        // swap
		db.updatePage(endPageId,
                      startPageTitle,
			          startPageTableId,
				      startPageStyle,
                      false);

		db.updatePage(startPageId,
					  endPageTitle,
					  endPageTableId,
					  endPageStyle,
                      false);
        db.close();
	}


	/**
	 * Add new page: 1 dialog
	 *
	 */
    private static int mAddAt;
    private static SharedPreferences mPref_add_new_page_location;
	public static  void addNewPage(final AppCompatActivity act, final int newTabId) {
        // get tab name
        String pageName = Define.getTabTitle(act, newTabId);

        // check if name is duplicated
		DB_folder dbFolder = new DB_folder(act,Pref.getPref_focusView_folder_tableId(act));
        dbFolder.open();
        final int pagesCount = dbFolder.getPagesCount(false);

        for (int i = 0; i < pagesCount; i++) {
            String tabTitle = dbFolder.getPageTitle(i, false);
            // new name for differentiation
            if (pageName.equalsIgnoreCase(tabTitle)) {
                pageName = tabTitle.concat("b");
            }
        }
        dbFolder.close();

        // get layout inflater
        View rootView = act.getLayoutInflater().inflate(R.layout.add_new_page, null);
        final TouchableEditText editPageName = (TouchableEditText)rootView.findViewById(R.id.new_page_name);

        // set cursor
        try {
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            f.set(editPageName, R.drawable.cursor);
        } catch (Exception ignored) {
        }

        final String hintPageName = pageName;

        // set hint
//        editPageName.setHint(pageName);

        editPageName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
//                    ((EditText) v).setText("");
//                    ((EditText) v).setSelection(0);
					((EditText) v).setHint(hintPageName);
                }
            }
        });


        editPageName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ((EditText) v).setText(hintPageName);
                ((EditText) v).setSelection(hintPageName.length());
                v.performClick();
                return false;
            }

        });


        // radio buttons
        final RadioGroup mRadioGroup = (RadioGroup) rootView.findViewById(R.id.radioGroup_new_page_at);

        // get new page location option
        mPref_add_new_page_location = act.getSharedPreferences("add_new_page_option", 0);
        if (mPref_add_new_page_location.getString("KEY_ADD_NEW_PAGE_TO", "right").equalsIgnoreCase("left"))
        {
            mRadioGroup.check(mRadioGroup.getChildAt(0).getId());
            mAddAt = 0;
        }
        else if (mPref_add_new_page_location.getString("KEY_ADD_NEW_PAGE_TO", "right").equalsIgnoreCase("right"))
        {
            mRadioGroup.check(mRadioGroup.getChildAt(1).getId());
            mAddAt = 1;
        }

        // update new page location option
        mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup RG, int id) {
                mAddAt = mRadioGroup.indexOfChild(mRadioGroup.findViewById(id));
                if (mAddAt == 0) {
                    mPref_add_new_page_location.edit().putString("KEY_ADD_NEW_PAGE_TO", "left").apply();
                } else if (mAddAt == 1) {
                    mPref_add_new_page_location.edit().putString("KEY_ADD_NEW_PAGE_TO", "right").apply();
                }
            }
        });

        // set view to dialog
        Builder builder1 = new Builder(act);
        builder1.setView(rootView);
        final AlertDialog dialog1 = builder1.create();
        dialog1.show();

        // cancel button
        Button btnCancel = (Button) rootView.findViewById(R.id.new_page_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog1.dismiss();
            }
        });

        // add button
        Button btnAdd = (Button) rootView.findViewById(R.id.new_page_add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String pageName;
                if (!Util.isEmptyString(editPageName.getText().toString()))
                    pageName = editPageName.getText().toString();
                else
                    pageName = Define.getTabTitle(act, newTabId);

                if (mAddAt == 0) {
                    if(pagesCount>0)
                        insertPage_leftmost(act, newTabId, pageName);
                    else
                        insertPage_rightmost(act, newTabId, pageName);
                }
                else
                    insertPage_rightmost(act, newTabId, pageName);

                dialog1.dismiss();
            }
        });
    }

	/*
	 * Insert Page to Rightmost
	 * 
	 */
	private static void insertPage_rightmost(final AppCompatActivity act, int newTblId, String tabName)
	{
		DB_folder dbFolder = new DB_folder(act,Pref.getPref_focusView_folder_tableId(act));
	    // insert tab name
		int style = Util.getNewPageStyle(act);
		dbFolder.insertPage(DB_folder.getFocusFolder_tableName(),tabName,newTblId,style,true );
		
		// insert table for new tab
		dbFolder.insertPageTable(dbFolder,DB_folder.getFocusFolder_tableId(),newTblId, true);

        int tabTotalCount = dbFolder.getPagesCount(true);

		// commit: final page viewed
		Pref.setPref_focusView_page_tableId(act, newTblId);

	    // set scroll X
		final int scrollX = (tabTotalCount) * 60 * 5; //over the last scroll X

        FolderUi.startTabsHostRun();

		if(TabsHost.mTabLayout != null) {
			TabsHost.mTabLayout.post(() -> {
				TabsHost.mTabLayout.scrollTo(scrollX, 0);
//					Pref.setPref_focusView_scrollX_byFolderTableId(act, scrollX);
			});
		}

		MainAct.mAct.invalidateOptionsMenu();
		//todo For first folder, first page: tab is not seen
	}

	/* 
	 * Insert Page to Leftmost
	 * 
	 */
	private static void insertPage_leftmost(final AppCompatActivity act, int newTabId, String tabName)
	{
		DB_folder dbFolder = new DB_folder(act,Pref.getPref_focusView_folder_tableId(act));
		
		
	    // insert tab name
		int style = Util.getNewPageStyle(act);
		dbFolder.insertPage(DB_folder.getFocusFolder_tableName(),tabName, newTabId, style,true );
		
		// insert table for new tab
		dbFolder.insertPageTable(dbFolder,DB_folder.getFocusFolder_tableId(),newTabId, true);

		//change to leftmost tab Id
		int tabTotalCount = dbFolder.getPagesCount(true);
		for(int i=0;i <(tabTotalCount-1);i++)
		{
			int tabIndex = tabTotalCount -1 -i ;
			swapPage(tabIndex,tabIndex-1);
			updateFinalPageViewed(act);
		}
		
	    // set scroll X
		final int scrollX = 0; // leftmost

		// commit: scroll X
        FolderUi.startTabsHostRun();

        if(TabsHost.mTabLayout != null){
            TabsHost.mTabLayout.post(new Runnable() {
                @Override
                public void run() {
                    System.out.println("PageUi / _insertPage_leftmost / _Runnable / scrollX = " + scrollX);
                    TabsHost.mTabLayout.scrollTo(scrollX, 0);
//                    Pref.setPref_focusView_scrollX_byFolderTableId(act, scrollX);
                }
            });
        }
		
		// update highlight tab
		if(MainAct.mPlaying_folderPos == FolderUi.getFocus_folderPos())
			MainAct.mPlaying_pagePos++;

		MainAct.mAct.invalidateOptionsMenu();
		TabsHost.setFocus_tabPos(0);
	}
	
	
	/*
	 * Update Final page which was focus view
	 * 
	 */
	protected static void updateFinalPageViewed(AppCompatActivity act)
	{
	    // get final viewed table Id
	    int tableId = Pref.getPref_focusView_page_tableId(act);
		DB_page.setFocusPage_tableId(tableId);
	
		DB_folder dbFolder = new DB_folder(act,Pref.getPref_focusView_folder_tableId(act));
		dbFolder.open();

		// get final view tab index of focus
		for(int i = 0; i<dbFolder.getPagesCount(false); i++)
		{
			if(tableId == dbFolder.getPageTableId(i, false))
				TabsHost.setFocus_tabPos(i);
			
	    	if(	dbFolder.getPageId(i, false)== TabsHost.getFirstPos_pageId())
	    		Pref.setPref_focusView_page_tableId(act, dbFolder.getPageTableId(i, false) );
		}
		dbFolder.close();
	}

    public static boolean isAudioPlayingPage()
    {
	    return ( (MainAct.mPlaying_pageTableId == TabsHost.mFocusPageTableId)&&//mNow_pageTableId) &&
			     (MainAct.mPlaying_pagePos == TabsHost.getFocus_tabPos()) &&
	     	     (MainAct.mPlaying_folderPos == FolderUi.getFocus_folderPos())  );
    }

}
