package com.example.eatanywhere;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

@SuppressLint("NewApi")
public class CameraCapture extends Activity {

	private Preview mPreview = null;
	private Camera mCamera = null;
	public String mPicName = null;

	
	public String getPicName() {
		if ( null == mPicName ) {
			genPicName();
		}
		return mPicName;
	}
	
	public String genPicName() {
		Calendar cal = Calendar.getInstance();
		cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		mPicName = sdf.format(cal.getTime())+(int)(65535*Math.random())+".jpeg";
		return mPicName;
	}
	
	public void startComment() {
		Intent it = new Intent();
		it.putExtra("picName", getPicName());
		it.setClass(CameraCapture.this, PhotoComment.class);
		startActivity(it);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mPreview = new Preview(this);
		//TODO what camera to open?
		mCamera = Camera.open(0);
		mPreview.setCamera(mCamera);
		setContentView( mPreview );
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if ( null != mCamera ) {
			mPreview.setCamera(null);
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if ( null == mCamera ) {
			mCamera = Camera.open();
		}
		mPreview.setCamera(mCamera);
	}
	protected void onStop() {
		super.onStop();
		if ( null != mCamera ) {
			mPreview.setCamera(null);
			mCamera.release();
			mCamera = null;
		}
	}

	protected void onStart() {
		super.onStart();
		if ( null == mCamera ) {
			mCamera = Camera.open();
		}
		mCamera.setDisplayOrientation(90);
		mPreview.setCamera(mCamera);	
	}

}

class Preview extends ViewGroup implements SurfaceHolder.Callback {

	private Camera mCamera = null;
	private SurfaceView mSurfaceView = null;
	private SurfaceHolder mHolder = null;
	Size mPreviewSize = null;
	List<Size> mSupportedPreviewSizes = null;
	Activity mActivity = null;

	public Preview(Context context) {
		super(context);
		mActivity = (Activity) context;
		mSurfaceView = new SurfaceView(context);
		TextView clickToCapture = new TextView(context);
		clickToCapture.setText("点击拍照");
		addView(mSurfaceView);
		addView(clickToCapture);
		Button btn = new Button(context);
		btn.setText("catch!");
		mSurfaceView.setOnClickListener(new OnClickListener() {
			@SuppressLint("SdCardPath")
			@Override
			public void onClick(View v) {
				if ( null != mCamera ) {
					mCamera.takePicture(null, null, new Camera.PictureCallback() {
						@Override
						public void onPictureTaken(byte[] data, Camera camera) {
							final byte[] picData = data;
							Runnable savePicThread = new Runnable() {
								public void run(){
									Bitmap bmp = BitmapFactory.decodeByteArray(picData, 0, picData.length);
									//rotate it!
									Matrix matrix = new Matrix();
									matrix.preRotate(90);
									bmp = bmp.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);

									FileOutputStream out = null;
									try {
										out = new FileOutputStream("/sdcard/1pic/"+((CameraCapture) mActivity).getPicName());
									} catch (FileNotFoundException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} finally{
										bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
										try {
											out.close();
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}
							};
							new Thread(savePicThread).start();
							((CameraCapture) mActivity).startComment();								
						}
					} );
				}
			}
			});
		//addView(btn);
		mHolder = mSurfaceView.getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	private int getBiggestPreviewSizeIndex() {
		int curMax = 0;
		int curIndex = 0;
		int i = 0;
		for ( i = 0; i < mSupportedPreviewSizes.size(); i++ ) {
			if ( mSupportedPreviewSizes.get(i).height > curMax ) {
				curMax = mSupportedPreviewSizes.get(i).height;
				curIndex = i;
			}
		}
		return curIndex;
	}

	public void setCamera(Camera cm) {
		mCamera = cm;
		if ( null != cm ) {
			mSupportedPreviewSizes = cm.getParameters().getSupportedPreviewSizes();
			mPreviewSize = mSupportedPreviewSizes.get(getBiggestPreviewSizeIndex());
		}
		requestLayout();
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
		mCamera.setParameters(parameters);
		mCamera.startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if ( null != mCamera ) {
			try {
				mCamera.setPreviewDisplay(holder);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if ( null != mCamera ) {
			mCamera.stopPreview();
		}
	}

	@Override
	protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
		if ( arg0 && getChildCount() >0 ) {
			final View child = getChildAt(0);
			final int width = arg3-arg1;
			final int height = arg4-arg2;
			child.layout(0, 0, mPreviewSize.height, mPreviewSize.width);
			final View clickToCapture = getChildAt(1);
			clickToCapture.layout(mPreviewSize.height/2-40, mPreviewSize.width/2, 
					mPreviewSize.height, mPreviewSize.width);
			/*
			final View btn = getChildAt(1);
			btn.layout(0, mPreviewSize.height+60, 60, mPreviewSize.height+120);
			*/
		}
	}

}