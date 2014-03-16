package com.example.resistordetect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.util.Log;


public class GLConstants {
	public static final boolean RGB_MODE = false;
	
	public final static String EXTRA_MESSAGE = "com.example.resistordetect.tapCoords";
	
	public static final int mBytesPerFloat = 4;
	public static final int mBytesPerStride = 7 * mBytesPerFloat;
	public static final int mPositionOffset = 0;
	public static final int mColorOffset = 3;
	public static final int mPositionDataSize = 3;
	public static final int mColorDataSize = 4;
	public static final int mTextureCoordinateDataSize = 2;
	public static final int SOBEL_SATURATION_OPEN_PASSES = 4;
	public static final int BAND_IMAGE_OPEN_PASSES = 4;
	public static final int DILATE_KERNEL_SIZE = 81;
	public static final int ULTIMATE_EROSION_MEMORY = 10;
	public static final int ULTIMATE_EROSION_MEMORY_DEPTH = 20;
	public static final long OPTIMAL_RESISTOR_PIXEL_COUNT = 27500l;
	public static final double MINIMAL_DETECTED_BAND_AREA_RATIO = 0.2d;
	public static final double SOBEL_THRESHOLD = 0.2;
	
	public static final int HISTOGRAM_BRACKETS = 180;
	public static final float HISTOGRAM_RANGES = 255.0f;
	public static final float DESIRED_MAXIMUM_INTENSITY_RATIO = 0.8f;
	public static final float DESIRED_MAXIMUM_INTENSITY = GLConstants.HISTOGRAM_RANGES * GLConstants.DESIRED_MAXIMUM_INTENSITY_RATIO;
	public static final float HISTOGRAM_BRACKET_SIZE = GLConstants.HISTOGRAM_RANGES / GLConstants.HISTOGRAM_BRACKETS;
	public static final MatOfInt[] mHistogramChannels = new MatOfInt[]{new MatOfInt(0), new MatOfInt(1), new MatOfInt(2)};
	public static final MatOfInt mHistogramSizes = new MatOfInt(new int[]{GLConstants.HISTOGRAM_BRACKETS});
	public static final MatOfFloat mHistogramRanges = new MatOfFloat(new float[]{0.0f, GLConstants.HISTOGRAM_RANGES});
	
	public static final Mat mNullMat = new Mat();
	
	public static Mat morphKernel;
	public static Mat ultimateMorphKernel;
	public static Mat bandMorphKernel;
	
	public static float[] colorRGBBlackMu = new float[3];
	public static float[] colorRGBBlackC = new float[9];
	public static float[] colorRGBBrownMu = new float[3];
	public static float[] colorRGBBrownC = new float[9];
	public static float[] colorRGBRedMu = new float[3];
	public static float[] colorRGBRedC = new float[9];
	public static float[] colorRGBOrangeMu = new float[3];
	public static float[] colorRGBOrangeC = new float[9];
	public static float[] colorRGBYellowMu = new float[3];
	public static float[] colorRGBYellowC = new float[9];
	public static float[] colorRGBGreenMu = new float[3];
	public static float[] colorRGBGreenC = new float[9];
	public static float[] colorRGBBlueMu = new float[3];
	public static float[] colorRGBBlueC = new float[9];
	public static float[] colorRGBVioletMu = new float[3];
	public static float[] colorRGBVioletC = new float[9];
	public static float[] colorRGBGrayMu = new float[3];
	public static float[] colorRGBGrayC = new float[9];	
	public static float[] colorRGBWhiteMu = new float[3];
	public static float[] colorRGBWhiteC = new float[9];
	
	public static float[] colorLabBlackMu = new float[2];
	public static float[] colorLabBlackC = new float[4];
	public static float[] colorLabBrownMu = new float[2];
	public static float[] colorLabBrownC = new float[4];
	public static float[] colorLabRedMu = new float[2];
	public static float[] colorLabRedC = new float[4];
	public static float[] colorLabOrangeMu = new float[2];
	public static float[] colorLabOrangeC = new float[4];
	public static float[] colorLabYellowMu = new float[2];
	public static float[] colorLabYellowC = new float[4];
	public static float[] colorLabGreenMu = new float[2];
	public static float[] colorLabGreenC = new float[4];
	public static float[] colorLabBlueMu = new float[2];
	public static float[] colorLabBlueC = new float[4];
	public static float[] colorLabVioletMu = new float[2];
	public static float[] colorLabVioletC = new float[4];
	public static float[] colorLabGrayMu = new float[2];
	public static float[] colorLabGrayC = new float[4];	
	public static float[] colorLabWhiteMu = new float[2];
	public static float[] colorLabWhiteC = new float[4];
	
