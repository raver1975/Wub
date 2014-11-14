package com.klemstinegroup.wub;

import java.io.Serializable;
import java.util.ArrayList;

public class CentralCommandNode implements Serializable{

	ArrayList<Node> nodes = new ArrayList<Node>();

	public CentralCommandNode() {
	}
	
	public CentralCommandNode(ArrayList<Node> c){
		nodes.addAll(c);
	}

}
