/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import java.util.*;

import javax.swing.table.*;

import org.openscience.cdk.qsar.*;

public class SpecificationModel extends AbstractTableModel {

	private IDescriptor descriptor = null;

	private List<Attribute> attributes = new ArrayList<Attribute>();


	public SpecificationModel(){
		this.attributes.add(new Attribute("Title"){

			@Override
			public String getValue(DescriptorSpecification specification){
				return specification.getImplementationTitle();
			}
		});

		this.attributes.add(new Attribute("Identifier"){

			@Override
			public String getValue(DescriptorSpecification specification){
				return specification.getImplementationIdentifier();
			}
		});

		this.attributes.add(new Attribute("Vendor"){

			@Override
			public String getValue(DescriptorSpecification specification){
				return specification.getImplementationVendor();
			}
		});
	}

	@Override
	public int getRowCount(){
		return this.attributes.size();
	}

	@Override
	public int getColumnCount(){
		return 2;
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
				IDescriptor descriptor = getDescriptor();
				if(descriptor == null){
					return null;
				}

				return attribute.getValue(descriptor.getSpecification());
			default:
				throw new IllegalArgumentException();
		}
	}

	public IDescriptor getDescriptor(){
		return this.descriptor;
	}

	public void setDescriptor(IDescriptor descriptor){
		this.descriptor = descriptor;

		fireTableDataChanged();
	}

	abstract static
	public class Attribute {

		private String name = null;


		public Attribute(String name){
			setName(name);
		}

		abstract
		public String getValue(DescriptorSpecification specification);

		public String getName(){
			return this.name;
		}

		private void setName(String name){
			this.name = name;
		}
	}
}