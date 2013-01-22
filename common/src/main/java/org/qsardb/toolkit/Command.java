/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit;

import java.util.*;

import com.beust.jcommander.*;

abstract
public class Command {

	abstract
	public void execute() throws Exception;

	static
	public Command getCommand(JCommander commander){
		Map<String, JCommander> commands = commander.getCommands();

		JCommander command = commands.get(commander.getParsedCommand());
		if(command == null){
			throw new ParameterException("Unknown command " + commander.getParsedCommand());
		}

		List<Object> objects = command.getObjects();

		return (Command)objects.get(0);
	}
}