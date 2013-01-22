/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import java.io.*;
import java.util.logging.*;

import org.qsardb.model.*;
import org.qsardb.toolkit.*;

import com.beust.jcommander.Parameter;

abstract
public class Differencer {

	@Parameter (
		names = {"--log-level"},
		description = "The logging level. " + LevelConverter.MESSAGE,
		converter = LevelConverter.class
	)
	private Level level = Level.INFO;

	@Parameter (
		names = {"--left"},
		description = "The left-hand side QDB file or directory",
		required = true
	)
	private File left = null;

	private Qdb leftQdb = null;

	@Parameter (
		names = {"--right"},
		description = "The right-hand side QDB file or directory",
		required = true
	)
	private File right = null;

	private Qdb rightQdb = null;


	abstract
	public void diff() throws Exception;

	public void run() throws Exception {
		logger.setLevel(this.level);

		Qdb leftQdb = new Qdb(StorageUtil.openInput(this.left));

		setLeftQdb(leftQdb);

		try {
			Qdb rightQdb = new Qdb(StorageUtil.openInput(this.right));

			setRightQdb(rightQdb);

			try {
				try {
					diff();
				} finally {
					leftQdb.close();
					rightQdb.close();
				}
			} finally {
				setRightQdb(null);
			}
		} finally {
			setLeftQdb(null);
		}
	}

	public Qdb getLeftQdb(){
		return this.leftQdb;
	}

	private void setLeftQdb(Qdb leftQdb){
		this.leftQdb = leftQdb;
	}

	public Qdb getRightQdb(){
		return this.rightQdb;
	}

	private void setRightQdb(Qdb rightQdb){
		this.rightQdb = rightQdb;
	}

	protected static final Logger logger = Logger.getLogger(Differencer.class.getName());
}