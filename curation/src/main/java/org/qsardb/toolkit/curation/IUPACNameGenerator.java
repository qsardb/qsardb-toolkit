/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.toolkit.curation;

import org.qsardb.model.*;

import com.beust.jcommander.*;

public class IUPACNameGenerator extends Generator {

	static
	public void main(String... args) throws Exception {
		IUPACNameGenerator generator = new IUPACNameGenerator();

		JCommander commander = new JCommander(generator);
		commander.setProgramName(IUPACNameGenerator.class.getName());

		try {
			commander.parse(args);
		} catch(ParameterException pe){
			commander.usage();

			System.exit(-1);
		}

		generator.run();
	}

	@Override
	public void generate() throws Exception {
		CompoundRegistry compounds = getQdb().getCompoundRegistry();

		boolean changed = false;

		for(Compound compound : compounds){
			String name = compound.getName();

			compound.setName(MarvinUtil.nameToIUPACName(name));

			changed |= MarvinUtil.isChanged(name, compound.getName());
		}

		if(changed){
			compounds.storeChanges();
		}
	}
}