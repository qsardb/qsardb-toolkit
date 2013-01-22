/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit;

import java.text.*;
import java.util.*;

import com.beust.jcommander.*;

public class DecimalFormatConverter implements IStringConverter<DecimalFormat> {

	@Override
	public DecimalFormat convert(String string){
		return new DecimalFormat(string, new DecimalFormatSymbols(Locale.US));
	}

	public static final String MESSAGE = "Possible values are java.text.DecimalFormat pattern strings";
}