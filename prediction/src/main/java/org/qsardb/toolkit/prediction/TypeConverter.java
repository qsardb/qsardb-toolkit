/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import org.qsardb.model.*;

import com.beust.jcommander.*;

public class TypeConverter implements IStringConverter<Prediction.Type> {

	@Override
	public Prediction.Type convert(String string){

		if("training".equalsIgnoreCase(string)){
			return Prediction.Type.TRAINING;
		} else

		if("validation".equalsIgnoreCase(string)){
			return Prediction.Type.VALIDATION;
		} else

		if("testing".equalsIgnoreCase(string)){
			return Prediction.Type.TESTING;
		}

		throw new IllegalArgumentException(string);
	}
}