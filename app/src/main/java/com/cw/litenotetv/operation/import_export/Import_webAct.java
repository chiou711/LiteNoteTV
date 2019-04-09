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

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.cw.litenotetv.R;
import com.cw.litenotetv.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Import_webAct extends AppCompatActivity
{
    String content=null;
    WebView webView;
    Button btn_import;
    // TODO Website path customization: input path, website rule for Import
//    String homeUrl = "http://litenoteapp.blogspot.tw/2017/09/xml-link.html"; // Google changed this address
    String homeUrl = "http://litenoteapp.blogspot.com/2017/09/xml-link.html";// current

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)//API23
        {
            // check permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED)
            {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                                                  new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                               Manifest.permission.READ_EXTERNAL_STORAGE},
                                                  Util.PERMISSIONS_REQUEST_STORAGE_WITH_DEFAULT_CONTENT_NO);
            }
            else
                doCreate();
        }
        else
            doCreate();
    }

    void doCreate()
    {
        setContentView(R.layout.import_web);

        // web view
        webView = (WebView)findViewById(R.id.webView);

        // cancel button
        Button btn_cancel = (Button) findViewById(R.id.import_web_cancel);
        btn_cancel.setFocusableInTouchMode(true);
        btn_cancel.requestFocus();
        btn_cancel.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                if (webView.canGoBack()) {
                    webView.goBack();
                    content = null;
                }
                else
                    finish();
            }
        });

        // import button
        btn_import = (Button) findViewById(R.id.import_web_import);
        btn_import.setFocusableInTouchMode(true);
        btn_import.setVisibility(View.INVISIBLE);
        btn_import.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                setResult(RESULT_OK);

                // import
                // save text in a file
                String dirName = "Download";
                String fileName = "temp.xml";
                String dirPath = Environment.getExternalStorageDirectory().toString() +
                        "/" +
                        dirName;
                File file = new File(dirPath, fileName);

                try
                {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    FileOutputStream fOut = new FileOutputStream(file);
                    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                    if(content != null) {
                        content = content.replaceAll("(?m)^[ \t]*\r?\n", "");
                    }
                    myOutWriter.append(content);
                    myOutWriter.close();

                    fOut.flush();
                    fOut.close();
                }
                catch (IOException e)
                {
                    Log.e("Exception", "File write failed: " + e.toString());
                }

                // import file content to DB
                Import_webAct_asyncTask task = new Import_webAct_asyncTask(Import_webAct.this,file.getPath());
                task.enableSaveDB(true);// view
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

        });

        webView.getSettings().setJavaScriptEnabled(true);

        // create instance
        final ImportInterface import_interface = new ImportInterface(webView);

        // load web content
        webView.addJavascriptInterface(import_interface, "INTERFACE");
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                btn_import.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
                System.out.println("Import_webAct / _setWebViewClient / url = " + url);
                System.out.println("Import_webAct / _setWebViewClient / homeUrl = " + homeUrl);
                view.loadUrl("javascript:window.INTERFACE.processContent(document.getElementsByTagName('body')[0].innerText);");
                if(!url.contains(homeUrl)) {
                    btn_import.setVisibility(View.VISIBLE);
                    btn_import.requestFocus();
                }
                else
                    btn_cancel.requestFocus();
            }

        });

        // show toast
        webView.addJavascriptInterface(import_interface, "LiteNote");

        // load content to web view
        webView.loadUrl(homeUrl);
    }

    // callback of granted permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        System.out.println("grantResults.length =" + grantResults.length);
        switch (requestCode)
        {
            case Util.PERMISSIONS_REQUEST_STORAGE_WITH_DEFAULT_CONTENT_NO:
            {
                // If request is cancelled, the result arrays are empty.
                if ( (grantResults.length > 0) &&
                     ( (grantResults[0] == PackageManager.PERMISSION_GRANTED) &&
                       (grantResults[1] == PackageManager.PERMISSION_GRANTED)    ))
                    doCreate();
                else
                    finish();
            }//case
        }//switch
    }

    @Override
    public void onBackPressed() {
        System.out.println("Import_webAct / _onBackPressed");
        // web view can go back
        if (webView.canGoBack()) {
            webView.goBack();
            content = null;
        }
        else
            super.onBackPressed();
    }

    /* An instance of this class will be registered as a JavaScript interface */
    class ImportInterface {

        WebView webView;
        public ImportInterface(WebView _webView)
        {
            webView = _webView;
        }

        @SuppressWarnings("unused")
        @android.webkit.JavascriptInterface
        public void processContent(final String _content)
        {
            webView.post(new Runnable()
            {
                public void run()
                {
                    content = _content;
                    System.out.println("Import_webAct.content = "+ content );
                }
            });
        }

        // note: this is used by home URL web page
        @android.webkit.JavascriptInterface
        public void showToast(String toastText) {
            Toast.makeText(Import_webAct.this, toastText, Toast.LENGTH_LONG).show();
        }


    }
}
