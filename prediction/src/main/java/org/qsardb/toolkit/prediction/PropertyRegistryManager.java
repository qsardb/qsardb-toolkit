/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import java.util.logging.*;

import org.qsardb.model.*;
import org.qsardb.toolkit.*;

import com.beust.jcommander.*;

public class PropertyRegistryManager extends ParameterRegistryManager<PropertyRegistry, Property>{

	static
	public void main(String... args) throws Exception {
		PropertyRegistryManager manager = new PropertyRegistryManager();

		JCommander commander = new JCommander(manager);
		commander.setProgramName(PropertyRegistryManager.class.getName());

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
}