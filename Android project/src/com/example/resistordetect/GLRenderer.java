package com.example.resistordetect;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.example.resistordetect.GLConstants.RendererState;

import android.R.string;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;


public class GLRenderer implements GLSurfaceView.Renderer {
	
	private final GLBackgroundSquare background = new GLBackgroundSquare();
	private Handler handler = null;
	public float[] mViewMatrix = new float[16];
	public float[] mProjectionMatrix = new float[16];
	public float[] mMVPMatrix = new float[16];
	private int mGLViewWidth = 0;
	private int mGLViewHeight = 0;
	
	GLRenderer() {
	}
	
	public void openGLThreadPass(Handler handler) {
		this.handler = handler;
		
	}
	
	public Thread getOpenGLThread() {
		Thread temp = Thread.currentThread();
		return temp;
	}
	
	@Override
	public void onDrawFrame(GL10 arg0) {			
		if (handler != null){
			if (GLVariables.mRendererState == RendererState.PREVIEW_RUNNING) {
				GLVariables.mPreviewRenderTime = SystemClock.uptimeMillis() - GLVariables.mPreviewRenderTime;
				GLVariables.mPreviewFPS = 1000.0f / (float) GLVariables.mPreviewRenderTime;
				int temp = GLES20.glGetError();
				Image.mCurrentFrame.updateTexImage();
				//Log.d("FPS", String.valueOf(GLVariables.mPreviewFPS));
				GLES20.glFinish();
				background.draw();
			} else {
				long time;
				switch (GLVariables.mRendererState) {
				case SET_WHITE_BALANCE:
					GLES20.glUniform3fv(GLVariables.mWhiteBalanceRatiosHandle, 1, GLVariables.mWhiteBalanceRatios, 0);
					GLVariables.mRendererState = RendererState.PREVIEW_RUNNING;
					Image.mCurrentFrame.updateTexImage();
					GLES20.glFinish();
					background.draw();
					break;
				case PREVIEW_SETUP:
					handler.dispatchMessage(Message.obtain(handler, GLConstants.PREVIEW_SETUP_FINISHED));
					break;
				case DETECTION_PREPROCESS_STARTUP:
					handler.dispatchMessage(Message.obtain(handler, GLConstants.DETECTION_PREPROCESS_STARTUP_FINISHED));
					break;
				case DETECTION_PREPROCESS:
					background.draw();
					GLES20.glFinish();
					handler.dispatchMessage(Message.obtain(handler, GLConstants.DETECTION_PREPROCESS_FINISHED));
					break;
				case PREVIEW_FINISHED:
					if (Build.VERSION.SDK_INT >= 14) {
						Image.mCurrentFrame.release();
						GLES20.glFinish();
					}
					break;
				case DETECTION_SETUP:
					handler.dispatchMessage(Message.obtain(handler, GLConstants.DETECTION_SETUP_FINISHED));
					break;
				case MAHALANOBIS:
					Log.d("ka", "ki");
					time = SystemClock.uptimeMillis();
					background.draw();
					GLES20.glFinish();
					time = SystemClock.uptimeMillis() - time;
					Log.d("draw time", "Mahalanobis time: " + String.valueOf(time));
					handler.dispatchMessage(Message.obtain(handler, GLConstants.MAHALANOBIS_FINISHED));
					break;
				case SOBEL_SATURATION:
					GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
					time = SystemClock.uptimeMillis();
					background.draw();
					GLES20.glFinish();
					time = SystemClock.uptimeMillis() - time;
					Log.d("draw time", "Sobel + Saturation time: " + String.valueOf(time));
					handler.dispatchMessage(Message.obtain(handler, GLConstants.SOBEL_SATURATION_FINISHED));
					break;
				case SOBEL_SATURATION_DILATE:
					GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
					time = SystemClock.uptimeMillis();
					background.draw();
					GLES20.glFinish();
					time = SystemClock.uptimeMillis() - time;
					Log.d("draw time", "Sobel + Saturation Dilate time: " + String.valueOf(time));
					handler.dispatchMessage(Message.obtain(handler, GLConstants.SOBEL_SATURATION_DILATE_FINISHED));
					break;
				default: break;							
				}
			}
		}
	}

