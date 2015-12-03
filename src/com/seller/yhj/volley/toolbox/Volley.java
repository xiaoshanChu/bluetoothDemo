/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.seller.yhj.volley.toolbox;

import java.io.File;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.seller.yhj.volley.Network;
import com.seller.yhj.volley.RequestQueue;


public class Volley {

    /**
     * Creates a default instance of the worker pool and calls {@link RequestQueue#start()} on it.
     *
     * @param context A {@link Context} to use for creating the cache dir.
     * @param stack An {@link HttpStack} to use for the network, or null for default.
     * @return A started {@link RequestQueue} instance.
     */
    public static RequestQueue newRequestQueue(Context context, HttpStack stack,String fileName) {
        File cacheDir = new File(getDiskCacheDir(context), fileName);

        if (stack == null) {
            if (Build.VERSION.SDK_INT >= 9) {
                stack = new HurlStack(context);
            } else {
                stack = new HttpClientStack(context);
            }
        }

        Network network = new BasicNetwork(stack);

        RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir), network);
        queue.start();

        return queue;
    }
    
    private static String getDiskCacheDir(Context context) {  
        String cachePath = null;  
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {  
            cachePath = context.getExternalCacheDir().getPath();  
        } else {  
            cachePath = context.getCacheDir().getPath();  
        }  
        return cachePath;  
    } 

    /**
     * Creates a default instance of the worker pool and calls {@link RequestQueue#start()} on it.
     *
     * @param context A {@link Context} to use for creating the cache dir.
     * @return A started {@link RequestQueue} instance.
     */
    public static RequestQueue newRequestQueue(Context context,String fileName) {
        return newRequestQueue(context, null,fileName);
    }
    
    
}
