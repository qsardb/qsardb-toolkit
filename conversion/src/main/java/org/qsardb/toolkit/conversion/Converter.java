/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.toolkit.conversion;

import java.io.*;
import java.util.logging.*;

import org.qsardb.model.*;
import org.qsardb.storage.directory.*;
import org.qsardb.toolkit.*;

import com.beust.jcommander.Parameter;

abstract
public class Converter {

	@Parameter (
		names = {"--log-level"},
		description = "The logging level. " + LevelConverter.MESSAGE,
		converter = LevelConverter.class
	)
	private Level level = Level.INFO;

	@Parameter (
		names = {"--target"},
		description = "QDB directory",
		required = true
	)
	private File dir = null;

	private Qdb qdb = null;


	public Converter(){
	}

	abstract
	public void convert() throws Exception;

	public void run() throws Exception {
		logger.setLevel(this.level);

		if(!this.dir.isDirectory()){
			this.dir.mkdirs();
		}

		Qdb qdb = new Qdb(new DirectoryStorage(this.dir));

		setQdb(qdb);

		try {
			try {
				convert();

				qdb.storeChanges();
			} finally {
				qdb.close();
			}
		} finally {
			setQdb(null);
		}
	}

	protected Qdb getQdb(){
		return this.qdb;
	}

	private void setQdb(Qdb qdb){
		this.qdb = qdb;
	}

	protected static final Logger logger = Logger.getLogger(Converter.class.getName());
}