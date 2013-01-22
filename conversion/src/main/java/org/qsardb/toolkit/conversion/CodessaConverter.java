/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.toolkit.conversion;

import java.io.*;

import org.qsardb.conversion.codessa.*;

import com.beust.jcommander.*;

public class CodessaConverter extends Converter {

	@Parameter (
		names = {"--source"},
		description = "CODESSA project archive file (ZIP, TAR) or directory",
		required = true
	)
	private File source = null;


	static
	public void main(String... args) throws Exception {
		CodessaConverter converter = new CodessaConverter();

		JCommander commander = new JCommander(converter);
		commander.setProgramName(CodessaConverter.class.getName());

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
		Codessa2Qdb.convert(getQdb(), this.source);
	}
}