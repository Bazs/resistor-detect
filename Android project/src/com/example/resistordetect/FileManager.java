package com.example.resistordetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.app.TaskStackBuilder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

public class FileManager {
	
	//public static List<>
	
	public static enum ImageType {
		IMAGE_TYPE_RGB, IMAGE_TYPE_LAB
	}
	
	private static class saveImageTaskParams {
		Bitmap bitmap;
		ImageType type;
		
		public saveImageTaskParams(Bitmap bitmap, ImageType type) {
			this.bitmap = bitmap;
			this.type = type;
		}
	}
	
	private static class loadImageTaskParams {
		File path;
		ImageView imageView;
		
		public loadImageTaskParams(File path, ImageView imageView) {
			this.path = path;
			this.imageView = imageView;
		}
	}
	
	private static class LoadImageTask extends AsyncTask<loadImageTaskParams, Void, Bitmap> {
		private ImageView imageView;
		
		@Override
		protected void onPreExecute() {
			Toast.makeText(ResistorDetect.getContext(), "Loading image...", Toast.LENGTH_SHORT).show();
		}
		
		
		@Override
		protected Bitmap doInBackground(loadImageTaskParams... params) {
			File path = params[0].path;
			this.imageView = params[0].imageView;
			BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
			bitmapOptions.inScaled = false;
			Bitmap image = BitmapFactory.decodeFile(path.getAbsolutePath());
			Image.imageBitmap = image;
			return image;
		}
		
		@Override
		protected void onPostExecute(Bitmap image){
			imageView.setImageBitmap(image);
		}
	}
	
	private static class SaveImageTask extends AsyncTask<saveImageTaskParams, String, Boolean> {
		@Override
		protected void onPreExecute() {
			Toast.makeText(ResistorDetect.getContext(), "Preparing picture for saving...", Toast.LENGTH_LONG).show();
		}
		
		@Override
		protected void onProgressUpdate(String... string) {
			Toast.makeText(ResistorDetect.getContext(), string[0], Toast.LENGTH_LONG).show();
		}
		
		@Override
		protected Boolean doInBackground(saveImageTaskParams... params) {
			ImageType type = params[0].type;
			Bitmap bitmap = params[0].bitmap;
			
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
			
			
			if (GLConstants.RGB_MODE == false) {
				Mat labMat = new Mat();
				Imgproc.cvtColor(mat, labMat, Imgproc.COLOR_RGB2Lab);
				mat = labMat.clone();
			}
			bitmap = Image.bitmapFromMat(mat);
			
			publishProgress(new String[]{"Saving picture..."});
			
			File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ResistorDetect");
			if (!imageStorageDir.exists()) {
				if (!imageStorageDir.mkdirs()) {
					Log.e("File error", "Directories couldn't be created!");
					return false;
				}
			}
			long space = imageStorageDir.getUsableSpace();
			if (imageStorageDir.getUsableSpace() < bitmap.getByteCount()) {
				Log.e("File error", "Not enough space to save image!");
				return false;
			}
		
			
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			File imageFile;
			switch (type) {
			case IMAGE_TYPE_LAB:
				imageFile = new File(imageStorageDir.getPath() + File.separator + "IMG_" + timeStamp + "_Lab.png");
				break;
			case IMAGE_TYPE_RGB:
				imageFile = new File(imageStorageDir.getPath() + File.separator + "IMG_" + timeStamp + "_RGB.png");
				break;
			default: return false;
			}
			
			try {
				FileOutputStream outStream = new FileOutputStream(imageFile);
				bitmap.compress(CompressFormat.PNG, 100, outStream);
				outStream.close();
				Log.d("FileManager", "Image saved.");
			} catch (IOException e) {
				Log.e("File error", "Error writing to file");
				e.printStackTrace();
				return false;
			}
			
			return true;
		}
		@Override
		protected void onPostExecute (Boolean success) {
			if (success) Toast.makeText(ResistorDetect.getContext(), "Picture saved", Toast.LENGTH_SHORT).show();
			else Toast.makeText(ResistorDetect.getContext(), "Picture could not be saved", Toast.LENGTH_SHORT).show();
		}
	}
	
	public static void saveImage(final Bitmap bitmap, final ImageType type) {
		new SaveImageTask().execute(new saveImageTaskParams(bitmap, type));
	}
	
	public static void loadImage(File imagePath, ImageView imageView) {
		new LoadImageTask().execute(new loadImageTaskParams(imagePath, imageView));
	}
	
}
