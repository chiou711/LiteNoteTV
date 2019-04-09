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

package com.cw.litenotetv.operation.slideshow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SlideshowScreenReceiver extends BroadcastReceiver {

    public static boolean toBeScreenOn = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            // do whatever you need to do here
        	System.out.println("SlideshowScreenReceiver / _onReceive / toBeScreenOn = false");
            toBeScreenOn = false;
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            // and do whatever you need to do here
        	System.out.println("SlideshowScreenReceiver / _onReceive / toBeScreenOn = true");
            toBeScreenOn = true;
        }
    }
}