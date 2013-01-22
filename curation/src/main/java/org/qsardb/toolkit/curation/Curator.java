/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.toolkit.curation;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;

import chemaxon.struc.*;

import org.qsardb.model.*;
import org.qsardb.storage.directory.*;

import com.beust.jcommander.*;
import com.beust.jcommander.Parameter;

public class Curator extends JFrame {

	@Parameter (
		names = "--dir",
		description = "QDB directory",
		required = true
	)
	private File dir = null;

	@Parameter (
		names = {"--offset"},
		description = "The Id of the Compound to start with"
	)
	private String offset = null;

	private Qdb qdb = null;

	private List<Compound> compounds = new ArrayList<Compound>();

	private int index = 0;

	private EventListenerList listeners = new EventListenerList();


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
		Curator curator = new Curator();

		JCommander commander = new JCommander(curator);
		commander.setProgramName(Curator.class.getName());

		try {
			commander.parse(args);
		} catch(ParameterException pe){
			commander.usage();

			System.exit(-1);
		}

		curator.init();
	}

	public Curator(){
		JPanel panel = new JPanel(new BorderLayout(2, 2));
		setContentPane(panel);

		JPanel compoundPanel = createCompoundPanel();
		panel.add(BorderLayout.CENTER, compoundPanel);

		JPanel buttonPanel = createButtonPanel();
		panel.add(BorderLayout.SOUTH, buttonPanel);

		ChangeListener changeListener = new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent event){
				setTitle("Compound " + String.valueOf(Curator.this.index + 1) + " of " + String.valueOf(Curator.this.compounds.size()));
			}
		};
		this.listeners.add(ChangeListener.class, changeListener);

		WindowListener windowListener = new WindowAdapter(){

			@Override
			public void windowClosing(WindowEvent event){
				int answer = JOptionPane.showConfirmDialog(Curator.this, "Save changes?", "Confirmation", JOptionPane.YES_NO_OPTION);

				switch(answer){
					case JOptionPane.YES_OPTION:
						storeChanges();
						break;
					default:
						break;
				}

				close();
			}
		};
		addWindowListener(windowListener);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setSize(600, 400);
		setLocationByPlatform(true);
	}

	private JPanel createCompoundPanel(){
		JPanel panel = new JPanel(new BorderLayout(2, 2));

		final
		MoleculeCanvas canvas = new MoleculeCanvas(this);
		panel.add(BorderLayout.CENTER, canvas);

		final
		CompoundEditorTable compoundTable = new CompoundEditorTable(this);
		panel.add(BorderLayout.SOUTH, new JScrollPane(compoundTable));

		ChangeListener changeListener = new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent event){
				Compound compound = getCompound();

				Molecule molecule = null;

				try {
					molecule = parseCompound(compound);
				} catch(Exception e){
					// Ignored
				}

				canvas.setMolecule(molecule);

				compoundTable.setCompound(compound);
			}
		};
		this.listeners.add(ChangeListener.class, changeListener);

		return panel;
	}

	private JPanel createButtonPanel(){
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

		final
		JButton previous = new JButton("Previous");
		panel.add(previous);

		previous.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent event){
				changeCompound(-1);
			}
		});

		final
		JButton next = new JButton("Next");
		panel.add(next);

		next.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent event){
				changeCompound(1);
			}
		});

		ChangeListener buttonListener = new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent event){
				previous.setEnabled(Curator.this.index > 0);
				next.setEnabled(Curator.this.index < Curator.this.compounds.size() - 1);
			}
		};
		this.listeners.add(ChangeListener.class, buttonListener);

		return panel;
	}

	private void init() throws Exception {
		Qdb qdb = new Qdb(new DirectoryStorage(this.dir));
		this.qdb = qdb;

		CompoundRegistry compounds = qdb.getCompoundRegistry();

		if(this.compounds.size() > 0){
			this.compounds.clear();
		}
		this.compounds.addAll(compounds);

		Compound compound = compounds.get(this.offset);
		if(this.offset != null && compound == null){
			logger.log(Level.WARNING, "Cannot find compound " + this.offset);
		}

		this.index = (compound != null ? this.compounds.indexOf(compound) : 0);

		refreshCompound();

		setVisible(true);
	}

	public void refreshCompound(){
		changeCompound(0);
	}

	private void changeCompound(int change){
		setIndex(this.index + change);

		ChangeEvent event = new ChangeEvent(this);

		Object[] objects = this.listeners.getListenerList();
		for(int i = 0; i < objects.length - 1; i += 2){
			Class<?> clazz = (Class<?>)objects[i];
			EventListener listener = (EventListener)objects[i + 1];

			if(ChangeListener.class.equals(clazz)){
				((ChangeListener)listener).stateChanged(event);
			}
		}
	}

	private void setIndex(int index){
		this.index = Math.min(Math.max(index, 0), this.compounds.size() - 1);
	}

	private Compound getCompound(){
		return this.compounds.get(this.index);
	}

	private void storeChanges(){

		try {
			this.qdb.storeChanges();
		} catch(Exception e){
			System.err.println(e);
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

	private Molecule parseCompound(Compound compound) throws Exception {
		Molecule molecule;

		String inChI = compound.getInChI();

		molecule = MarvinUtil.parseInChI(inChI);
		if(molecule != null){
			return molecule;
		}

		String name = normalize(compound.getName());

		molecule = MarvinUtil.parseName(name);
		if(molecule != null){

			if(!(compound.getName()).equals(name)){
				compound.setName(name);
			}

			return molecule;
		}

		throw new IllegalArgumentException(compound.getId());
	}

	static
	private String normalize(String string){

		for(int i = 0; i < string.length(); i++){
			char c = string.charAt(i);

			if(Character.isDigit(c) || (c == ',' || c == '-' || c == '\'')){
				continue;
			} // End if

			if(Character.isUpperCase(c)){
				String substring = string.substring(i);

				if(substring.startsWith("H-")){
					return string;
				} else

				if(substring.startsWith("N,") || substring.startsWith("N-") || substring.startsWith("N'")){
					return string;
				}

				return string.substring(0, i) + Character.toLowerCase(c) + string.substring(i + 1);
			}

			return string;
		}

		return string;
	}

	private static final Logger logger = Logger.getLogger(Curator.class.getName());
}