/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.toolkit.curation;

import java.awt.*;

import javax.swing.*;

import chemaxon.marvin.beans.*;
import chemaxon.struc.*;

public class MoleculeCanvas extends JPanel {

	private Curator curator = null;

	private MViewPane marvinPanel = null;


	public MoleculeCanvas(Curator curator){
		super();

		this.curator = curator;

		setLayout(new BorderLayout(2, 2));

		this.marvinPanel = new MViewPane();
		this.marvinPanel.setPopupMenusEnabled(true);

		add(BorderLayout.CENTER, this.marvinPanel);
	}

	public void setMolecule(Molecule molecule){
		this.marvinPanel.setM(0, molecule);
	}
}