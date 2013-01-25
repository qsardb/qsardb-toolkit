/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.toolkit.curation;

import org.qsardb.model.*;

import com.beust.jcommander.*;

public class InChIGenerator extends Generator {

	static
	public void main(String... args) throws Exception {
		InChIGenerator generator = new InChIGenerator();

		JCommander commander = new JCommander(generator);
		commander.setProgramName(InChIGenerator.class.getName());

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
			String inChI = compound.getInChI();

			compound.setInChI(MarvinUtil.nameToInChI(name));

			changed |= MarvinUtil.isChanged(inChI, compound.getInChI());
		}

		if(changed){
			compounds.storeChanges();
		}
	}
}