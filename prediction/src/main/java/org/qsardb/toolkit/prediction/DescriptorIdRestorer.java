/*
 * Copyright (c) 2013 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import java.io.*;

import org.dmg.pmml.*;

import com.beust.jcommander.*;
import com.beust.jcommander.Parameter;

/**
 * Restores CDK descriptor identifiers in Random forest (RF) PMML files.
 */
public class DescriptorIdRestorer {

	@Parameter (
		names = {"--input"},
		description = "Input PMML file",
		required = true
	)
	private File input = null;

	@Parameter (
		names = {"--output"},
		description = "Output PMML file. If missing, the input PMML file is used",
		required = false
	)
	private File output = null;


	static
	public void main(String... args) throws Exception {
		DescriptorIdRestorer restorer = new DescriptorIdRestorer();

		JCommander commander = new JCommander(restorer);
		commander.setProgramName(DescriptorIdRestorer.class.getName());

		try {
			commander.parse(args);
		} catch(ParameterException pe){
			commander.usage();

			System.exit(-1);
		}

		restorer.run();
	}

	private void run() throws Exception {
		PMML pmml = IOUtil.unmarshal(this.input);

		Visitor visitor = new AbstractVisitor(){

			@Override
			public VisitorAction visit(DataField dataField){
				dataField.setName(rename(dataField.getName()));

				return super.visit(dataField);
			}

			@Override
			public VisitorAction visit(MiningField miningField){
				miningField.setName(rename(miningField.getName()));

				return super.visit(miningField);
			}

			@Override
			public VisitorAction visit(SimplePredicate simplePredicate){
				simplePredicate.setField(rename(simplePredicate.getField()));

				return super.visit(simplePredicate);
			}

			private FieldName rename(FieldName name){
				String value = name.getValue();

				if(value.indexOf('_') > -1){
					return FieldName.create(value.replace('_', '-'));
				}

				return name;
			}
		};
		pmml.accept(visitor);

		IOUtil.marshal(pmml, this.output != null ? this.output : this.input);
	}
}