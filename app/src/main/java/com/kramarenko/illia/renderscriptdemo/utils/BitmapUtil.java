package com.kramarenko.illia.renderscriptdemo.utils;

import android.graphics.Bitmap;

import com.kramarenko.illia.renderscriptdemo.ContrastExecutorCallback;

/**
 * Created by richi on 2015.07.09..
 */
public abstract class BitmapUtil {


    public static void javaImageContrastAsync(final Bitmap _bitmap, ContrastExecutorCallback _callback, int _numOfThreads) {
        final int[] pixels = new int[_bitmap.getHeight()*_bitmap.getWidth()];
        _bitmap.getPixels(pixels, 0, _bitmap.getWidth(), 0, 0, _bitmap.getWidth(), _bitmap.getHeight());

        ContrastExecutor executor = new ContrastExecutor(_numOfThreads, pixels, _bitmap, _callback);
        executor.prepare().executeAllThreads();
    }
}
