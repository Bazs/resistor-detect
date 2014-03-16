package com.example.resistordetect.banddescriptors;

import org.opencv.imgproc.Moments;



public class BandMoments {
	private Moments moments;
	private BandColor color;
	
	public BandMoments(Moments moments, BandColor color) {
		this.moments = moments;
		this.color = color;
	}
	
	public Moments getMoments() {
		return moments;
	}
	
	public BandColor getColor() {
		return color;
	}
}
