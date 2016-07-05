package com.itjw.gcodefx;

import java.util.Collection;

import javafx.scene.Group;
import javafx.scene.Node;

public class Layer extends Group {

	public Layer() {
	}

	public Layer(Node... children) {
		super(children);
	}

	public Layer(Collection<Node> children) {
		super(children);
	}

}
