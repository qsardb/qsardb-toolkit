/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.curation;

import chemaxon.struc.*;

public class CurationException extends RuntimeException {

	private Molecule left = null;

	private Molecule right = null;


	public CurationException(String message, Molecule left, Molecule right){
		super(message);

		setLeft(left);
		setRight(right);
	}

	public Molecule getLeft(){
		return this.left;
	}

	private void setLeft(Molecule left){
		this.left = left;
	}

	public Molecule getRight(){
		return this.right;
	}

	private void setRight(Molecule right){
		this.right = right;
	}
}