/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.event.*;

import org.qsardb.cargo.bodo.*;
import org.qsardb.model.*;
import org.qsardb.storage.directory.*;

import com.beust.jcommander.*;
import com.beust.jcommander.Parameter;

import org.openscience.cdk.qsar.*;

public class DescriptorEditor extends JFrame {

	@Parameter (
		names = {"--dir"},
		description = "QDB directory",
		required = true
	)
	private File dir = null;

	@Parameter (
		names = {"--id"},
		description = "The Id of the Descriptor to edit",
		required = true
	)
	private String id = null;

	private Qdb qdb = null;

	private Descriptor descriptor = null;

	private JList list = null;


	static
	public void main(final String... args){
		Runnable runnable = new Runnable(){

			@Override
			public void run(){

				try {
					mainGUI(args);
				} catch(Exception e){
					e.printStackTrace();
				}
			}
		};
		SwingUtilities.invokeLater(runnable);
	}

	static
	private void mainGUI(String... args) throws Exception {
		DescriptorEditor editor = new DescriptorEditor();

		JCommander commander = new JCommander(editor);
		commander.setProgramName(DescriptorEditor.class.getName());

		try {
			commander.parse(args);
		} catch(ParameterException pe){
			commander.usage();

			System.exit(-1);
		}

		editor.init();
	}

	public DescriptorEditor(){
		JPanel panel = new JPanel(new BorderLayout(2, 2));
		setContentPane(panel);

		JPanel descriptorPanel = createDescriptorPanel();
		panel.add(BorderLayout.CENTER, descriptorPanel);

		JPanel buttonPanel = createButtonPanel();
		panel.add(BorderLayout.SOUTH, buttonPanel);

		WindowListener windowListener = new WindowAdapter(){

			@Override
			public void windowClosing(WindowEvent event){
				close();
			}
		};
		addWindowListener(windowListener);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setSize(800, 400);
		setLocationByPlatform(true);
	}

	private JPanel createDescriptorPanel(){
		JPanel panel = new JPanel(new BorderLayout(2, 2));

		CdkDescriptorModel listModel = new CdkDescriptorModel();

		final
		JList list = new JList(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		this.list = list;

		panel.add(BorderLayout.WEST, new JScrollPane(list));

		JPanel infoPanel = new JPanel(new BorderLayout(2, 2));
		panel.add(BorderLayout.CENTER, infoPanel);

		final
		SpecificationViewerTable specificationTable = new SpecificationViewerTable();
		specificationTable.setRowSelectionAllowed(false);
		specificationTable.setColumnSelectionAllowed(false);
		infoPanel.add(BorderLayout.NORTH, new TitlePanel("Specification", specificationTable));

		final
		ParameterEditorTable parameterTable = new ParameterEditorTable();
		infoPanel.add(BorderLayout.CENTER, new TitlePanel("Parametrization", new JScrollPane(parameterTable)));

		ListSelectionListener selectionListener = new ListSelectionListener(){

			@Override
			public void valueChanged(ListSelectionEvent event){

				if(event.getValueIsAdjusting()){
					return;
				}

				CdkDescriptor descriptor = (CdkDescriptor)list.getSelectedValue();

				specificationTable.setDescriptor(descriptor.getDescriptor());
				parameterTable.setDescriptor(descriptor.getDescriptor());
			}
		};
		list.addListSelectionListener(selectionListener);

		return panel;
	}

	private JPanel createButtonPanel(){
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

		JButton select = new JButton("Store");
		panel.add(select);

		select.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent event){
				storeChanges();

				// XXX
				close();
			}
		});

		JButton cancel = new JButton("Cancel");
		panel.add(cancel);

		cancel.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent event){
				close();
			}
		});

		return panel;
	}

	private void init() throws Exception {
		Qdb qdb = new Qdb(new DirectoryStorage(this.dir));
		this.qdb = qdb;

		try {
			this.descriptor = this.qdb.getDescriptor(this.id);
			if(this.descriptor == null){
				throw new IllegalArgumentException("Descriptor \'" + this.id + "\' not found");
			}

			loadCargo();
		} catch(Exception e){
			close();
		}

		setVisible(true);
	}

	private void storeChanges(){

		try {
			try {
				storeCargo();
			} catch(Exception e){
				System.err.println(e);
			}

			this.qdb.storeChanges();
		} catch(Exception e){
			System.err.println(e);
		}
	}

	private void loadCargo() throws Exception {
		IDescriptor descriptor = null;

		if(this.descriptor.hasCargo(BODOCargo.class)){
			BODOCargo bodoCargo = this.descriptor.getCargo(BODOCargo.class);

			descriptor = BODOUtil.parse(bodoCargo.loadBodoDescriptor());
		}

		CdkDescriptor cdkDescriptor = new CdkDescriptor(descriptor);

		if(descriptor != null){
			CdkDescriptorModel listModel = (CdkDescriptorModel)this.list.getModel();

			int index = listModel.indexOf(cdkDescriptor);
			if(index > -1){
				CdkDescriptor listCdkDescriptor = listModel.getElementAt(index);

				(listCdkDescriptor.getDescriptor()).setParameters((cdkDescriptor.getDescriptor()).getParameters());
			}
		}

		this.list.setSelectedValue(cdkDescriptor, true);
	}

	private void storeCargo() throws Exception {
		CdkDescriptor cdkDescriptor = (CdkDescriptor)this.list.getSelectedValue();
		if(cdkDescriptor == null){
			return;
		}

		IDescriptor descriptor = cdkDescriptor.getDescriptor();
		if(descriptor != null){
			BODOCargo bodoCargo = this.descriptor.getOrAddCargo(BODOCargo.class);

			bodoCargo.storeBodoDescriptor(BODOUtil.format(descriptor));
		} else

		{
			if(this.descriptor.hasCargo(BODOCargo.class)){
				this.descriptor.removeCargo(BODOCargo.class);
			}
		}
	}

	private void close(){

		try {
			this.qdb.close();
		} catch(Exception e){
			System.err.println(e);
		}

		dispose();
	}

	static
	private class TitlePanel extends JPanel {

		public TitlePanel(String title, JComponent component){
			super(new BorderLayout());

			setBorder(BorderFactory.createTitledBorder(title));

			add(BorderLayout.CENTER, component);
		}
	}
}