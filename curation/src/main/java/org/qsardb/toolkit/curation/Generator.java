/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.curation;

import java.io.*;

import org.qsardb.model.*;
import org.qsardb.storage.directory.*;

import com.beust.jcommander.Parameter;

abstract
public class Generator {

	@Parameter (
		names = {"--dir"},
		description = "QDB directory",
		required = true
	)
	private File dir = null;

	private Qdb qdb = null;


	abstract
	public void generate() throws Exception;

	public void run() throws Exception {
		Qdb qdb = new Qdb(new DirectoryStorage(this.dir));

		try {
			setQdb(qdb);

			try {
				generate();
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
}