	public static float colorRGBBlackThresh = 125f;
	public static float colorRGBBrownThresh = 125f;
	public static float colorRGBRedThresh = 100f;
	public static float colorRGBOrangeThresh = 100f;
	public static float colorRGBYellowThresh = 0.02f;
	public static float colorRGBGreenThresh = 100f;
	public static float colorRGBBlueThresh = 0.02f;
	public static float colorRGBVioletThresh = 0.02f;
	public static float colorRGBGrayThresh = 60f;
	public static float colorRGBWhiteThresh = 120f;
	
	static {
		
	}
	
	public static enum RendererState {
		PREVIEW_SETUP, PREVIEW_RUNNING, SET_WHITE_BALANCE, DETECTION_PREPROCESS_STARTUP, DETECTION_PREPROCESS, PREVIEW_FINISHED, PREVIEW_DISPLAYING, DETECTION_SETUP, SOBEL_SATURATION, SOBEL_SATURATION_DILATE, IDLE, MAHALANOBIS;
	}
	
	
	public static final int	DETECTION_SETUP_FINISHED = 0;
	public static final int	SOBEL_SATURATION_FINISHED = 1;
	public static final int	SOBEL_SATURATION_DILATE_FINISHED = 2;
	public static final int MAHALANOBIS_FINISHED = 3;
	public static final int PREVIEW_SETUP_FINISHED = 4;
	public static final int DETECTION_PREPROCESS_STARTUP_FINISHED = 5;
	public static final int DETECTION_PREPROCESS_FINISHED = 6;

	
	private static final float mMorph9by9Kernel[] = {
		0.0f,     0.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     0.0f,     0.0f,
	     0.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     0.0f,
	     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,
	     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,
	     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,
	     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,
	     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,
	     0.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     0.0f,
	     0.0f,     0.0f,     1.0f,     1.0f,     1.0f,     1.0f,     1.0f,     0.0f,     0.0f
	};
	
	public static FloatBuffer mOffsetsBuffer;
	public static FloatBuffer mMorph9by9KernelBuffer;
	public static FloatBuffer mCinvBuffer;
	
