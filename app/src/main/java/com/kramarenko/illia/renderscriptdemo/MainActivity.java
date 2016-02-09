package com.kramarenko.illia.renderscriptdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.support.v8.renderscript.ScriptIntrinsicLUT;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kramarenko.illia.renderscriptdemo.utils.BitmapUtil;

//import android.renderscript.RenderScript;

public class MainActivity extends AppCompatActivity implements ContrastExecutorCallback {

    private ImageView mImageView;
    private Bitmap mInBitmap, mOutBitmap;
    private ProgressBar mProgressBar;
    private TextView mText1, mText2, mRes, mThreads;
    private EditText mThreadsInput;
    private long mStartTime;

    private RenderScript mRS;
    private ScriptC_contrast mContrastScript;
    private ScriptC_swirl mSwirlScript;
    private Allocation mInAllocation;
    private Allocation mOutAllocation;

    private ScriptIntrinsicLUT mLutScript;
    private ScriptIntrinsicBlur mBlurScript;

    private float mThreadsOrRadius = 1;
    private String threads = "Java Threads/Radius: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.ImageView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mText1 = (TextView) findViewById(R.id.text);
        mText2 = (TextView) findViewById(R.id.text2);
        mRes = (TextView) findViewById(R.id.resolution);
        mThreads = (TextView) findViewById(R.id.threads);
        mThreadsInput = (EditText) findViewById(R.id.threadsInput);

        mThreads.setText(threads + mThreadsOrRadius);

