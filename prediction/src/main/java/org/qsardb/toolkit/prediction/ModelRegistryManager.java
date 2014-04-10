/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.qsardb.cargo.pmml.*;
import org.qsardb.cargo.rds.*;
import org.qsardb.model.*;
import org.qsardb.toolkit.*;

import com.beust.jcommander.*;
import com.beust.jcommander.Parameter;

public class ModelRegistryManager extends ContainerRegistryManager<ModelRegistry, Model> {

	static
	public void main(String...  args) throws Exception {
		ModelRegistryManager manager = new ModelRegistryManager();

		JCommander commander = new JCommander(manager);
		commander.setProgramName(ModelRegistryManager.class.getName());

		commander.addCommand(manager.new AddCommand());
		commander.addCommand(manager.new AttachPmmlCommand());
		commander.addCommand(manager.new AttachRdsCommand());
		commander.addCommand(manager.new AttachBibTeXCommand());
		commander.addCommand(manager.new RemoveCommand());
		commander.addCommand(manager.new SetCommand());

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
	public ModelRegistry getContainerRegistry(){
		return getQdb().getModelRegistry();
	}

	@Parameters (
		commandNames = {"add"},
		commandDescription = "Add new model"
	)
	private class AddCommand extends ContainerRegistryManager<ModelRegistry, Model>.AddCommand {

		@Parameter (
			names = {"--property-id"},
			description = "Property Id",
			required = true
		)
		private String propertyId = null;


		@Override
		public Model toContainer(){
			Property property = getQdb().getProperty(this.propertyId);
			if(property == null){
				throw new IllegalArgumentException("Property \'" + this.propertyId + "\' not found");
			}

			Model result = new Model(super.id, property);
			result.setName(super.name);

			reserveCargos(result, super.cargos);

			return result;
		}
	}

	@Parameters (
		commandNames = {"attach-pmml"},
		commandDescription = "Attach PMML Cargo"
	)
	private class AttachPmmlCommand extends ContainerRegistryManager<ModelRegistry, Model>.AttachFileCommand {

		@Parameter (
			names = {"--pmml"},
			description = "PMML file"
		)
		private File file = null;


		@Override
		public String getId(){
			return PMMLCargo.ID;
		}

		@Override
		public File getFile(){
			return this.file;
		}
	}

	@Parameters (
		commandNames = {"attach-rds"},
		commandDescription = "Attach RDS Cargo"
	)
	private class AttachRdsCommand extends ContainerRegistryManager<ModelRegistry, Model>.AttachFileCommand {

		@Parameter (
			names = {"--rds"},
			description = "RDS file"
		)
		private File file = null;


		@Override
		public String getId(){
			return RDSCargo.ID;
		}

		@Override
		public File getFile(){
			return this.file;
		}
	}

	@Parameters (
		commandNames = {"remove"},
		commandDescription = "Remove existing model and all related existing predictions"
	)
	private class RemoveCommand extends ContainerRegistryManager<ModelRegistry, Model>.RemoveCommand {

		@Override
		public void execute() throws Exception {
			ModelRegistry models = getContainerRegistry();

			Model model = models.get(super.id);
			if(model == null){
				throw new IllegalArgumentException("Id \'" +super.id + "\' not found");
			}

			PredictionRegistry predictions = getQdb().getPredictionRegistry();

			Collection<Prediction> modelPredictions = predictions.getByModel(model);
			predictions.removeAll(modelPredictions);

			super.execute();

			predictions.storeChanges();
		}
	}

	@Parameters (
		commandNames = {"set-attribute"},
		commandDescription = "Set model attributes"
	)
	private class SetCommand extends ContainerRegistryManager<ModelRegistry, Model>.SetCommand {
	}
}
