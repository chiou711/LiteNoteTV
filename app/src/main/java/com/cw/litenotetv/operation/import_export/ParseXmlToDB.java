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

import java.io.FileInputStream;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.cw.litenotetv.db.DB_drawer;
import com.cw.litenotetv.main.MainAct;
import com.cw.litenotetv.db.DB_folder;
import com.cw.litenotetv.db.DB_page;
import com.cw.litenotetv.tabs.TabsHost;
import com.cw.litenotetv.util.Util;
import com.cw.litenotetv.util.preferences.Pref;

import android.content.Context;
import android.util.Xml;

public class ParseXmlToDB {

    private String pageName,title,body,picture,audio,link;
    private DB_folder mDb_folder;
    private DB_page mDb_page;

    private Context mContext;
   
    private FileInputStream fileInputStream = null;
    public static boolean isParsing;
    String fileBody = "";
    private String strSplitter;
    private boolean mEnableInsertDB = true;
    int folderTableId;

    ParseXmlToDB(FileInputStream fileInputStream, Context context)
    {
        mContext = context;
        this.fileInputStream = fileInputStream;

        folderTableId = Pref.getPref_focusView_folder_tableId(mContext);
        mDb_folder = new DB_folder(MainAct.mAct, folderTableId);

        mDb_page = new DB_page(MainAct.mAct,TabsHost.getCurrentPageTableId());

        isParsing = true;
    }

    public String getTitle()
    {
    return title;
    }

    public String getBody()
    {
    return body;
    }

    public String getPicture()
    {
    return picture;
    }

    public String getAudio()
    {
    return audio;
    }

    public String getPage()
    {
    return pageName;
    }

