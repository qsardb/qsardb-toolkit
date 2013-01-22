/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import java.util.logging.*;

import org.qsardb.model.*;
import org.qsardb.toolkit.*;

import com.beust.jcommander.*;
import com.beust.jcommander.Parameter;

public class PredictionRegistryManager extends ParameterRegistryManager<PredictionRegistry, Prediction> {

	static
	public void main(String... args) throws Exception {
		PredictionRegistryManager manager = new PredictionRegistryManager();

		JCommander commander = new JCommander(manager);
		commander.setProgramName(PredictionRegistryManager.class.getName());

		commander.addCommand(manager.new AddCommand());
		commander.addCommand(manager.new AttachValuesCommand());
		commander.addCommand(manager.new AttachUcumCommand());
		commander.addCommand(manager.new RemoveCommand());

		Command command;

		try {
			commander.parse(args);

			command = Command.getCommand(commander);
		} catch(ParameterException pe){
			commander.usage();

			logger.log(Level.SEVERE, pe.getMessage());

			System.exit(-1);

			return;
		}

		manager.run(command);
	}

	@Override
	public PredictionRegistry getContainerRegistry(){
		return getQdb().getPredictionRegistry();
	}

	@Parameters (
		commandNames = {"add"},
		commandDescription = "Add new prediction"
	)
	private class AddCommand extends ContainerRegistryManager<PredictionRegistry, Prediction>.AddCommand {

		@Parameter (
			names = {"--model-id"},
			description = "Model Id",
			required = true
		)
		private String modelId = null;

		@Parameter (
			names = {"--type"},
			description = "Type",
			converter = TypeConverter.class
		)
		private Prediction.Type type = Prediction.Type.TRAINING;


		@Override
		public Prediction toContainer(){
			Model model = getQdb().getModel(this.modelId);
			if(model == null){
				throw new IllegalArgumentException("Model \'" + this.modelId + "\' not found");
			}

			Prediction result = new Prediction(super.id, model, this.type);
			result.setName(super.name);

			reserveCargos(result, super.cargos);

			return result;
		}
	}

	@Parameters (
		commandNames = {"remove"},
		commandDescription = "Remove existing prediction"
	)
	private class RemoveCommand extends ContainerRegistryManager<PredictionRegistry, Prediction>.RemoveCommand {
	}
}