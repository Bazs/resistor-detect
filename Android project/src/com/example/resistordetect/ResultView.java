package com.example.resistordetect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.objdetect.HOGDescriptor;
import org.opencv.utils.Converters;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;

import com.example.resistordetect.FileManager.ImageType;
import com.example.resistordetect.GLConstants.RendererState;
import com.example.resistordetect.banddescriptors.BandCenters;
import com.example.resistordetect.banddescriptors.BandCentersByAreaComparer;
import com.example.resistordetect.banddescriptors.BandCentersByXCoordComparer;
import com.example.resistordetect.banddescriptors.BandColor;
import com.example.resistordetect.banddescriptors.BandMoments;

class ResultView extends GLSurfaceView {
	private GLRenderer mRenderer;
	private Context mContext;
	private long time;
	private ResultActivity mHostActivity;
	private int mValue;
	private String centersText = new String();
	private  Handler mDecodeHandler = new Handler(){
		public void handleMessage (Message msg){
			switch (msg.what) {
			// Message received after the GLES20 context is ready (the first OnDraw() is called in the renderer)
			case GLConstants.DETECTION_SETUP_FINISHED:
				Log.d("handler", "Decode setup finished");
				GLVariables.openGLSetupTime = SystemClock.uptimeMillis() - GLVariables.openGLSetupTime;
				Log.d("OpenGL setup time", String.valueOf(GLVariables.openGLSetupTime) + "ms");
				time = SystemClock.uptimeMillis();
				
				GLVariables.mTextureDataHandle = mRenderer.loadBitmapForProcessing(Image.imageBitmap);
				
				/*mHostActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mHostActivity.setResultImage(0, Image.imageBitmap);
					}
				});*/
				
				//next line only for debugging
				//GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
				
				int height = GLVariables.mCurrentTextureSize[1];
				int width = GLVariables.mCurrentTextureSize[0];
				GLES20.glViewport(0, 0, width, height);
				
				GLVariables.vertexShaderHandle = mRenderer.addVertexShader(ShaderStringFromTextStaticHelper.readShaderRaw(R.raw.decode_vertex_shader));
				// the following line is used in RGB detection
				int mahalanobisShaderHandle = mRenderer.addFragmentShader(ShaderStringFromTextStaticHelper.readShaderRaw(R.raw.mahalanobis));
				// the following line is used in Lab detection
				//int mahalanobisShaderHandle = mRenderer.addFragmentShader(ShaderStringFromTextStaticHelper.readShaderRaw(R.raw.mahalanobis_2d));
				int mahalanobisProgramHandle = mRenderer.createProgram(GLVariables.vertexShaderHandle, mahalanobisShaderHandle, new String[] {"a_Position", "a_Color", "a_TexCoordinate"});
				mRenderer.useProgram(mahalanobisProgramHandle);
				/*int sobel_s_thresholdHandle = mRenderer.addFragmentShader(readShaderRaw(mContext, R.raw.sobel_s_thresh));
				int programSobelSaturationHandle = mRenderer.createProgram(GLVariables.vertexShaderHandle, sobel_s_thresholdHandle, new String[] {"a_Position", "a_Color", "a_TexCoordinate"});
				mRenderer.useProgram(programSobelSaturationHandle);*/

				GLVariables.mMVPMatrixHandle = GLES20.glGetUniformLocation(GLVariables.mCurrentProgramHandle, "u_MVPMatrix");
				GLVariables.mPositionHandle = GLES20.glGetAttribLocation(GLVariables.mCurrentProgramHandle, "a_Position");
				GLVariables.mColorHandle = GLES20.glGetAttribLocation(GLVariables.mCurrentProgramHandle, "a_Color");
				GLVariables.mTextureUniformHandle = GLES20.glGetUniformLocation(GLVariables.mCurrentProgramHandle, "u_Texture");
				GLVariables.mTextureCoordinateHandle = GLES20.glGetAttribLocation(GLVariables.mCurrentProgramHandle, "a_TexCoordinate");
				//GLVariables.mPixelSizeHandle = GLES20.glGetUniformLocation(GLVariables.mCurrentProgramHandle, "u_PixelSize");
				GLVariables.mMu1Handle = GLES20.glGetUniformLocation(GLVariables.mCurrentProgramHandle, "u_Mu1");
				GLVariables.mCinv1Handle = GLES20.glGetUniformLocation(GLVariables.mCurrentProgramHandle, "u_Cinv1");
				GLVariables.mThresh1Handle = GLES20.glGetUniformLocation(GLVariables.mCurrentProgramHandle, "u_Thresh1");
				GLVariables.mMu2Handle = GLES20.glGetUniformLocation(GLVariables.mCurrentProgramHandle, "u_Mu2");
				GLVariables.mCinv2Handle = GLES20.glGetUniformLocation(GLVariables.mCurrentProgramHandle, "u_Cinv2");
				GLVariables.mThresh2Handle = GLES20.glGetUniformLocation(GLVariables.mCurrentProgramHandle, "u_Thresh2");
				GLVariables.mMu3Handle = GLES20.glGetUniformLocation(GLVariables.mCurrentProgramHandle, "u_Mu3");
				GLVariables.mCinv3Handle = GLES20.glGetUniformLocation(GLVariables.mCurrentProgramHandle, "u_Cinv3");
				GLVariables.mThresh3Handle = GLES20.glGetUniformLocation(GLVariables.mCurrentProgramHandle, "u_Thresh3");
				GLVariables.mMu4Handle = GLES20.glGetUniformLocation(GLVariables.mCurrentProgramHandle, "u_Mu4");
				GLVariables.mCinv4Handle = GLES20.glGetUniformLocation(GLVariables.mCurrentProgramHandle, "u_Cinv4");
				GLVariables.mThresh4Handle = GLES20.glGetUniformLocation(GLVariables.mCurrentProgramHandle, "u_Thresh4");
				GLVariables.mWhiteBalanceRatiosHandle = GLES20.glGetUniformLocation(GLVariables.mCurrentProgramHandle, "u_WhiteBalanceRatios");
				
				GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLVariables.mTextureDataHandle);
				GLES20.glUniform1i(GLVariables.mTextureUniformHandle, 0);
				if (GLConstants.RGB_MODE == true) GLES20.glUniform3fv(GLVariables.mWhiteBalanceRatiosHandle, 1, GLVariables.mWhiteBalanceRatios, 0);
				else GLES20.glUniform3fv(GLVariables.mWhiteBalanceRatiosHandle, 1, new float[]{1.0f, 1.0f, 1.0f}, 0);
				//GLES20.glUniform2f(GLHandles.mPixelSizeHandle, (float)1.0/Preview.viewWidth, (float)1.0/Preview.viewHeight);
				
				//GLES20.glUniform2f(GLVariables.mPixelSizeHandle, (float)1.0/GLVariables.mCurrentTextureSize[0], (float)1.0/GLVariables.mCurrentTextureSize[1]);
				
				GLES20.glUniformMatrix4fv(GLVariables.mMVPMatrixHandle, 1, false, mRenderer.mMVPMatrix, 0);
				// the following line is used in RGB detection
				
				loadRGBColorDataIntoOpenGL(GLConstants.colorRGBBlackMu, GLConstants.colorRGBBlackC, GLConstants.colorRGBBrownMu, GLConstants.colorRGBBrownC, GLConstants.colorRGBRedMu, GLConstants.colorRGBRedC, GLConstants.colorRGBOrangeMu, GLConstants.colorRGBOrangeC);
				//loadRGBColorDataIntoOpenGL(GLConstants.colorRGBOrangeMu, GLConstants.colorRGBOrangeC, GLConstants.colorRGBBrownMu, GLConstants.colorRGBBrownC, GLConstants.colorRGBRedMu, GLConstants.colorRGBRedC, GLConstants.colorRGBBlackMu, GLConstants.colorRGBBlackC);
				//loadRGBColorDataIntoOpenGL(GLConstants.colorRGBBlackMu, GLConstants.colorRGBBlackC,GLConstants.colorRGBOrangeMu, GLConstants.colorRGBOrangeC, GLConstants.colorRGBRedMu, GLConstants.colorRGBRedC, GLConstants.colorRGBBrownMu, GLConstants.colorRGBBrownC);
				
				GLES20.glUniform1f(GLVariables.mThresh1Handle, GLConstants.colorRGBBlackThresh);
				GLES20.glUniform1f(GLVariables.mThresh2Handle, GLConstants.colorRGBBrownThresh);
				GLES20.glUniform1f(GLVariables.mThresh3Handle, GLConstants.colorRGBRedThresh);
				GLES20.glUniform1f(GLVariables.mThresh4Handle, GLConstants.colorRGBOrangeThresh);
				// the following line is used in Lab detection
				//loadLabColorDataIntoOpenGL(GLConstants.colorLabBlackMu, GLConstants.colorLabBlackC, GLConstants.colorLabBrownMu, GLConstants.colorLabBrownC, GLConstants.colorLabRedMu, GLConstants.colorLabRedC, GLConstants.colorLabOrangeMu, GLConstants.colorLabOrangeC);
				
				int[] readCinv = new int[]{1, 2, 3};
				GLES20.glGetUniformiv(GLVariables.mCurrentProgramHandle, GLVariables.mTextureUniformHandle, readCinv, 0);
				
				//GLVariables.mDetectionState = DetectionState.SOBEL_SATURATION;
				GLVariables.mRendererState = RendererState.MAHALANOBIS;
				
				time = SystemClock.uptimeMillis() - time;
				Log.d("Render setup time", String.valueOf(time) + "ms");
				
				requestRender();
				break;
			case GLConstants.MAHALANOBIS_FINISHED:
				Log.d("handler", "Mahalanobis finished");
				time = SystemClock.uptimeMillis();
				List<Mat> fromList;
				if (GLVariables.mMahalanobisPasses == 0){
					for (int i = 0; i < 10; i++) {
						Image.bandMats[i] = new Mat(GLVariables.mCurrentTextureSize[1], GLVariables.mCurrentTextureSize[0], CvType.CV_8UC1);
					}
				}
				if (GLVariables.mMahalanobisPasses < 3) {
					ByteBuffer readPixels = ByteBuffer.allocateDirect((int) (Image.imageMat.total() * 4));
					readPixels.order(ByteOrder.nativeOrder());
					GLES20.glReadPixels(0, 0, GLVariables.mCurrentTextureSize[0], GLVariables.mCurrentTextureSize[1], GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, readPixels);
					GLES20.glFinish();
					byte[] readPixelsBytes = new byte[readPixels.capacity()];
					readPixels.get(readPixelsBytes);
					//readPixels.asIntBuffer().get(readPixelsInts);
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
				
					time = SystemClock.uptimeMillis() - time;
					Log.d("pic extraction time", String.valueOf(time) + "ms");
					Image.imageMat = tempMat.clone();

					// perform morphological closing to get solid regions
					for (int i = 0; i < GLConstants.BAND_IMAGE_OPEN_PASSES - 1; i++) {
						Imgproc.dilate(Image.imageMat, tempMat, GLConstants.bandMorphKernel);
						Image.imageMat = tempMat.clone();
					}
					for (int i = 0; i < GLConstants.BAND_IMAGE_OPEN_PASSES - 1; i++) {
						Imgproc.erode(Image.imageMat, tempMat, GLConstants.bandMorphKernel);
						Image.imageMat = tempMat.clone();
					}
					
					// use median filtering to remove tiny blobs
					Imgproc.medianBlur(Image.imageMat, tempMat, 5);
					Image.imageMat = tempMat.clone();
					
			
					
					// create separate Mat-s for all 10 color bands
					MatOfInt fromTo;
					List<Mat> toList;
					if (GLVariables.mMahalanobisPasses < 2) {
						int[] fromToInt = {0,0, 1,1, 2,2, 3,3};
						fromTo = new MatOfInt(fromToInt);
						toList = new ArrayList<Mat>(4);
						for (int i = 0; i < 4; i++) {
							toList.add(Image.bandMats[GLVariables.mMahalanobisPasses * 4 + i]);
						}
					} else {
						int[] fromToInt = {0,0, 1,1};
						fromTo = new MatOfInt(fromToInt);
						toList = new ArrayList<Mat>(2);
						for (int i = 8; i < 10; i++) {
							toList.add(Image.bandMats[i]);
						}
					}
					Core.mixChannels(Arrays.asList(Image.imageMat), toList, fromTo);
					Image.mBandsImages[GLVariables.mMahalanobisPasses] = Image.bitmapFromMat(Image.imageMat);
					if (GLVariables.mMahalanobisPasses == 2){
						 mHostActivity.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									//for (int i = 1; i < 4; i++) mHostActivity.setResultImage(i, Image.mBandsImages[i-1]);
								}
							});
					}					

