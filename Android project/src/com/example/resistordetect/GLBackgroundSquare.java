package com.example.resistordetect;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.util.Log;

public class GLBackgroundSquare {
	private FloatBuffer vertexBuffer;
	private FloatBuffer textureBuffer;
	private final float vertices[] = {
			-1.0f, -1.0f, 0.0f,
			0.0f, 1.0f, 0.0f, 1.0f,
			
			-1.0f, 1.0f, 0.0f,
			0.0f, 1.0f, 0.0f, 1.0f,
			
			1.0f, -1.0f, 0.0f,
			0.0f, 1.0f, 0.0f, 1.0f,
			
			1.0f, 1.0f, 0.0f,
			0.0f, 1.0f, 0.0f, 1.0f
	};
	private final float textureVertices[] = {
			0.0f, 1.0f,
			0.0f, 0.0f,
			1.0f, 1.0f,
			1.0f, 0.0f
	};
	
	
	public GLBackgroundSquare() {
		
		ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(vertices.length * GLConstants.mBytesPerFloat);
		vertexByteBuffer.order(ByteOrder.nativeOrder());	
		vertexBuffer = vertexByteBuffer.asFloatBuffer();
		vertexBuffer.put(vertices);
		
		ByteBuffer textureByteBuffer = ByteBuffer.allocateDirect(textureVertices.length * GLConstants.mBytesPerFloat);
		textureByteBuffer.order(ByteOrder.nativeOrder());	
		textureBuffer = textureByteBuffer.asFloatBuffer();
		textureBuffer.put(textureVertices);
	}
	
	public void draw() {
		vertexBuffer.position(GLConstants.mPositionOffset);		
		GLES20.glVertexAttribPointer(GLVariables.mPositionHandle, GLConstants.mPositionDataSize, GLES20.GL_FLOAT, false, GLConstants.mBytesPerStride, vertexBuffer);
		GLES20.glEnableVertexAttribArray(GLVariables.mPositionHandle);
		float[] temp = new float[vertices.length];
		GLES20.glGetVertexAttribfv(GLVariables.mPositionHandle, 0, temp, 0);
		
		vertexBuffer.position(GLConstants.mColorOffset);		
		GLES20.glVertexAttribPointer(GLVariables.mColorHandle, GLConstants.mColorDataSize, GLES20.GL_FLOAT, false, GLConstants.mBytesPerStride, vertexBuffer);
		GLES20.glEnableVertexAttribArray(GLVariables.mColorHandle);
		
		textureBuffer.position(0);
		GLES20.glVertexAttribPointer(GLVariables.mTextureCoordinateHandle, GLConstants.mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, textureBuffer);		
		GLES20.glEnableVertexAttribArray(GLVariables.mTextureCoordinateHandle);
		
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
		GLES20.glFinish();
	}
	
}

