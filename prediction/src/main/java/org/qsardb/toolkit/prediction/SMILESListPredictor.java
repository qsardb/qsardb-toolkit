/*
 * Copyright (c) 2013 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import java.io.*;

import com.beust.jcommander.*;

public class SMILESListPredictor extends Predictor {

	@Parameter (
		names = {"--smiles-file"},
		description = "SMILES list file",
		required = true
	)
	private File smilesFile = null;


	static
	public void main(String... args) throws Exception {
		SMILESListPredictor predictor = new SMILESListPredictor();

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
		InputStream is = new FileInputStream(this.smilesFile);

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

			while(true){
				String line = reader.readLine();
				if(line == null){
					break;
				}

				predict(CdkUtil.parseSMILES(line.trim()));
			}

			reader.close();
		} finally {
			is.close();
		}
	}
}