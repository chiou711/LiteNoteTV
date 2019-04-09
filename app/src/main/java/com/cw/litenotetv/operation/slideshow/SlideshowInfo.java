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

import java.util.ArrayList;
import java.util.List;

public class SlideshowInfo
{
   private List<ViewHolder> showList;

   public class ViewHolder {
       String title;
       String imagePath;
       String text;
       Integer position;
   }

   // constructor 
   public SlideshowInfo()
   {
       showList = new ArrayList<>();
   }

   public void addShowItem(String title, String path,String text,Integer position)
   {
       ViewHolder holder = new ViewHolder();
       holder.title =  title;
       holder.imagePath = path;
       holder.text = text;
       holder.position = position;
       showList.add(holder);
   }

   public ViewHolder getShowItem(Integer index)
   {
       System.out.println("SlideshowInfo / _getShowItem / index = " + index);
       if ((index >= 0) && (index < showList.size()))
           return showList.get(index);
       else
           return null;
   }

   public int showItemsSize()
   {
       return showList.size();
   }
}