package com.example.resistordetect.banddescriptors;

import java.util.Comparator;

public class BandCentersByAreaComparer implements Comparator<BandCenters> {

	@Override
	public int compare(BandCenters lhs, BandCenters rhs) {
		double l = lhs.getArea();
		double r = lhs.getArea();
		return l < r ?  -1
				  :l > r ? 1
				  :0; 
	}

}
