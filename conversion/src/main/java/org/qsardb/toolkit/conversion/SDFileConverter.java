/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.toolkit.conversion;

import java.io.*;

import org.qsardb.cargo.structure.*;
import org.qsardb.conversion.sdfile.*;
import org.qsardb.conversion.table.*;

import com.beust.jcommander.*;

public class SDFileConverter extends TableConverter {

	@Parameter (
		names = {"--molfile"},
		description = "Include/exclude molfile column (automatic)",
		arity = 1
	)
	private boolean molfile = true;

	@Parameter (
		names = {"--source"},
		description = "SD file",
		required = true
	)
	private File source = null;


	static
	public void main(String... args) throws Exception {
		SDFileConverter converter = new SDFileConverter();

		JCommander commander = new JCommander(converter);
		commander.setProgramName(SDFileConverter.class.getName());

		try {
			commander.parse(args);
		} catch(ParameterException pe){
			commander.usage();

			System.exit(-1);
		}

		converter.run();
	}

	@Override
	protected Table createTable() throws IOException {

		if(!this.source.isFile()){
			throw new IOException(this.source.getAbsolutePath() + " is not a file");
		}

		return new SDFile(this.source);
	}

	@Override
	protected TableSetup createTableSetup() throws Exception {
		TableSetup setup = super.createTableSetup();

		if(this.molfile){
			setup.addMapping(SDFile.COLUMN_MOLFILE, new CompoundCargoMapping(ChemicalMimeData.MDL_MOLFILE.getId()));
		}

		return setup;
	}

	@Override
	protected String prepareId(String column) {
		return column.replaceAll("\\s", "_");
	}

	@Override
	protected String prepareName(String column) {
		return column;
	}

}
