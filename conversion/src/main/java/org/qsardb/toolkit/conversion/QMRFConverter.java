/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.toolkit.conversion;

import it.jrc.ecb.qmrf.*;

import java.io.*;
import java.net.*;

import org.qsardb.conversion.qmrf.*;

import com.beust.jcommander.*;

public class QMRFConverter extends Converter {

	@Parameter (
		names = "--id",
		description = "QMRF numeric identifier in the ECB inventory",
		required = true
	)
	private String id = null;


	static
	public void main(String... args) throws Exception {
		QMRFConverter converter = new QMRFConverter();

		JCommander commander = new JCommander(converter);
		commander.setProgramName(QMRFConverter.class.getName());

		try {
			commander.parse(args);
		} catch(ParameterException pe){
			commander.usage();

			System.exit(-1);
		}

		converter.run();
	}

	@Override
	public void convert() throws Exception {
		URL url = new URL("http://qsardb.jrc.it/qmrf/download.jsp?filetype=xml&id=" + Integer.parseInt(this.id));

		InputStream is = url.openStream();

		try {
			QMRF qmrf = QmrfUtil.loadQmrf(is);

			Qmrf2Qdb.convert(getQdb(), qmrf);
		} finally {
			is.close();
		}
	}
}