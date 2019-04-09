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

import java.io.File;
import java.io.IOException;
import java.util.Date;

import com.cw.litenotetv.page.Page_recycler;
import com.cw.litenotetv.R;
import com.cw.litenotetv.db.DB_page;
import com.cw.litenotetv.tabs.TabsHost;
import com.cw.litenotetv.util.image.UtilImage;
import com.cw.litenotetv.util.Util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import static android.os.Build.VERSION_CODES.M;

/*
 * Note: 
 * 	cameraImageUri: used to show in confirmation Continue dialog
 *  	Two conditions:
 *  	1. is got after taking picture
 *  	2. is kept during rotation
 * 
 *  UtilImage.bShowExpandedImage: used to control DB saving state
 * 
 */
public class Note_addCameraImage extends Activity {

    Long noteId;
    String cameraImageUri;
	String imageUriInDB;
	private DB_page dB_page;
    boolean bUseCameraImage;
	final int TAKE_IMAGE_ACT = 1;
	private Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.out.println("Note_addCameraImage / onCreate");
        if(Build.VERSION.SDK_INT >= M)//api23
        {
            // check permission
            int permissionCamera = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            if( permissionCamera != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,
                                                   new String[]{ Manifest.permission.CAMERA  },
                                                   Util.PERMISSIONS_REQUEST_CAMERA);
            }
            else
                doCreate(savedInstanceState);
        }
        else
            doCreate(savedInstanceState);

    }

	// callback of granted permission
	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
	{
		System.out.println("Note_addCameraImage / _onRequestPermissionsResult / grantResults.length =" + grantResults.length);
		switch (requestCode)
		{
			case Util.PERMISSIONS_REQUEST_CAMERA:
			{
                // If request is cancelled, the result arrays are empty.
                if ( (grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED) )
                    doCreate(null);
                else
                    finish();
			}
			break;
		}//switch
	}

    void doCreate(Bundle savedInstanceState)
	{
		imageUriInDB = "";
		cameraImageUri = "";
		bUseCameraImage = false;

		// get row Id from saved instance
		noteId = (savedInstanceState == null) ? null :
				(Long) savedInstanceState.getSerializable(DB_page.KEY_NOTE_ID);

		// get picture Uri in DB if instance is not null
        dB_page = new DB_page(this, TabsHost.getCurrentPageTableId());
		if(savedInstanceState != null)
		{
			System.out.println("Note_addCameraImage / onCreate / noteId =  " + noteId);
			if(noteId != null)
				imageUriInDB = dB_page.getNotePictureUri_byId(noteId);
		}

		// at the first beginning
		if(savedInstanceState == null)
		{
			takeImageWithName();
			if((UtilImage.mExpandedImageView != null) &&
					(UtilImage.mExpandedImageView.getVisibility() == View.VISIBLE) &&
					(UtilImage.bShowExpandedImage == true))
			{
				UtilImage.closeExpandedImage();
			}
		}
	}

    @Override
    protected void onResume() {
        super.onResume();
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
    	super.onRestoreInstanceState(savedInstanceState);
    	if(savedInstanceState.getBoolean("UseCameraImage"))
    		bUseCameraImage = true;
    	else
    		bUseCameraImage = false;
    	
    	cameraImageUri = savedInstanceState.getString("showCameraImageUri");
    	
    	if(savedInstanceState.getBoolean("ShowConfirmContinueDialog"))
    	{
    		showContinueConfirmationDialog();
    		System.out.println("showContinueDialog again");
    	}
    	
    }

    // for Add new picture (stage 1)
    // for Rotate screen (stage 2)
    @Override
    protected void onPause() {
    	System.out.println("Note_addCameraImage / onPause");
        super.onPause();
        
        if( !UtilImage.bShowExpandedImage )
        {
        	System.out.println("Note_addCameraImage / onPause / keep pictureUriInDB");
        	noteId = savePictureStateInDB(noteId, imageUriInDB);
        }
    }

    // for Add new picture (stage 2)
    // for Rotate screen (stage 2)
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
   	 	System.out.println("Note_addCameraImage / onSaveInstanceState");
   	 	
        if(bUseCameraImage)
        {
        	outState.putBoolean("UseCameraImage",true);
        	outState.putString("showCameraImageUri", cameraImageUri);
        }
        else
        {
        	outState.putBoolean("UseCameraImage",false);
        	outState.putString("showCameraImageUri", "");
        }
        
        // if confirmation dialog still shows?
        if(UtilImage.bShowExpandedImage == true)
        {
        	outState.putBoolean("ShowConfirmContinueDialog",true);
        }
        else
        	outState.putBoolean("ShowConfirmContinueDialog",false);
        
        outState.putSerializable(DB_page.KEY_NOTE_ID, noteId);
    }
    
    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
    	if((UtilImage.mExpandedImageView != null) &&
           (UtilImage.mExpandedImageView.getVisibility() == View.VISIBLE) &&
           (UtilImage.bShowExpandedImage == true))
    	{
	    	UtilImage.closeExpandedImage();
    	}
	    finish();
    }
    
    
    // Create temporary image file
    private File createTempImageFile() throws IOException 
    {
		// First, create a sub-directory named App name under DCIM if needed 
        File imageDir = Util.getPicturesDir(this);
		if(!imageDir.isDirectory())
			imageDir.mkdir();        
		
		// note: createTempFile will generate random number and a 0 bit file size instance first
        // Create an image file name
//      String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
		String imageFileName = "IMG_" + Util.getCurrentTimeString();
		File imageFile = new File(imageDir /* directory */,
        						  imageFileName  /* prefix */ +
        						  ".jpg" 		 /* suffix */);
        
        System.out.println("Note_addCameraImage / _createTempImageFile / imageFile path = " + imageFile.getPath());
        return imageFile;
    }
    
    private void takeImageWithName() 
    {
        Intent takeImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takeImageIntent.resolveActivity(getPackageManager()) != null) 
        {
            // Create temporary image File where the photo will save in
            File tempFile = null;
            try 
            {
                tempFile = createTempImageFile();
            } 
            catch (IOException ex)
            {
                // Error occurred while creating the File
            }
            
            // Continue only if the File was successfully created
            if (tempFile != null) 
            {
            	imageUri = Uri.fromFile(tempFile); // so far, file size is 0
                takeImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); // appoint Uri for captured image
                imageUriInDB = imageUri.toString();
                startActivityForResult(takeImageIntent, TAKE_IMAGE_ACT);
            }
        }
    }   
    
    
    // On Activity Result
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) 
	{
		System.out.println("Note_addCameraImage / onActivityResult");
		if (requestCode == TAKE_IMAGE_ACT)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				// disable Rotate to avoid leak window
//				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

				// Note: 
				// 1 .for Google Camera App, imageReturnedIntent is null
				//    its default path is 
				//    a. open App by cell phone menu: /sdcard/DCIM/Camera
				//    b. open App by intent: intent appoints
				// 2. for Camera360 App, get data from imageReturnedIntent is null
				//    and its default path is /sdcard/DCIM/Camera (??? has duplication, how to solve?)
				
				// check returned intent
				if(imageReturnedIntent == null)
				{
					System.out.println("returned intent is null"); // Google Camera case
				}
				else
				{
					Uri imageUri = imageReturnedIntent.getData();
					
					if(imageUri == null)
						System.out.println("-- imageUri = " + null); // Camera360 case
					else
						System.out.println("-- imageUri = " + imageUri.toString());
				}
				
				// The following source code can be used 
				// if we know the file name is got from returned intent
				// note: unfortunately, this is not working for Google Camera,
				//       so using this way will get null after taking image
/*				
				// get Uri from returned intent, scheme is content
				// example: content://media/external/images/media/43983
				Uri picUri = imageReturnedIntent.getData();
				
				if(picUri == null)
					System.out.println("-- picUri = " + null);
				else
					System.out.println("-- picUri = " + picUri.toString());
					
				// get file path and add prefix (file://)
				String realPath = Util.getRealPathByUri(this, picUri);
				System.out.println("--- realPath = " + realPath);
				if("content".equalsIgnoreCase(picUri.getScheme()))
				{
					// example: file:///storage/ext_sd/DCIM/100MEDIA/IMAG0146.jpg
					// path of default camera App: 100MEDIA for hTC , 100ANDRO for Sony
					pictureUriInDB = "file://".concat(realPath);
					System.out.println("---- pictureUriInDB = " + pictureUriInDB);
				}
				
				
				// get picture name
				File pic = new File(pictureUriInDB);
				String picName = pic.getName();
//				System.out.println("picName = " + picName);
				
				// get directory by removing picture name
				String picDir = realPath.replace(picName, "");
//				System.out.println("picDir = " + picDir);
				
//				// get current picture directory
//				SharedPreferences pref_takePicture;
//        		pref_takePicture = getSharedPreferences("takePicutre", 0);	
//        		String currentPictureDir = pref_takePicture.getString("KEY_SET_PICTURE_DIR","unknown");
//        		
//        		// update picture directory if needed
//        		if(	!picDir.equalsIgnoreCase(currentPictureDir))		   
//        				pref_takePicture.edit().putString("KEY_SET_PICTURE_DIR",picDir).apply();
*/

				SharedPreferences pref_takeImage;
        		pref_takeImage = getSharedPreferences("takeImage", 0);
        		
				if( !UtilImage.bShowExpandedImage )
		        	noteId = savePictureStateInDB(noteId,imageUriInDB);
				
				// set for Rotate any times
		        if(noteId != null)
		        {
		        	cameraImageUri = dB_page.getNotePictureUri_byId(noteId);
		        }
	            
    			if( getIntent().getExtras().getString("extra_ADD_NEW_TO_TOP", "false").equalsIgnoreCase("true") &&
    				(dB_page.getNotesCount(true) > 0) )
		               Page_recycler.swap(Page_recycler.mDb_page);
    			
    			Toast.makeText(this, R.string.toast_saved , Toast.LENGTH_SHORT).show();

				// check and delete duplicated image file in 100ANDRO (Sony) / 100MEDIA (hTC)
//				int lastContentId = getLastCapturedImageId(this);
				handleDuplicatedImage(this);
    			
        		// show confirm Continue dialog
	        	if(pref_takeImage.getString("KEY_SHOW_CONFIRMATION_DIALOG","no").equalsIgnoreCase("yes"))
	        	{
	    			 bUseCameraImage = true; 
		            // set Continue Taking Picture dialog
	        		showContinueConfirmationDialog();
	        	}
	        	else
	        	// not show confirm Continue dialog
	        	{
	    			bUseCameraImage = false; 
	        		
	        		// take image without confirmation dialog 
		  		    noteId = null; // set null for Insert
		  		    takeImageWithName();
	        	}

	            // enable Rotate 
//	            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			} 
			else if (resultCode == RESULT_CANCELED)
			{
				// hide action bar
				if(getActionBar() != null)
					getActionBar().hide();
				
				// set action bar to transparent
//				getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
//				getActionBar().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//				setTitle("");
				
				// disable content view
//				findViewById(android.R.id.content).setVisibility(View.INVISIBLE);

				// set background to transparent
				getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
				
				Toast.makeText(this, R.string.note_cancel_add_new, Toast.LENGTH_LONG).show();
				
				// delete the temporary note in DB
				if(noteId != null)
					dB_page.deleteNote(noteId,true);

                // When auto time out of taking picture App happens, 
            	// Note_addCameraImage activity will start from onCreate,
                // at this case, imageUri is null
                if(imageUri != null)
                {
	           		File tempFile = new File(imageUri.getPath());
	        		if(tempFile.isFile())
	        		{
	                    // delete 0 bit temporary file
	        			tempFile.delete();
	        			System.out.println("temp 0 bit file is deleted");
	        		}
                }
                finish();
                return; // must add this
			}
			
		}
	}

	public void handleDuplicatedImage(Context context)
	{
	    /*
	     * Checking for duplicated images
	     * This is necessary because some camera App implementation not only save image where 
	     * you want them to save, but also in their App default location.
	     */
		int lastContentId = getLastCapturedImageId(context);
	    if (lastContentId == 0)
	        return;
	    
	    Cursor imageCursor = UtilImage.getImageContentCursorByContentId(context,lastContentId);
	    
	    // New file: file1
		String path1 = null;
	    File file1 = null;
	    long dateTaken = 0;
	    if (imageCursor.getCount() > 0) 
	    {
	        imageCursor.moveToFirst(); // newest one
	        path1 = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
	        dateTaken = imageCursor.getLong(imageCursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
	        System.out.println("date taken = " + Util.getTimeString(dateTaken) );
	        System.out.println("last Id point to file path: " + path1);
	        file1 = new File(path1);
	    }
	    else
	    	System.out.println("imageCursor.getCount() = " + imageCursor.getCount() ); 	

	    System.out.println("- file1 size = " + file1.length());
	    System.out.println("- file1 path = " + file1.getPath());
	    imageCursor.close();

	    // Last file: file2
	    Uri uri = Uri.parse(imageUriInDB);
	    File file2 = new File(uri.getPath());
	    System.out.println("- file2 size = " + file2.length());
	    System.out.println("- file2 path = " + file2.getPath());
	    
	    boolean isSameSize = false;
	    if(file1.length() == file2.length())
	    {
	    	System.out.println("-- file lenghts are the same");
	    	isSameSize = true;
	    }
	    else
	    	System.out.println("-- files are different");
	    
	    boolean isSameFilePath = false;
	    if(file1.getPath().equalsIgnoreCase( file2.getPath()))
	    {
	    	System.out.println("-- file paths are the same");
	    	isSameFilePath = true;
	    }
	    else
	    	System.out.println("-- file paths are different");
	    
	    // Check time for avoiding Delete existing file, since lastContentId could points to 
	    // wrong file by experiment
    	Date now = new Date(); 
        System.out.println("current time = " + Util.getTimeString(now.getTime()) );
	    long elapsedTime = Math.abs(dateTaken - now.getTime() );

	    // check if there is a duplicated file
        if( isSameSize && !isSameFilePath && (file1 != null) && (elapsedTime < 10000) ) // tolerance 10 seconds
	    {
    		// delete file
        	// for ext_sd file, it can not be deleted after Kitkat, so this will be false
	        boolean bDeleteFile1 = file1.delete(); 

	        // check if default image file is deleted
	        if (bDeleteFile1) // for Before Kitkat
	        {
	        	System.out.println("deleted file path1 = " + path1);

	        	// delete 
	        	int deletedRows = context.getContentResolver()
	        							 .delete( MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
	        									  MediaStore.Images.ImageColumns.DATA
					        	                    + "='"
					        	                    + path1 + "'",
					        	                  null);	        	  
        	  
	        	System.out.println("deleted thumbnail 1 / deletedRows = " + deletedRows);	  
	       }
	       else // for After Kitkat
	       {
	    	   	boolean bDeleteFile2 = file2.delete(); 
	    	   	
	    	   	// check if self-naming file is deleted
	    	   	if (bDeleteFile2)
	    	   	{
	    	   		System.out.println("deleted file path2 = " + file2.getPath());
	    	   		String repPath =  file2.getPath();
	         	  
	    	   		// update new Uri to DB
	    	   		imageUriInDB = "file://" + Uri.parse(file1.getPath()).toString();
					if( !UtilImage.bShowExpandedImage )
			        	noteId = savePictureStateInDB(noteId,imageUriInDB);
					
					// set for Rotate any times
			        if(noteId != null)
			        {
			        	cameraImageUri = dB_page.getNotePictureUri_byId(noteId);
			        }
			        
	    	   		// delete
	    	   		int deletedRows = context.getContentResolver()
	    	   								 .delete( MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
	    	   										  MediaStore.Images.ImageColumns.DATA
	    	   										  + "='"
	    	   										  + repPath + "'",
	    	   										  null);	        	  
	         	  
	    	   		System.out.println("deleted thumbnail 2 / deletedRows = " + deletedRows);	  	    	   
	    	   	}
	       }
	    }
	}

	public static int getLastCapturedImageId(Context context)
	{
	    final String[] imageColumns = { MediaStore.Images.Media._ID };
	    final String imageOrderBy = MediaStore.Images.Media._ID+" DESC";
	    final String imageWhere = null;
	    final String[] imageArguments = null;
	    Cursor imageCursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
	    														imageColumns,
	    														imageWhere,
	    														imageArguments,
	    														imageOrderBy);
	    if(imageCursor.moveToFirst())
	    {
	        int id = imageCursor.getInt(imageCursor.getColumnIndex(MediaStore.Images.Media._ID));
	        imageCursor.close();
	        System.out.println("Note_addCameraImage / _getLastCapturedImageId / last captured image Id = " + id);
	        return id;
	    }else
	    {
	        return 0;
	    }
	}	
	
	// show Continue dialog
	void showContinueConfirmationDialog()
	{
        setContentView(R.layout.note_add_camera_image);
        setTitle(R.string.note_take_picture_continue_dlg_title); 
        
		// Continue button
        Button okButton = (Button) findViewById(R.id.note_add_new_picture_continue);
        okButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_camera, 0, 0, 0);
		// OK
        okButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
        		
            	// take image without confirmation dialog 
	  		    noteId = null; // set null for Insert
	  		    takeImageWithName();
	  		    UtilImage.bShowExpandedImage = false; // set for getting new row Id
            }
        });
        
        // cancel button
        Button cancelButton = (Button) findViewById(R.id.note_add_new_picture_cancel);
        cancelButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);
        // cancel
        cancelButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_CANCELED);
            	if((UtilImage.mExpandedImageView != null) &&
             	   (UtilImage.mExpandedImageView.getVisibility() == View.VISIBLE) &&
            	   (UtilImage.bShowExpandedImage == true))
            	{
        	    	UtilImage.closeExpandedImage();
            	}
	            finish();
            }
        });
        
        final String pictureUri = cameraImageUri;
        final ImageView imageView = (ImageView) findViewById(R.id.expanded_image_after_take);
        
	    	imageView.post(new Runnable() {
		        @Override
		        public void run() {
		        	try 
		        	{
						UtilImage.showImage(imageView, pictureUri , Note_addCameraImage.this);
					} 
		        	catch (IOException e) 
		        	{
						e.printStackTrace();
						System.out.println("show image error");
					}
		        } 
		    });
	}


	Long savePictureStateInDB(Long rowId, String pictureUri)
	{
		if (rowId == null) // for Add new
		{
			if(!Util.isEmptyString(pictureUri))
			{
				// insert
				String name = Util.getDisplayNameByUriString(pictureUri, this);
				System.out.println("Note_addCameraImage / _savePictureStateInDB / insert");
				rowId = dB_page.insertNote(name, pictureUri, "", "", "", "", 1, (long) 0);// add new note, get return row Id
			}
		}
		return rowId;
	}

}
