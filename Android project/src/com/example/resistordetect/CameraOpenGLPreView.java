package com.example.resistordetect;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import com.example.resistordetect.GLConstants.RendererState;

public class CameraOpenGLPreView extends GLSurfaceView implements CameraPreviewSurface, OnFrameAvailableListener, Camera.PreviewCallback {

	private GLRenderer mRenderer;
	private Camera mCamera;
	private Context mContext;
	private int viewWidth = 0;
	private int viewHeight = 0;
	private float[] tapCoords = new float[]{0.0f, 0.0f};
	private final CameraPreviewTapListener mTapListener = new CameraPreviewTapListener(this);
	public CameraActivity host;
	private final PictureCallback mDetectionStartPictureCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			GLVariables.totalCalculationTime = SystemClock.uptimeMillis();
			BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
			bitmapOptions.inScaled = false;
			Image.imageBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			Image.imageMat = new Mat();
			Utils.bitmapToMat(Image.imageBitmap, Image.imageMat);
			stop();
			GLVariables.mRendererState = RendererState.DETECTION_PREPROCESS_STARTUP;
			requestRender();
		}
	};
	
	public void setHost(CameraActivity host) {
		this.host = host;
	}
	
	private Handler mPreviewHandler = new Handler(){
		public void handleMessage (Message msg){
			switch (msg.what){
			case GLConstants.PREVIEW_SETUP_FINISHED:
				Log.d("handler", "Preview setup finished");
				
				int width = mRenderer.getGLViewWidth();
				int height = mRenderer.getGLViewHeight();
				Camera.Parameters camParams = mCamera.getParameters();
				Camera.Size bestSize = CameraPreviewSizeStaticHelper.getBestPreviewSize(width, height, camParams);
				GLES20.glViewport(0, 0, bestSize.width, bestSize.height);
				camParams.setPreviewSize(bestSize.width, bestSize.height);
				camParams.setPreviewFormat(ImageFormat.NV21);
				List<String> supportedWhiteBalances = camParams.getSupportedWhiteBalance();
				GLVariables.mPresetWhiteBalance = true;
				if (Build.VERSION.SDK_INT >= 14) {
					if (camParams.isAutoExposureLockSupported()) GLVariables.mPresetWhiteBalance = false;
				}
				/*if (GLVariables.mPresetWhiteBalance == true) {
					if (!supportedWhiteBalances.isEmpty()) {
						if (supportedWhiteBalances.contains(Camera.Parameters.WHITE_BALANCE_WARM_FLUORESCENT)) camParams.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_WARM_FLUORESCENT);
						else if (supportedWhiteBalances.contains(Camera.Parameters.WHITE_BALANCE_FLUORESCENT)) camParams.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_FLUORESCENT);
						else {
							String notAutoSetting = null;
							for (String balanceSetting : supportedWhiteBalances) {
								if (balanceSetting != Camera.Parameters.WHITE_BALANCE_AUTO) {
									notAutoSetting = balanceSetting;
									break;
								}
							}
							if (notAutoSetting != null) camParams.setWhiteBalance(notAutoSetting);
						}
					}
				}*/
				mCamera.setParameters(camParams);
				viewHeight = bestSize.height;
				viewWidth = bestSize.width;

				GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);

				GLVariables.vertexShaderHandle = mRenderer.addVertexShader(ShaderStringFromTextStaticHelper.readShaderRaw(R.raw.preview_vertex_shader));
				int previewShaderHandle = mRenderer.addFragmentShader(ShaderStringFromTextStaticHelper.readShaderRaw(R.raw.camera_preview));
				int previewProgramHandle = mRenderer.createProgram(GLVariables.vertexShaderHandle, previewShaderHandle, new String[] {"a_Position", "a_Color", "a_TexCoordinate"});
				mRenderer.useProgram(previewProgramHandle);
				
				setupPreviewTexture();
				Image.mCurrentFrame = new SurfaceTexture(GLVariables.mPreviewTextureHandle[0]);
				try {
					mCamera.setPreviewTexture(Image.mCurrentFrame);
				} catch (IOException e) {
					Log.e("Camera", "Couldn't set preview texture.");
					e.printStackTrace();
				}
				
				GLVariables.mPositionHandle = GLES20.glGetAttribLocation(GLVariables.mCurrentProgramHandle, "a_Position");
				GLVariables.mColorHandle = GLES20.glGetAttribLocation(GLVariables.mCurrentProgramHandle, "a_Color");
				GLVariables.mTextureUniformHandle = GLES20.glGetUniformLocation(GLVariables.mCurrentProgramHandle, "u_Texture");
				GLVariables.mTextureCoordinateHandle = GLES20.glGetAttribLocation(GLVariables.mCurrentProgramHandle, "a_TexCoordinate");
				GLVariables.mWhiteBalanceRatiosHandle = GLES20.glGetUniformLocation(GLVariables.mCurrentProgramHandle, "u_WhiteBalanceRatios");
				
				GLES20.glUniform3fv(GLVariables.mWhiteBalanceRatiosHandle, 1, GLVariables.mWhiteBalanceRatios, 0);
				
				GLVariables.mRendererState = RendererState.PREVIEW_RUNNING;
				startPreview();
				break;
			case GLConstants.DETECTION_PREPROCESS_STARTUP_FINISHED:
				int detectionPreprocessShaderHandle = mRenderer.addFragmentShader(ShaderStringFromTextStaticHelper.readShaderRaw(R.raw.detection_preprocess));
				int detectionPreprocessProgramHandle = mRenderer.createProgram(GLVariables.vertexShaderHandle, detectionPreprocessShaderHandle, new String[] {"a_Position", "a_Color", "a_TexCoordinate"});
				mRenderer.useProgram(detectionPreprocessProgramHandle);
				
				int imageWidth = Image.imageMat.cols();
				int imageHeight = Image.imageMat.rows();
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
				org.opencv.core.Rect cropRect = new org.opencv.core.Rect(cropX, cropY, cropWidth, cropHeight);
				Mat tempImage = new Mat(Image.imageMat, cropRect);
				Image.imageBitmap = Image.bitmapFromMat(tempImage);
				
				GLVariables.mTextureDataHandle = mRenderer.setupFrameBufferForPreprocessing(Image.imageBitmap);
				GLES20.glViewport(0, 0, GLVariables.mCurrentTextureSize[0], GLVariables.mCurrentTextureSize[1]);
				
				GLVariables.mPositionHandle = GLES20.glGetAttribLocation(GLVariables.mCurrentProgramHandle, "a_Position");
				GLVariables.mColorHandle = GLES20.glGetAttribLocation(GLVariables.mCurrentProgramHandle, "a_Color");
				GLVariables.mTextureUniformHandle = GLES20.glGetUniformLocation(GLVariables.mCurrentProgramHandle, "u_Texture");
				GLVariables.mTextureCoordinateHandle = GLES20.glGetAttribLocation(GLVariables.mCurrentProgramHandle, "a_TexCoordinate");
				GLVariables.mWhiteBalanceRatiosHandle = GLES20.glGetUniformLocation(GLVariables.mCurrentProgramHandle, "u_WhiteBalanceRatios");
				
				GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLVariables.mTextureDataHandle);
				GLES20.glUniform1i(GLVariables.mTextureUniformHandle, 0);
				
				GLES20.glUniform3fv(GLVariables.mWhiteBalanceRatiosHandle, 1, GLVariables.mWhiteBalanceRatios, 0);
				
				GLVariables.mRendererState = RendererState.DETECTION_PREPROCESS;
				requestRender();
				break;
			case GLConstants.DETECTION_PREPROCESS_FINISHED:
				int temp1 = GLVariables.mCurrentTextureSize[0] * GLVariables.mCurrentTextureSize[1] * 4;
				int temp2 = (int) Image.imageMat.total() * 4;
				ByteBuffer readPixels = ByteBuffer.allocateDirect((int) (GLVariables.mCurrentTextureSize[0] * GLVariables.mCurrentTextureSize[1] * 4));
				readPixels.order(ByteOrder.nativeOrder());
				GLES20.glReadPixels(0, 0, GLVariables.mCurrentTextureSize[0], GLVariables.mCurrentTextureSize[1], GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, readPixels);
				byte[] readPixelsBytes = new byte[readPixels.capacity()];
				readPixels.get(readPixelsBytes);
				readPixels = null;


				byte[] tempBytes = new byte[4];
				Mat tempMat = new Mat(GLVariables.mCurrentTextureSize[1], GLVariables.mCurrentTextureSize[0], CvType.CV_8UC4);
				for (int i = 0; i < GLVariables.mCurrentTextureSize[1]; i++) {
					for (int j = 0; j < GLVariables.mCurrentTextureSize[0]; j++) {
						int idx = (i * GLVariables.mCurrentTextureSize[0] + j) * 4;
						for (int k = 0; k < 4; k++) tempBytes[k] = readPixelsBytes[idx + k];
						tempMat.put(i, j, tempBytes);
					}
				}
				Image.imageMat = tempMat.clone();
				
				Intent transitionIntent = new Intent(mContext, ResultActivity.class);
				transitionIntent.putExtra(GLConstants.EXTRA_MESSAGE, tapCoords);
				GLVariables.mRendererState = RendererState.PREVIEW_FINISHED;
				mContext.startActivity(transitionIntent);			
				break;
			default:
				break;
			}
		}
	};


	public CameraOpenGLPreView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		
		setEGLContextClientVersion(2);
		mRenderer = new GLRenderer();
		
		setRenderer(mRenderer);
		this.setOnTouchListener(mTapListener);
	}

	
	private void startPreview() {
		Image.mCurrentFrame.setOnFrameAvailableListener(this);
		GLVariables.mPreviewRenderTime = SystemClock.uptimeMillis();
		mCamera.startPreview();
	}
	
	public void setCamera (Camera camera) {
		if (camera == null) throw new RuntimeException("The camera passed to" + this.getClass().toString() + "is null!");
		mCamera = camera;
		GLVariables.mRendererState = RendererState.PREVIEW_SETUP;
		mRenderer.openGLThreadPass(mPreviewHandler);
	}

	@Override
	public int getViewWidth() {
		return viewWidth;
	}


	@Override
	public int getViewHeight() {
		return viewHeight;
	}


	@Override
	public void setTapCoords(float[] tapCoords) {
		if (tapCoords.length != 2) throw new RuntimeException("tapCoords must be a float array with a length of 2!");
		this.tapCoords = tapCoords;		
	}


	@Override
	public float[] getTapCoords() {
		return tapCoords;
	}


	@Override
	public void takePicture() {
		mCamera.takePicture(null, null, mTapListener.getDetectionStartPictureCallback());
	}

	private void setupPreviewTexture() {
		GLES20.glGenTextures(1, GLVariables.mPreviewTextureHandle, 0);	
		if (GLVariables.mPreviewTextureHandle[0] == 0) throw new RuntimeException("Error loading texture");
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLVariables.mTextureHandle[0]);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_MIRRORED_REPEAT);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_MIRRORED_REPEAT);
	}

	@Override
	public void onFrameAvailable(SurfaceTexture surfaceTexture) {
		requestRender();		
	}

	@Override
	public void stop() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
		if (GLVariables.mRendererState != RendererState.PREVIEW_FINISHED) {
			GLVariables.mRendererState = RendererState.PREVIEW_FINISHED;
			requestRender();
		}
	}


	@Override
	public void getWhiteBalanceData() {
		if (!GLVariables.mPresetWhiteBalance) {
			Camera.Parameters camParams = mCamera.getParameters();
			camParams.setAutoWhiteBalanceLock(false);
			mCamera.setParameters(camParams);
		}
		mCamera.setOneShotPreviewCallback(this);		
	}


	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		YuvImage yuvImage = new YuvImage(data,ImageFormat.NV21, viewWidth, viewHeight, null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		yuvImage.compressToJpeg(new Rect(0, 0, viewWidth, viewHeight), 100, baos);
		
		data = baos.toByteArray();
		
		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		bitmapOptions.inScaled = false;
		Image.imageBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
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
		GLVariables.mRendererState = RendererState.SET_WHITE_BALANCE;
		if (!GLVariables.mPresetWhiteBalance) {
			Camera.Parameters camParams = mCamera.getParameters();
			camParams.setAutoWhiteBalanceLock(true);
			mCamera.setParameters(camParams);
		}
		Toast.makeText(mContext, "White balance adjusted!", Toast.LENGTH_SHORT).show();
	}
	
	public Camera getCamera() {
		return mCamera;
	}
}
