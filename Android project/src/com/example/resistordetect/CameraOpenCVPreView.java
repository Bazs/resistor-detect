package com.example.resistordetect;

import java.io.IOException;

import org.opencv.android.JavaCameraView;
import org.opencv.android.NativeCameraView;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import android.content.Context;
import android.content.Intent;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;

public class CameraOpenCVPreView extends JavaCameraView implements SurfaceHolder.Callback, CameraPreviewSurface, OnTouchListener{

	private Context mContext;
	private GestureDetector mDetector;
	protected int viewWidth;
	protected int viewHeight;
	private float[] tapCoords = new float[]{0.0f, 0.0f};
	private SurfaceHolder mHolder;
	private final CameraPreviewTapListener mTapListener = new CameraPreviewTapListener(this);
	
	public CameraOpenCVPreView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mContext = context;
		
		viewWidth = 0;
        viewHeight = 0;
		
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mDetector = new GestureDetector(mContext, mTapListener);
		mDetector.setIsLongpressEnabled(true);
		this.setOnTouchListener(this);
	}


	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		if (viewWidth == 0 && viewHeight == 0){
			connectCamera(0, 0);
			viewWidth = getWidth();
			Log.d("tag", String.valueOf(viewWidth));
			viewHeight = getHeight();
			Camera.Parameters camParams = mCamera.getParameters();
			Camera.Size bestSize = CameraPreviewSizeStaticHelper.getBestPreviewSize(viewWidth, viewHeight, camParams);
			viewWidth = bestSize.width;
			viewHeight = bestSize.height;
			disconnectCamera();
			connectCamera(viewWidth, viewHeight);
			ViewGroup.LayoutParams layoutParams = getLayoutParams();
			layoutParams.width = bestSize.width;
			layoutParams.height = bestSize.height;
			setLayoutParams(layoutParams);
			requestLayout();
		}
	}

	public void surfaceCreated(SurfaceHolder arg0) {
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		
	}
	
	public Camera getCamera() {
		return mCamera;
	}
	
	public void setTapCoords(float[] tapCoords) {
		if (tapCoords.length != 2) throw new RuntimeException("tapCoords must be a float array with a length of 2!");
		this.tapCoords = tapCoords;
	}
	
	public int getViewWidth() {
		return viewWidth;
	}
	
	public int getViewHeight() {
		return viewHeight;
	}

	@Override
	public void takePicture() {
		mCamera.setPreviewCallback(null);
		mCamera.takePicture(null, null, mTapListener.getDetectionStartPictureCallback());
	}

	@Override
	public float[] getTapCoords() {
		return tapCoords;
	}


	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return mDetector.onTouchEvent(event);
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
