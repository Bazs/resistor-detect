package com.example.resistordetect;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.resistordetect.FileManager.ImageType;
import com.example.resistordetect.GLConstants.RendererState;
import com.example.resistordetect.R.id;

public class CameraActivity extends Activity implements CvCameraViewListener2, AutoFocusCallback {
	
	private boolean getWhiteBalanceSpecs;
	private Context mContext;
	private Camera mCamera;
	private CameraPreView mPreview;
	private ViewTreeObserver mViewTreeObserver;
	private FrameLayout mPreviewLayout;
	protected int viewWidth;
	protected int viewHeight;
	private CameraOpenCVPreView mOpenCvCameraView;
	private CameraOpenGLPreView mOpenGLCameraView;
	private ImageView mLoadedImageView;
	private int mCurrentlyLoadedImageIndex = 0;
	private File[] mResistorImages;
	private TextView mLoadedImageIndicesTextView;
	private GestureDetector mDetector;
	//only for non-camera
	private float[] tapCoords = new float[]{0.0f, 0.0f};
	
	public ImageView debugImage;
	
	static {
	    if (!OpenCVLoader.initDebug()) {
	        throw new RuntimeException("Failed to init OpenCV.");
	    } else {
	    }
	}
	private final View.OnClickListener mFocusClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			mOpenGLCameraView.getCamera().autoFocus(CameraActivity.this);
		}
	};
	private final View.OnClickListener mTakePictureClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			mCamera.takePicture(null, null, mTakePicturePictureCallback);	
		}
	};
	private final View.OnClickListener mLoadNextImageClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mResistorImages.length != 0){
				if (mCurrentlyLoadedImageIndex + 1 == mResistorImages.length) mCurrentlyLoadedImageIndex = 0;
				else mCurrentlyLoadedImageIndex++;
			}
			FileManager.loadImage(mResistorImages[mCurrentlyLoadedImageIndex], mLoadedImageView);
			mLoadedImageIndicesTextView.setText(String.valueOf(mCurrentlyLoadedImageIndex + 1) + "/" + String.valueOf(mResistorImages.length));
		}
	};
	private final View.OnClickListener mLoadPreviousImageClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mResistorImages.length != 0){
				if (mCurrentlyLoadedImageIndex == 0) mCurrentlyLoadedImageIndex = mResistorImages.length - 1;
				else mCurrentlyLoadedImageIndex--;
			}
			FileManager.loadImage(mResistorImages[mCurrentlyLoadedImageIndex], mLoadedImageView);
			mLoadedImageIndicesTextView.setText(String.valueOf(mCurrentlyLoadedImageIndex + 1) + "/" + String.valueOf(mResistorImages.length));
		}
	};
	private final PictureCallback mTakePicturePictureCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
			bitmapOptions.inScaled = false;
			Bitmap picTaken = BitmapFactory.decodeByteArray(data, 0, data.length);
			FileManager.saveImage(picTaken, ImageType.IMAGE_TYPE_RGB);
		}
	};
	
	private static class adjustWhiteBalanceTask extends AsyncTask<ImageView, Void, Bitmap> {

		private ImageView imageView;
		
		@Override
		protected Bitmap doInBackground(ImageView... params) {
			imageView = params[0];
			Bitmap bitmap = Image.imageBitmap;
			
			Mat mat = new Mat();
			Utils.bitmapToMat(bitmap, mat);
			mat.convertTo(mat, CvType.CV_64FC4);
			
			List<Mat> inChannels = new ArrayList<Mat>(3);
			Mat[] outChannels = new Mat[]{new Mat(), new Mat(), new Mat()};
			Core.split(mat, inChannels);
			
			Mat[] ratios = new Mat[]{new Mat(1, 1, CvType.CV_64FC1), new Mat(1, 1, CvType.CV_64FC1), new Mat(1, 1, CvType.CV_64FC1)};
			for (int i = 0; i < 3; i ++) {
				ratios[i].put(0, 0, GLVariables.mWhiteBalanceRatios[i]);
				Imgproc.filter2D(inChannels.get(i), outChannels[i], -1, ratios[i]);
			}
			Core.merge(Arrays.asList(outChannels), mat);
			mat.convertTo(mat, CvType.CV_8UC4);
			bitmap = Image.bitmapFromMat(mat);
		
			return bitmap;
		}
		
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			imageView.setImageBitmap(bitmap);
			if (GLConstants.RGB_MODE == false) Image.imageBitmap = bitmap;
			Toast.makeText(ResistorDetect.getContext(), "White balance adjustment completed", Toast.LENGTH_SHORT).show();
		}
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;

		getWhiteBalanceSpecs = false;
		
		// for the OpenCV based preview
		/*mOpenCvCameraView = (CameraOpenCVPreView) findViewById(R.id.opencv_camera_preview);
		mOpenCvCameraView.setCvCameraViewListener(this);*/
		// for the OpengGL base preview
		//mOpenGLCameraView = (CameraOpenGLPreView) findViewById(R.id.opengl_camera_preview);

		//mOpenGLCameraView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		
		Button nextImageButton = (Button) findViewById(id.button_next_image);
		nextImageButton.setOnClickListener(mLoadNextImageClickListener);
		
		Button previousImageButton = (Button) findViewById(id.button_previous_image);
		previousImageButton.setOnClickListener(mLoadPreviousImageClickListener);
		
		Button focusButton = (Button) findViewById(id.button_focus);
		focusButton.setOnClickListener(mFocusClickListener);
		
		Button takePictureButton = (Button) findViewById(id.button_take_picture);
		takePictureButton.setOnClickListener(mTakePictureClickListener);
		
		Button savePictureButton = (Button) findViewById(id.button_save_picture);
		savePictureButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mResistorImages.length != 0){
					ImageType type;
					if (GLConstants.RGB_MODE == false) type = ImageType.IMAGE_TYPE_LAB;
					else type = ImageType.IMAGE_TYPE_RGB;
					FileManager.saveImage(Image.imageBitmap, type);
				}
			}
		});
		
		debugImage = (ImageView) findViewById(id.debug_image_view);
		
		//mOpenGLCameraView.setHost(this);
		
		mDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
			
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
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
				
				
				Intent transitionIntent = new Intent(ResistorDetect.getContext(), ResultActivity.class);
				transitionIntent.putExtra(GLConstants.EXTRA_MESSAGE, tapCoords);
				TimeHelper.time = SystemClock.uptimeMillis() - TimeHelper.time;
				Log.d("Image prep time",String.valueOf(TimeHelper.time) + "ms");
				GLVariables.openGLSetupTime = SystemClock.uptimeMillis();
				mContext.startActivity(transitionIntent);				
				return false;
			}
			
			@Override
			public void onShowPress(MotionEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
					float distanceY) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void onLongPress(MotionEvent e) {
				
				
				
				Utils.bitmapToMat(Image.imageBitmap, GLVariables.mWhiteBalanceFrame);
				
				float currentChannelMaxValue, currentBracketValue;
				for (int i = 0; i < 3; i++) {
					Imgproc.calcHist(Arrays.asList(GLVariables.mWhiteBalanceFrame), GLConstants.mHistogramChannels[i], GLConstants.mNullMat, GLVariables.mHistogram[i], 
							GLConstants.mHistogramSizes, GLConstants.mHistogramRanges, false);
					GLVariables.mBackgroundColorValues[i] = 0;
					currentChannelMaxValue = 0;
					for (int j = 0; j < GLConstants.HISTOGRAM_BRACKETS; j++) {
						currentBracketValue = (float) GLVariables.mHistogram[i].get(j, 0)[0];
						if (currentChannelMaxValue < currentBracketValue) {
							currentChannelMaxValue = currentBracketValue;
							GLVariables.mBackgroundColorValues[i] = j;
						}
					}
				}
				
				for (int i = 0; i < 3; i++) {
					GLVariables.mBackgroundColorValues[i] = GLVariables.mBackgroundColorValues[i] * GLConstants.HISTOGRAM_BRACKET_SIZE + GLConstants.HISTOGRAM_BRACKET_SIZE / 2.0f;
					GLVariables.mWhiteBalanceRatios[i] = GLConstants.DESIRED_MAXIMUM_INTENSITY / GLVariables.mBackgroundColorValues[i];
				}
				
				Toast.makeText(mContext, "WB values acquired, adjusting image...", Toast.LENGTH_SHORT).show();
				
				new adjustWhiteBalanceTask().execute(mLoadedImageView);
				
			}
			
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
					float velocityY) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean onDown(MotionEvent event) {
				// TODO Auto-generated method stub
				tapCoords[0] = event.getX()/mLoadedImageView.getWidth();
				tapCoords[1] = event.getY()/mLoadedImageView.getHeight();
				return true;
			}
		});
		mLoadedImageView = (ImageView) findViewById(id.loaded_image_view);
		mLoadedImageView.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return mDetector.onTouchEvent(event);
			}
		});
		
		
		File resistorImagesDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ResistorDetect/Resistor images");
		if (!resistorImagesDirectory.exists()) {
			if (!resistorImagesDirectory.mkdirs()) {
				Log.e("File error", "Directories couldn't be created!");
			}
		}
		mResistorImages = resistorImagesDirectory.listFiles();
		
	}

    
    protected void onResume(){
    	super.onResume();
    	
    	if (mResistorImages.length == 0) Toast.makeText(ResistorDetect.getContext(), "No images to load!", Toast.LENGTH_SHORT).show();
		else {
			FileManager.loadImage(mResistorImages[mCurrentlyLoadedImageIndex], mLoadedImageView);
		}
		
		mLoadedImageIndicesTextView = (TextView) findViewById(id.loaded_image_indices_text_view);
		mLoadedImageIndicesTextView.setText(String.valueOf(mCurrentlyLoadedImageIndex + 1) + "/" + String.valueOf(mResistorImages.length));
    	// for OpenCV based preview
    	//mOpenCvCameraView.enableView();
    	
    	// for native SurfaceView based preview
    	/*if (safeCameraOpen(0)) {
    		mPreview = new CameraPreView(this, mCamera);
    		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
    		preview.addView(mPreview);
    		
        } else {
        	Log.d("para","a kameraval");
        }*/
    	
    	// for native GLSurfaceView based preview
    	/*if (safeCameraOpen(0)) {
    		mOpenGLCameraView.setCamera(mCamera);    		
        } else {
        	Log.d("para","a kameraval");
        }*/
    	
    }
    
    protected void onPause() {
    	super.onPause();
    	
    	//if (mOpenGLCameraView != null) mOpenGLCameraView.stop();
    	
    	// used with OpenCV based camera preview
    	//if (mOpenCvCameraView != null) mOpenCvCameraView.disableView();

    	// used with SurfaceView based camera preview
    	/*FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
    	preview.removeView(mPreview);
    	mPreview = null;
    	if (mCamera != null) {
    		mCamera.release();
    		mCamera = null;
    	}*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    private boolean safeCameraOpen(int id) {
    	boolean qOpened = false;
    	
    	try {
    		releaseCameraAndPreview();
    		mCamera = Camera.open(id);
    		qOpened = (mCamera !=null);
    		
    	} catch (Exception e) {
    		Log.e(getString(R.string.app_name),"failed to open Camera");
    		e.printStackTrace();
    	}
    	
    	if (qOpened) {
    		/*Camera.Parameters camParams = mCamera.getParameters();
    		//camParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
    		
			camParams.setWhiteBalance(Parameters.WHITE_BALANCE_WARM_FLUORESCENT);
			int low = camParams.getMinExposureCompensation();
			int high = camParams.getMaxExposureCompensation();
			camParams.setExposureCompensation(high);
			//camParams.setSceneMode(Parameters.SCENE_MODE_BARCODE);
			//camParams.setFocusMode(Parameters.FOCUS_MODE_MACRO);

    		mCamera.setParameters(camParams);*/
    	}
    	
    	return qOpened;
    }
    
    private void releaseCameraAndPreview() {
    	mPreview = null;
    	if (mCamera != null) {
    		mCamera.release();
    		mCamera = null;
    	}
    }
    

	@Override
	public void onCameraViewStarted(int width, int height) {
		Log.d("Opencv", "started");
	}


	@Override
	public void onCameraViewStopped() {
		
	}


	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		/*GLVariables.mWhiteBalanceFrame = inputFrame.rgba();
		
		if (getWhiteBalanceSpecs == true) {
			getWhiteBalanceSpecs = false;
			float currentChannelMaxValue, currentBracketValue;
			for (int i = 0; i < 3; i++) {
				Imgproc.calcHist(Arrays.asList(GLVariables.mWhiteBalanceFrame), GLConstants.mHistogramChannels[i], GLConstants.mNullMat, GLVariables.mHistogram[i], 
						GLConstants.mHistogramSizes, GLConstants.mHistogramRanges, false);
				GLVariables.mBackgroundColorValues[i] = 0;
				currentChannelMaxValue = 0;
				for (int j = 0; j < GLConstants.HISTOGRAM_BRACKETS; j++) {
					currentBracketValue = (float) GLVariables.mHistogram[i].get(j, 0)[0];
					if (currentChannelMaxValue < currentBracketValue) {
						currentChannelMaxValue = currentBracketValue;
						GLVariables.mBackgroundColorValues[i] = j;
					}
				}
			}
	
			for (int i = 0; i < 3; i++) {
				GLVariables.mBackgroundColorValues[i] = GLVariables.mBackgroundColorValues[i] * GLConstants.HISTOGRAM_BRACKET_SIZE + GLConstants.HISTOGRAM_BRACKET_SIZE / 2.0f;
				float temp = GLConstants.HISTOGRAM_RANGES / GLVariables.mBackgroundColorValues[i];
				GLVariables.mColorCompensationRatios[i].put(0, 0, GLConstants.HISTOGRAM_RANGES / GLVariables.mBackgroundColorValues[i]);
				Log.d("compensation value", GLVariables.mColorCompensationRatios[i].dump());
			}
			
			Log.d("Megcsinálta", "a buzi hisztogramot!");
			
		}
		
		
		Core.split(GLVariables.mWhiteBalanceFrame, GLVariables.mPreviewChannels);
		for (int i = 0; i < 3; i ++) {
			Imgproc.filter2D(GLVariables.mPreviewChannels.get(i), GLVariables.mPreviewChannels.get(i), -1, GLVariables.mColorCompensationRatios[i]);
		}
		Core.merge(GLVariables.mPreviewChannels, GLVariables.mWhiteBalanceFrame);
		return inputFrame.rgba();*/
		return null;
	}


	@Override
	public void onAutoFocus(boolean success, Camera arg1) {
		if (success) Toast.makeText(mContext, "Focus successful", Toast.LENGTH_SHORT).show();
		else Toast.makeText(mContext, "Focus unsuccessful", Toast.LENGTH_SHORT).show();
	}

    
}
