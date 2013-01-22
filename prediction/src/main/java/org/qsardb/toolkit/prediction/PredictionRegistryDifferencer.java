/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import java.util.logging.*;

import org.qsardb.model.*;

import com.beust.jcommander.*;

public class PredictionRegistryDifferencer extends ParameterRegistryDifferencer<PredictionRegistry, Prediction> {

	static
	public void main(String... args) throws Exception {
		PredictionRegistryDifferencer differencer = new PredictionRegistryDifferencer();

		JCommander commander = new JCommander(differencer);
		commander.setProgramName(PredictionRegistryDifferencer.class.getName());

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
		boolean result = diff(getLeftQdb().getPredictionRegistry(), getRightQdb().getPredictionRegistry());

		if(result){
			logger.log(Level.WARNING, "Left-hand side and right-hand side differ");
		}
	}
}