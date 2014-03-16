package com.example.resistordetect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.util.Log;

public class ShaderStringFromTextStaticHelper {
	
	public static String readShaderRaw(int shaderSource) {
		Context con = ResistorDetect.getContext();
		InputStream stream = con.getResources().openRawResource(shaderSource);
		InputStreamReader inputReader = new InputStreamReader(stream);
		BufferedReader buffReader = new BufferedReader(inputReader);
		String line;
		StringBuilder shaderText = new StringBuilder();
		
		try {
			while ((line = buffReader.readLine()) != null) {
				shaderText.append(line);
				shaderText.append('\n');
			} 
		}catch (IOException e) {
			Log.e("shader", "Couldn't read the shader file");
			return null;
		}
		
		return shaderText.toString();
	}
	
}
