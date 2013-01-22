/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import com.beust.jcommander.*;

public class InChIPredictor extends Predictor {

	@Parameter (
		names = {"--inchi"},
		description = "InChI",
		required = true
	)
	private String inChI = null;


	static
	public void main(String[] args) throws Exception {
		InChIPredictor predictor = new InChIPredictor();

		JCommander commander = new JCommander(predictor);
		commander.setProgramName(InChIPredictor.class.getName());

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
		predict(CdkUtil.parseInChI(this.inChI));
	}
}