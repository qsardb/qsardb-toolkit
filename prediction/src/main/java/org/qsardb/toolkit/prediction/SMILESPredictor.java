/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import com.beust.jcommander.*;

public class SMILESPredictor extends Predictor {

	@Parameter (
		names = {"--smiles"},
		description = "SMILES",
		required = true
	)
	private String smiles = null;


	static
	public void main(String... args) throws Exception {
		SMILESPredictor predictor = new SMILESPredictor();

		JCommander commander = new JCommander(predictor);
		commander.setProgramName(SMILESPredictor.class.getName());

		try {
			commander.parse(args);
		} catch(ParameterException pe){
			commander.usage();

			System.exit(-1);
		}

		predictor.run();
	}

	@Override
	public void predict() throws Exception {
		predict(CdkUtil.parseSMILES(this.smiles));
	}
}