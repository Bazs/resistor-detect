package com.example.resistordetect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;

public class Image {
	public static Bitmap imageBitmap;
	public static Mat imageMat;
	public static Bitmap lowResBitmap;
	public static Mat[] bandMats = new Mat[10];
	
	public static SurfaceTexture mCurrentFrame;
	
	public static Bitmap[] mBandsImages = new Bitmap[3];
	
	public static Bitmap sobelBitmap;
	public static Bitmap saturationBitmap;
	
	public static Bitmap bitmapFromMat(Mat mat) {
		Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.RGB_565);
		Utils.matToBitmap(mat, bitmap);
		return bitmap;
		
	}
}

