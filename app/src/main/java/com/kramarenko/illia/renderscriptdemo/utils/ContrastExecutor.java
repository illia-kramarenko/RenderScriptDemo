package com.kramarenko.illia.renderscriptdemo.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.kramarenko.illia.renderscriptdemo.ContrastExecutorCallback;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by illia on 23.09.15.
 */
public class ContrastExecutor {

    private static final int sMinValue = 20;
    private static final int sValue = 60;

    private int mNumOfThreads;
    private int[] mPixels;
    private Bitmap mResultBitmap;

    private boolean[] mFlags;
    private Runnable[] mRunners;
    private int[] mLoopEnds;

    private ExecutorService mExecutor;
    private ContrastExecutorCallback mCallback;

    public ContrastExecutor(int _numOfThreads, int[] _pixels, Bitmap _inBitmap, ContrastExecutorCallback _callback) {
        this.mNumOfThreads = _numOfThreads;
        this.mPixels = _pixels;
        this.mResultBitmap = _inBitmap/*.copy(_inBitmap.getConfig(), true)*/;
        this.mCallback = _callback;
    }

    public ContrastExecutor prepare() {

        mExecutor = Executors.newFixedThreadPool(mNumOfThreads);

        mFlags = new boolean[mNumOfThreads];
        mLoopEnds = new int[mNumOfThreads - 1];
        mRunners = new Runnable[mNumOfThreads];

        for (int i = 0; i < mFlags.length; i++) {
            mFlags[i] = false;
        }

        for (int i = 0; i < mLoopEnds.length; i++) {
            mLoopEnds[i] = (mPixels.length / mNumOfThreads) * (i + 1);
        }

        for (int i = 0; i < mRunners.length; i++){
            final int final_i = i;

            mRunners[i] = new Runnable() {
                @Override
                public void run() {
                    if (mNumOfThreads != 1) {
                        if (final_i == 0)
                            forLoop(mPixels, 0, mLoopEnds[final_i]);
                        if (final_i > 0 && final_i < (mRunners.length - 1))
                            forLoop(mPixels, mLoopEnds[final_i - 1], mLoopEnds[final_i]);
                        if (final_i == (mRunners.length - 1))
                            forLoop(mPixels, mLoopEnds[final_i - 1], mPixels.length);
                    } else
                        forLoop(mPixels, 0, mPixels.length);

                    mFlags[final_i] = true;
                    if(runnersEnded())
                        if (!mResultBitmap.isRecycled()) {
                            mResultBitmap.setPixels(mPixels, 0,
                                    mResultBitmap.getWidth(), 0, 0,
                                    mResultBitmap.getWidth(),
                                    mResultBitmap.getHeight());
                            mCallback.onExecuteFinish();
                        }
                }
            };
        }
        return this;
    }

    public void executeAllThreads(){
        for (Runnable r : mRunners){
            mExecutor.execute(r);
        }
    }

    private boolean runnersEnded(){
        for (boolean mFlag : mFlags) {
            if (!mFlag)
                return false;
        }
        return true;
    }

    private void forLoop(int[] _pixels, int _start, int _end) {
        for (int i = _start; i < _end; ++i) {
            int red = Color.red(_pixels[i]);
            int blue = Color.blue(_pixels[i]);
            int green = Color.green(_pixels[i]);
            if (red < 60) {
                red = (int) (0.017 * (red * red));
            } else {
                red = (int) (((61.5 - Math.pow((9.08 - 0.035 * red), 2)) * 4) + 10);
                if (red > 255)
                    red = 255;
            }
            if (green < 60) {
                green = (int) (0.017 * (green * green));
            } else {
                green = (int) (((61.5 - Math.pow((9.08 - 0.035 * green), 2)) * 4) + 10);
                if (green > 255)
                    green = 255;
            }
            if (blue < 60) {
                blue = (int) (0.017 * (blue * blue));
            } else {
                blue = (int) (((61.5 - Math.pow((9.08 - 0.035 * blue), 2)) * 4) + 10);
                if (blue > 255)
                    blue = 255;
            }
            _pixels[i] = Color.argb(255, red, green, blue);
        }
    }
}