    public void parseXMLAndInsertDB(XmlPullParser myParser)
    {
        int event;
        String text=null;
        try
        {
            event = myParser.getEventType();
            boolean isEnd = false;
            while (event != XmlPullParser.END_DOCUMENT)
            {
                String name = myParser.getName(); //name: null, link, item, title, description
                System.out.println("ParseXmlToDB / _parseXMLAndInsertDB / name = " + name);
                System.out.println("ParseXmlToDB / _parseXMLAndInsertDB / event = " + event);
                switch (event)
                {
                    case XmlPullParser.START_TAG:
                    if(name.equals("note"))
                    {
                        strSplitter = "--- note ---";
                    }
                    break;

                    case XmlPullParser.TEXT:
                        text = myParser.getText();
                        System.out.println("ParseXmlToDB / _parseXMLAndInsertDB / text = " + text);
                    break;

                    case XmlPullParser.END_TAG:
                        if(name.equals("folder_name"))
                        {
                            String folderName = text.trim();
                            if(mEnableInsertDB)
                            {
                                // insert folder
                                DB_drawer dB_drawer = new DB_drawer(MainAct.mAct);
                                dB_drawer.insertFolder(folderTableId, folderName, true); // Note: must set false for DB creation stage
                                dB_drawer.insertFolderTable(folderTableId, true);

                                Pref.setPref_focusView_folder_tableId(MainAct.mAct,folderTableId);
                                DB_folder.setFocusFolder_tableId(folderTableId);
                                TabsHost.setLastPageTableId(0);

                                mDb_folder = new DB_folder(MainAct.mAct, folderTableId);
                                folderTableId++;
                            }
                            fileBody = fileBody.concat(Util.NEW_LINE + "*** " + "Folder:" + " " + folderName + " ***");
                        }
                        else if(name.equals("page_name"))
                        {
                            pageName = text.trim();
                            if(mEnableInsertDB)
                            {
                                int style = Util.getNewPageStyle(mContext);

                                // style is not set in XML file, so insert default style instead
                                mDb_folder.insertPage(DB_folder.getFocusFolder_tableName(),
                                                      pageName,
                                                      TabsHost.getLastPageTableId() + 1,
                                                      style ,
                                                      true);

                                // insert table for new tab
                                mDb_folder.insertPageTable(mDb_folder,DB_folder.getFocusFolder_tableId(), TabsHost.getLastPageTableId() + 1, true );
                                // update last tab Id after Insert
                                TabsHost.setLastPageTableId(TabsHost.getLastPageTableId() + 1);//todo ??? logic error? should be max page Id?

                                // update from 0 to 1 if Import starts from Empty
                                int pgsCnt = mDb_folder.getPagesCount(true);
                                if((pgsCnt > 0) && (Pref.getPref_focusView_page_tableId(MainAct.mAct) ==0))
                                    Pref.setPref_focusView_page_tableId(MainAct.mAct, 1);
                            }
                            fileBody = fileBody.concat(Util.NEW_LINE + "=== " + "Page:" + " " + pageName + " ===");
                       }
                       else if(name.equals("title"))
                       {
                            text = text.replace("[n]"," ");
                            text = text.replace("[s]"," ");
                            title = text.trim();
                       }
                       else if(name.equals("body"))
                       {
                            body = text.trim();
                       }
                       else if(name.equals("picture"))
                       {
                            picture = text.trim();
                            picture = Util.getDefaultExternalStoragePath(picture);
                       }
                       else if(name.equals("audio"))
                       {
                            audio = text.trim();
                            audio = Util.getDefaultExternalStoragePath(audio);
                       }
                       else if(name.equals("link"))
                       {
                           text = text.replace("&apos;","\'");
                           text = text.replace("&quot;","\"");
                           text = text.replace("&amp;","\\&");

                            System.out.println("ParseXmlToDB / _parseXMLAndInsertDB / name 2 = " + name);
                            link = text.trim();
                            System.out.println("ParseXmlToDB / _parseXMLAndInsertDB / link = " + link);
                            if(mEnableInsertDB)
                            {
                                DB_page.setFocusPage_tableId(TabsHost.getLastPageTableId());
                                if(title.length() !=0 || body.length() != 0 || picture.length() !=0 || audio.length() !=0 ||link.length() !=0)
                                {
                                    if((!Util.isEmptyString(picture)) || (!Util.isEmptyString(audio)))
                                        mDb_page.insertNote(title, picture, audio, "", link, body,1, (long) 0); // add mark for media
                                    else
                                        mDb_page.insertNote(title, picture, audio, "", link, body,0, (long) 0);
                                }
                            }
                            fileBody = fileBody.concat(Util.NEW_LINE + strSplitter);
                            fileBody = fileBody.concat(Util.NEW_LINE + "title:" + " " + title);
                            fileBody = fileBody.concat(Util.NEW_LINE + "body:" + " " + body);
                            fileBody = fileBody.concat(Util.NEW_LINE + "picture:" + " " + picture);
                            fileBody = fileBody.concat(Util.NEW_LINE + "audio:" + " " + audio);
                            fileBody = fileBody.concat(Util.NEW_LINE + "link:" + " " + link);
                            fileBody = fileBody.concat(Util.NEW_LINE);
                       }
                       else if(name.equals("LiteNote"))
                       {
                           isEnd = true;
                       }
                    break;
                }

                if(!isEnd)
                    event = myParser.next();
                else {
                    event = XmlPullParser.END_DOCUMENT;
                    System.out.println("ParseXmlToDB / _parseXMLAndInsertDB / isEnd = " + isEnd);
                }
            }

            isParsing = false;
            System.out.println("ParseXmlToDB / _parseXMLAndInsertDB / isParsing = " + isParsing);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    void handleXML()
    {
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    InputStream stream = fileInputStream;
                    XmlPullParser myParser = XmlPullParserFactory.newInstance().newPullParser();

                    // add for fixing ampersand will halt the system while parsing
                    // (to allow requesting that parser be as lenient as possible when parsing invalid XML)
                    myParser.setFeature(Xml.FEATURE_RELAXED, true);

                    myParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    myParser.setInput(stream, null);

                    //myParser.defineEntityReplacementText("amp", "&#38;#38;");
                    //cf. https://developer.android.com/reference/org/xmlpull/v1/XmlPullParser.html#defineEntityReplacementText(java.lang.String, java.lang.String)
                    //Note: The list of pre-defined entity names will always contain standard
                    // XML entities such as amp (&amp;), lt (&lt;), gt (&gt;), quot (&quot;), and apos (&apos;).
                    // Those cannot be redefined by this method!

                    parseXMLAndInsertDB(myParser);
                    stream.close();
                }
                catch (Exception e)
                { }
            }
        });
        thread.start();
    }

    void enableInsertDB(boolean en)
    {
        mEnableInsertDB = en;
    }
}