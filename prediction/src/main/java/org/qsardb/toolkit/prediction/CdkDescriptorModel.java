/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import java.util.*;

import javax.swing.*;

import org.openscience.cdk.qsar.*;

public class CdkDescriptorModel extends DefaultListModel implements Iterable<CdkDescriptor> {

	public CdkDescriptorModel(){
		addElement(new CdkDescriptor(null));

		DescriptorEngine engine = new DescriptorEngine(DescriptorEngine.MOLECULAR);

		List<IDescriptor> descriptors = engine.getDescriptorInstances();

		descriptors:
		for(IDescriptor descriptor : descriptors){
			String[] names = descriptor.getDescriptorNames();
			if(names.length > 1){

				// All SimpleMolecuralDescriptor instances refer the same IDescriptor instance
				for(String name : names){
					SimpleMolecularDescriptor simpleDescriptor = new SimpleMolecularDescriptor((IMolecularDescriptor)descriptor, name);

					addElement(new CdkDescriptor(simpleDescriptor));
				}

				continue descriptors;
			} else

			{
				addElement(new CdkDescriptor(descriptor));
			}
		}
	}

	@Override
	public Iterator<CdkDescriptor> iterator(){
		return new Iterator<CdkDescriptor>(){

			private int index = 0;


			@Override
			public boolean hasNext(){
				return this.index < getSize();
			}

			@Override
			public CdkDescriptor next(){
				return getElementAt(this.index++);
			}

			@Override
			public void remove(){
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public CdkDescriptor getElementAt(int index){
		return (CdkDescriptor)super.getElementAt(index);
	}
}