/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.qsardb.cargo.map.*;

import com.beust.jcommander.*;

abstract
public class ParameterRegistryDifferencer<R extends org.qsardb.model.ParameterRegistry<R, P>, P extends org.qsardb.model.Parameter<R, P>> extends Differencer {

	@Parameter (
		names = {"--scale"},
		description = "The scale for java.math.BigDecimal comparsion"
	)
	private int scale = 0;


	public boolean diff(R left, R right) throws IOException {
		boolean result = false;

		Set<String> leftIds = loadIds(left);
		Set<String> rightIds = loadIds(right);

		Set<String> ids = new LinkedHashSet<String>(leftIds);
		ids.retainAll(rightIds);

		logger.log(Level.FINE, "Left-hand side has " + (leftIds.size() - ids.size()) + " unique parameters");
		logger.log(Level.FINE, "Right-hand side has " + (rightIds.size() - ids.size()) + " unique parameters");

		logger.log(Level.INFO, "Differencing " + (ids.size()) + " common parameters");

		for(String id : ids){
			result |= diff(left.get(id), right.get(id));
		}

		return result;
	}

	private Set<String> loadIds(R parameters){
		Set<String> ids = new LinkedHashSet<String>();

		for(P parameter : parameters){
			ids.add(parameter.getId());
		}

		return ids;
	}

	public boolean diff(P left, P right) throws IOException {
		boolean result = false;

		logger.log(Level.INFO, "Differencing \'" + left.getId() + "\'");

		Map<String, String> leftValues = loadValues(left);
		Map<String, String> rightValues = loadValues(right);

		Set<String> ids = new LinkedHashSet<String>(leftValues.keySet());
		ids.retainAll(rightValues.keySet());

		logger.log(Level.FINE, "Left-hand side parameter has " + (leftValues.size() - ids.size()) + " unique values");
		logger.log(Level.FINE, "Right-hand side parameter has " + (rightValues.size() - ids.size()) + " unique values");

		logger.log(Level.INFO, "Differencing " + (ids.size()) + " common values");

		int count = 0;

		for(String id : ids){
			String leftValue = leftValues.get(id);
			String rightValue = rightValues.get(id);

			boolean equals = ValueUtil.equals(leftValue, rightValue, this.scale);
			if(equals){
				count++;
			} else

			{
				logger.log(Level.FINER, "Compound '" + id + "': "+ leftValue + " vs. " + rightValue);
			}

			result |= !(equals);
		}

		logger.log(Level.INFO, "Found " + (count) + " equal values and " + (ids.size() - count) + " unequal values");

		return result;
	}

	private Map<String, String> loadValues(P parameter) throws IOException {
		ValuesCargo valuesCargo = parameter.getCargo(ValuesCargo.class);

		return valuesCargo.loadStringMap();
	}
}