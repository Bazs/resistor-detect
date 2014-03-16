package com.example.resistordetect;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import com.example.resistordetect.GLConstants.RendererState;
import com.example.resistordetect.banddescriptors.BandMoments;

public class GLVariables {
	public static int mMVPMatrixHandle;
	public static int mPositionHandle;
	public static int mColorHandle;
	public static int mTextureUniformHandle;
	public static int mTextureCoordinateHandle;
	public static int mPixelSizeHandle;
	public static int mMu1Handle;
	public static int mCinv1Handle;
	public static int mThresh1Handle;
	public static int mMu2Handle;
	public static int mCinv2Handle;
	public static int mThresh2Handle;
	public static int mMu3Handle;
	public static int mCinv3Handle;
	public static int mThresh3Handle;
	public static int mMu4Handle;
	public static int mCinv4Handle;
	public static int mThresh4Handle;
	public static int mWhiteBalanceRatiosHandle;
	public static int mTextureDataHandle;
	public static int mCurrentProgramHandle;
	public static int[] mTextureHandle = new int[3];
	public static int[] mPreviewTextureHandle = new int[1];
	public static int[] mFrameBufferHandle = new int[1];
	public static int mOffsetsHandle;
	public static int mKernelHandle;
	public static RendererState mRendererState;
	public static int mDilatationPasses = 0;
	public static int mMahalanobisPasses = 0;
	public static double mMainAxisAngle;
	public static Mat mCenterLineProjectionMat;
	public static long mPreviewRenderTime;
	public static float mPreviewFPS;
	
	public static boolean mPresetWhiteBalance;
	
	public static Mat[] mHistogram = new Mat[]{new Mat(), new Mat(), new Mat()};
	public static float[] mBackgroundColorValues = new float[3];
	public static float[] mWhiteBalanceRatios = new float[]{1.0f, 1.0f, 1.0f};
	public static List<Mat> mPreviewChannels = new ArrayList<Mat>(4);
	public static Mat mWhiteBalanceFrame = new Mat();
	
	public static BandMoments[] mBandMoments = new BandMoments[10];
	
	// Width, Height of the image currently in use in OpenGL
	public static int mCurrentTextureSize[] = new int[2];
	
	public static int vertexShaderHandle;
	public static int vertexShader9by9ConvolutionHandle;
	
	public static long totalCalculationTime;
	public static long openGLSetupTime;
	
	
	/*static {
		GLVariables.mDetectionState = DetectionState.SETUP;
		GLVariables.mDilatationPasses = 0;
	}*/
}
