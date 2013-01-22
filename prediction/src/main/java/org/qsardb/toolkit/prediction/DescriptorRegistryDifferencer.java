/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import java.util.logging.*;

import org.qsardb.model.*;

import com.beust.jcommander.*;

public class DescriptorRegistryDifferencer extends ParameterRegistryDifferencer<DescriptorRegistry, Descriptor> {

	static
	public void main(String... args) throws Exception {
		DescriptorRegistryDifferencer differencer = new DescriptorRegistryDifferencer();

		JCommander commander = new JCommander(differencer);
		commander.setProgramName(DescriptorRegistryDifferencer.class.getName());

		try {
			commander.parse(args);
		} catch(ParameterException pe){
			commander.usage();

			System.exit(-1);
		}

		differencer.run();
	}

	@Override
	public void diff() throws Exception {
		boolean result = diff(getLeftQdb().getDescriptorRegistry(), getRightQdb().getDescriptorRegistry());

		if(result){
			logger.log(Level.WARNING, "Left-hand side and right-hand side differ");
		}
	}
}