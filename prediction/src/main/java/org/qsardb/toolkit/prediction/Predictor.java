/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

import org.qsardb.cargo.bodo.*;
import org.qsardb.cargo.rds.*;
import org.qsardb.evaluation.*;
import org.qsardb.model.*;
import org.qsardb.toolkit.*;

import com.beust.jcommander.Parameter;

import net.sf.blueobelisk.*;

import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.qsar.*;

abstract
public class Predictor {

	@Parameter (
		names = {"--log-level"},
		description = "The logging level. " + LevelConverter.MESSAGE,
		converter = LevelConverter.class
	)
	private Level level = Level.INFO;

	@Parameter (
		names = {"--model-id"},
		description = "Model Id"
	)
	private String modelId = null;

	@Parameter (
		names = {"--format"},
		description = "Prediction value format. " + DecimalFormatConverter.MESSAGE,
		converter = DecimalFormatConverter.class
	)
	private DecimalFormat format = null;

	@Parameter (
		names = {"--archive"},
		description = "QDB file or directory",
		required = true
	)
	private File archive = null;

	private Qdb qdb = null;

	private Evaluator evaluator = null;


	abstract
	public void predict() throws Exception;

	public void predict(IAtomContainer molecule) throws Exception {
		Map<Descriptor, Object> parameters = new LinkedHashMap<Descriptor, Object>();

		molecule = DescriptorUtil.prepareMolecule(molecule);

		DescriptorValueCache cache = new DescriptorValueCache();

		List<Descriptor> descriptors = this.evaluator.getDescriptors();
		for(Descriptor descriptor : descriptors){
			BODOCargo bodoCargo = descriptor.getCargo(BODOCargo.class);

			BODODescriptor bodoDescriptor = bodoCargo.loadBodoDescriptor();

			IMolecularDescriptor cdkDescriptor = (IMolecularDescriptor)BODOUtil.parse(bodoDescriptor);

			Object value = cache.calculate(cdkDescriptor, molecule);

			parameters.put(descriptor, value);
		}

		Object result = this.evaluator.evaluateAndFormat(parameters, this.format);

		System.out.println(result);
	}

	public void run() throws Exception {
		logger.setLevel(this.level);

		Qdb qdb = new Qdb(StorageUtil.openInput(this.archive));

		setQdb(qdb);

		try {
			try {
				initEvaluator();

				try {
					predict();
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
		ModelRegistry models = getQdb().getModelRegistry();

		Model model;

		if(this.modelId != null){
			model = models.get(this.modelId);

			if(model == null){
				throw new IllegalArgumentException("Model \'" + this.modelId + "\' not found");
			}
		} else

		{
			Iterator<Model> it = models.iterator();
			if(!it.hasNext()){
				throw new IllegalArgumentException("No models found");
			}

			model = it.next();
		}

		EvaluatorFactory evaluatorFactory = EvaluatorFactory.getInstance();
		evaluatorFactory.setActivating(true);

		Evaluator evaluator = evaluatorFactory.getEvaluator(model);

		evaluator.init();

		this.evaluator = evaluator;
	}

	private void destroyEvaluator() throws Exception {
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

	protected Qdb getQdb(){
		return this.qdb;
	}

	private void setQdb(Qdb qdb){
		this.qdb = qdb;
	}

	protected static final Logger logger = Logger.getLogger(Predictor.class.getName());
}