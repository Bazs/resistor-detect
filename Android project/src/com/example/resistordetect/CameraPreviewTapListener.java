package com.example.resistordetect;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import com.example.resistordetect.GLConstants.RendererState;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.MediaActionSound;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.SystemClock;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

interface CameraPreviewSurface {
	int getViewWidth();
	int getViewHeight();
	void setTapCoords(float[] tapCoords);
	float[] getTapCoords();
	void takePicture();
	Context getContext();
	void getWhiteBalanceData();
	void stop();
}

public class CameraPreviewTapListener implements android.view.GestureDetector.OnGestureListener, OnTouchListener {

	// TODO Implement this part when you'll have a device with Android 4.0+
	
	/*private MediaActionSound mFocusCompleteSound = new MediaActionSound();
	private MediaActionSound mShutterClickSound = new MediaActionSound();
	
	static {
		
	}*/
	
	
	private final PictureCallback mDetectionStartPictureCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			GLVariables.totalCalculationTime = SystemClock.uptimeMillis();
			long time = GLVariables.totalCalculationTime;

			
			BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
			bitmapOptions.inScaled = false;
			Image.imageBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			GLVariables.mRendererState = RendererState.IDLE;
			Image.imageMat = new Mat();
			Utils.bitmapToMat(Image.imageBitmap, Image.imageMat);
			Image.imageBitmap.recycle();
			int imageWidth = Image.imageBitmap.getWidth();
			int imageHeight = Image.imageBitmap.getHeight();
			int cropWidth, cropHeight, cropX, cropY;
			cropWidth = (int) (imageWidth * 0.25);
			cropHeight = (int) (imageHeight * 0.25);
			// crop to the quarter of the original image where the user tapped

			float[] tapCoords = mHostView.getTapCoords();

			if (tapCoords[0] < 0.25) {
				cropX = 0;
			} else if (tapCoords[0] > 0.75) {
				cropX = (int) (0.75 * imageWidth);
			} else {
				cropX = (int) ((tapCoords[0] - 0.125f) * (float) imageWidth);
			}

			if (tapCoords[1] < 0.25) {
				cropY = 0;
			} else if (tapCoords[1] > 0.75) {
				cropY = (int) (0.75 * imageHeight);
			} else {
				cropY = (int) ((tapCoords[1] - 0.125) * (float) imageHeight);
			}
			Log.d("tapcoords", "X: " + String.valueOf(tapCoords[0]) + " Y: " + String.valueOf(tapCoords[1]) + " cropX: " + String.valueOf(cropX) + " cropY: " + String.valueOf(cropY));
			Rect cropRect = new Rect(cropX, cropY, cropWidth, cropHeight);
			Mat tempImage = new Mat(Image.imageMat, cropRect);
			Image.imageMat = tempImage.clone();
			Image.imageBitmap = Bitmap.createBitmap(Image.imageMat.cols(), Image.imageMat.rows(), Bitmap.Config.RGB_565);
			//Utils.matToBitmap(Image.imageMat, Image.imageBitmap);
			
			
			Intent transitionIntent = new Intent(mHostView.getContext(),ResultActivity.class);
			transitionIntent.putExtra(GLConstants.EXTRA_MESSAGE, tapCoords);
			time = SystemClock.uptimeMillis() - time;
			Log.d("Image prep time",String.valueOf(time) + "ms");
			GLVariables.openGLSetupTime = SystemClock.uptimeMillis();
			mHostView.stop();
			mHostView.getContext().startActivity(transitionIntent);			
		}
	};
	private CameraPreviewSurface mHostView;
	private GestureDetector mDetector;
	
	public CameraPreviewTapListener (View hostView) {
		mHostView = (CameraPreviewSurface) hostView;
		mDetector = new GestureDetector(mHostView.getContext(), this);
	}
	
	@Override
	public boolean onDown(MotionEvent event) {
		obtainAndSetTapCoords(event);
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent event) {
		obtainAndSetTapCoords(event);
		mHostView.getWhiteBalanceData();
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent event) {
		obtainAndSetTapCoords(event);
		mHostView.takePicture();
		return true;
	}

	private void obtainAndSetTapCoords(MotionEvent event) {
		float[] tapCoords = new float[2];
		tapCoords[0] = event.getX()/mHostView.getViewWidth();
		tapCoords[1] = event.getY()/mHostView.getViewHeight();
		mHostView.setTapCoords(tapCoords);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return mDetector.onTouchEvent(event);
	}
	
	public PictureCallback getDetectionStartPictureCallback() {
		return mDetectionStartPictureCallback;
	}
}

