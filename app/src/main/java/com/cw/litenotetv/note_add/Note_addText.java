/*
 * Copyright (C) 2019 CW Chiu
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

package com.cw.litenotetv.note_add;

import com.cw.litenotetv.db.DB_folder;
import com.cw.litenotetv.main.MainAct;
import com.cw.litenotetv.page.Page_recycler;
import com.cw.litenotetv.R;
import com.cw.litenotetv.db.DB_page;
import com.cw.litenotetv.tabs.TabsHost;
import com.cw.litenotetv.util.ColorSet;
import com.cw.litenotetv.util.Util;
import com.cw.litenotetv.util.preferences.Pref;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


public class Note_addText extends AppCompatActivity {

	DB_page dB_page;
    static Long rowId;
    boolean enSaveDb = true;
	static final int ADD_TEXT_NOTE = R.id.ADD_TEXT_NOTE;
	EditText titleEditText;
	EditText bodyEditText;
	EditText linkEditText;
	Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	    System.out.println("Note_addText / _onCreate");

        // get row Id from saved instance
        rowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(DB_page.KEY_NOTE_ID);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
	    System.out.println("Note_addText / _onResume");

	    setContentView(R.layout.note_add_new_text);

	    Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
	    setSupportActionBar(mToolbar);
	    if (getSupportActionBar() != null) {
		    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	    }

	    setTitle(R.string.add_new_note_title);// set title

	    dB_page = new DB_page(this, TabsHost.getCurrentPageTableId());

	    UI_init_text();

	    if(rowId != null)
	        populateFields_text(rowId);
    }


	// for Add new note
	// for Rotate screen
	@Override
	protected void onPause() {
		System.out.println("Note_addText / _onPause");
		super.onPause();
		rowId = saveStateInDB(rowId, enSaveDb,"", "", "");
		System.out.println("Note_addText / _onPause / rowId = " + rowId);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.out.println("Note_addText / _onDestroy");
	}

	// for Rotate screen
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		System.out.println("Note_addText / _onSaveInstanceState");
		outState.putSerializable(DB_page.KEY_NOTE_ID, rowId);
	}

	@Override
	public void onBackPressed()
	{
		stopEdit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.add_note_menu, menu);
		mMenu = menu;
		mMenu.findItem(R.id.ADD_TEXT_NOTE).setIcon(R.drawable.ic_input_add);

		titleEditText.addTextChangedListener(setTextWatcher());
		bodyEditText.addTextChangedListener(setTextWatcher());
		linkEditText.addTextChangedListener(setTextWatcher());

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// called after onCreateOptionsMenu
		if(!isTextAdded())
			mMenu.findItem(R.id.ADD_TEXT_NOTE).setVisible(false);
		else
			mMenu.findItem(R.id.ADD_TEXT_NOTE).setVisible(true);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:
				stopEdit();
				return true;

			case ADD_TEXT_NOTE:
				//add new note again
				if(isTextAdded())
				{
					enSaveDb = true;
					rowId = saveStateInDB(rowId, enSaveDb,"", "", "");

					if( getIntent().getExtras().getString("extra_ADD_NEW_TO_TOP", "false").equalsIgnoreCase("true") &&
							(Page_recycler.mDb_page.getNotesCount(true) > 0) )
						Page_recycler.swap(Page_recycler.mDb_page);

					Toast.makeText(Note_addText.this, getString(R.string.toast_saved) +" + 1", Toast.LENGTH_SHORT).show();

					UI_init_text();
					rowId = null;
					populateFields_text(rowId);
				}
				return true;
		}
		return super.onOptionsItemSelected(item);
	}


	TextWatcher setTextWatcher()
	{
		return new TextWatcher(){
			public void afterTextChanged(Editable s)
			{
				if(!isTextAdded())
					mMenu.findItem(R.id.ADD_TEXT_NOTE).setVisible(false);
				else
					mMenu.findItem(R.id.ADD_TEXT_NOTE).setVisible(true);
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			public void onTextChanged(CharSequence s, int start, int before, int count){}
		};
	}

	// confirmation to update change or not
	void confirmUpdateChangeDlg()
	{
		getIntent().putExtra("NOTE_ADDED","edited");

		AlertDialog.Builder builder = new AlertDialog.Builder(Note_addText.this);
		builder.setTitle(R.string.confirm_dialog_title)
				.setMessage(R.string.add_new_note_confirm_save)
				.setPositiveButton(R.string.confirm_dialog_button_yes, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						enSaveDb = true;
						setResult(RESULT_OK, getIntent());
						finish();
					}})
				.setNeutralButton(R.string.btn_Cancel, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which)
					{   // do nothing
					}})
				.setNegativeButton(R.string.confirm_dialog_button_no, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						deleteNote(rowId);
						enSaveDb = false;
						setResult(RESULT_CANCELED, getIntent());
						finish();
					}})
				.show();
	}


	boolean isTextAdded()
	{
		boolean bEdit = false;
		String curTitle = titleEditText.getText().toString();
		String curBody = bodyEditText.getText().toString();
		String curLink = linkEditText.getText().toString();

		if(!Util.isEmptyString(curTitle)||
				!Util.isEmptyString(curBody) ||
				!Util.isEmptyString(curLink)   )
		{
			bEdit = true;
		}

		return bEdit;
	}

	void UI_init_text()
	{
		titleEditText = (EditText) findViewById(R.id.edit_title);
		bodyEditText = (EditText) findViewById(R.id.edit_body);
		linkEditText = (EditText) findViewById(R.id.edit_link);

		int focusFolder_tableId = Pref.getPref_focusView_folder_tableId(this);
		DB_folder db = new DB_folder(MainAct.mAct, focusFolder_tableId);
		int style = db.getPageStyle(TabsHost.getFocus_tabPos(), true);

		LinearLayout block = (LinearLayout) findViewById(R.id.edit_title_block);
		if(block != null)
			block.setBackgroundColor(ColorSet.mBG_ColorArray[style]);

		//set title color
		titleEditText.setTextColor(ColorSet.mText_ColorArray[style]);
		titleEditText.setBackgroundColor(ColorSet.mBG_ColorArray[style]);

		//set body color
		bodyEditText.setTextColor(ColorSet.mText_ColorArray[style]);
		bodyEditText.setBackgroundColor(ColorSet.mBG_ColorArray[style]);

		//set link color
		linkEditText.setTextColor(ColorSet.mText_ColorArray[style]);
		linkEditText.setBackgroundColor(ColorSet.mBG_ColorArray[style]);
	}

	// populate text fields
	void populateFields_text(Long rowId)
	{
		if (rowId != null) {
			// title
			String strTitleEdit = dB_page.getNoteTitle_byId(rowId);
			titleEditText.setText(strTitleEdit);
			titleEditText.setSelection(strTitleEdit.length());

			// body
			String strBodyEdit = dB_page.getNoteBody_byId(rowId);
			bodyEditText.setText(strBodyEdit);
			bodyEditText.setSelection(strBodyEdit.length());

			// link
			String strLinkEdit = dB_page.getNoteLinkUri_byId(rowId);
			linkEditText.setText(strLinkEdit);
			linkEditText.setSelection(strLinkEdit.length());
		}
		else
		{
			// renew title
			String strBlank = "";
			titleEditText.setText(strBlank);
			titleEditText.setSelection(strBlank.length());
			titleEditText.requestFocus();

			// renew body
			bodyEditText.setText(strBlank);
			bodyEditText.setSelection(strBlank.length());

			// renew link
			linkEditText.setText(strBlank);
			linkEditText.setSelection(strBlank.length());
		}
	}

    void stopEdit()
    {
	    if(isTextAdded())
		    confirmUpdateChangeDlg();
	    else
	    {
		    deleteNote(rowId);
		    enSaveDb = false;
		    NavUtils.navigateUpFromSameTask(this);
	    }
    }

	void deleteNote(Long rowId)
	{
		System.out.println("Note_addText / _deleteNote");
		// for Add new note (noteId is null first), but decide to cancel
		if(rowId != null)
			dB_page.deleteNote(rowId,true);
	}


	Long saveStateInDB(Long rowId,boolean enSaveDb, String pictureUri, String audioUri, String drawingUri)
	{
		String linkUri = "";
		if(linkEditText != null)
			linkUri = linkEditText.getText().toString();
		String title = titleEditText.getText().toString();
		String body = bodyEditText.getText().toString();

		if(enSaveDb)
		{
			if (rowId == null) // for Add new
			{
				if( (!Util.isEmptyString(title)) ||
						(!Util.isEmptyString(body)) ||
						(!Util.isEmptyString(pictureUri)) ||
						(!Util.isEmptyString(audioUri)) ||
						(!Util.isEmptyString(linkUri))            )
				{
					// insert
					System.out.println("Note_addText / _saveStateInDB / insert");
					rowId = dB_page.insertNote(title, pictureUri, audioUri, drawingUri, linkUri, body, 0, (long) 0);// add new note, get return row Id
				}
			}
			else if ( Util.isEmptyString(title) &&
					Util.isEmptyString(body) &&
					Util.isEmptyString(pictureUri) &&
					Util.isEmptyString(drawingUri) &&
					Util.isEmptyString(audioUri) &&
					Util.isEmptyString(linkUri)         )
			{
				// delete
				System.out.println("Note_edit_ui / _saveStateInDB / delete");
				deleteNote(rowId);
				rowId = null;
			}
		}
		return rowId;
	}

}
