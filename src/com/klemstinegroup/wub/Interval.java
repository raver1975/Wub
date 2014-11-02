package com.klemstinegroup.wub;

public class Interval {

	int min;
	int max;

	public Interval(int min, int max) {
		this.min = min;
		this.max = max;
	}

	public String toString() {
		return min + ":" + max;
	}
}