	@Override
	public void onSurfaceChanged(GL10 arg0, int width, int height) {
		mGLViewHeight = height;
		mGLViewWidth = width;
	}

	@Override
	public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
		GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
		/*int height = Image.image.getHeight();
		int width = Image.image.getWidth();*/
		
		
	/*	final float ratio = (float)width / height;
		final float left = -ratio;
		final float right = ratio;
		final float bottom = -1.0f;
		final float top = 1.0f;
		Matrix.orthoM(mProjectionMatrix, 0, left, right, bottom, top, -1, 1);
		
		final float eyeX = 0.0f;
		final float eyeY = 0.0f;
		final float eyeZ = 0.0f;
		
		final float lookX = 0.0f;
		final float lookY = 0.0f;
		final float lookZ = -5.0f;
		
		final float upX = 0.0f;
		final float upY = 1.0f;
		final float upZ = 0.0f;
		
		Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);*/
		//mMVPMatrix = mProjectionMatrix;
		Matrix.setIdentityM(mMVPMatrix, 0);
	}
	
	

	public int addVertexShader(String vertexShader) {
		int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);


		if (vertexShaderHandle != 0){
			GLES20.glShaderSource(vertexShaderHandle, vertexShader);
			GLES20.glCompileShader(vertexShaderHandle);

			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

			if (compileStatus[0] == 0) {
				Log.e("vertex shader",GLES20.glGetShaderInfoLog(vertexShaderHandle));
				GLES20.glDeleteShader(vertexShaderHandle);
				vertexShaderHandle = 0;
			}
		}
		if (vertexShaderHandle == 0) {
			throw new RuntimeException("Error creating vertex shader");
		}
		return vertexShaderHandle;
	}
	
	public int addFragmentShader(String fragmentShader) {
		int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
		
		if (fragmentShaderHandle != 0){
			GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);
			GLES20.glCompileShader(fragmentShaderHandle);
			
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
			
			if (compileStatus[0] == 0) {
				Log.e("fragment shader",GLES20.glGetShaderInfoLog(fragmentShaderHandle));
				GLES20.glDeleteShader(fragmentShaderHandle);
				fragmentShaderHandle = 0;
			}
		}
		if (fragmentShaderHandle == 0) throw new RuntimeException("Error creating fragment shader");
		return fragmentShaderHandle;
	}
	
	public int createProgram(int vertexShaderHandle, int fragmentShaderHandle, String[] attributes) {
		int programHandle = GLES20.glCreateProgram();
		
		if (programHandle != 0) {
			GLES20.glAttachShader(programHandle, vertexShaderHandle);
			GLES20.glAttachShader(programHandle, fragmentShaderHandle);
			
			int i = 0;
			for (String attrib : attributes){
				GLES20.glBindAttribLocation(programHandle, i, attrib);
				i++;
			}
			
			GLES20.glLinkProgram(programHandle);
			
			final int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
			
			if (linkStatus[0] == 0) {
				GLES20.glDeleteProgram(programHandle);
				programHandle = 0;
			}
		} 
		if (programHandle == 0) throw new RuntimeException("Error creating GLSL program.");
		
		return programHandle;
	}
	
	public void useProgram(int programHandle) {
		GLVariables.mCurrentProgramHandle = programHandle;
		GLES20.glUseProgram(GLVariables.mCurrentProgramHandle);
	}
	
	public void switchTexture() {
		long time = SystemClock.uptimeMillis();
		if (GLVariables.mTextureDataHandle == GLVariables.mTextureHandle[0]){
			GLVariables.mTextureDataHandle = GLVariables.mTextureHandle[1];
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLVariables.mTextureDataHandle);
			if (GLVariables.mDilatationPasses == GLConstants.SOBEL_SATURATION_OPEN_PASSES) GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			else GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, GLVariables.mTextureHandle[2], 0);
		} else if (GLVariables.mTextureDataHandle == GLVariables.mTextureHandle[1]) {
			GLVariables.mTextureDataHandle = GLVariables.mTextureHandle[2];
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLVariables.mTextureDataHandle);
			if (GLVariables.mDilatationPasses == GLConstants.SOBEL_SATURATION_OPEN_PASSES) GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			else GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, GLVariables.mTextureHandle[1], 0);
		} else if (GLVariables.mTextureDataHandle == GLVariables.mTextureHandle[2]) {
			GLVariables.mTextureDataHandle = GLVariables.mTextureHandle[1];
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLVariables.mTextureDataHandle);
			if (GLVariables.mDilatationPasses == GLConstants.SOBEL_SATURATION_OPEN_PASSES) GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			else GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, GLVariables.mTextureHandle[2], 0);
		}
		time = SystemClock.uptimeMillis() - time;
		Log.d("texture switch time", String.valueOf(time) + "ms");
	}

	public int setupFrameBufferForPreprocessing(Bitmap texture) {
		GLVariables.mTextureHandle = new int[]{0, 0};
		GLES20.glGenTextures(2, GLVariables.mTextureHandle, 0);
		
		if (GLVariables.mTextureHandle[0] != 0){
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLVariables.mTextureHandle[0]);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_MIRRORED_REPEAT);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_MIRRORED_REPEAT);

			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, texture, 0);
		} else throw new RuntimeException("Error loading texture");
		
		texture.recycle();
		
		if (GLVariables.mTextureHandle[1] != 0){
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLVariables.mTextureHandle[1]);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_MIRRORED_REPEAT);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_MIRRORED_REPEAT);
			
			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, texture.getWidth(), texture.getHeight(), 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
		} else throw new RuntimeException("Error loading texture");
		
		GLVariables.mFrameBufferHandle = new int[]{0};
		GLES20.glGenFramebuffers(1, GLVariables.mFrameBufferHandle, 0);

		if (GLVariables.mFrameBufferHandle[0] != 0) {
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLVariables.mFrameBufferHandle[0]);
			GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, GLVariables.mTextureHandle[1], 0);
		} else throw new RuntimeException("Error creating frame buffer");
		
		GLVariables.mCurrentTextureSize[0] = texture.getWidth();
		GLVariables.mCurrentTextureSize[1] = texture.getHeight();
		
		return GLVariables.mTextureHandle[0];
	}

	public int loadBitmapForProcessing(Bitmap texture) {

		GLVariables.mTextureHandle = new int[]{0, 0, 0};
		GLES20.glGenTextures(3, GLVariables.mTextureHandle, 0);

		if (GLVariables.mTextureHandle[0] != 0){

			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLVariables.mTextureHandle[0]);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, texture, 0);
		} else throw new RuntimeException("Error loading texture");
		
		if (GLVariables.mTextureHandle[1] != 0){

			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLVariables.mTextureHandle[1]);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

			//GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, Preview.viewWidth, Preview.viewHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, texture.getWidth(), texture.getHeight(), 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

		} else throw new RuntimeException("Error loading texture");

		if (GLVariables.mTextureHandle[2] != 0){

			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLVariables.mTextureHandle[2]);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

			//GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, Preview.viewWidth, Preview.viewHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, texture.getWidth(), texture.getHeight(), 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

		} else throw new RuntimeException("Error loading texture");

		GLVariables.mFrameBufferHandle = new int[]{0};
		GLES20.glGenFramebuffers(1, GLVariables.mFrameBufferHandle, 0);

		if (GLVariables.mFrameBufferHandle[0] != 0) {
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLVariables.mFrameBufferHandle[0]);
			GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, GLVariables.mTextureHandle[1], 0);
		} else throw new RuntimeException("Error creating frame buffer");

		GLVariables.mCurrentTextureSize[0] = texture.getWidth();
		GLVariables.mCurrentTextureSize[1] = texture.getHeight();
		
		return GLVariables.mTextureHandle[0];
	}
	
	public int getGLViewHeight() {
		return mGLViewHeight;
	}

	public int getGLViewWidth() {
		return mGLViewWidth;
	}


}
