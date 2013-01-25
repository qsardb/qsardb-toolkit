/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.toolkit.curation;

import org.qsardb.model.*;

import com.beust.jcommander.*;
import com.beust.jcommander.Parameter;

public class InChIGenerator extends Generator {

	@Parameter (
		names = {"--standard"},
		description = "Standard InChI (suppress non-standard InChI options FixedH and SUU)",
		arity = 1
	)
	private boolean standard = true;


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

			compound.setInChI(MarvinUtil.nameToInChI(name, this.standard));

			changed |= MarvinUtil.isChanged(inChI, compound.getInChI());
		}

		if(changed){
			compounds.storeChanges();
		}
	}
}