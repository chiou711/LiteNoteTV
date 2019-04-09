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

package com.cw.litenotetv.note_add;

import java.io.File;

import com.cw.litenotetv.page.Page_recycler;
import com.cw.litenotetv.R;
import com.cw.litenotetv.db.DB_page;
import com.cw.litenotetv.tabs.TabsHost;
import com.cw.litenotetv.util.Util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class Note_addReadyVideo extends Activity {

    Long rowId;
    TextView progress;
	private DB_page dB_page;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        System.out.println("Note_addOkVideo / onCreate");

		setContentView(R.layout.note_add_prepare);

        // get row Id from saved instance
        rowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(DB_page.KEY_NOTE_ID);
		dB_page = new DB_page(this, TabsHost.getCurrentPageTableId());

		// at the first beginning
        if(savedInstanceState == null)
        	addPicture();
        
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
    	super.onRestoreInstanceState(savedInstanceState);
    }

    // for Rotate screen
    @Override
    protected void onPause() {
    	System.out.println("Note_addOkVideo / onPause");
        super.onPause();
    }

    // for Add Ok picture (stage 2)
    // for Rotate screen (stage 2)
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
   	 	System.out.println("Note_addOkVideo / onSaveInstanceState");
        outState.putSerializable(DB_page.KEY_NOTE_ID, rowId);
    }
    
    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
    }
    
    void addPicture()
    {
	    startActivityForResult(Util.chooseMediaIntentByType(Note_addReadyVideo.this,"video/*"),
	    					   Util.CHOOSER_SET_PICTURE);        
    }
    
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) 
	{
		System.out.println("Note_addReadyVideo / onActivityResult");
		if (resultCode == Activity.RESULT_OK)
		{
            setContentView(R.layout.note_add_prepare);
            progress = findViewById(R.id.add_audio_progress);//must add this, otherwise text view is not updated

			// for ready picture
			if(requestCode == Util.CHOOSER_SET_PICTURE)
			{
				Uri selectedUri = imageReturnedIntent.getData(); 
				String authority = selectedUri.getAuthority();
				// SAF support, take persistent Uri permission
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
				{
			    	int takeFlags = imageReturnedIntent.getFlags()
			                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
			                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

					// add for solving inspection error
					takeFlags |= Intent.FLAG_GRANT_READ_URI_PERMISSION;

			    	// Check for the freshest data.
			    	if(authority.equalsIgnoreCase("com.google.android.apps.docs.storage") )
			    	{
			    		getContentResolver().takePersistableUriPermission(selectedUri, takeFlags);
			    	}
				}				
				
				String scheme = selectedUri.getScheme();
				// check option of Add multiple
				String option = getIntent().getExtras().getString("EXTRA_ADD_EXIST", "single_to_bottom");
     			
				// add single file
				if(option.equalsIgnoreCase("single_to_top") || 
           		   option.equalsIgnoreCase("single_to_bottom")	)
				{
					String uriStr = selectedUri.toString();
		  		    rowId = null; // set null for Insert
		        	rowId = savePictureStateInDB(rowId,uriStr);
		        	
		        	if( (dB_page.getNotesCount(true) > 0) &&
		        		option.equalsIgnoreCase("single_to_top"))
		        	{
		        		Page_recycler.swap(Page_recycler.mDb_page);
		        	}
		        	
		        	if(!Util.isEmptyString(uriStr))	
		        	{
		                String name = Util.getDisplayNameByUriString(uriStr, this);
		        		Util.showSavedFileToast(name,this);
		        	}
				}
				// add multiple files in the selected file's directory
				else if((option.equalsIgnoreCase("directory_to_top") || 
						 option.equalsIgnoreCase("directory_to_bottom")) &&
						 (scheme.equalsIgnoreCase("file") ||
						  scheme.equalsIgnoreCase("content") )              )
				{
					String realPath = Util.getLocalRealPathByUri(this, selectedUri);
					if(realPath != null)
					{
						// get file name
						File file = new File("file://".concat(realPath));
						String fileName = file.getName();
						
						// get directory
						String dirStr = realPath.replace(fileName, "");
						File dir = new File(dirStr);
						
						// get Urls array
						String[] urlsArray = Util.getUrlsByFiles(dir.listFiles(),Util.VIDEO);
						if(urlsArray == null)
						{
							Toast.makeText(this,"No file is found",Toast.LENGTH_SHORT).show();
							finish();
						}
                        else
                        {
                            // show Start
                            Toast.makeText(this, R.string.add_new_start, Toast.LENGTH_SHORT).show();
                        }

						int i= 1;
						int total=0;
						
						for(int cnt = 0; cnt < urlsArray.length; cnt++)
						{
							if(!Util.isEmptyString(urlsArray[cnt]))
								total++;
						}
						
						// note: the order add insert items depends on file manager 
						for(String urlStr:urlsArray)
						{
							System.out.println("urlStr = " + urlStr);
				  		    rowId = null; // set null for Insert
				  		    if(!Util.isEmptyString(urlStr))
				  		    	rowId = savePictureStateInDB(rowId,urlStr);
				        	
				        	if( (dB_page.getNotesCount(true) > 0) &&
	  		        			option.equalsIgnoreCase("directory_to_top") ) 
				        	{
				        		Page_recycler.swap(Page_recycler.mDb_page);
				        	}
				    		
				        	// avoid showing empty toast
				        	if(!Util.isEmptyString(urlStr))
				        	{
				                String name = Util.getDisplayNameByUriString(urlStr, Note_addReadyVideo.this);
				                name = i + "/" + total + ": " + name;
//				        		Util.showSavedFileToast(name,this);
                                progress.append("\r\n"+name);
				        	}
				        	i++;
						}

                        // show Stop
                        Toast.makeText(this,R.string.add_new_stop,Toast.LENGTH_SHORT).show();
					}
					else
					{
						Toast.makeText(this,
								R.string.add_new_file_error,
								Toast.LENGTH_LONG)
								.show();					
					}	
				}
				
//				addPicture();
				finish();
			}
		} 
		else if (resultCode == RESULT_CANCELED)
		{
	        // hide action bar
			if(getActionBar() != null)
				getActionBar().hide();

			// set background to transparent
			getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
			
			Toast.makeText(Note_addReadyVideo.this, R.string.note_cancel_add_new, Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED, getIntent());
            finish();
            return; // must add this
		}
	}

	Long savePictureStateInDB(Long rowId, String pictureUri)
	{
		if (rowId == null) // for Add new
		{
			if( !Util.isEmptyString(pictureUri))
			{
				// insert
				String name = Util.getDisplayNameByUriString(pictureUri, this);
				System.out.println("Note_addReadyVideo / _savePictureStateInDB / insert");
				rowId = dB_page.insertNote(name, pictureUri, "", "", "", "", 1, (long) 0);// add new note, get return row Id
			}
		}
		return rowId;
	}
}