        new Thread(new Runnable() {
            @Override
            public void run() {
                initRS();
                initRsSwirl();
                initLut();
                initBlur();
                makeBitmap();
                initAllocations();
            }
        }).start();

    }

    private void initRS() {
        mRS = RenderScript.create(MainActivity.this);
        mContrastScript = new ScriptC_contrast(mRS);
    }

    private void initRsSwirl() {
        mSwirlScript = new ScriptC_swirl(mRS);
    }

    private void makeBitmap() {
        if (mInBitmap != null)
            mInBitmap.recycle();

        // 3200*1800 ~5.7mpx
        BitmapFactory.Options o = new BitmapFactory.Options();
//        o.inSampleSize = 2;
        o.inDither = false;
        o.inMutable = true;
        mInBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image2, o);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mImageView.setImageBitmap(mInBitmap);
                mRes.setText(mInBitmap.getWidth() + "x" + mInBitmap.getHeight());
            }
        });
    }

    public void refresh(View view) {
        makeBitmap();
    }

    public void onOkPressed(View view) {
        mThreadsOrRadius = Float.parseFloat(mThreadsInput.getText().toString());
        mThreads.setText(threads + mThreadsOrRadius);
    }

    public void initLut() {
        mLutScript = ScriptIntrinsicLUT.create(mRS, Element.U8_4(mRS));
        for (int index = 0; index < 256; index++) {
            int value = index;
            if (value < 60) {
                value = (int) (0.017 * (value * value));
            } else {
                value = (int) (((61.5 - Math.pow((9.08 - 0.035 * value), 2)) * 4) + 10);
                if (value > 255)
                    value = 255;
            }
            mLutScript.setRed(index, value);
            mLutScript.setBlue(index, value);
            mLutScript.setGreen(index, value);
        }
    }

    public void initBlur() {
        mBlurScript = ScriptIntrinsicBlur.create(mRS, Element.U8_4(mRS));
    }

    private void initAllocations() {
        mInAllocation = Allocation.createFromBitmap(mRS, mInBitmap,
                Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT);
        mOutAllocation = Allocation.createTyped(mRS, mInAllocation.getType());
    }


    /** ============ Java Contrast ============ **/
    public void javaContrast(View view) {
        mStartTime = System.nanoTime();
        BitmapUtil.javaImageContrastAsync(mInBitmap, this, (int) mThreadsOrRadius);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onExecuteFinish() {
        runOnUiThread(executeFinish);
    }

    private Runnable executeFinish = new Runnable() {
        @Override
        public void run() {
            long nanoDelta = System.nanoTime() - mStartTime;
            mImageView.setImageBitmap(mInBitmap);
            mProgressBar.setVisibility(View.INVISIBLE);

            double sec = (double) nanoDelta / 1000000000.0D;
            String sDelta = String.format("%.9f", sec);
            mText1.setText(sDelta);

        }
    };

    /** ============ Renderscript Contrast ============ **/

    public void rsContrast(View view) {
        mProgressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mStartTime = System.nanoTime();

                mInAllocation = Allocation.createFromBitmap(mRS, mInBitmap,
                        Allocation.MipmapControl.MIPMAP_NONE,
                        Allocation.USAGE_SCRIPT);
                mContrastScript.forEach_root(mInAllocation, mOutAllocation);
                mOutAllocation.copyTo(mInBitmap);

                final long nanoDelta = System.nanoTime() - mStartTime;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        mImageView.setImageBitmap(mInBitmap);

                        double sec = (double) nanoDelta / 1000000000.0D;
                        String sDelta = String.format("%.9f", sec);
                        mText2.setText(sDelta);
                    }
                });
            }
        }).start();
    }

    /** ============ Lut Contrast ============ **/

    public void lutContrast(View view) {
        mProgressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mStartTime = System.nanoTime();

                mInAllocation = Allocation.createFromBitmap(mRS, mInBitmap,
                        Allocation.MipmapControl.MIPMAP_NONE,
                        Allocation.USAGE_SCRIPT);
                mLutScript.forEach(mInAllocation, mOutAllocation);
                mOutAllocation.copyTo(mInBitmap);

                final long nanoDelta = System.nanoTime() - mStartTime;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        mImageView.setImageBitmap(mInBitmap);

                        double sec = (double) nanoDelta / 1000000000.0D;
                        String sDelta = String.format("%.9f", sec);
                        mText2.setText(sDelta);
                    }
                });
            }
        }).start();
    }

    /** ============ Blur ============ **/

    public void rsBlur(View view) {
        mProgressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mStartTime = System.nanoTime();

                mInAllocation = Allocation.createFromBitmap(mRS, mInBitmap,
                        Allocation.MipmapControl.MIPMAP_NONE,
                        Allocation.USAGE_SCRIPT);

                mBlurScript.setRadius(mThreadsOrRadius);
                mBlurScript.setInput(mInAllocation);
                mBlurScript.forEach(mOutAllocation);

                mOutAllocation.copyTo(mInBitmap);

                final long nanoDelta = System.nanoTime() - mStartTime;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        mImageView.setImageBitmap(mInBitmap);

                        double sec = (double) nanoDelta / 1000000000.0D;
                        String sDelta = String.format("%.9f", sec);
                        mText2.setText(sDelta);
                    }
                });
            }
        }).start();
    }

    public void nothing(View view) {
        mProgressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
//                mOutBitmap = mInBitmap.copy(mInBitmap.getConfig(), true);
                mStartTime = System.nanoTime();

                mInAllocation = Allocation.createFromBitmap(mRS, mInBitmap,
                        Allocation.MipmapControl.MIPMAP_NONE,
                        Allocation.USAGE_SCRIPT);

                mSwirlScript.set_width(mInBitmap.getWidth());
                mSwirlScript.set_height(mInBitmap.getHeight());
                mSwirlScript.set_gIn(mInAllocation);
                mSwirlScript.set_gOut(mOutAllocation);
                mSwirlScript.set_gScript(mSwirlScript);

//                mSwirlScript.forEach_root(mInAllocation, mOutAllocation);
                //mSwirlScript.invoke_filter();
                mOutAllocation.copyTo(mInBitmap);

                final long nanoDelta = System.nanoTime() - mStartTime;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        mImageView.setImageBitmap(mInBitmap);

                        double sec = (double) nanoDelta / 1000000000.0D;
                        String sDelta = String.format("%.9f", sec);
                        mText2.setText(sDelta);
                    }
                });
            }
        }).start();
    }
}
