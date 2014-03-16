package com.example.resistordetect;

import java.util.ArrayList;
import java.util.List;

import android.hardware.Camera;
import android.util.Log;

public class CameraPreviewSizeStaticHelper {

	public static Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters params) {
    	Camera.Size result = null;
    	
    	List<Camera.Size> temp = params.getSupportedPreviewSizes();
    	
    	for (String modes : params.getSupportedFocusModes()) {
    		Log.d("modes", modes);
    	}
    	
    	for (Camera.Size size : params.getSupportedPreviewSizes()) {
    		if (size.width <= width && size.height <= height) {
    			if (result == null) {
    				result = size;
    			}
    		} else if (result != null) {
    			int resultArea = result.width * result.height;
    			int newArea = size.width * size.height;
    			
    			if (newArea > resultArea) {
    				result = size;
    			}
    		}
    	}
    	
    	return result;
    }
	
}
