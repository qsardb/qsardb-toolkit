/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import java.io.*;

import com.beust.jcommander.*;

import org.openscience.cdk.*;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.io.iterator.*;

public class SDFilePredictor extends Predictor {

	@Parameter (
		names = {"--sdfile"},
		description = "SD file",
		required = true
	)
	private File sdfile = null;


	static
	public void main(String... args) throws Exception {
		SDFilePredictor predictor = new SDFilePredictor();

		JCommander commander = new JCommander(predictor);
		commander.setProgramName(SDFilePredictor.class.getName());

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
		InputStream is = new FileInputStream(this.sdfile);

		try {
			IteratingMDLReader reader = new IteratingMDLReader(is, DefaultChemObjectBuilder.getInstance());

			while(reader.hasNext()){
				IAtomContainer molecule = reader.next();

				predict(molecule);
			}

			reader.close();
		} finally {
			is.close();
		}
	}
}