package com.example.resistordetect;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import org.opencv.*;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.example.resistordetect.FileManager.ImageType;

class CameraPreView extends SurfaceView implements SurfaceHolder.Callback, CameraPreviewSurface {
	
	public CameraPreView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	SurfaceHolder mHolder;
	Camera mCamera;
	
	protected static int viewWidth;
	protected static int viewHeight;
	private float[] tapCoords;
	private GestureDetector mDetector;
	private final CameraPreviewTapListener mTapListener = new CameraPreviewTapListener(this);

	CameraPreView(Context context, Camera camera) {
		super (context);

		mCamera = camera;
		
        viewWidth = 0;
        viewHeight = 0;
		tapCoords = new float[]{0.0f, 0.0f};
        
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mDetector = new GestureDetector(mTapListener);
		this.setOnTouchListener(
				new OnTouchListener() {

					@Override
					public boolean onTouch(View arg0, MotionEvent me) {
						// TODO Auto-generated method stub
						return mDetector.onTouchEvent(me);
					}
				}
				);
	}
	
	public int getViewWidth() {
		return viewWidth;
	}
	
	public int getViewHeight() {
		return viewHeight;
	}
	
	
	public boolean onSingleTapConfirmed(MotionEvent e) {
		return true;
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

		if (viewWidth == 0 && viewHeight == 0){

			viewWidth = getWidth();
			Log.d("tag", String.valueOf(viewWidth));
			viewHeight = getHeight();
			Camera.Parameters camParams = mCamera.getParameters();
			Camera.Size bestSize = CameraPreviewSizeStaticHelper.getBestPreviewSize(viewWidth, viewHeight, camParams);
			viewWidth = bestSize.width;
			viewHeight = bestSize.height;
			camParams.setPreviewSize(bestSize.width, bestSize.height);
			mCamera.setParameters(camParams);
			try {
				mCamera.setPreviewDisplay(mHolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.d("Camera parameters", mCamera.getParameters().getWhiteBalance());
			mCamera.startPreview();
			//mHolder.setFixedSize(bestSize.width, bestSize.height);
			ViewGroup.LayoutParams layoutParams = getLayoutParams();
			layoutParams.width = bestSize.width;
			layoutParams.height = bestSize.height;
			setLayoutParams(layoutParams);
			requestLayout();


		} 
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		if (mCamera != null) {
			mCamera.stopPreview();
		}
	}
	private void stopPreviewAndFreeCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			
			mCamera = null;
		}
		
	}

	@Override
	protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
		// TODO Auto-generated method stub
		
	}

	
	
	public float[] getTapCoords() {
		return tapCoords;
	}
	

	@Override
	public void setTapCoords(float[] tapCoords) {
		if (tapCoords.length != 2) throw new RuntimeException("tapCoords must be a float array with a length of 2!");
		this.tapCoords = tapCoords;		
	}

	@Override
	public void takePicture() {
		mCamera.takePicture(null, null, mTapListener.getDetectionStartPictureCallback());
	}

	@Override
	public void getWhiteBalanceData() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}
}
