package com.example.resistordetect;

import android.app.Application;
import android.content.Context;

public class ResistorDetect extends Application {
	
	private static Context mContext;
	
	public void onCreate() {
		super.onCreate();
		mContext = this;
	}
	
	public static Context getContext() {
		return mContext;
	}
	
}
