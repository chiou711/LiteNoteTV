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

package com.cw.litenotetv.operation.import_export;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cw.litenotetv.R;
import com.cw.litenotetv.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by cw on 2017/9/16.
 */
// Show progress progressBar
class Import_webAct_asyncTask extends AsyncTask<Void, Integer, Void> {

    private ProgressBar progressBar;
    private boolean enableSaveDB;
    private AppCompatActivity act;
    private File file;
    private View contentBlock;

    Import_webAct_asyncTask(AppCompatActivity _act, String _filePath)
    {
        act = _act;
        Util.lockOrientation(act);

        contentBlock = act.findViewById(R.id.contentBlock);
        contentBlock.setVisibility(View.GONE);

        progressBar = (ProgressBar) act.findViewById(R.id.import_progress);
        progressBar.setVisibility(View.VISIBLE);

        file = new File(_filePath);
    }

    void enableSaveDB(boolean enable)
    {
        enableSaveDB = enable;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (this.progressBar != null ){
            progressBar.setProgress(values[0]);
        }
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        insertSelectedFileContentToDB(enableSaveDB);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        if(enableSaveDB)
        {
            contentBlock.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

//				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            Util.unlockOrientation(act);
            Toast.makeText(act, R.string.toast_import_finished,Toast.LENGTH_SHORT).show();
        }
    }

    ParseXmlToDB importObject;
    private void insertSelectedFileContentToDB(boolean enableInsertDB)
    {
        FileInputStream fileInputStream = null;
        try
        {
            fileInputStream = new FileInputStream(file);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        // import data by HandleXmlByFile class
        if(fileInputStream != null) {
            importObject = new ParseXmlToDB(fileInputStream, act);
            importObject.enableInsertDB(enableInsertDB);
            importObject.handleXML();
            while (importObject.isParsing) ;
        }
    }
}

