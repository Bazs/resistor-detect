package com.example.resistordetect;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

public class ResultActivity extends Activity {

	private ResultView mGLView;
	private ImageView mResultImageView1, mResultImageView2, mResultImageView3, mResultImageView4;
	private float[] tapCoords;
	private static String messageBoxMessage;
	private TextView mResistorValueTextView;
	
	public static class MessageBox extends DialogFragment {
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        // Use the Builder class for convenient dialog construction
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setMessage(messageBoxMessage)
	               .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                       // FIRE ZE MISSILES!
	                   }
	               });
	        // Create the AlertDialog object and return it
	        return builder.create();
	    }
	}
	

	public void showMessage() {
	    DialogFragment newFragment = new MessageBox();
	    newFragment.show(getFragmentManager(), "dialogbox");
	}
	
	public void setMessageBoxMessage(String message) {
		messageBoxMessage = message;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent starterIntent = getIntent();
		tapCoords = starterIntent.getFloatArrayExtra(GLConstants.EXTRA_MESSAGE);		
		
		setContentView(R.layout.result_activity);
		
		mResultImageView1 = (ImageView) findViewById(R.id.ResultImageView1);
		mResultImageView2 = (ImageView) findViewById(R.id.ResultImageView2);
		mResultImageView3 = (ImageView) findViewById(R.id.ResultImageView3);
		mResultImageView4 = (ImageView) findViewById(R.id.ResultImageView4);
		
		mResistorValueTextView = (TextView) findViewById(R.id.ResistorValueTextView);
		mGLView = (ResultView) findViewById(R.id.ResultViewID);
		mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}

	protected void onPause() {
		super.onPause();
		mGLView.onPause();
	}
	
	protected void onResume() {
		super.onResume();
		mGLView.onResume();
		if (mGLView.decodeProcess() == false) showMessage();
		
	}
	
	protected void showDialogToUser() {
		showMessage();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.result, menu);
		return true;
	}
	
	public void setResultImage(int index, Bitmap bitmap) {
		switch(index) {
		case 1:
			mResultImageView1.setImageBitmap(bitmap);
			break;
		case 2:
			mResultImageView2.setImageBitmap(bitmap);
			break;
		case 3:
			mResultImageView3.setImageBitmap(bitmap);
			break;
		default: break;
		}
	}

	public void setResistorValueTextView(String text) {
		mResistorValueTextView.setText(text);
		mResistorValueTextView.setTextSize(20);
	}
}
