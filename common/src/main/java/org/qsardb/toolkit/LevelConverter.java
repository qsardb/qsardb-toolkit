/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.toolkit;

import java.util.logging.*;

import com.beust.jcommander.*;

public class LevelConverter implements IStringConverter<Level> {

	@Override
	public Level convert(String string){
		return Level.parse(string);
	}

	public static final String MESSAGE = "Possible values are java.util.logging.Level constant names";
}