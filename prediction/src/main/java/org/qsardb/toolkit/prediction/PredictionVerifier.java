/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import java.io.*;
import java.math.*;
import java.util.*;
import java.util.logging.*;

import org.qsardb.cargo.map.*;
import org.qsardb.cargo.rds.*;
import org.qsardb.evaluation.*;
import org.qsardb.evaluation.Evaluator.Result;
import org.qsardb.model.*;
import org.qsardb.toolkit.*;

import com.beust.jcommander.*;
import com.beust.jcommander.Parameter;

public class PredictionVerifier {

	@Parameter (
		names = {"--log-level"},
		description = "The logging level. " + LevelConverter.MESSAGE,
		converter = LevelConverter.class
	)
	private Level level = Level.INFO;

	@Parameter (
		names = {"--id"},
		description = "Prediction Id",
		required = true
	)
	private String id = null;

	@Parameter (
		names = {"--scale"},
		description = "The scale for java.math.BigDecimal comparsion"
	)
	private int scale = 0;

	@Parameter (
		names = {"--archive"},
		description = "QDB file or directory",
		required = true
	)
	private File archive = null;

	private Qdb qdb = null;

	private Prediction prediction = null;

	private Evaluator evaluator = null;


	static
	public void main(String... args) throws Exception {
		PredictionVerifier verifier = new PredictionVerifier();

		JCommander commander = new JCommander(verifier);
		commander.setProgramName(PredictionVerifier.class.getName());

		try {
			commander.parse(args);
		} catch(ParameterException pe){
			commander.usage();

			System.exit(-1);
		}

		verifier.run();
	}

	private void verify() throws Exception {
		Prediction prediction = getPrediction();

		ValuesCargo valuesCargo = prediction.getCargo(ValuesCargo.class);

		Map<String, String> values = valuesCargo.loadStringMap();

		int count = 0;

		Set<String> ids = values.keySet();
		for(String id : ids){
			String value = values.get(id);

			Map<Descriptor, Object> parameters = new LinkedHashMap<Descriptor, Object>();

			List<Descriptor> descriptors = this.evaluator.getDescriptors();
			for(Descriptor descriptor : descriptors){
				Object parameter = getValue(descriptor, id);

				parameters.put(descriptor, parameter);
			}

			logger.log(Level.FINE, "Verifying Compound \'" + id + "\': " + formaDescriptortMap(parameters));

			Result result = this.evaluator.evaluate(parameters);

			boolean equals = ValueUtil.equals(value, result.getValue(), this.scale);
			if(equals){
				count++;
			} else

			{
				logger.log(Level.WARNING, "Compound '" + id + "': " + value + " vs. " + result.getValue());
			}
		}
	}

	private Object getValue(Descriptor descriptor, String id) throws IOException {
		ValuesCargo valuesCargo = descriptor.getCargo(ValuesCargo.class);

		Map<String, String> values = valuesCargo.loadStringMap();

		String value = values.get(id);

		try {
			return new BigDecimal(value);
		} catch(Exception e){
			// Ignored
		}

		return value;
	}

	public void run() throws Exception {
		logger.setLevel(this.level);

		Qdb qdb = new Qdb(StorageUtil.openInput(this.archive));

		setQdb(qdb);

		try {
			try {
				initEvaluator();

				try {
					verify();
				} finally {
					destroyEvaluator();
				}
			} finally {
				qdb.close();
			}
		} finally {
			setQdb(null);
		}
	}

	private void initEvaluator() throws Exception {
		Prediction prediction = getQdb().getPrediction(this.id);
		if(prediction == null){
			throw new IllegalArgumentException("Prediction '" + this.id + "' not found");
		}

		setPrediction(prediction);

		Model model = prediction.getModel();

		EvaluatorFactory evaluatorFactory = EvaluatorFactory.getInstance();
		evaluatorFactory.setActivating(true);

		Evaluator evaluator = evaluatorFactory.getEvaluator(model);

		evaluator.init();

		this.evaluator = evaluator;
	}

	private void destroyEvaluator() throws Exception {
		setPrediction(null);

		Evaluator evaluator = this.evaluator;

		try {
			evaluator.destroy();
		} finally {
			this.evaluator = null;

			if(Context.getEngine() != null){
				Context.stopEngine();
			}
		}
	}

	public Qdb getQdb(){
		return this.qdb;
	}

	private void setQdb(Qdb qdb){
		this.qdb = qdb;
	}

	public Prediction getPrediction(){
		return this.prediction;
	}

	private void setPrediction(Prediction prediction){
		this.prediction = prediction;
	}

	static
	private String formaDescriptortMap(Map<Descriptor, ?> map){
		Map<String, String> result = new LinkedHashMap<String, String>();

		Collection<? extends Map.Entry<Descriptor, ?>> entries = map.entrySet();
		for(Map.Entry<Descriptor, ?> entry : entries){
			result.put((entry.getKey()).getId(), entry.getValue() != null ? String.valueOf(entry.getValue()) : null);
		}

		return String.valueOf(result);
	}

	protected static final Logger logger = Logger.getLogger(PredictionVerifier.class.getName());
}