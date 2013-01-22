/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import java.io.*;

import org.qsardb.cargo.map.*;
import org.qsardb.cargo.ucum.*;
import org.qsardb.model.*;

import com.beust.jcommander.*;
import com.beust.jcommander.Parameter;

abstract
public class ParameterRegistryManager<R extends ParameterRegistry<R, P>, P extends org.qsardb.model.Parameter<R, P>> extends ContainerRegistryManager<R, P> {

	@Parameters (
		commandNames = {"attach-values"},
		commandDescription = "Attach Values Cargo"
	)
	protected class AttachValuesCommand extends ContainerRegistryManager<R, P>.AttachFileCommand {

		@Parameter (
			names = {"--csv"},
			description = "CSV (TSV) file"
		)
		private File file = null;


		@Override
		public String getId(){
			return ValuesCargo.ID;
		}

		@Override
		public File getFile(){
			return this.file;
		}
	}

	@Parameters (
		commandNames = {"attach-ucum"},
		commandDescription = "Attach UCUM Cargo"
	)
	protected class AttachUcumCommand extends ContainerRegistryManager<R, P>.AttachCommand {

		@Parameter (
			names = {"--unit"},
			description = "Unit specification in UCUM code system",
			required = true
		)
		private String ucum = null;


		@Override
		public String getId(){
			return UCUMCargo.ID;
		}

		@Override
		public Payload getPayload(){
			return new StringPayload(this.ucum);
		}
	}
}