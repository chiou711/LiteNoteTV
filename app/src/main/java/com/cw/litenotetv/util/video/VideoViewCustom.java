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

package com.cw.litenotetv.util.video;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;
// Custom video view
public class VideoViewCustom extends VideoView {

    private int mForceHeight = 0;
    private int mForceWidth = 0;
    public VideoViewCustom(Context context) {
        super(context);
    }

    public VideoViewCustom(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoViewCustom(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setDimensions(int w, int h) {
        this.mForceHeight = h;
        this.mForceWidth = w;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        Log.i("@@@@", "VideoViewCustom / onMeasure");
        setMeasuredDimension(mForceWidth, mForceHeight);
    }
    
    // interface of Play/Pause listener
    public static interface PlayPauseListener {
        void onPlay();
        void onPause();
    }    
    
    // instance of Play/Pause listener
    private PlayPauseListener mListener;
    
    // set Play/Pause listener
    public void setPlayPauseListener(PlayPauseListener listener) 
    {
        mListener = listener;
    }
    
    // redirect VideoView _start/_pause function to listener
    @Override
    public void start() {
        super.start();
        if (mListener != null) {
            mListener.onPlay();
        }
    } 
    
    @Override
    public void pause() {
        super.pause();
        if (mListener != null) {
            mListener.onPause();
        }
    }
    
}
