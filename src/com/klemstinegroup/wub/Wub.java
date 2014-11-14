package com.klemstinegroup.wub;

public class Wub {

	public static void main(String[] args) {
//		AudioObject.factory("songs/test.play");
		if (args.length > 0)
			AudioObject.key = args[0];
		for (int i = 1; i < args.length; i++) {
			AudioObject.factory(args[i]);
		}
		if (args.length<2)AudioObject.factory();
	}

}
