/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import org.openscience.cdk.qsar.*;

public class CdkDescriptor {

	private IDescriptor descriptor = null;


	public CdkDescriptor(IDescriptor descriptor){
		setDescriptor(descriptor);
	}

	@Override
	public int hashCode(){
		return formatDisplayName(getDescriptor()).hashCode();
	}

	@Override
	public boolean equals(Object object){

		if(object instanceof CdkDescriptor){
			CdkDescriptor that = (CdkDescriptor)object;

			return (formatDisplayName(this.getDescriptor())).equals(formatDisplayName(that.getDescriptor()));
		}

		return false;
	}

	@Override
	public String toString(){
		return formatDisplayName(getDescriptor());
	}

	public IDescriptor getDescriptor(){
		return this.descriptor;
	}

	private void setDescriptor(IDescriptor descriptor){
		this.descriptor = descriptor;
	}

	static
	private String formatDisplayName(IDescriptor descriptor){

		if(descriptor != null){

			if(descriptor instanceof SimpleMolecularDescriptor){
				SimpleMolecularDescriptor simpleDescriptor = (SimpleMolecularDescriptor)descriptor;

				return formatName(simpleDescriptor.getDescriptor()) + " [" + simpleDescriptor.getName() + "]";
			}

			return formatName(descriptor);
		} else

		{
			return "(none)";
		}
	}

	static
	private String formatName(IDescriptor descriptor){
		String name = (descriptor.getClass()).getSimpleName();

		return name.replace("Descriptor", "");
	}
}