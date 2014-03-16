package com.example.resistordetect.banddescriptors;

import java.util.Comparator;

public class BandCentersByXCoordComparer implements Comparator<BandCenters> {

	@Override
	public int compare(BandCenters lhs, BandCenters rhs) {
		double l = lhs.getX();
		double r = rhs.getX();
		return l < r ?  -1
			  :l > r ? 1
			  :0;
	}
	
}
