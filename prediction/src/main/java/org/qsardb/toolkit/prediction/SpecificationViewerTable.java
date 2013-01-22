/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;

import org.openscience.cdk.qsar.*;

public class SpecificationViewerTable extends JTable {

	public SpecificationViewerTable(){
		super(new SpecificationModel());

		setFocusable(false);
		setShowGrid(false);

		TableColumnModel columnModel = getColumnModel();

		TableColumn name = columnModel.getColumn(0);
		name.setWidth(100);
		name.setMinWidth(100);
		name.setMaxWidth(100);
		name.setPreferredWidth(100);
		name.setResizable(false);
	}

	@Override
	public void addNotify(){
		super.addNotify();

		Container parent = getParent();

		setBackground(parent.getBackground());
		setForeground(parent.getForeground());
	}

	public void setDescriptor(IDescriptor descriptor){
		SpecificationModel model = getModel();

		model.setDescriptor(descriptor);
	}

	@Override
	public SpecificationModel getModel(){
		return (SpecificationModel)super.getModel();
	}
}