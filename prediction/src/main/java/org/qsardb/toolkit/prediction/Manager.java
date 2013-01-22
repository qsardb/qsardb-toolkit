/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import java.io.*;
import java.util.logging.*;

import org.qsardb.model.*;
import org.qsardb.storage.directory.*;
import org.qsardb.toolkit.*;

import com.beust.jcommander.Parameter;

abstract
public class Manager {

	@Parameter (
		names = {"--log-level"},
		description = "The logging level. " + LevelConverter.MESSAGE,
		converter = LevelConverter.class
	)
	private Level level = Level.INFO;

	@Parameter (
		names = {"--dir"},
		description = "QDB directory",
		required = true
	)
	private File dir = null;

	private Qdb qdb = null;


	public void run(Command command) throws Exception {
		logger.setLevel(this.level);

		Qdb qdb = new Qdb(new DirectoryStorage(this.dir));

		setQdb(qdb);

		try {
			try {
				command.execute();
			} finally {
				qdb.close();
			}
		} finally {
			setQdb(null);
		}
	}

	public Qdb getQdb(){
		return this.qdb;
	}

	private void setQdb(Qdb qdb){
		this.qdb = qdb;
	}

	protected static final Logger logger = Logger.getLogger(Manager.class.getName());
}