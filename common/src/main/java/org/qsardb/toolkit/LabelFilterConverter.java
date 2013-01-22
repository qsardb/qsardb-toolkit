/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit;

import java.util.logging.*;

import org.qsardb.query.*;

import com.beust.jcommander.*;

public class LabelFilterConverter implements IStringConverter<LabelFilter<?>> {

	@Override
	@SuppressWarnings (
		value = {"rawtypes"}
	)
	public LabelFilter<?> convert(String string){

		try {
			return new LabelFilter(string);
		} catch(Exception e){
			logger.log(Level.SEVERE, "Failed to convert String", e);

			return null;
		}
	}

	private static final Logger logger = Logger.getLogger(LabelFilterConverter.class.getName());
}