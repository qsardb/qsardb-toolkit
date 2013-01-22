/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.toolkit.curation;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

import javax.swing.*;

public class ValueSelectionDialog extends JDialog {

	private JList list = null;

	private String value = null;


	public ValueSelectionDialog(){
		super((Frame)null, true);

		this.list = new JList();
		this.list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		getContentPane().add(BorderLayout.CENTER, new JScrollPane(this.list));

		MouseListener mouseListener = new MouseAdapter(){

			@Override
			public void mouseClicked(MouseEvent event){

				if(event.getClickCount() >= 2){
					setValue(getSelectedValue());

					setVisible(false);
				}
			}
		};
		this.list.addMouseListener(mouseListener);

		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

		setSize(400, 300);
		setLocationByPlatform(true);
	}

	public String select(List<String> values){
		setValue(null);

		DefaultListModel model = new DefaultListModel();
		for(String value : values){
			model.addElement(value);
		}

		this.list.setModel(model);

		setVisible(true);

		return getValue();
	}

	private String getSelectedValue(){
		return (String)this.list.getSelectedValue();
	}

	private String getValue(){
		return this.value;
	}

	private void setValue(String value){
		this.value = value;
	}
}