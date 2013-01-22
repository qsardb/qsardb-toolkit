/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import javax.swing.*;
import javax.swing.table.*;

import org.openscience.cdk.qsar.*;

public class ParameterEditorTable extends JTable {

	public ParameterEditorTable(){
		super(new ParameterModel());

		TableColumnModel columnModel = getColumnModel();

		TableColumn name = columnModel.getColumn(0);
		name.setWidth(100);
		name.setMinWidth(100);
		name.setMaxWidth(100);
		name.setPreferredWidth(100);
	}

	public void setDescriptor(IDescriptor descriptor){
		ParameterModel model = getModel();

		CellEditor editor = getCellEditor();
		if(editor != null){
			editor.cancelCellEditing();
		}

		model.setDescriptor(descriptor);
	}

	@Override
	public ParameterModel getModel(){
		return (ParameterModel)super.getModel();
	}
}