	public static void initConstants(){
		// calculate the texture coordinate offsets for a 9x9 convolution kernel
		//float pixelSize[] = new float[2];
		//pixelSize[0] = (float) 1.0/Preview.viewWidth;
		//pixelSize[1] = (float) 1.0/Preview.viewHeight;
		
		/*pixelSize[0] = (float) 1.0/GLVariables.mCurrentTextureSize[0];
		pixelSize[1] = (float) 1.0/GLVariables.mCurrentTextureSize[1];
		
		int kernelWidth = (int) Math.sqrt(GLConstants.DILATE_KERNEL_SIZE);
		int kernelMiddle = (int) Math.floor(kernelWidth / 2.0);

		float offsets[] = new float[GLConstants.DILATE_KERNEL_SIZE*2];
		for (int i = 0; i < kernelWidth; i++) {
			for (int j = 0; j < kernelWidth; j++) {
				offsets[(j*kernelWidth + i) * 2] = pixelSize[0]*(i-kernelMiddle);
				offsets[(j*kernelWidth + i) * 2 + 1] = pixelSize[1]*(j-kernelMiddle);
			}
		}

		ByteBuffer offsetsByteBuffer = ByteBuffer.allocateDirect(offsets.length * GLConstants.mBytesPerFloat);
		offsetsByteBuffer.order(ByteOrder.nativeOrder());
		// the offsets are stored in a static variable of the GLConstants class
		GLConstants.mOffsetsBuffer = offsetsByteBuffer.asFloatBuffer();
		GLConstants.mOffsetsBuffer.put(offsets);
		mOffsetsBuffer.position(0);
		
		// loads the morphological kernel into a static ByteBuffer
		ByteBuffer morhp9by9KernelByteBuffer = ByteBuffer.allocateDirect(mMorph9by9Kernel.length * GLConstants.mBytesPerFloat);
		morhp9by9KernelByteBuffer.order(ByteOrder.nativeOrder());
		GLConstants.mMorph9by9KernelBuffer = morhp9by9KernelByteBuffer.asFloatBuffer();
		GLConstants.mMorph9by9KernelBuffer.put(GLConstants.mMorph9by9Kernel);
		mMorph9by9KernelBuffer.position(0);*/
		
	/*	ByteBuffer mCinvByteBuffer = ByteBuffer.allocateDirect(9 * GLConstants.mBytesPerFloat);
		mCinvByteBuffer.order(ByteOrder.nativeOrder());
		GLConstants.mCinvBuffer = mCinvByteBuffer.asFloatBuffer();*/
		
		GLConstants.bandMorphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5.0, 5.0));
		
		loadColorDataFloatRGB(ResistorDetect.getContext(), R.raw.android_black_constants, colorRGBBlackMu, colorRGBBlackC);
		loadColorDataFloatRGB(ResistorDetect.getContext(), R.raw.android_brown_constants, colorRGBBrownMu, colorRGBBrownC);
		loadColorDataFloatRGB(ResistorDetect.getContext(), R.raw.android_red_constants, colorRGBRedMu, colorRGBRedC);
		loadColorDataFloatRGB(ResistorDetect.getContext(), R.raw.android_orange_constants, colorRGBOrangeMu, colorRGBOrangeC);
		loadColorDataFloatRGB(ResistorDetect.getContext(), R.raw.android_yellow_constants, colorRGBYellowMu, colorRGBYellowC);
		loadColorDataFloatRGB(ResistorDetect.getContext(), R.raw.android_green_constants, colorRGBGreenMu, colorRGBGreenC);
		loadColorDataFloatRGB(ResistorDetect.getContext(), R.raw.android_blue_constants, colorRGBBlueMu, colorRGBBlueC);
		loadColorDataFloatRGB(ResistorDetect.getContext(), R.raw.android_violet_constants, colorRGBVioletMu, colorRGBVioletC);
		loadColorDataFloatRGB(ResistorDetect.getContext(), R.raw.android_gray_constants, colorRGBGrayMu, colorRGBGrayC);
		loadColorDataFloatRGB(ResistorDetect.getContext(), R.raw.android_white_constants, colorRGBWhiteMu, colorRGBWhiteC);
		
		/*loadColorDataFloatLab(ResistorDetect.getContext(), R.raw.android_black_lab_constants, colorLabBlackMu, colorLabBlackC);
		loadColorDataFloatLab(ResistorDetect.getContext(), R.raw.android_brown_lab_constants, colorLabBrownMu, colorLabBrownC);
		loadColorDataFloatLab(ResistorDetect.getContext(), R.raw.android_red_lab_constants, colorLabRedMu, colorLabRedC);
		loadColorDataFloatLab(ResistorDetect.getContext(), R.raw.android_orange_lab_constants, colorLabOrangeMu, colorLabOrangeC);
		loadColorDataFloatLab(ResistorDetect.getContext(), R.raw.android_yellow_lab_constants, colorLabYellowMu, colorLabYellowC);
		loadColorDataFloatLab(ResistorDetect.getContext(), R.raw.android_green_lab_constants, colorLabGreenMu, colorLabGreenC);
		loadColorDataFloatLab(ResistorDetect.getContext(), R.raw.android_blue_lab_constants, colorLabBlueMu, colorLabBlueC);
		loadColorDataFloatLab(ResistorDetect.getContext(), R.raw.android_violet_lab_constants, colorLabVioletMu, colorLabVioletC);
		loadColorDataFloatLab(ResistorDetect.getContext(), R.raw.android_gray_lab_constants, colorLabGrayMu, colorLabGrayC);
		loadColorDataFloatLab(ResistorDetect.getContext(), R.raw.android_white_lab_constants, colorLabWhiteMu, colorLabWhiteC);*/
	}
	
	public static void loadCinv(float[] colorRedCFloat) {
		GLConstants.mCinvBuffer.put(colorRedCFloat);
		GLConstants.mCinvBuffer.position(0);
	}
	
	private static boolean loadColorDataFloatLab(Context con, int colorSource, float[] muFloat, float[] cFloat) {
		InputStream stream = con.getResources().openRawResource(colorSource);
		InputStreamReader inputReader = new InputStreamReader(stream);
		BufferedReader buffReader = new BufferedReader(inputReader);
		String line;
		StringBuilder colorDataText = new StringBuilder();
		
		try {
			while ((line = buffReader.readLine()) != null) {
				colorDataText.append(line);
				colorDataText.append(',');
			} 
		}catch (IOException e) {
			Log.e("Color data", "Couldn't read color data file");
			return false;
		}
		
		String colorDataString =  colorDataText.toString();
		List<String> colorDataStrings = Arrays.asList(colorDataString.split(","));
		int i = 0;
		for (; i < 2; i++) muFloat[i] = Float.parseFloat(colorDataStrings.get(i));
		for (; i < 6; i++) cFloat[i - 2] = Float.parseFloat(colorDataStrings.get(i));
		return true;
	}
	
	private static boolean loadColorDataFloatRGB(Context con, int colorSource, float[] muFloat, float[] cFloat) {
		InputStream stream = con.getResources().openRawResource(colorSource);
		InputStreamReader inputReader = new InputStreamReader(stream);
		BufferedReader buffReader = new BufferedReader(inputReader);
		String line;
		StringBuilder colorDataText = new StringBuilder();
		
		try {
			while ((line = buffReader.readLine()) != null) {
				colorDataText.append(line);
				colorDataText.append(',');
			} 
		}catch (IOException e) {
			Log.e("Color data", "Couldn't read color data file");
			return false;
		}
		
		String colorDataString =  colorDataText.toString();
		List<String> colorDataStrings = Arrays.asList(colorDataString.split(","));
		int i = 0;
		for (; i < 3; i++) muFloat[i] = Float.parseFloat(colorDataStrings.get(i));
		for (; i < 12; i++) cFloat[i - 3] = Float.parseFloat(colorDataStrings.get(i));
		return true;

	}
	
}
