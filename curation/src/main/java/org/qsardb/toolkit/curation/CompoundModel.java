/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.toolkit.curation;

import java.io.*;
import java.util.*;

import javax.swing.table.*;

import org.qsardb.model.*;
import org.qsardb.resolution.chemical.*;

public class CompoundModel extends AbstractTableModel {

	private Compound compound = null;

	private List<Attribute> attributes = new ArrayList<Attribute>();

	private List<Resolver> verifiers = new ArrayList<Resolver>();


	public CompoundModel(){
		this.attributes.add(new Attribute("Name"){

			@Override
			public String getValue(){
				return getCompound().getName();
			}

			@Override
			public void setValue(String value){
				getCompound().setName(value);
			}

			@Override
			public Identifier createIdentifier(){
				return new NameIdentifier();
			}

			@Override
			public Resolver createResolver(Identifier identifier){
				return new NameResolver(identifier);
			}

			@Override
			public Resolver createVerifier(Identifier identifier){
				return null;
			}
		});

		this.attributes.add(new Attribute("CAS"){

			@Override
			public String getValue(){
				return getCompound().getCas();
			}

			@Override
			public void setValue(String value){
				getCompound().setCas(value);
			}

			@Override
			public Identifier createIdentifier(){
				return new CASIdentifier();
			}

			@Override
			public Resolver createResolver(Identifier identifier){
				return new CASResolver(identifier);
			}

			@Override
			public Resolver createVerifier(Identifier identifier){
				return new CASVerifier(identifier);
			}
		});

		this.attributes.add(new Attribute("InChI"){

			@Override
			public String getValue(){
				return getCompound().getInChI();
			}

			@Override
			public void setValue(String value){
				getCompound().setInChI(value);
			}

			@Override
			public Identifier createIdentifier(){
				return new InChIIdentifier();
			}

			@Override
			public Resolver createResolver(Identifier identifier){
				return new InChIResolver(identifier);
			}

			@Override
			public Resolver createVerifier(Identifier identifier){
				return new InChIVerifier(identifier);
			}
		});

		this.attributes.add(new Attribute("Labels"){

			@Override
			public String getValue(){
				StringBuffer sb = new StringBuffer();

				String sep = "";

				Collection<String> labels = getCompound().getLabels();
				for(String label : labels){
					sb.append(sep).append(label);

					sep = ", ";
				}

				return sb.toString();
			}

			@Override
			public void setValue(String value){
				Collection<String> labels = getCompound().getLabels();
				labels.clear();

				for(StringTokenizer st = new StringTokenizer(value, ","); st.hasMoreTokens(); ){
					labels.add((st.nextToken()).trim());
				}
			}

			@Override
			public Identifier createIdentifier(){
				return null;
			}

			@Override
			public Resolver createResolver(Identifier identifier){
				return null;
			}

			@Override
			public Resolver createVerifier(Identifier identifier){
				return null;
			}
		});
	}

	@Override
	public int getRowCount(){
		return this.attributes.size();
	}

	@Override
	public int getColumnCount(){
		return 3;
	}

	@Override
	public String getColumnName(int column){

		switch(column){
			case 0:
				return "Name";
			case 1:
				return "Value";
			case 2:
				return "Status";
			default:
				throw new IllegalArgumentException();
		}
	}

	@Override
	public Class<?> getColumnClass(int column){
		return String.class;
	}

	@Override
	public String getValueAt(int row, int column){
		Attribute attribute = this.attributes.get(row);

		switch(column){
			case 0:
				return attribute.getName();
			case 1:
				return attribute.getValue();
			case 2:
				Status status = attribute.getStatus();

				return (status != null ? status.getValue() : null);
			default:
				throw new IllegalArgumentException();
		}
	}

	@Override
	public boolean isCellEditable(int row, int column){

		switch(column){
			case 1:
				return true;
			default:
				return false;
		}
	}

	@Override
	public void setValueAt(Object value, int row, int column){
		Attribute attribute = this.attributes.get(row);

		switch(column){
			case 1:
				attribute.setValue(value != null ? String.valueOf(value) : null); // XXX

				fireTableCellUpdated(row, column);
				break;
			default:
				throw new IllegalArgumentException();
		}
	}

	public List<Attribute> getAttributes(){
		return this.attributes;
	}

	public Compound getCompound(){
		return this.compound;
	}

	public void setCompound(Compound compound){
		this.compound = compound;

		verifyAttributes();

		fireTableDataChanged();
	}

	private void verifyAttributes(){
		this.verifiers.clear();

		List<Attribute> attributes = getAttributes();

		Attribute left = attributes.get(0);
		Identifier identifier = left.createIdentifier();

		for(int i = 1; i < attributes.size(); i++){
			Attribute right = attributes.get(i);
			right.setStatus(null);

			String value = right.getValue();
			if(value != null){
				Resolver resolver = right.createVerifier(identifier);

				if(resolver != null){
					verify(right, resolver);
				}
			}
		}
	}

	private void verify(final Attribute attribute, final Resolver verifier){
		Thread thread = new Thread(){

			@Override
			public void run(){
				Status status = Status.UNKNOWN;
				setStatus(status);

				try {
					verifier.resolve(getCompound());

					status = Status.FOUND_CORRECT;
				} catch(FileNotFoundException fnfe){
					status = Status.NOT_FOUND;
				} catch(Exception e){
					status = Status.FOUND_INCORRECT;
				}

				setStatus(status);

				removeVerifier(verifier);
			}

			private void setStatus(Status status){

				if((CompoundModel.this.verifiers).contains(verifier)){
					attribute.setStatus(status);

					int row = (CompoundModel.this.attributes).indexOf(attribute);
					int column = 2;

					fireTableCellUpdated(row, column);
				}
			}
		};

		addVerifier(verifier);

		thread.start();
	}

	private void addVerifier(Resolver verifier){
		this.verifiers.add(verifier);
	}

	private void removeVerifier(Resolver verifier){
		this.verifiers.remove(verifier);
	}

	abstract static
	public class Attribute {

		private String name = null;

		private Status status = null;


		public Attribute(String name){
			setName(name);
		}

		abstract
		public String getValue();

		abstract
		public void setValue(String value);

		public Status getStatus(){
			return this.status;
		}

		public void setStatus(Status status){
			this.status = status;
		}

		abstract
		public Identifier createIdentifier();

		abstract
		public Resolver createResolver(Identifier identifier);

		abstract
		public Resolver createVerifier(Identifier identifier);

		public String getName(){
			return this.name;
		}

		private void setName(String name){
			this.name = name;
		}
	}

	static
	private enum Status {
		UNKNOWN("Verifying.."),
		FOUND_CORRECT("Correct"),
		FOUND_INCORRECT("Not correct"),
		NOT_FOUND("Not found"),
		;

		private String value = null;


		Status(String value){
			setValue(value);
		}

		public String getValue(){
			return this.value;
		}

		private void setValue(String value){
			this.value = value;
		}
	}
}