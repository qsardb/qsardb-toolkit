/*
 * Copyright (c) 2014 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import com.beust.jcommander.*;
import java.util.logging.Level;
import org.qsardb.model.Archive;
import org.qsardb.toolkit.Command;

public class ArchiveManager extends Manager {
	
	public static void main(String[] args) throws Exception {
		ArchiveManager manager = new ArchiveManager();

		JCommander commander = new JCommander(manager);
		commander.setProgramName(commander.getClass().getName());
		commander.addCommand(manager.new ArchiveCommand());

		try {
			commander.parse(args);
			Command command = Command.getCommand(commander);
			manager.run(command);
		} catch (ParameterException e) {
			commander.usage();
			logger.log(Level.SEVERE, e.getMessage());
			System.exit(-1);
		}

	}

	@Parameters (
		commandNames = {"set-attribute"},
		commandDescription = "Set archive attributes"
	)
	private class ArchiveCommand extends Command {

		@Parameter (
			names = {"--name"},
			description = "Set name attribute"
		)
		protected String name;

		@Parameter (
			names = {"--description"},
			description = "Set description attribute"
		)
		protected String description;

		@Override
		public void execute() throws Exception {
			Archive archive = getQdb().getArchive();
			boolean changed = false;

			if (name != null) {
				archive.setName(name);
				changed = true;
			}

			if (description != null) {
				archive.setDescription(description);
				changed = true;
			}

			if (changed) {
				archive.storeChanges();
			} else {
				logger.warning("Nothing to change");
			}
		}
	}
}