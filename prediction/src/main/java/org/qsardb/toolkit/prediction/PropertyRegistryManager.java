/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import java.util.logging.*;

import org.qsardb.model.*;
import org.qsardb.toolkit.*;

import com.beust.jcommander.*;
import com.beust.jcommander.Parameter;

public class PropertyRegistryManager extends ParameterRegistryManager<PropertyRegistry, Property>{

	static
	public void main(String... args) throws Exception {
		PropertyRegistryManager manager = new PropertyRegistryManager();

		JCommander commander = new JCommander(manager);
		commander.setProgramName(PropertyRegistryManager.class.getName());

		commander.addCommand(manager.new AddCommand());
		commander.addCommand(manager.new AttachValuesCommand());
		commander.addCommand(manager.new AttachUcumCommand());
		commander.addCommand(manager.new AttachBibTeXCommand());
		commander.addCommand(manager.new AddBibTeXCommand());
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
	public PropertyRegistry getContainerRegistry(){
		return getQdb().getPropertyRegistry();
	}

	@Parameters (
		commandNames = {"add"},
		commandDescription = "Add new property"
	)
	private class AddCommand extends ContainerRegistryManager<PropertyRegistry, Property>.AddCommand {

		@Override
		public Property toContainer(){
			Property property = new Property(super.id);
			property.setName(super.name);

			reserveCargos(property, super.cargos);

			return property;
		}
	}

	@Parameters (
		commandNames = {"remove"},
		commandDescription = "Remove existing property"
	)
	private class RemoveCommand extends ContainerRegistryManager<PropertyRegistry, Property>.RemoveCommand {
	}

	@Parameters (
		commandNames = {"set-attribute"},
		commandDescription = "Set property attributes"
	)
	private class SetCommand extends ParameterRegistryManager<PropertyRegistry, Property>.SetCommand {

		@Parameter (
			names = {"--species"},
			description = "Set species attribute. Use \"Binomial name (common name)\" format."
		)
		private String species = null;

		@Parameter (
			names = {"--endpoint"},
			description = "Set endpoint attribute (QMRF classification system)"
		)
		private String endpoint = null;

		@Override
		public void handleAttributeOptions(Property property) {
			super.handleAttributeOptions(property);

			if (species != null){
				property.setSpecies(species);
			}

			if (endpoint != null) {
				property.setEndpoint(endpoint);
			}
		}
	}
}
