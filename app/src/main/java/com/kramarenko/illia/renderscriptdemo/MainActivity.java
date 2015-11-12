package com.kramarenko.illia.renderscriptdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.example.hellocompute.ScriptC_contrast;
import com.kramarenko.illia.renderscriptdemo.utils.BitmapUtil;

//import android.renderscript.RenderScript;

public class MainActivity extends AppCompatActivity implements ContrastExecutorCallback {

    private ImageView mImageView;
    private Bitmap mInBitmap;
    private ProgressBar mProgressBar;
    private TextView mText1, mText2, mRes, mThreads;
    private EditText mThreadsInput;
    private long mStartTime;

    private RenderScript mRS;
    private ScriptC_contrast mScript;
    private Allocation mInAllocation;
    private Allocation mOutAllocation;

    private int mNumOfThreads = 1;
    private String threads = "Java Threads: ";

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

        mThreads.setText(threads + mNumOfThreads);

        new Thread(new Runnable() {
            @Override
            public void run() {
                initRS();
                makeBitmap();
            }
        }).start();

    }

    private void initRS() {
        mRS = RenderScript.create(MainActivity.this);
        mScript = new ScriptC_contrast(mRS);
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
        mNumOfThreads = Integer.parseInt(mThreadsInput.getText().toString());
        mThreads.setText(threads + mNumOfThreads);
    }

    /** ============ Java Contrast ============ **/
    public void javaContrast(View view) {
        mStartTime = System.nanoTime();
        BitmapUtil.javaImageContrastAsync(mInBitmap, this, mNumOfThreads);
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
                mOutAllocation = Allocation.createTyped(mRS, mInAllocation.getType());
//                mScript.set_gIn(mInAllocation);
//                mScript.set_gOut(mOutAllocation);
//                mScript.set_gScript(mScript);
//                mScript.invoke_filter();
                mScript.forEach_root(mInAllocation, mOutAllocation);
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
