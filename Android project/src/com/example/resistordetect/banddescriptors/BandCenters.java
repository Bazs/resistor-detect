package com.example.resistordetect.banddescriptors;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;

public class BandCenters {
	private BandColor color;
	private Mat center = new Mat(1, 2, CvType.CV_64FC1);
	private double area;
	
	public BandCenters(double x, double y, BandColor color, double area) {
		center.put(0, 0, x);
		center.put(0, 1, y);
		this.color = color;
		this.area = area;
	}
	
	public BandCenters(Mat center, BandColor color, double area) throws RuntimeException {
		if ((center.width() != 2) || (center.height() != 1) || (center.type() != CvType.CV_64FC1)) throw new RuntimeException("Input Mat must be 1 row, 2 cols, CV_64FC1");
		this.center = center.clone();
		this.color = color;
		this.area = area;
	}
	
	public double getArea() {
		return area;
	}
	
	public void setArea(double area) {
		this.area = area;
	}
	
	public double getX() {
		return center.get(0, 0)[0];
	}
	
	public double getY() {
		return center.get(0, 1)[0];
	}
	
	public void setX(double x) {
		center.put(0, 0, x);
	}
	
	public void setY(double y) {
		center.put(0, 1, y);
	}
	
	public Mat getCenter() {
		return center;
	}
	
	
	public BandColor getColor() {
		return color;
	}
	
}