					GLVariables.mMahalanobisPasses++;
					switch (GLVariables.mMahalanobisPasses) {
					case 1:
						// the following line is used in RGB detection
						loadRGBColorDataIntoOpenGL(GLConstants.colorRGBYellowMu, GLConstants.colorRGBYellowC, GLConstants.colorRGBGreenMu, GLConstants.colorRGBGreenC, GLConstants.colorRGBBlueMu, GLConstants.colorRGBBlueC, GLConstants.colorRGBVioletMu, GLConstants.colorRGBVioletC);
						GLES20.glUniform1f(GLVariables.mThresh1Handle, GLConstants.colorRGBYellowThresh);
						GLES20.glUniform1f(GLVariables.mThresh2Handle, GLConstants.colorRGBGreenThresh);
						GLES20.glUniform1f(GLVariables.mThresh3Handle, GLConstants.colorRGBBlueThresh);
						GLES20.glUniform1f(GLVariables.mThresh4Handle, GLConstants.colorRGBVioletThresh);
						// the following line is used in Lab detection
						//loadLabColorDataIntoOpenGL(GLConstants.colorLabYellowMu, GLConstants.colorLabYellowC, GLConstants.colorLabGreenMu, GLConstants.colorLabGreenC, GLConstants.colorLabBlueMu, GLConstants.colorLabBlueC, GLConstants.colorLabVioletMu, GLConstants.colorLabVioletC);
					break;
					case 2: 
						// the following line is used in RGB detection
						loadRGBColorDataIntoOpenGLShort(GLConstants.colorRGBGrayMu, GLConstants.colorRGBGrayC, GLConstants.colorRGBWhiteMu, GLConstants.colorRGBWhiteC);
						GLES20.glUniform1f(GLVariables.mThresh1Handle, GLConstants.colorRGBGrayThresh);
						GLES20.glUniform1f(GLVariables.mThresh2Handle, GLConstants.colorRGBWhiteThresh);
						// the following line is used in Lab detection
						//loadLabColorDataIntoOpenGLShort(GLConstants.colorLabGrayMu, GLConstants.colorLabGrayC, GLConstants.colorLabWhiteMu, GLConstants.colorLabWhiteC);
					break;
					default: break;
					}
					requestRender();
				} else {
					GLVariables.mRendererState = RendererState.IDLE;

					Image.imageBitmap = Image.bitmapFromMat(Image.bandMats[1]);
					if (GLVariables.mMahalanobisPasses == 3) {
						GLVariables.mMahalanobisPasses++;
						
						//GLES20.glDeleteTextures(3, GLVariables.mTextureHandle, 0);
	
						List<MatOfPoint> maskPointsList = new ArrayList<MatOfPoint>();
						List<BandMoments> bandMomentsList = new ArrayList<BandMoments>();
						Moments currentMoments = new Moments();
						double largestArea = 0;
						BandColor[] colors = BandColor.values();

						for (int i = 0; i < 10; i++) {
							maskPointsList.clear();
							Imgproc.findContours(Image.bandMats[i], maskPointsList, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
							for (int j = 0; j < maskPointsList.size(); j++) {
								currentMoments = Imgproc.moments(maskPointsList.get(j));
								if (currentMoments.get_m00() > largestArea) largestArea = currentMoments.get_m00(); 
								bandMomentsList.add(new BandMoments(currentMoments, colors[i]));
							}
						}

						List<BandCenters> bandCentersList = new ArrayList<BandCenters>();
						double x, y;
						Mat currentCenter = new Mat(1, 2, CvType.CV_64FC1);
						Mat tempCenter = new Mat(1, 2, CvType.CV_64FC1);
						for (int i = 0; i < bandMomentsList.size(); i++) {
							currentMoments = bandMomentsList.get(i).getMoments();
							if (currentMoments.get_m00() > GLConstants.MINIMAL_DETECTED_BAND_AREA_RATIO * largestArea) {
								x = currentMoments.get_m10() / currentMoments.get_m00();
								y = currentMoments.get_m01() / currentMoments.get_m00();
								tempCenter.put(0, 0, x);
								tempCenter.put(0, 1, y);
								// project the band centers to the centerline of the resistor
								Core.gemm(GLVariables.mCenterLineProjectionMat, tempCenter.t(), 1, new Mat(), 0, currentCenter);
								//Log.d("Temp center", tempCenter.dump());
								bandCentersList.add(new BandCenters(currentCenter.t(), bandMomentsList.get(i).getColor(), currentMoments.get_m00()));
								//Log.d("Center", bandCentersList.get(bandCentersList.size() - 1).getCenter().dump());
							}
						}

						// order the bands by ascending x coordinate
						Collections.sort(bandCentersList, new BandCentersByXCoordComparer());
						
						// find the largest distance between two consecutive bands
						double largestDist = 0;
						double currentDist;

						// if there's only one band center found, quit with message to the user
						if (bandCentersList.size() < 2) {
							mHostActivity.runOnUiThread(new Runnable() {				
								@Override
								public void run() {
//									mHostActivity.setMessageBoxMessage("Only 1 color band found, try again");
//									mHostActivity.showDialogToUser();						
								}
							});

						} else {
							// in this array the i-th distance is the distance between the i-th and i+i -th band in bandCentersList
							double[] distArray = new double[bandCentersList.size() - 1];
							for (int i = 0; i < bandCentersList.size() - 1; i++) {
								Core.subtract(bandCentersList.get(i).getCenter(), bandCentersList.get(i + 1).getCenter(), tempCenter);
								currentDist = Math.sqrt(Math.pow(tempCenter.get(0, 0)[0], 2.0d) + Math.pow(tempCenter.get(0, 1)[0], 2.0d));
								distArray[i] = currentDist;
								if (currentDist > largestDist) largestDist = currentDist;
							}
							// if two same color bands are close together compared to the largest distance, merge them into one,
							// the merged band's center being the algebraic average of the two centers, and the areas are summed
							// this is the current length of the list after merges
							// this is the counter for traversing the original list
							int currentCounter = 0;
							// this is the counter for a merge in progress
							int currentMergeCounter = 0;
							// list of band centers, which are close to each other
							List<BandCenters> sourceMergeList = new ArrayList<BandCenters>();
							// the centers in the above list merged together by color
							List<BandCenters> targetMergeList = new ArrayList<BandCenters>();
							// list of colors already in the current merge
							List<BandColor> mergeColors = new ArrayList<BandColor>();
							// final list of merged band centers
							List<BandCenters> mergedBandCentersList = new ArrayList<BandCenters>();
							int[] mergedSameColorBandsCount = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

							
			
							
							for (currentCounter = 0; currentCounter < bandCentersList.size();) {
								x = 0;
								y = 0;
								sourceMergeList.clear();
								targetMergeList.clear();
								mergeColors.clear();
								currentMergeCounter = 0;
								for (int i = 0; i < 10; i++) {
									mergedSameColorBandsCount[i] = 0;
								}
								if (currentCounter < distArray.length - 1) {
									while (distArray[currentCounter + currentMergeCounter] < largestDist * 0.2d) {
										if (currentMergeCounter == 0) {
											sourceMergeList.add(bandCentersList.get(currentCounter));
											currentMergeCounter++;
										}
										sourceMergeList.add(bandCentersList.get(currentCounter + currentMergeCounter));
										currentMergeCounter++;
										if (currentMergeCounter == distArray.length) break;
									}
									if (sourceMergeList.isEmpty() == false) {
										for (int i = 0; i < sourceMergeList.size(); i++) {
											int j = 0;
											if (mergeColors.contains(sourceMergeList.get(i).getColor())) {
												for (; j < targetMergeList.size(); j++) {
													if (targetMergeList.get(j).getColor() == sourceMergeList.get(i).getColor()) break;
												}
											} else {
												mergeColors.add(sourceMergeList.get(i).getColor());
												targetMergeList.add(new BandCenters(0.0d, 0.0d, sourceMergeList.get(i).getColor(), 0.0d));
												j = targetMergeList.size() - 1;
											}
											targetMergeList.get(j).setX(targetMergeList.get(j).getX() + sourceMergeList.get(i).getX());
											targetMergeList.get(j).setY(targetMergeList.get(j).getY() + sourceMergeList.get(i).getY());
											targetMergeList.get(j).setArea(targetMergeList.get(j).getArea() + sourceMergeList.get(i).getArea());
											mergedSameColorBandsCount[targetMergeList.get(j).getColor().ordinal()]++;
										}
										Collections.sort(targetMergeList, new BandCentersByAreaComparer());
										targetMergeList.get(0).setX(targetMergeList.get(0).getX() / mergedSameColorBandsCount[targetMergeList.get(0).getColor().ordinal()]);
										targetMergeList.get(0).setY(targetMergeList.get(0).getY() / mergedSameColorBandsCount[targetMergeList.get(0).getColor().ordinal()]);
										mergedBandCentersList.add(targetMergeList.get(0));
										currentCounter = currentCounter + sourceMergeList.size();

									} else {
										mergedBandCentersList.add(bandCentersList.get(currentCounter));
										currentCounter++;
									}
								} else {
									mergedBandCentersList.add(bandCentersList.get(currentCounter));
									currentCounter++;
								}
							}
							
							for (BandCenters thisCenter : mergedBandCentersList) {
								centersText += "\nX: " + String.valueOf(thisCenter.getX()) + "\nY: " + String.valueOf(thisCenter.getY()) + "\nArea: " + String.valueOf(thisCenter.getArea()) + "\nColor: " + thisCenter.getColor().name();
								Log.d("Band center", "X: " + String.valueOf(thisCenter.getX()) + " Y: " + String.valueOf(thisCenter.getY()) + " Area: " + String.valueOf(thisCenter.getArea()) + " Color: " + thisCenter.getColor().name());
							}

							distArray = new double[mergedBandCentersList.size() - 1];
							for (int i = 0; i < mergedBandCentersList.size() - 1; i++) {
								Core.subtract(mergedBandCentersList.get(i).getCenter(), mergedBandCentersList.get(i + 1).getCenter(), tempCenter);
								currentDist = Math.sqrt(Math.pow(tempCenter.get(0, 0)[0], 2.0d) + Math.pow(tempCenter.get(0, 1)[0], 2.0d));
								distArray[i] = currentDist;
							}
							if (distArray[0] > distArray[distArray.length - 1]) {
								Collections.reverse(mergedBandCentersList);
							}
							mValue = 0;
							if (mergedBandCentersList.size() == 3) {
								mValue += mergedBandCentersList.get(0).getColor().ordinal() * 10;
								mValue += mergedBandCentersList.get(1).getColor().ordinal();
								mValue = (int) (mValue * Math.pow(10.0d, (double) mergedBandCentersList.get(2).getColor().ordinal()));
							} else if (mergedBandCentersList.size() == 4) {
								mValue += mergedBandCentersList.get(0).getColor().ordinal() * 100;
								mValue += mergedBandCentersList.get(1).getColor().ordinal() * 10;
								mValue += mergedBandCentersList.get(2).getColor().ordinal();
								mValue = (int) (mValue * Math.pow(10.0d, (double) mergedBandCentersList.get(3).getColor().ordinal()));
							}
							//switch (mergedBandCentersList.)
							Log.d("Value", String.valueOf(mValue) + "Ohms");
							
							mHostActivity.runOnUiThread(new Runnable() {				
								@Override
								public void run() {
									mHostActivity.setResistorValueTextView(String.valueOf(mValue) + " Ohms " + centersText);
									for (int i = 1; i < 4; i++) mHostActivity.setResultImage(i, Image.mBandsImages[i-1]);
								}
							});
							
						}
					}



					// TBC


				}
				break;
				/*case GLConstants.SOBEL_SATURATION_FINISHED:
				Log.d("handler","Sobel + Saturation finished");
				if (GLVariables.mDilatationPasses == 0 ) {
					
					time = SystemClock.uptimeMillis();
					int sobel_s_dilateHandle = mRenderer.addFragmentShader(readShaderRaw(mContext, R.raw.sobel_s_dilate_9by9));
					int programSobelSaturationDilatationHandle = mRenderer.createProgram(GLVariables.vertexShaderHandle, sobel_s_dilateHandle, new String[] {"a_Position", "a_Color", "a_TexCoordinate"});
					mRenderer.useProgram(programSobelSaturationDilatationHandle);
					
					GLVariables.mOffsetsHandle = GLES20.glGetUniformLocation(GLVariables.mCurrentProgramHandle, "u_Offsets");
					GLVariables.mKernelHandle = GLES20.glGetUniformLocation(GLVariables.mCurrentProgramHandle, "u_Kernel");
					GLVariables.mMVPMatrixHandle = GLES20.glGetUniformLocation(GLVariables.mCurrentProgramHandle, "u_MVPMatrix");
					GLVariables.mPositionHandle = GLES20.glGetAttribLocation(GLVariables.mCurrentProgramHandle, "a_Position");
					GLVariables.mColorHandle = GLES20.glGetAttribLocation(GLVariables.mCurrentProgramHandle, "a_Color");
					GLVariables.mTextureUniformHandle = GLES20.glGetUniformLocation(GLVariables.mCurrentProgramHandle, "u_Texture");
					GLVariables.mTextureCoordinateHandle = GLES20.glGetAttribLocation(GLVariables.mCurrentProgramHandle, "a_TexCoordinate");
					//GLHandles.mPixelSizeHandle = GLES20.glGetUniformLocation(GLHandles.mCurrentProgramHandle, "u_PixelSize");
					
					//GLES20.glUniform2f(GLHandles.mPixelSizeHandle, (float)1.0/Preview.viewWidth, (float)1.0/Preview.viewHeight);
					
					//GLES20.glUniform2f(GLHandles.mPixelSizeHandle, (float)1.0/Image.image.getWidth(), (float)1.0/Image.image.getHeight());
					
					GLES20.glUniform2fv(GLVariables.mOffsetsHandle, GLConstants.DILATE_KERNEL_SIZE, GLConstants.mOffsetsBuffer);
					GLES20.glUniform1fv(GLVariables.mKernelHandle, GLConstants.DILATE_KERNEL_SIZE, GLConstants.mMorph9by9KernelBuffer);
					GLES20.glUniformMatrix4fv(GLVariables.mMVPMatrixHandle, 1, false, GLRenderer.mMVPMatrix, 0);
					float[] readKernel = new float[GLConstants.DILATE_KERNEL_SIZE * 2];
					GLES20.glGetUniformfv(GLVariables.mCurrentProgramHandle, GLVariables.mOffsetsHandle, readKernel, 0);
					time = SystemClock.uptimeMillis() - time;
					Log.d("Dilate setup time", String.valueOf(time) + "ms");
				}
				
				
				mRenderer.switchTexture();
				GLVariables.mDetectionState = DetectionState.SOBEL_SATURATION_DILATE;
				
				requestRender();
				break;
			case GLConstants.SOBEL_SATURATION_DILATE_FINISHED:
				Log.d("handler","Sobel + Saturation Dilate finished" + ", pass " + String.valueOf(GLVariables.mDilatationPasses) );
				if (GLVariables.mDilatationPasses < GLConstants.mSobelSaturationDilatePasses){
					requestRender();
					GLVariables.mDilatationPasses++;
				} else if (GLVariables.mDilatationPasses == GLConstants.mSobelSaturationDilatePasses){
					
					ByteBuffer readImage = ByteBuffer.allocateDirect(3 * GLVariables.mCurrentTextureSize[1] * GLVariables.mCurrentTextureSize[1]);
					readImage.order(ByteOrder.nativeOrder());
					readImage.position(0);
					GLES20.glReadPixels(0, 0, GLVariables.mCurrentTextureSize[0], GLVariables.mCurrentTextureSize[1], GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, readImage);
					
					GLVariables.totalCalculationTime = SystemClock.uptimeMillis() - GLVariables.totalCalculationTime;
					Log.d("total time", String.valueOf(GLVariables.totalCalculationTime) + "ms");
				}
				break;*/
			}
		}
	};

	
	
	public ResultView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mHostActivity = (ResultActivity) mContext;
		setEGLContextClientVersion(2);
		mRenderer = new GLRenderer();
		
		setRenderer(mRenderer);
	}
	
	
	
	public boolean decodeProcess() {
		GLVariables.mRendererState = RendererState.DETECTION_SETUP;
		GLVariables.mDilatationPasses = 0;
		GLConstants.initConstants();
		//mHostActivity.setResultImage(1, Image.imageBitmap);
		
		
		// transform to L*a*b colorspace
		Mat labMat = new Mat();
		if (GLConstants.RGB_MODE == false) {
			labMat = new Mat();
			Imgproc.cvtColor(Image.imageMat, labMat, Imgproc.COLOR_RGB2Lab);
		}
		//FileManager.saveImage(Image.bitmapFromMat(labMat), ImageType.IMAGE_TYPE_LAB);
		
		Mat fullResGrayscaleMat = new Mat();
		Imgproc.cvtColor(Image.imageMat, fullResGrayscaleMat, Imgproc.COLOR_RGB2GRAY);
		
		// create reduced resolution image for initial detection of the resistor
		Mat lowResMat = new Mat();
		if (Image.imageMat.width() > 240){
			Imgproc.resize(Image.imageMat, lowResMat, new Size(240.0, 180.0));
		} else lowResMat = Image.imageMat.clone();
		Image.lowResBitmap = Bitmap.createBitmap(lowResMat.cols(), lowResMat.rows(), Bitmap.Config.RGB_565);
		Utils.matToBitmap(lowResMat, Image.lowResBitmap);
		/*GLVariables.mCurrentTextureSize[0] = Image.lowResBitmap.getWidth();
		GLVariables.mCurrentTextureSize[1] = Image.lowResBitmap.getHeight();*/
		
		// use Sobel operator for edge detection
		Mat lowResGrayscaleMat = new Mat();
		Mat HSVMat = new Mat();
		Imgproc.cvtColor(lowResMat, lowResGrayscaleMat, Imgproc.COLOR_RGB2GRAY);
	
		Mat sobelMatX = new Mat();
		Mat sobelMatY = new Mat();
		Imgproc.Sobel(lowResGrayscaleMat, sobelMatX, -1, 1, 0);
		Imgproc.Sobel(lowResGrayscaleMat, sobelMatY, -1, 0, 1);
		sobelMatX.convertTo(sobelMatX, CvType.CV_32F);
		sobelMatY.convertTo(sobelMatY, CvType.CV_32F);
		Mat sobelMat = new Mat();
		Core.magnitude(sobelMatX, sobelMatY, sobelMat);
		// threshold the the Sobel-ed image
		sobelMat.convertTo(sobelMat, CvType.CV_8UC1);
		Mat sobelBinaryMat = new Mat();
		Imgproc.threshold(sobelMat, sobelBinaryMat, 0xFF * GLConstants.SOBEL_THRESHOLD, 0xFF, Imgproc.THRESH_BINARY);
		
		//mHostActivity.setResultImage(1, Image.bitmapFromMat(sobelBinaryMat));
		
		// create saturation-thresholded image
		Imgproc.cvtColor(lowResMat, HSVMat, Imgproc.COLOR_RGB2HSV);
		int[] fromToInt = {1, 0};
		MatOfInt fromTo = new MatOfInt(fromToInt);
		Mat sChannelMat = new Mat(HSVMat.rows(), HSVMat.cols(), CvType.CV_8UC1);
		List<Mat> fromList = new ArrayList<Mat>(1);
		fromList.add(HSVMat);
		List<Mat> toList = new ArrayList<Mat>(1);
		toList.add(sChannelMat);
		Core.mixChannels(fromList, toList, fromTo);
		sChannelMat = toList.get(0);
		Mat sChannelBinaryMat = new Mat();
		Imgproc.threshold(sChannelMat, sChannelBinaryMat, 0xFF * 0.3, 0xFF, Imgproc.THRESH_BINARY);

		//mHostActivity.setResultImage(1, Image.bitmapFromMat(sChannelBinaryMat));
		
		// compute bitwise and of the two thresholded images
		Mat[] resistorMaskMemory = new Mat[GLConstants.ULTIMATE_EROSION_MEMORY_DEPTH];
		for (int i = 0; i < GLConstants.ULTIMATE_EROSION_MEMORY_DEPTH; i++) {
			resistorMaskMemory[i] = new Mat();
		}
		
		//mHostActivity.setResultImage(0, Image.bitmapFromMat(sChannelBinaryMat));
		
		GLConstants.morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(9.0, 9.0));
		Mat tempMat = new Mat();
		Mat tempMat2 = new Mat();
		time = SystemClock.uptimeMillis();
		for (int i = 0; i < GLConstants.SOBEL_SATURATION_OPEN_PASSES - 1; i++) {
			Imgproc.dilate(sobelBinaryMat, tempMat, GLConstants.morphKernel);
			Imgproc.dilate(sChannelBinaryMat, tempMat2, GLConstants.morphKernel);
			sobelBinaryMat = tempMat.clone();
			sChannelBinaryMat = tempMat2.clone();
		}

		
		
		for (int i = 0; i < GLConstants.SOBEL_SATURATION_OPEN_PASSES - 1; i++) {
			Imgproc.erode(sobelBinaryMat, tempMat, GLConstants.morphKernel);
			Imgproc.erode(sChannelBinaryMat, tempMat2, GLConstants.morphKernel);
			sobelBinaryMat = tempMat.clone();
			sChannelBinaryMat = tempMat2.clone();
		}
		time = SystemClock.uptimeMillis() - time;
		Log.d("CV dilation time", String.valueOf(time) + "ms");
		
		
		//mHostActivity.setResultImage(1, Image.bitmapFromMat(sChannelBinaryMat));
		//mHostActivity.setResultImage(1, Image.bitmapFromMat(sobelBinaryMat));
		Core.bitwise_and(sobelBinaryMat, sChannelBinaryMat, resistorMaskMemory[0]);
		//resistorMaskMemory[0] = sobelBinaryMat.clone();
		//mHostActivity.setResultImage(1, Image.bitmapFromMat(resistorMaskMemory[0]));
		
		// use ultimate erosion to get only the resistor body
		GLConstants.ultimateMorphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3.0, 3.0));
		int erodeRuns = 0;
		// first erode until nothing remains
		do {
			for (int i = 1; i < (GLConstants.ULTIMATE_EROSION_MEMORY_DEPTH); i++) {
				resistorMaskMemory[GLConstants.ULTIMATE_EROSION_MEMORY_DEPTH - i] = resistorMaskMemory[GLConstants.ULTIMATE_EROSION_MEMORY_DEPTH-i-1].clone(); 
			}
			Imgproc.erode(resistorMaskMemory[0], tempMat, GLConstants.ultimateMorphKernel);
			resistorMaskMemory[0] = tempMat;
			erodeRuns++;
		} while (Core.countNonZero(resistorMaskMemory[0]) != 0);
		// then grow back from an earlier state
		// if there were suspiciously few erosions, indicate error and return
		if (erodeRuns < GLConstants.ULTIMATE_EROSION_MEMORY) {
			mHostActivity.setMessageBoxMessage("Couldn't find resistor");
			return false;
		}
		Mat resistorMaskMat = resistorMaskMemory[Math.min(GLConstants.ULTIMATE_EROSION_MEMORY, GLConstants.ULTIMATE_EROSION_MEMORY_DEPTH - 1)].clone();
		//mHostActivity.setResultImage(1, Image.bitmapFromMat(resistorMaskMat));
		for (int i = 0; i < (erodeRuns - Math.min(GLConstants.ULTIMATE_EROSION_MEMORY, 9)); i++) {
			Imgproc.dilate(resistorMaskMat, tempMat, GLConstants.ultimateMorphKernel);
			resistorMaskMat = tempMat.clone();
		}
		
		//mHostActivity.setResultImage(1, Image.bitmapFromMat(resistorMaskMat));
	/*	// free unneeded resources
		grayscaleMat = null;
		HSVMat = null;
		sobelMatX = null;
		sobelMatY = null;
		sobelMat = null;
		sobelBinaryMat = null;
		sChannelMat = null;
		sChannelBinaryMat = null;
		dilatedSChannelMat = null;
		dilatedSobelBinaryMat = null;
		for (int i = 0; i < 9; i++) {
			resistorMaskMemory[i] = null;
		}*/
		
		Mat fullResResistorMaskMat = new Mat();
		Imgproc.resize(resistorMaskMat, fullResResistorMaskMat, Image.imageMat.size());
		fullResResistorMaskMat.convertTo(fullResResistorMaskMat, Image.imageMat.type());
		Mat maskedImageMat = new Mat();
		
		// following line used for RGB detection - now not in use
		Image.imageMat.copyTo(maskedImageMat, fullResResistorMaskMat);
		//mHostActivity.setResultImage(1, Image.bitmapFromMat(maskedImageMat));
		// following line used for Lab detection
	    if(GLConstants.RGB_MODE == false) labMat.copyTo(maskedImageMat, fullResResistorMaskMat);
		
		
		Image.imageMat = maskedImageMat.clone();
		
		//mHostActivity.setResultImage(1, Image.bitmapFromMat(fullResResistorMaskMat));
		
		// crop only to the masked area
		List<MatOfPoint> maskPointsList = new ArrayList<MatOfPoint>();
		Imgproc.findContours(fullResResistorMaskMat, maskPointsList, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		
		// in doing so, calculate the orientation of the main axis as well
		Moments resMoments = Imgproc.moments(maskPointsList.get(0));
		double mu11 = resMoments.get_mu11(); 
		double mu20 = resMoments.get_mu20();
		double mu02 = resMoments.get_mu02();
		GLVariables.mMainAxisAngle = Math.atan2(mu02, mu20);
		GLVariables.mMainAxisAngle = (mu11 > 0) ? -GLVariables.mMainAxisAngle : GLVariables.mMainAxisAngle;
		Log.d("Main axis angle", String.valueOf(GLVariables.mMainAxisAngle * 180 / Math.PI + " degrees"));
		double v = Math.tan(GLVariables.mMainAxisAngle);
		double den = 1.0d + Math.pow(v, 2.0d);
		GLVariables.mCenterLineProjectionMat = new Mat(2, 2, CvType.CV_64FC1);
		GLVariables.mCenterLineProjectionMat.put(0, 0, 1.0d / den);
		GLVariables.mCenterLineProjectionMat.put(0, 1, v / den);
		GLVariables.mCenterLineProjectionMat.put(1, 0, v / den);
		GLVariables.mCenterLineProjectionMat.put(1, 1, Math.pow(v, 2.0d) / den);
		//Log.d("Projection matrix", GLVariables.mCenterLineProjectionMat.dump());
		// the above Mat is the matrix of orthogonal projection onto the centerline of the resistor
		
		
		tempMat = new Mat(Image.imageMat, Imgproc.boundingRect(maskPointsList.get(0)));
		double ratio = Math.sqrt( (double) (GLConstants.OPTIMAL_RESISTOR_PIXEL_COUNT) / tempMat.total() );
		int width, height;
		width = (int) (ratio * tempMat.width());
		height = (int) (ratio * tempMat.height());
		width = (width % 2 == 1) ? width + 1 : width;
		height = (height % 2 == 1) ? height + 1 : height;
		Imgproc.resize(tempMat, Image.imageMat, new Size(width, height));
		Image.imageBitmap = Image.bitmapFromMat(Image.imageMat);
		
		//mHostActivity.setResultImage(1, Image.imageBitmap);
		
		/*tempMat = new Mat(fullResGrayscaleMat, Imgproc.boundingRect(maskPointsList.get(0)));
		Imgproc.resize(tempMat, Image.imageMat, new Size(width, height));

		time = SystemClock.uptimeMillis();
		Mat cannyMat = new Mat();
		Imgproc.Canny(Image.imageMat, cannyMat, 0.3 * 0xFF, 0.4 * 0xFF);
		time = SystemClock.uptimeMillis() - time;
		Log.d("Canny time", String.valueOf(time) + "ms");
		
	
		List<Mat> cannyMats = new ArrayList<Mat>(3);
		for (int i = 0; i < 3; i++) cannyMats.add(cannyMat.clone());
		Core.merge(cannyMats, tempMat);
		Mat houghLines = new Mat();
		Imgproc.HoughLinesP(cannyMat, houghLines, 1.0, Math.PI / 180.0d, 20, 8, 4);
		for (int i = 0; i < houghLines.total() / 4; i++) {
			Core.line(tempMat, new Point(houghLines.get(0, i)[0], houghLines.get(0, i)[1]), new Point(houghLines.get(0, i)[2], houghLines.get(0, i)[3]), new Scalar(0, 0, 255), 3);
		}*/
		
		
		GLVariables.mMahalanobisPasses = 0;
		
		// following commented-out code is for testing the OpenCV Mahalanobis calculation time
		
		/*Utils.bitmapToMat(Image.imageBitmap, Image.imageMat);
		double[] currColor = new double[3];
		double dist;
		double[] distList = new double[(int) Image.imageMat.total()];
		Mat currColorMat = new Mat(1, 3, CvType.CV_32F);
		Mat[] colorMasks = new Mat[10];
		for (int i = 0; i < 10; i++) {
			colorMasks[i] = new Mat(Image.imageMat.rows(), Image.imageMat.cols(), CvType.CV_8UC1);
		}
		time = SystemClock.uptimeMillis();
		Mat colorRGBREDMuMat = new Mat(1, 3, currColorMat.type());
		colorRGBREDMuMat.put(0, 0, GLConstants.colorRGBRedMu[0]);
		colorRGBREDMuMat.put(0, 1, GLConstants.colorRGBRedMu[1]);
		colorRGBREDMuMat.put(0, 2, GLConstants.colorRGBRedMu[2]);
		Mat colorRGBRedCMat = new Mat(3, 3, currColorMat.type());
		for (int i = 0; i < Image.imageMat.rows(); i++) {
			for (int j = 0; j < Image.imageMat.cols(); j++) {
				currColor = Image.imageMat.get(i, j);
				if (currColor[0] == 0.0 && currColor[1] == 0 && currColor[2] == 0) {
					colorMasks[2].put(i, j, 0);
				} else {
					currColorMat.put(0, 0, currColor);
					
					dist = Core.Mahalanobis(currColorMat, colorRGBREDMuMat, colorRGBRedCMat);
					distList[i * Image.imageMat.cols() + j] = dist;
					if (dist < 2.0d) colorMasks[2].put(i, j, 0xFF);
					else colorMasks[2].put(i, j, 0);
					//Log.d("Distance", String.valueOf(dist));
				}
			}
		}
		time = SystemClock.uptimeMillis() - time;
		Log.d("Mahalanobis time", String.valueOf(time) + "ms");*/
		//mHostActivity.setResultImage(Image.imageBitmap);
		
		
		
		
		mRenderer.openGLThreadPass(mDecodeHandler);

		requestRender();
		return true;
	}
	
	
	private List<Mat> loadColorDataMat(Context con, int colorSource) {
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
			return null;
		}
		
		String colorDataString =  colorDataText.toString();
		List<String> colorDataStrings = Arrays.asList(colorDataString.split(","));
		Mat muMat = new Mat(1, 3, CvType.CV_32F);
		int i = 0;
		for (; i < 3; i++) {
			muMat.put(0, i, Double.parseDouble(colorDataStrings.get(i)));
		}
		Mat CMat = new Mat(3, 3, CvType.CV_32F);
		for (; i< 12; i++) {
			CMat.put((i-3) / 3, i % 3, Double.parseDouble(colorDataStrings.get(i)));
		}
		List<Mat> mats = new ArrayList<Mat>(2);
		mats.add(muMat);
		mats.add(CMat);
		return mats;
	}
	
	private float[] loadColorDataFloat(Context con, int colorSource) {
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
			return null;
		}
		
		String colorDataString =  colorDataText.toString();
		List<String> colorDataStrings = Arrays.asList(colorDataString.split(","));
		float[] floats = new float[12];
		for (int i = 0; i < 12; i++) {
			floats[i] = Float.parseFloat(colorDataStrings.get(i));
		}
		return floats;

	}
	
	
	private void loadRGBColorDataIntoOpenGL (float[] mu1Float, float[] c1Float, float[] mu2Float, float[] c2Float, float[] mu3Float, float[] c3Float, float[] mu4Float, float[] c4Float) {
		GLES20.glUniform3f(GLVariables.mMu1Handle, mu1Float[0], mu1Float[1], mu1Float[2]);
		GLES20.glUniformMatrix3fv(GLVariables.mCinv1Handle, 1, false, c1Float, 0);
		GLES20.glUniform3f(GLVariables.mMu2Handle, mu2Float[0], mu2Float[1], mu2Float[2]);
		GLES20.glUniformMatrix3fv(GLVariables.mCinv2Handle, 1, false, c2Float, 0);
		GLES20.glUniform3f(GLVariables.mMu3Handle, mu3Float[0], mu3Float[1], mu3Float[2]);
		GLES20.glUniformMatrix3fv(GLVariables.mCinv3Handle, 1, false, c3Float, 0);
		GLES20.glUniform3f(GLVariables.mMu4Handle, mu4Float[0], mu4Float[1], mu4Float[2]);
		GLES20.glUniformMatrix3fv(GLVariables.mCinv4Handle, 1, false, c3Float, 0);
	}
	
	private void loadLabColorDataIntoOpenGL (float[] mu1Float, float[] c1Float, float[] mu2Float, float[] c2Float, float[] mu3Float, float[] c3Float, float[] mu4Float, float[] c4Float) {
		GLES20.glUniform2f(GLVariables.mMu1Handle, mu1Float[0], mu1Float[1]);
		GLES20.glUniformMatrix2fv(GLVariables.mCinv1Handle, 1, false, c1Float, 0);
		GLES20.glUniform2f(GLVariables.mMu2Handle, mu2Float[0], mu2Float[1]);
		GLES20.glUniformMatrix2fv(GLVariables.mCinv2Handle, 1, false, c2Float, 0);
		GLES20.glUniform2f(GLVariables.mMu3Handle, mu3Float[0], mu3Float[1]);
		GLES20.glUniformMatrix2fv(GLVariables.mCinv3Handle, 1, false, c3Float, 0);
		GLES20.glUniform2f(GLVariables.mMu4Handle, mu4Float[0], mu4Float[1]);
		GLES20.glUniformMatrix2fv(GLVariables.mCinv4Handle, 1, false, c3Float, 0);
	}
	
	private void loadRGBColorDataIntoOpenGLShort(float[] mu1Float, float[] c1Float, float[] mu2Float, float[] c2Float) {
		GLES20.glUniform3f(GLVariables.mMu1Handle, mu1Float[0], mu1Float[1], mu1Float[2]);
		GLES20.glUniformMatrix3fv(GLVariables.mCinv1Handle, 1, false, c1Float, 0);
		GLES20.glUniform3f(GLVariables.mMu2Handle, mu2Float[0], mu2Float[1], mu2Float[2]);
		GLES20.glUniformMatrix3fv(GLVariables.mCinv2Handle, 1, false, c2Float, 0);
	}
	
	private void loadLabColorDataIntoOpenGLShort(float[] mu1Float, float[] c1Float, float[] mu2Float, float[] c2Float) {
		GLES20.glUniform2f(GLVariables.mMu1Handle, mu1Float[0], mu1Float[1]);
		GLES20.glUniformMatrix2fv(GLVariables.mCinv1Handle, 1, false, c1Float, 0);
		GLES20.glUniform2f(GLVariables.mMu2Handle, mu2Float[0], mu2Float[1]);
		GLES20.glUniformMatrix2fv(GLVariables.mCinv2Handle, 1, false, c2Float, 0);
	}
	
}
